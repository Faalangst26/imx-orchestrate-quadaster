package nl.geostandaarden.imx.orchestrate.source.rest.mapper;

import static org.springframework.util.StringUtils.uncapitalize;

import graphql.ExecutionResult;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import nl.geostandaarden.imx.orchestrate.source.rest.config.RestOrchestrateConfig;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ResponseMapper {

    //Configuratie gegevens, worden geset de in RestSourceType klasse
    private final RestOrchestrateConfig config;


    //Verwerk execution result, unwrap deze en return een nieuwe Map<String, Object>.
    //0..1 resultaten
    public Mono<Map<String, Object>> processFindOneResult(Mono<ExecutionResult> executionResult) {
        return executionResult.map(result -> Optional.ofNullable((Map<String, Object>) result.getData())
                        .orElse(Map.of()))
                .map(ResponseMapper::unwrapRefs);
    }

    //Verwerk execution result, unwrap deze en return een nieuwe Map<String, Object>.
    //0..N resultaten
    public Flux<Map<String, Object>> processFindResult(Mono<ExecutionResult> executionResult, String objectName) {
        return executionResult.map(e -> getCollectionResult(e, objectName))
                .flatMapMany(Flux::fromIterable)
                .map(ResponseMapper::unwrapRefs);

    }

    //Verwerk execution result, unwrap deze en return een nieuwe Map<String, Object>.
    //0..N resultaten
    public Flux<Map<String, Object>> processBatchResult(Mono<ExecutionResult> executionResult, String objectName) {
        return null;

    }

    //Verkrijg een list met Maps<String, Object> vanuit een executionResult
    private List<Map<String, Object>> getCollectionResult(ExecutionResult executionResult, String objectName) {
        return null;

    }

    //Verkrijg een lijst met Maps<String, Object> vanuit een executionResult
    private List<Map<String, Object>> getBatchResult(ExecutionResult executionResult, String objectName) {
        var data = executionResult.getData();
        return ((Map<String, List<Map<String, Object>>>) data)
                .get(uncapitalize(objectName) + config.getBatchSuffix());
    }

    //Kijk in de input of er values met de key "ref" of "refs" in een map zitten, en verkrijg hiervan de bijhorende value.
    //Return dan alleen alles uit de input met "ref" of "refs" erin.
    private static Map<String, Object> unwrapRefs(Map<String, Object> item) {
        return item.entrySet()
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
                }, HashMap::putAll);
    }
}
