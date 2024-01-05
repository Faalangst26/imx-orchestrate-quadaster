package nl.geostandaarden.imx.orchestrate.source.rest.mapper;


import lombok.RequiredArgsConstructor;
import nl.geostandaarden.imx.orchestrate.engine.exchange.CollectionRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.ObjectRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.SelectedProperty;
import nl.geostandaarden.imx.orchestrate.engine.source.SourceException;
import nl.geostandaarden.imx.orchestrate.model.filters.FilterExpression;
import nl.geostandaarden.imx.orchestrate.model.filters.FilterOperator;
import nl.geostandaarden.imx.orchestrate.source.rest.config.RestOrchestrateConfig;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RequiredArgsConstructor
public class CollectionRestMapper extends AbstractRestMapper<CollectionRequest> {
  private static final String OPERATION_NAME = "Query";

  private final RestOrchestrateConfig config;

  @Override
  public Map<String, Object> convert(CollectionRequest request) {

    Set<SelectedProperty> selectedProperties = request.getSelectedProperties();
    FilterExpression filterExpression = request.getFilter();

    Map<String, Object> restApiRequest = new HashMap<>();

    if (!selectedProperties.isEmpty()) {
      restApiRequest.put("selectedProperties", selectedProperties);
    }

    if (filterExpression != null) {
      String filterValue = mapToFilterValue(filterExpression);
      restApiRequest.put("filter", filterValue);
    }

    return restApiRequest;

  }
  private String mapToFilterValue(FilterExpression filterExpression) {

    return "mappedFilterValue";
  }
}
