package nl.geostandaarden.imx.orchestrate.source.rest.mapper;

import lombok.RequiredArgsConstructor;
import nl.geostandaarden.imx.orchestrate.engine.exchange.ObjectRequest;
import nl.geostandaarden.imx.orchestrate.source.rest.config.RestOrchestrateConfig;
import java.util.Map;

@RequiredArgsConstructor
public class ObjectRestMapper extends AbstractRestMapper<ObjectRequest> {

  private static final String OPERATION_NAME = "Query";

  private final RestOrchestrateConfig config;

  @Override
  public Map<String, Object> convert(ObjectRequest request) {
    Map<String, Object> data = request.getObjectKey();
    return data;
  }

}
