package nl.geostandaarden.imx.orchestrate.source.rest.mapper;

import nl.geostandaarden.imx.orchestrate.engine.exchange.DataRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.SelectedProperty;
import nl.geostandaarden.imx.orchestrate.engine.source.SourceException;

import java.util.List;
import java.util.Map;
import java.util.Set;

abstract class AbstractRestMapper<T extends DataRequest> {

  abstract Map<String, Object> convert(T request);

}
