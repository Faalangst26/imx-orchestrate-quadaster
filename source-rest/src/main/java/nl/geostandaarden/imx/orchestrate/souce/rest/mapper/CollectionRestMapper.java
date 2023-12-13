package nl.geostandaarden.imx.orchestrate.souce.rest.mapper;

import graphql.ExecutionInput;
import graphql.language.*;
import lombok.RequiredArgsConstructor;
import nl.geostandaarden.imx.orchestrate.engine.exchange.CollectionRequest;
import nl.geostandaarden.imx.orchestrate.engine.source.SourceException;
import nl.geostandaarden.imx.orchestrate.model.filters.FilterExpression;
import nl.geostandaarden.imx.orchestrate.model.filters.FilterOperator;
import nl.geostandaarden.imx.orchestrate.souce.rest.config.RestOrchestrateConfig;
import nl.geostandaarden.imx.orchestrate.souce.rest.executor.ExecutionInputRest;
import nl.geostandaarden.imx.orchestrate.source.graphql.config.GraphQlOrchestrateConfig;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static nl.geostandaarden.imx.orchestrate.souce.rest.mapper.MapperConstants.NODES;
import static org.springframework.util.StringUtils.uncapitalize;

@RequiredArgsConstructor
public class CollectionRestMapper extends AbstractRestMapper<CollectionRequest> {

  private static final String OPERATION_NAME = "Query";

  private final RestOrchestrateConfig config;

  public ExecutionInputRest convert(CollectionRequest request) {
    var filterExpression = request.getFilter();

    var fieldName = uncapitalize(request.getObjectType()
        .getName()) + config.getCollectionSuffix();

    var selectionSet = createSelectionSet(request.getSelectedProperties());
    var nodes = new Field(NODES, selectionSet);

    var fieldBuilder = Field.newField(fieldName)
        .selectionSet(new SelectionSet(List.of(nodes)));

    if (filterExpression != null) {
      var filterField = getFilterField(filterExpression);
      var filterValue = ObjectValue.newObjectValue()
          .objectField(filterField)
          .build();

      var filter = Argument.newArgument()
          .name("filter")
          .value(filterValue)
          .build();

      fieldBuilder.arguments(List.of(filter));
    }

    var query = OperationDefinition.newOperationDefinition()
        .name(OPERATION_NAME)
        .operation(OperationDefinition.Operation.QUERY)
        .selectionSet(new SelectionSet(List.of(fieldBuilder.build())))
        .build();

    return ExecutionInputRest.newExecutionInput()
        .query(AstPrinter.printAst(query))
        .build();
  }

  private ObjectField getFilterField(FilterExpression filterExpression) {
    var filterOperator = filterExpression.getOperator();
    var reverseFieldPaths = new ArrayList<>(filterExpression.getPath()
      .getSegments());
    Collections.reverse(reverseFieldPaths);
    var value = filterExpression.getValue();

    var objectField = ObjectField.newObjectField()
      .name(mapToFilterOperator(filterOperator))
      .value(ValueMapper.mapToValue(value))
      .build();

    return getFilterField(reverseFieldPaths, objectField);
  }

  private ObjectField getFilterField(List<String> reverseFieldPaths, ObjectField childObjectField) {
    var fieldName = reverseFieldPaths.get(0);
    var fieldValue = ObjectValue.newObjectValue()
      .objectField(childObjectField)
      .build();
    var objectField = ObjectField.newObjectField()
      .name(fieldName)
      .value(fieldValue)
      .build();

    if (reverseFieldPaths.size() > 1) {
      return getFilterField(reverseFieldPaths.subList(1, reverseFieldPaths.size()), objectField);
    }
    return objectField;
  }

  private String mapToFilterOperator(FilterOperator filterOperator) {
    return switch (filterOperator.getType()) {
      case "equals" -> "eq";
      default -> throw new SourceException(String.format("Unknown filter operator '%s'", filterOperator.getType()));
    };
  }

}
