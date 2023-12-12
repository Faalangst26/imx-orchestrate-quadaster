package nl.geostandaarden.imx.orchestrate.souce.rest.mapper;

import static org.springframework.util.StringUtils.uncapitalize;

import graphql.ExecutionResult;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import nl.geostandaarden.imx.orchestrate.souce.rest.config.RestOrchestrateConfig;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ResponseMapper {

    private final RestOrchestrateConfig config;

    public Mono<Map<String, Object>> processFindOneResult(Mono<ExecutionResult> executionResult) {
        /*return executionResult.map(result -> Optional.ofNullable((Map<String, Object>) result.getData())
                        .orElse(Map.of()))
                .map(ResponseMapper::unwrapRefs);*/
        return null;
    }

    public Flux<Map<String, Object>> processFindResult(Mono<ExecutionResult> executionResult, String objectName) {
        /*return executionResult.map(e -> getCollectionResult(e, objectName))
                .flatMapMany(Flux::fromIterable)
                .map(ResponseMapper::unwrapRefs);*/
        return null;
    }

    public Flux<Map<String, Object>> processBatchResult(Mono<ExecutionResult> executionResult, String objectName) {
        /*return executionResult.map(e -> getBatchResult(e, objectName))
                .flatMapMany(Flux::fromIterable)
                .map(ResponseMapper::unwrapRefs);*/
        return null;
    }

    private List<Map<String, Object>> getCollectionResult(ExecutionResult executionResult, String objectName) {
/*        var data = executionResult.getData();
        var result = ((Map<String, Map<String, List<Map<String, Object>>>>) data)
                .get(uncapitalize(objectName) + config.getCollectionSuffix());
        return result.get(MapperConstants.NODES);*/
        return null;
    }

    private List<Map<String, Object>> getBatchResult(ExecutionResult executionResult, String objectName) {
        /*var data = executionResult.getData();
        return ((Map<String, List<Map<String, Object>>>) data).get(uncapitalize(objectName) + config.getBatchSuffix());*/
        return null;
    }
    private static Map<String, Object> unwrapRefs(Map<String, Object> item) {
        /*return item.entrySet()
                .stream()
                .collect(HashMap::new, (acc, e) -> {
                    var value = e.getValue();

                    if (value instanceof Map<?, ?> mapValue) {
                        if (mapValue.containsKey("ref")) {
                            value = mapValue.get("ref");
                        }

                        if (mapValue.containsKey("refs")) {
                            value = mapValue.get("refs");
                        }
                    }

                    acc.put(e.getKey(), value);
                }, HashMap::putAll);*/
        return null;
    }
}
