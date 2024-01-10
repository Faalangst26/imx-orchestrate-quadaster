package nl.geostandaarden.imx.orchestrate.source.rest.mapper;

import nl.geostandaarden.imx.orchestrate.engine.exchange.DataRequest;
import nl.geostandaarden.imx.orchestrate.source.rest.Result.AbstractResult;

import java.util.Map;

abstract class AbstractRestMapper<T extends DataRequest> {

  abstract Map<String, Object> convert(T request);


}
