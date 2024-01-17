package nl.geostandaarden.imx.orchestrate.source.rest.mapper;

import lombok.RequiredArgsConstructor;
import nl.geostandaarden.imx.orchestrate.engine.exchange.CollectionRequest;
import nl.geostandaarden.imx.orchestrate.source.rest.config.RestOrchestrateConfig;
import java.util.*;

@RequiredArgsConstructor
public class CollectionRestMapper extends AbstractRestMapper<CollectionRequest> {
  private static final String OPERATION_NAME = "Query";

  private final RestOrchestrateConfig config;

  @Override
  public Map<String, Object> convert(CollectionRequest request) {
    Map<String, Object> data = new HashMap<>();

    // Add properties from AbstractDataRequest
    data.put("ObjectType", request.getObjectType());
    data.put("SelectedProperties", request.getSelectedProperties());

    // Add filter if present
    if (request.getFilter() != null) {
      data.put("Filter", request.getFilter());
    }

    return data;
  }

}
