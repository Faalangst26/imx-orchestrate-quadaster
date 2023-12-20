package nl.geostandaarden.imx.orchestrate.source.rest.mapper;

import graphql.language.*;
import lombok.RequiredArgsConstructor;
import nl.geostandaarden.imx.orchestrate.engine.exchange.ObjectRequest;
import nl.geostandaarden.imx.orchestrate.source.rest.config.RestOrchestrateConfig;
import nl.geostandaarden.imx.orchestrate.source.rest.executor.ExecutionInputRest;


import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static org.springframework.util.StringUtils.*;

@RequiredArgsConstructor
public class ObjectRestMapper extends AbstractRestMapper<ObjectRequest> {

  private static final String OPERATION_NAME = "Query";

  private final RestOrchestrateConfig config;

  @Override
  public Map<String, String> convert(ObjectRequest request) {
    var properties = request.getObjectKey();
    Map<String,String> queryValues = properties.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> (String)e.getValue()));
    return queryValues;

  }

  private List<Argument> getArguments(ObjectRequest request) {
    return request.getObjectKey()
            .entrySet()
            .stream()
            .map(entry -> getArgument(entry.getKey(), entry.getValue()))
            .toList();
  }
  private Argument getArgument(String name, Object value) {
    return new Argument(name, ValueMapper.mapToValue(value));
  }


}
