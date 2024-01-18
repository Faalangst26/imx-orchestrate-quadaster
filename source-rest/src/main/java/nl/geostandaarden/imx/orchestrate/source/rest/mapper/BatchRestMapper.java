package nl.geostandaarden.imx.orchestrate.source.rest.mapper;

import lombok.RequiredArgsConstructor;
import nl.geostandaarden.imx.orchestrate.engine.exchange.BatchRequest;

import java.util.*;

@RequiredArgsConstructor
public class BatchRestMapper extends AbstractRestMapper<BatchRequest> {

  public Map<String, Object> convert(BatchRequest request) {
    Map<String, Object> data = new HashMap<>();
    return data;
  }

}
