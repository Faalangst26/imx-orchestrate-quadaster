package nl.geostandaarden.imx.orchestrate.source.file;

import static java.util.Collections.emptyList;
import static nl.geostandaarden.imx.orchestrate.source.file.FileUtils.getObjectProperties;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import lombok.extern.slf4j.Slf4j;
import nl.geostandaarden.imx.orchestrate.engine.exchange.CollectionRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.DataRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.ObjectRequest;
import nl.geostandaarden.imx.orchestrate.engine.source.DataRepository;
import nl.geostandaarden.imx.orchestrate.model.filters.FilterExpression;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
class FileRepository implements DataRepository {

  private final Map<String, Map<Map<String, Object>, ObjectNode>> objectMap = new LinkedHashMap<>();

  @Override
  public Mono<Map<String, Object>> findOne(ObjectRequest objectRequest) {
    var objectProperties = getObjectNodes(objectRequest)
        .map(typeObjects -> typeObjects.get(objectRequest.getObjectKey()))
        .map(objectNode -> getObjectProperties(objectNode, objectRequest.getSelectedProperties()))
        .orElse(null);


    var uitgepakt = Mono.justOrEmpty(objectProperties);
    uitgepakt.subscribe(items -> {
      for (var item : items.keySet() ){

        log.debug("output map: " + item + ":" + items.get(item));
      }

    }, error -> {

    });
    return uitgepakt;
  }

  @Override
  public Flux<Map<String, Object>> find(CollectionRequest collectionRequest) {
    var objectList = getObjectNodes(collectionRequest)
        .map(Map::values)
        .orElse(emptyList())
        .stream()
        .filter(createFilter(collectionRequest.getFilter()))
        .map(objectNode -> getObjectProperties(objectNode, collectionRequest.getSelectedProperties()))
        .toList();

    return Flux.fromIterable(objectList);
  }

  private Predicate<ObjectNode> createFilter(FilterExpression filterExpression) {
    if (filterExpression == null) {
      return objectNode -> true;
    }

    var valueClassName = filterExpression.getValue()
        .getClass()
        .getName();

    // TODO: Handle special type-mapping
    if (valueClassName.startsWith("org.locationtech.jts.geom")) {
      return objectNode -> false;
    }

    var jsonValue = new ObjectMapper()
        .valueToTree(filterExpression.getValue());

    var jsonPointer = JsonPointer.compile("/".concat(filterExpression.getPath().toString()));

    return objectNode -> {
      var propertyNode = objectNode.at(jsonPointer);

      if (propertyNode.isArray()) {
        var arrayElements = propertyNode.elements();

        while (arrayElements.hasNext()) {
          if (jsonValue.equals(arrayElements.next())) {
            return true;
          }
        }

        return false;
      }

      return propertyNode.equals(jsonValue);
    };
  }

  public void add(String typeName, Map<String, Object> objectKey, ObjectNode objectNode) {
    objectMap.putIfAbsent(typeName, new LinkedHashMap<>());
    objectMap.get(typeName)
        .put(objectKey, objectNode);
  }

  private Optional<Map<Map<String, Object>, ObjectNode>> getObjectNodes(DataRequest dataRequest) {
    return Optional.ofNullable(objectMap.get(dataRequest.getObjectType()
        .getName()));
  }
}
