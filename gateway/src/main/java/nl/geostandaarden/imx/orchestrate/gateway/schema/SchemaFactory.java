package nl.geostandaarden.imx.orchestrate.gateway.schema;

import static graphql.language.FieldDefinition.newFieldDefinition;
import static graphql.language.ObjectTypeDefinition.newObjectTypeDefinition;
import static graphql.schema.FieldCoordinates.coordinates;
import static graphql.schema.GraphQLCodeRegistry.newCodeRegistry;
import static graphql.schema.SchemaTransformer.transformSchema;
import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;
import static nl.geostandaarden.imx.orchestrate.gateway.schema.SchemaUtils.applyCardinality;
import static nl.geostandaarden.imx.orchestrate.gateway.schema.SchemaUtils.requiredListType;
import static nl.geostandaarden.imx.orchestrate.gateway.schema.SchemaUtils.requiredType;
import static org.apache.commons.lang3.StringUtils.uncapitalize;

import graphql.language.InputObjectTypeDefinition;
import graphql.language.InputValueDefinition;
import graphql.language.ObjectTypeDefinition;
import graphql.language.Type;
import graphql.language.TypeName;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.TypeDefinitionRegistry;
import java.util.List;
import java.util.function.UnaryOperator;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import nl.geostandaarden.imx.orchestrate.engine.OrchestrateEngine;
import nl.geostandaarden.imx.orchestrate.engine.OrchestrateException;
import nl.geostandaarden.imx.orchestrate.gateway.fetch.GenericDataFetcher;
import nl.geostandaarden.imx.orchestrate.gateway.fetch.ObjectKeyFetcher;
import nl.geostandaarden.imx.orchestrate.gateway.fetch.ObjectLineageFetcher;
import nl.geostandaarden.imx.orchestrate.gateway.fetch.PropertyValueFetcher;
import nl.geostandaarden.imx.orchestrate.model.AbstractRelation;
import nl.geostandaarden.imx.orchestrate.model.Attribute;
import nl.geostandaarden.imx.orchestrate.model.ModelMapping;
import nl.geostandaarden.imx.orchestrate.model.ObjectType;
import nl.geostandaarden.imx.orchestrate.model.Path;
import nl.geostandaarden.imx.orchestrate.model.PathMapping;
import nl.geostandaarden.imx.orchestrate.model.Property;
import nl.geostandaarden.imx.orchestrate.model.PropertyMapping;
import nl.geostandaarden.imx.orchestrate.model.lineage.ObjectLineage;
import nl.geostandaarden.imx.orchestrate.model.lineage.ObjectReference;
import nl.geostandaarden.imx.orchestrate.model.lineage.OrchestratedDataElement;
import nl.geostandaarden.imx.orchestrate.model.lineage.PathExecution;
import nl.geostandaarden.imx.orchestrate.model.lineage.PathMappingExecution;
import nl.geostandaarden.imx.orchestrate.model.lineage.PropertyMappingExecution;
import nl.geostandaarden.imx.orchestrate.model.lineage.SourceDataElement;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SchemaFactory {

  private final TypeDefinitionRegistry typeDefinitionRegistry = new TypeDefinitionRegistry();

  private final GraphQLCodeRegistry.Builder codeRegistryBuilder = newCodeRegistry();

  private final ObjectTypeDefinition.Builder queryTypeBuilder = newObjectTypeDefinition().name(SchemaConstants.QUERY_TYPE);

  private final ModelMapping modelMapping;

  private final GenericDataFetcher genericDataFetcher;

  private final ObjectLineageFetcher objectLineageFetcher;

  private final UnaryOperator<String> lineageRenamer;

  public static GraphQLSchema create(OrchestrateEngine engine) {
    var modelMapping = engine.getModelMapping();

    UnaryOperator<String> lineageRenamer =
        fieldName -> modelMapping.getLineageNameMapping().getOrDefault(fieldName, fieldName);

    var genericDataFetcher = new GenericDataFetcher(engine, lineageRenamer.apply(SchemaConstants.HAS_LINEAGE_FIELD));
    var objectLineageFetcher = new ObjectLineageFetcher(modelMapping.getLineageNameMapping());

    return new SchemaFactory(modelMapping, genericDataFetcher, objectLineageFetcher, lineageRenamer).create();
  }

  private GraphQLSchema create() {
    registerLineageTypes();

    modelMapping.getTargetModel()
        .getObjectTypes()
        .forEach(this::registerObjectType);

    typeDefinitionRegistry.add(queryTypeBuilder.build());

    var runtimeWiring = newRuntimeWiring()
        .codeRegistry(codeRegistryBuilder.build())
        .build();

    var schema = new SchemaGenerator()
        .makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);

    return transformSchema(schema, new SchemaVisitor(lineageRenamer.apply(SchemaConstants.HAS_LINEAGE_FIELD)));
  }

  private void registerObjectType(ObjectType objectType) {
    if (!modelMapping.getObjectTypeMappings()
        .containsKey(objectType.getName())) {
      return;
    }

    var objectTypeDefinition = createObjectTypeDefinition(objectType);
    typeDefinitionRegistry.add(objectTypeDefinition);

    var identityProperties = objectType.getIdentityProperties();

    if (identityProperties.isEmpty()) {
      return;
    }

    var baseName = uncapitalize(objectTypeDefinition.getName());
    var collectionName = baseName.concat(SchemaConstants.QUERY_COLLECTION_SUFFIX);

    var objectField = newFieldDefinition()
        .name(baseName)
        .type(new TypeName(objectTypeDefinition.getName()))
        .inputValueDefinitions(createIdentityArguments(objectType))
        .build();

    var collectionField = newFieldDefinition()
        .name(collectionName)
        .type(requiredListType(objectTypeDefinition.getName()))
        .build();

    var filterFields = objectType.getProperties(Attribute.class)
        .stream()
        .filter(attribute -> isFilterable(objectType, attribute))
        .map(attribute -> InputValueDefinition.newInputValueDefinition()
            .name(attribute.getName())
            .type(mapFieldType(attribute, false))
            .build())
        .toList();

    if (!filterFields.isEmpty()) {
      typeDefinitionRegistry.add(InputObjectTypeDefinition.newInputObjectDefinition()
          .name(objectType.getName()
              .concat(SchemaConstants.QUERY_FILTER_SUFFIX))
          .inputValueDefinitions(filterFields)
          .build());

      var filterArgument = InputValueDefinition.newInputValueDefinition()
          .name(SchemaConstants.QUERY_FILTER_ARGUMENTS)
          .type(new TypeName(objectType.getName()
              .concat(SchemaConstants.QUERY_FILTER_SUFFIX)))
          .build();

      collectionField = collectionField.transform(builder -> builder.inputValueDefinition(filterArgument));
    }

    var objectKeyType = InputObjectTypeDefinition.newInputObjectDefinition()
        .name(objectType.getName().concat(SchemaConstants.KEY_TYPE_SUFFIX))
        .inputValueDefinitions(objectType.getIdentityProperties()
            .stream()
            .map(Attribute.class::cast)
            .map(property -> InputValueDefinition.newInputValueDefinition()
                .name(property.getName())
                .type(requiredType(property.getType()
                    .getName()))
                .build())
            .toList())
        .build();

    var batchName = baseName.concat(SchemaConstants.QUERY_BATCH_SUFFIX);

    var batchField = newFieldDefinition()
        .name(batchName)
        .type(requiredListType(objectTypeDefinition.getName()))
        .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
            .name(SchemaConstants.BATCH_KEYS_ARG)
            .type(requiredListType(objectKeyType.getName()))
            .build())
        .build();

    typeDefinitionRegistry.add(objectKeyType);

    queryTypeBuilder.fieldDefinition(objectField)
        .fieldDefinition(collectionField)
        .fieldDefinition(batchField);

    codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(SchemaConstants.QUERY_TYPE, baseName), genericDataFetcher)
        .dataFetcher(FieldCoordinates.coordinates(SchemaConstants.QUERY_TYPE, collectionName), genericDataFetcher)
        .dataFetcher(FieldCoordinates.coordinates(SchemaConstants.QUERY_TYPE, batchName), genericDataFetcher);
  }

  private boolean isFilterable(ObjectType objectType, Attribute attribute) {
    if (attribute.isIdentifier()) {
      return false;
    }

    var pathMappings = modelMapping.getObjectTypeMappings(objectType)
        .stream()
        .flatMap(typeMapping -> typeMapping.getPropertyMapping(attribute)
            .stream()
            .flatMap(propertyMapping -> propertyMapping.getPathMappings().stream()))
        .toList();

    var firstPath = pathMappings.get(0)
        .getPath();

    return pathMappings.size() == 1 && firstPath.isLeaf();
  }

  private void registerLineageTypes() {
    typeDefinitionRegistry.add(newObjectTypeDefinition()
        .name(lineageRenamer.apply(ObjectLineage.class.getSimpleName()))
        .fieldDefinition(newFieldDefinition()
            .name(lineageRenamer.apply("orchestratedDataElements"))
            .type(requiredListType(lineageRenamer.apply(OrchestratedDataElement.class.getSimpleName())))
            .build())
        .build());

    typeDefinitionRegistry.add(newObjectTypeDefinition()
        .name(lineageRenamer.apply(OrchestratedDataElement.class.getSimpleName()))
        .fieldDefinition(newFieldDefinition()
            .name(lineageRenamer.apply("subject"))
            .type(requiredType(lineageRenamer.apply(ObjectReference.class.getSimpleName())))
            .build())
        .fieldDefinition(newFieldDefinition()
            .name(lineageRenamer.apply("property"))
            .type(requiredType("String"))
            .build())
        .fieldDefinition(newFieldDefinition()
            .name(lineageRenamer.apply("value"))
            .type(new TypeName("PropertyResult"))
            .build())
        .fieldDefinition(newFieldDefinition()
            .name(lineageRenamer.apply("wasGeneratedBy"))
            .type(requiredType(lineageRenamer.apply(PropertyMappingExecution.class.getSimpleName())))
            .build())
        .fieldDefinition(newFieldDefinition()
            .name(lineageRenamer.apply("wasDerivedFrom"))
            .type(requiredListType(lineageRenamer.apply(SourceDataElement.class.getSimpleName())))
            .build())
        .build());

    typeDefinitionRegistry.add(newObjectTypeDefinition()
        .name(lineageRenamer.apply(PropertyMappingExecution.class.getSimpleName()))
        .fieldDefinition(newFieldDefinition()
            .name(lineageRenamer.apply("used"))
            .type(requiredType(PropertyMapping.class.getSimpleName()))
            .build())
        .fieldDefinition(newFieldDefinition()
            .name(lineageRenamer.apply("wasInformedBy"))
            .type(requiredListType(lineageRenamer.apply(PathMappingExecution.class.getSimpleName())))
            .build())
        .build());

    typeDefinitionRegistry.add(newObjectTypeDefinition()
        .name(PropertyMapping.class.getSimpleName())
        .fieldDefinition(newFieldDefinition()
            .name("pathMappings")
            .type(requiredListType(PathMapping.class.getSimpleName()))
            .build())
        .build());

    typeDefinitionRegistry.add(newObjectTypeDefinition()
        .name(PathMapping.class.getSimpleName())
        .fieldDefinition(newFieldDefinition()
            .name("path")
            .type(requiredType(Path.class.getSimpleName()))
            .build())
        .build());

    typeDefinitionRegistry.add(newObjectTypeDefinition()
        .name(Path.class.getSimpleName())
        .fieldDefinition(newFieldDefinition()
            .name("segments")
            .type(requiredListType("String"))
            .build())
        .build());

    typeDefinitionRegistry.add(newObjectTypeDefinition()
        .name(lineageRenamer.apply(PathMappingExecution.class.getSimpleName()))
        .fieldDefinition(newFieldDefinition()
            .name(lineageRenamer.apply("used"))
            .type(requiredType(PathMapping.class.getSimpleName()))
            .build())
        .fieldDefinition(newFieldDefinition()
            .name(lineageRenamer.apply("wasInformedBy"))
            .type(requiredListType(lineageRenamer.apply(PathExecution.class.getSimpleName())))
            .build())
        .build());

    typeDefinitionRegistry.add(newObjectTypeDefinition()
        .name(lineageRenamer.apply(PathExecution.class.getSimpleName()))
        .fieldDefinition(newFieldDefinition()
            .name(lineageRenamer.apply("used"))
            .type(requiredType(Path.class.getSimpleName()))
            .build())
        .fieldDefinition(newFieldDefinition()
            .name(lineageRenamer.apply("startNode"))
            .type(requiredType(lineageRenamer.apply(ObjectReference.class.getSimpleName())))
            .build())
        .fieldDefinition(newFieldDefinition()
            .name(lineageRenamer.apply("references"))
            .type(requiredListType(lineageRenamer.apply(SourceDataElement.class.getSimpleName())))
            .build())
        .build());

    typeDefinitionRegistry.add(newObjectTypeDefinition()
        .name(lineageRenamer.apply(SourceDataElement.class.getSimpleName()))
        .fieldDefinition(newFieldDefinition()
            .name(lineageRenamer.apply("subject"))
            .type(requiredType(lineageRenamer.apply(ObjectReference.class.getSimpleName())))
            .build())
        .fieldDefinition(newFieldDefinition()
            .name(lineageRenamer.apply("property"))
            .type(requiredType("String"))
            .build())
        .fieldDefinition(newFieldDefinition()
            .name(lineageRenamer.apply("value"))
            .type(new TypeName(lineageRenamer.apply("PropertyResult")))
            .build())
        .build());

    typeDefinitionRegistry.add(newObjectTypeDefinition()
        .name(lineageRenamer.apply(ObjectReference.class.getSimpleName()))
        .fieldDefinition(newFieldDefinition()
            .name(lineageRenamer.apply("objectType"))
            .type(requiredType("String"))
            .build())
        .fieldDefinition(newFieldDefinition()
            .name(lineageRenamer.apply("objectKey"))
            .type(requiredType("String"))
            .build())
        .build());

    typeDefinitionRegistry.add(newObjectTypeDefinition()
        .name(lineageRenamer.apply("PropertyResult"))
        .fieldDefinition(newFieldDefinition()
            .name(lineageRenamer.apply("stringValue"))
            .type(new TypeName("String"))
            .build())
        .fieldDefinition(newFieldDefinition()
            .name(lineageRenamer.apply("integerValue"))
            .type(new TypeName("Int"))
            .build())
        .fieldDefinition(newFieldDefinition()
            .name(lineageRenamer.apply("booleanValue"))
            .type(new TypeName("Boolean"))
            .build())
        .fieldDefinition(newFieldDefinition()
            .name(lineageRenamer.apply("objectValue"))
            .type(new TypeName(lineageRenamer.apply(ObjectReference.class.getSimpleName())))
            .build())
        .build());

    var valueFetcher = new PropertyValueFetcher(lineageRenamer);
    var objectKeyFetcher = new ObjectKeyFetcher(lineageRenamer);

    codeRegistryBuilder.dataFetcher(coordinates(lineageRenamer.apply(OrchestratedDataElement.class.getSimpleName()),
            lineageRenamer.apply("value")), valueFetcher)
        .dataFetcher(coordinates(lineageRenamer.apply(SourceDataElement.class.getSimpleName()),
            lineageRenamer.apply("value")), valueFetcher)
        .dataFetcher(coordinates(lineageRenamer.apply(ObjectReference.class.getSimpleName()),
            lineageRenamer.apply("objectKey")), objectKeyFetcher);
  }

  private ObjectTypeDefinition createObjectTypeDefinition(ObjectType objectType) {
    var objectTypeDefinitionBuilder = newObjectTypeDefinition()
        .name(objectType.getName());

    objectType.getProperties()
        .stream()
        .map(property -> newFieldDefinition()
            .name(property.getName())
            .type(mapFieldType(property))
            .build())
        .forEach(objectTypeDefinitionBuilder::fieldDefinition);

    var lineageFieldName = lineageRenamer.apply(SchemaConstants.HAS_LINEAGE_FIELD);

    objectTypeDefinitionBuilder.fieldDefinition(newFieldDefinition()
        .name(lineageFieldName)
        .type(requiredType(lineageRenamer.apply(ObjectLineage.class.getSimpleName())))
        .build());

    codeRegistryBuilder.dataFetcher(coordinates(objectType.getName(), lineageFieldName), objectLineageFetcher);

    return objectTypeDefinitionBuilder.build();
  }

  private Type<?> mapFieldType(Property property) {
    if (property instanceof Attribute attribute) {
      return mapFieldType(attribute, true);
    }

    if (property instanceof AbstractRelation relation) {
      var target = relation.getTarget();
      return applyCardinality(new TypeName(target.getName()), relation.getCardinality());
    }

    throw new OrchestrateException("Could not map field type");
  }

  private Type<?> mapFieldType(Attribute attribute, boolean applyCardinality) {
    var typeName = attribute.getType()
        .getName();

    var type = switch (typeName) {
      case "Integer" -> new TypeName("Int");
      case "Double" -> new TypeName("Float");
      // TODO: Refactor
      case "Geometry" -> new TypeName("String");
      default -> new TypeName(typeName);
    };

    return applyCardinality ? applyCardinality(type, attribute.getCardinality()) : type;
  }

  private List<InputValueDefinition> createIdentityArguments(ObjectType objectType) {
    return objectType.getIdentityProperties(Attribute.class)
        .stream()
        .map(attribute -> InputValueDefinition.newInputValueDefinition()
            .name(attribute.getName())
            .type(mapFieldType(attribute, true))
            .build())
        .toList();
  }
}
