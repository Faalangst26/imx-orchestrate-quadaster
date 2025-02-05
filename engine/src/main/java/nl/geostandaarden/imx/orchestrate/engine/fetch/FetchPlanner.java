package nl.geostandaarden.imx.orchestrate.engine.fetch;

import static java.util.Collections.unmodifiableSet;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toSet;
import static nl.geostandaarden.imx.orchestrate.model.ModelUtils.extractKey;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import nl.geostandaarden.imx.orchestrate.engine.OrchestrateException;
import nl.geostandaarden.imx.orchestrate.engine.exchange.BatchRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.CollectionRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.CollectionResult;
import nl.geostandaarden.imx.orchestrate.engine.exchange.DataRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.ObjectRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.ObjectResult;
import nl.geostandaarden.imx.orchestrate.engine.exchange.SelectedProperty;
import nl.geostandaarden.imx.orchestrate.engine.source.Source;
import nl.geostandaarden.imx.orchestrate.model.AbstractRelation;
import nl.geostandaarden.imx.orchestrate.model.Attribute;
import nl.geostandaarden.imx.orchestrate.model.InverseRelation;
import nl.geostandaarden.imx.orchestrate.model.ModelMapping;
import nl.geostandaarden.imx.orchestrate.model.ObjectType;
import nl.geostandaarden.imx.orchestrate.model.ObjectTypeMapping;
import nl.geostandaarden.imx.orchestrate.model.ObjectTypeRef;
import nl.geostandaarden.imx.orchestrate.model.Path;
import nl.geostandaarden.imx.orchestrate.model.PathMapping;
import nl.geostandaarden.imx.orchestrate.model.Property;
import nl.geostandaarden.imx.orchestrate.model.PropertyMapping;
import nl.geostandaarden.imx.orchestrate.model.Relation;
import nl.geostandaarden.imx.orchestrate.model.filters.FilterDefinition;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public final class FetchPlanner {

  private final ModelMapping modelMapping;

  private final Map<String, Source> sources;

  public Mono<ObjectResult> plan(ObjectRequest request) {
    var input = FetchInput.newInput(request.getObjectKey());

    return fetch(request, input, false)
        .take(1)
        .singleOrEmpty();
  }

  public Mono<CollectionResult> plan(CollectionRequest request) {
    var input = FetchInput.newInput(Map.of());

    return fetch(request, input, true)
        .collectList()
        .map(objectResults -> CollectionResult.builder()
            .objectResults(objectResults)
            .build());
  }

  public Mono<CollectionResult> plan(BatchRequest request) {
    var inputs = request.getObjectKeys()
        .stream()
        .map(FetchInput::newInput)
        .toList();

    return fetch(request, inputs)
        .collectList()
        .map(objectResults -> CollectionResult.builder()
            .objectResults(objectResults)
            .build());
  }

  private Set<Path> resolveSourcePaths(DataRequest request, ObjectTypeMapping typeMapping, Path basePath) {
    var sourceRoot = modelMapping.getSourceType(typeMapping.getSourceRoot());

    return request.getSelectedProperties()
        .stream()
        .flatMap(selectedProperty -> resolveSourcePaths(sourceRoot, typeMapping, selectedProperty, basePath))
        .collect(toSet());
  }

  private Stream<Path> resolveSourcePaths(ObjectType sourceType, ObjectTypeMapping typeMapping, SelectedProperty selectedProperty, Path basePath) {
    var propertyMappingOptional = typeMapping.getPropertyMapping(selectedProperty.getProperty());

    if (propertyMappingOptional.isPresent()) {
      return propertyMappingOptional.stream()
          .flatMap(propertyMapping -> resolveSourcePaths(sourceType, propertyMapping, selectedProperty, basePath));
    }

    if (selectedProperty.getProperty() instanceof AbstractRelation relation) {
      var relTargetType = modelMapping.getTargetType(relation.getTarget());

      return modelMapping
          .getObjectTypeMappings(relTargetType)
          .stream()
          .filter(targetTypeMapping -> targetTypeMapping.getSourceRoot()
              .equals(typeMapping.getSourceRoot()))
          .flatMap(targetTypeMapping -> resolveSourcePaths(selectedProperty.getNestedRequest(), targetTypeMapping, basePath).stream());
    }

    return Stream.empty();
  }

  private Stream<Path> resolveSourcePaths(ObjectType sourceType, PropertyMapping propertyMapping, SelectedProperty selectedProperty, Path basePath) {
    var sourcePaths = propertyMapping.getPathMappings()
        .stream()
        .flatMap(pathMapping -> {
          // TODO: Conditionality & recursion
          var nextPaths = pathMapping.getNextPathMappings()
              .stream()
              .map(PathMapping::getPath);

          return Stream.concat(Stream.of(pathMapping.getPath()), nextPaths)
              .map(basePath::append);
        });

    if (selectedProperty.getProperty() instanceof AbstractRelation relation) {
      var nestedRequest = Optional.ofNullable(selectedProperty.getNestedRequest())
          .orElseThrow(() -> new OrchestrateException("Nested request not present for relation: " + relation.getName()));

      return sourcePaths.flatMap(sourcePath -> {
        var sourceRelTarget = resolveSourceRelation(sourceType, sourcePath).getTarget();

        var nestedTypeMapping = modelMapping.getObjectTypeMapping(nestedRequest.getObjectType(), sourceRelTarget)
            .orElseThrow(() -> new OrchestrateException("Type mapping not found for source root: " + sourceRelTarget));

        return resolveSourcePaths(nestedRequest, nestedTypeMapping, Path.fromProperties())
            .stream()
            .map(sourcePath::append);
      });
    }

    return sourcePaths;
  }

  private AbstractRelation resolveSourceRelation(ObjectType objectType, Path sourcePath) {
    var property = objectType.getProperty(sourcePath.getFirstSegment());

    if (property instanceof AbstractRelation relation) {
      if (sourcePath.isLeaf()) {
        return relation;
      }

      return resolveSourceRelation(modelMapping.getSourceType(relation.getTarget()), sourcePath.withoutFirstSegment());
    }

    throw new OrchestrateException("Path segment is not a relation: " + sourcePath.getFirstSegment());
  }

  private Flux<ObjectResult> fetch(DataRequest request, FetchInput input, boolean isCollection) {
    var typeMappings = modelMapping.getObjectTypeMappings(request.getObjectType());

    return Flux.fromIterable(typeMappings)
        .flatMapSequential(typeMapping -> {
          var sourcePaths = resolveSourcePaths(request, typeMapping, Path.fromProperties());

          var resultMapper = ObjectResultMapper.builder()
              .modelMapping(modelMapping)
              .build();

          return fetchSourceObject(typeMapping.getSourceRoot(), sourcePaths, isCollection, null)
              .execute(input)
              .map(result -> resultMapper.map(result, request));
        });
  }

  private Flux<ObjectResult> fetch(BatchRequest request, List<FetchInput> inputs) {
    var typeMappings = modelMapping.getObjectTypeMappings(request.getObjectType());

    return Flux.fromIterable(typeMappings)
        .flatMapSequential(typeMapping -> {
          var sourcePaths = resolveSourcePaths(request, typeMapping, Path.fromProperties());

          var resultMapper = ObjectResultMapper.builder()
              .modelMapping(modelMapping)
              .build();

          return fetchSourceObject(typeMapping.getSourceRoot(), sourcePaths, false, null)
              .executeBatch(inputs)
              .map(result -> resultMapper.map(result, request));
        });
  }

  private FetchOperation fetchSourceObject(ObjectTypeRef sourceTypeRef, Set<Path> sourcePaths, boolean isCollection, FilterDefinition filter) {
    var source = sources.get(sourceTypeRef.getModelAlias());
    var sourceType = modelMapping.getSourceType(sourceTypeRef);
    var selectedProperties = new HashSet<>(selectIdentity(sourceTypeRef));

    sourcePaths.stream()
        .filter(Path::isLeaf)
        .map(sourcePath -> sourceType.getProperty(sourcePath.getFirstSegment()))
        .filter(not(Property::isIdentifier))
        .map(SelectedProperty::forProperty)
        .forEach(selectedProperties::add);

    var nextOperations = new HashSet<NextOperation>();

    sourcePaths.stream()
        .filter(not(Path::isLeaf))
        .collect(groupingBy(Path::getFirstSegment, mapping(Path::withoutFirstSegment, toSet())))
        .forEach((propertyName, nestedSourcePaths) -> {
          var property = sourceType.getProperty(propertyName);

          if (property instanceof InverseRelation inverseRelation) {
            var filterDefinition = createFilterDefinition(sourceType, inverseRelation);
            var originTypeRef = inverseRelation.getTarget(sourceTypeRef);

            nextOperations.add(NextOperation.builder()
                .property(inverseRelation)
                .delegateOperation(fetchSourceObject(originTypeRef, nestedSourcePaths, true, filterDefinition))
                .build());

            return;
          }

          if (property instanceof Relation relation) {
            var targetTypeRef = relation.getTarget(sourceTypeRef);
            var targetType = modelMapping.getSourceType(targetTypeRef);
            var filterMappings = relation.getFilterMappings();

            if (!filterMappings.isEmpty()) {
              var filterMapping = filterMappings.get(0);
              var sourcePath = filterMapping.getSourcePath();

              if (!sourcePath.isLeaf()) {
                throw new OrchestrateException("Only leaf paths are (currently) supported for filter mapping: " + filterMapping.getProperty());
              }

              var filterProperty = sourceType.getProperty(sourcePath.getFirstSegment());
              selectedProperties.add(SelectedProperty.forProperty(filterProperty));

              var targetProperty = targetType.getProperty(filterMapping.getProperty());

              if (targetProperty instanceof Attribute targetAttribute) {
                var relationFilter = FilterDefinition.builder()
                    .path(Path.fromProperties(targetProperty))
                    .operator(filterMapping.getOperator())
                    .valueExtractor(properties -> targetAttribute.getType()
                        .mapSourceValue(properties.get(sourcePath.getFirstSegment())))
                    .build();

                nextOperations.add(NextOperation.builder()
                    .property(relation)
                    .delegateOperation(fetchSourceObject(targetTypeRef, nestedSourcePaths, true, relationFilter))
                    .build());
              } else {
                throw new OrchestrateException("Filter property is not an attribute: " + targetProperty.getName());
              }

              return;
            }

            var keyMapping = relation.getKeyMapping();

            if (keyMapping != null) {
              keyMapping.values()
                  .forEach(keyPath -> {
                    if (!keyPath.isLeaf()) {
                      throw new OrchestrateException("Only leaf paths are (currently) supported for key mapping: " + keyPath);
                    }

                    var keyProperty = sourceType.getProperty(keyPath.getFirstSegment());
                    selectedProperties.add(SelectedProperty.forProperty(keyProperty));
                  });
            } else {
              var targetModel = modelMapping.getSourceModel(targetTypeRef.getModelAlias());

              // TODO: Refactor
              if (property.getCardinality().isSingular()) {
                selectedProperties.add(SelectedProperty.forProperty(property, ObjectRequest.builder(targetModel)
                    .objectType(targetTypeRef)
                    .objectKey(Map.of())
                    .selectedProperties(selectIdentity(targetTypeRef))
                    .build()));
              } else {
                // TODO: Filter
                selectedProperties.add(SelectedProperty.forProperty(property, CollectionRequest.builder(targetModel)
                    .objectType(targetTypeRef)
                    .selectedProperties(selectIdentity(targetTypeRef))
                    .build()));
              }
            }

            var identityPaths = targetType.getIdentityProperties()
                .stream()
                .map(Path::fromProperties)
                .collect(Collectors.toSet());

            // If only identity is selected, no next operation is needed
            if (identityPaths.equals(nestedSourcePaths)) {
              return;
            }

            nextOperations.add(NextOperation.builder()
                .property(relation)
                .delegateOperation(fetchSourceObject(targetTypeRef, nestedSourcePaths, false, null))
                .build());

            return;
          }

          throw new OrchestrateException("Could not map property: " + propertyName);
        });

    // TODO: Refactor
    var sourceModel = modelMapping.getSourceModel(sourceTypeRef.getModelAlias());

    if (isCollection) {
      return CollectionFetchOperation.builder()
          .model(sourceModel)
          .source(source)
          .objectType(sourceType)
          .selectedProperties(unmodifiableSet(selectedProperties))
          .nextOperations(unmodifiableSet(nextOperations))
          .filter(filter)
          .build();
    }

    return ObjectFetchOperation.builder()
        .model(sourceModel)
        .source(source)
        .objectType(sourceType)
        .selectedProperties(unmodifiableSet(selectedProperties))
        .nextOperations(unmodifiableSet(nextOperations))
        .build();
  }

  private FilterDefinition createFilterDefinition(ObjectType targetType, Map<String, Object> arguments) {
    if (arguments == null) {
      return null;
    }

    if (arguments.entrySet().size() > 1) {
      throw new OrchestrateException("Currently only a single filter property is supported.");
    }

    var firstEntry = arguments.entrySet()
        .iterator()
        .next();

    var pathMappings = modelMapping.getObjectTypeMappings(targetType)
        .get(0)
        .getPropertyMapping(firstEntry.getKey())
        .map(PropertyMapping::getPathMappings)
        .orElse(List.of());

    if (pathMappings.size() > 1) {
      throw new OrchestrateException("Currently only a single path mapping is supported when filtering.");
    }

    var firstPath = pathMappings.get(0)
        .getPath();

    if (!firstPath.isLeaf()) {
      throw new OrchestrateException("Currently only direct source root properties can be filtered.");
    }

    return ((Attribute) targetType.getProperty(firstEntry.getKey()))
        .getType()
        .createFilterDefinition(firstPath, firstEntry.getValue());
  }

  private FilterDefinition createFilterDefinition(ObjectType sourceType, InverseRelation inverseRelation) {
    var filterDefinition = FilterDefinition.builder();
    var keyMapping = inverseRelation.getOriginRelation()
        .getKeyMapping();

    if (keyMapping != null) {
      // TODO: Composite keys
      var keyMappingEntry = keyMapping.entrySet()
          .iterator()
          .next();

      filterDefinition.path(keyMappingEntry.getValue())
          .valueExtractor(input -> input.get(keyMappingEntry.getKey()));
    } else {
      filterDefinition.path(Path.fromProperties(inverseRelation.getOriginRelation()))
          .valueExtractor(input -> extractKey(sourceType, input));
    }

    return filterDefinition.build();
  }

  private Set<SelectedProperty> selectIdentity(ObjectTypeRef typeRef) {
    return modelMapping.getSourceType(typeRef)
        .getIdentityProperties()
        .stream()
        .map(SelectedProperty::forProperty)
        .collect(toSet());
  }
}