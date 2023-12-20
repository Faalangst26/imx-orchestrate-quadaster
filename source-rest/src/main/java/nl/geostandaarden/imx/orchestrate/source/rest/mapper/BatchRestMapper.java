package nl.geostandaarden.imx.orchestrate.source.rest.mapper;

import graphql.language.*;
import lombok.RequiredArgsConstructor;
import nl.geostandaarden.imx.orchestrate.engine.exchange.BatchRequest;
import nl.geostandaarden.imx.orchestrate.engine.source.SourceException;
import nl.geostandaarden.imx.orchestrate.source.rest.config.RestOrchestrateConfig;
import nl.geostandaarden.imx.orchestrate.source.rest.executor.ExecutionInputRest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.springframework.util.StringUtils.uncapitalize;

@RequiredArgsConstructor
public class BatchRestMapper extends AbstractRestMapper<BatchRequest> {

  private static final String OPERATION_NAME = "Query";

  private final RestOrchestrateConfig config;

  public Map<String, Object> convert(BatchRequest request) {

    return null;
  }

  private List<Argument> getArguments(BatchRequest request) {
    var argumentMap = new ConcurrentHashMap<String, List<String>>();

    for (var objectKey : request.getObjectKeys()) {
      var entry = objectKey.entrySet()
          .stream()
          .findFirst()
          .orElseThrow();

      if (!argumentMap.containsKey(entry.getKey())) {
        argumentMap.put(entry.getKey(), new ArrayList<>());
      }

      argumentMap.get(entry.getKey())
          .add((String) entry.getValue());
    }

    if (argumentMap.size() > 1) {
      throw new SourceException("Batch requests can only contain values for 1 key property.");
    }

    var argument = argumentMap.entrySet()
        .stream()
        .findFirst()
        .orElseThrow();
    return List.of(getArgument(argument.getKey(), argument.getValue()));
  }

  private Argument getArgument(String name, Object value) {
    return new Argument(name, ValueMapper.mapToValue(value));
  }

}
