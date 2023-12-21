package nl.geostandaarden.imx.orchestrate.source.rest.mapper;

import lombok.RequiredArgsConstructor;
import nl.geostandaarden.imx.orchestrate.engine.exchange.BatchRequest;
import nl.geostandaarden.imx.orchestrate.engine.source.SourceException;
import nl.geostandaarden.imx.orchestrate.source.rest.config.RestOrchestrateConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class BatchRestMapper extends AbstractRestMapper<BatchRequest> {

  private static final String OPERATION_NAME = "Query";

  private final RestOrchestrateConfig config;

  public Map<String, Object> convert(BatchRequest request) {

    return null;
  }




}
