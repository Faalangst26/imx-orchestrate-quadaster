package nl.geostandaarden.imx.orchestrate.source.rest.mapper;

import graphql.language.*;
import lombok.RequiredArgsConstructor;
import nl.geostandaarden.imx.orchestrate.engine.exchange.ObjectRequest;
import nl.geostandaarden.imx.orchestrate.source.rest.config.RestOrchestrateConfig;
import nl.geostandaarden.imx.orchestrate.source.rest.executor.ExecutionInputRest;


import java.util.List;

import static org.springframework.util.StringUtils.uncapitalize;

@RequiredArgsConstructor
public class ObjectRestMapper extends AbstractRestMapper<ObjectRequest> {

  private static final String OPERATION_NAME = "Query";

  private final RestOrchestrateConfig config;

  @Override
  public ExecutionInputRest convert(ObjectRequest request) {
    var fieldName = uncapitalize(request.getObjectType()
        .getName());

    var arguments = getArguments(request);

    var selectionSet = createSelectionSet(request.getSelectedProperties());
    var queryField = new Field(fieldName, arguments, selectionSet);

    var query = OperationDefinition.newOperationDefinition()
        .name(OPERATION_NAME)
        .operation(OperationDefinition.Operation.QUERY)
        .selectionSet(new SelectionSet(List.of(queryField)))
        .build();

    return ExecutionInputRest.newExecutionInput()
        .query(AstPrinter.printAst(query))
        .build();
  }

  private List<Argument> getArguments(ObjectRequest request) {
    return request.getObjectKey()
            .entrySet()
            .stream()
            .map(entry -> getArgument(entry.getKey(), entry.getValue()))
            .toList();
  }
  private Argument getArgument(String name, Object value) {
    return new Argument(name, ValueMapper.mapToValue(value));
  }


}
