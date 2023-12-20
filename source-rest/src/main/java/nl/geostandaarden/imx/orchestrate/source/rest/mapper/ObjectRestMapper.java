package nl.geostandaarden.imx.orchestrate.source.rest.mapper;

import graphql.language.*;
import lombok.RequiredArgsConstructor;
import nl.geostandaarden.imx.orchestrate.engine.exchange.ObjectRequest;
import nl.geostandaarden.imx.orchestrate.source.rest.config.RestOrchestrateConfig;


import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class ObjectRestMapper extends AbstractRestMapper<ObjectRequest> {

  private static final String OPERATION_NAME = "Query";

  private final RestOrchestrateConfig config;

  @Override
  public Map<String, Object> convert(ObjectRequest request) {

    return request.getObjectKey();

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
