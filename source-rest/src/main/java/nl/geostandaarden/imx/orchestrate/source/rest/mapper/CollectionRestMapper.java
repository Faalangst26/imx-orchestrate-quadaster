package nl.geostandaarden.imx.orchestrate.source.rest.mapper;


import lombok.RequiredArgsConstructor;
import nl.geostandaarden.imx.orchestrate.engine.exchange.CollectionRequest;
import nl.geostandaarden.imx.orchestrate.engine.source.SourceException;
import nl.geostandaarden.imx.orchestrate.model.filters.FilterExpression;
import nl.geostandaarden.imx.orchestrate.model.filters.FilterOperator;
import nl.geostandaarden.imx.orchestrate.source.rest.config.RestOrchestrateConfig;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class CollectionRestMapper extends AbstractRestMapper<CollectionRequest> {

  private static final String OPERATION_NAME = "Query";

  private final RestOrchestrateConfig config;

  public Map<String, Object> convert(CollectionRequest request) {
    return null;
  }


  private String mapToFilterOperator(FilterOperator filterOperator) {
    return switch (filterOperator.getType()) {
      case "equals" -> "eq";
      default -> throw new SourceException(String.format("Unknown filter operator '%s'", filterOperator.getType()));
    };
  }

}
