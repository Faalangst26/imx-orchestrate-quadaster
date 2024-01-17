package nl.geostandaarden.imx.orchestrate.source.rest.mapper;

import lombok.RequiredArgsConstructor;
import nl.geostandaarden.imx.orchestrate.engine.exchange.BatchRequest;
import nl.geostandaarden.imx.orchestrate.source.rest.config.RestOrchestrateConfig;
import java.util.*;

@RequiredArgsConstructor
public class BatchRestMapper extends AbstractRestMapper<BatchRequest> {

  private static final String OPERATION_NAME = "Query";

  private final RestOrchestrateConfig config;

  public Map<String, Object> convert(BatchRequest request) {
    Map<String, Object> data = new HashMap<>();
    return data;
  }

}
