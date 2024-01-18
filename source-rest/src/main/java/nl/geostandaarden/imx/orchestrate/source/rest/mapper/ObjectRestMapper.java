package nl.geostandaarden.imx.orchestrate.source.rest.mapper;

import lombok.RequiredArgsConstructor;
import nl.geostandaarden.imx.orchestrate.engine.exchange.ObjectRequest;

import java.util.Map;

@RequiredArgsConstructor
public class ObjectRestMapper extends AbstractRestMapper<ObjectRequest> {

  @Override
  public Map<String, Object> convert(ObjectRequest request) {
      return request.getObjectKey();
  }

}
