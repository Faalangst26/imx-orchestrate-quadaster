package nl.geostandaarden.imx.orchestrate.source.rest.mapper;

import lombok.RequiredArgsConstructor;
import nl.geostandaarden.imx.orchestrate.engine.exchange.CollectionRequest;

import java.util.*;

@RequiredArgsConstructor
public class CollectionRestMapper extends AbstractRestMapper<CollectionRequest> {

  @Override
  public Map<String, Object> convert(CollectionRequest request) {
    Map<String, Object> data = new HashMap<>();


    data.put("ObjectType", request.getObjectType());
    data.put("SelectedProperties", request.getSelectedProperties());


    if (request.getFilter() != null) {
      data.put("Filter", request.getFilter());
    }

    return data;
  }

}
