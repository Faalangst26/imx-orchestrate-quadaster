package nl.geostandaarden.imx.orchestrate.souce.rest.mapper;

import graphql.ExecutionInput;
import graphql.language.*;
import lombok.RequiredArgsConstructor;
import nl.geostandaarden.imx.orchestrate.engine.exchange.BatchRequest;
import nl.geostandaarden.imx.orchestrate.engine.source.SourceException;
import nl.geostandaarden.imx.orchestrate.souce.rest.config.RestOrchestrateConfig;
import nl.geostandaarden.imx.orchestrate.souce.rest.executor.ExecutionInputRest;
import nl.geostandaarden.imx.orchestrate.source.graphql.config.GraphQlOrchestrateConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.springframework.util.StringUtils.uncapitalize;

@RequiredArgsConstructor
public class BatchRestMapper extends AbstractRestMapper<BatchRequest> {

  private static final String OPERATION_NAME = "Query";

  private final RestOrchestrateConfig config;

  public ExecutionInputRest convert(BatchRequest request) {
    var fieldName = uncapitalize(request.getObjectType()
        .getName()) + config.getBatchSuffix();

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

  private List<Argument> getArguments(BatchRequest request) {
    var argumentMap = new ConcurrentHashMap<String, List<String>>();

    for (var objectKey : request.getObjectKeys()) {
      var entry = objectKey.entrySet()
          .stream()
          .findFirst()
          .orElseThrow();

      if (!argumentMap.containsKey(entry.getKey())) {
        argumentMap.put(entry.getKey(), new ArrayList<>());
      }

      argumentMap.get(entry.getKey())
          .add((String) entry.getValue());
    }

    if (argumentMap.size() > 1) {
      throw new SourceException("Batch requests can only contain values for 1 key property.");
    }

    var argument = argumentMap.entrySet()
        .stream()
        .findFirst()
        .orElseThrow();
    return List.of(getArgument(argument.getKey(), argument.getValue()));
  }

  private Argument getArgument(String name, Object value) {
    return new Argument(name, ValueMapper.mapToValue(value));
  }

}
