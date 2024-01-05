package nl.geostandaarden.imx.orchestrate.source.rest.mapper;

import java.util.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.geostandaarden.imx.orchestrate.source.rest.config.RestOrchestrateConfig;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Slf4j
public class ResponseMapper {

    //Configuratie gegevens, worden geset de in RestSourceType klasse
    private final RestOrchestrateConfig config;


    //Verwerk execution result, unwrap deze en return een nieuwe Map<String, Object>.
    //0..1 resultaten
    public Mono<Map<String, Object>> processFindOneResult(Mono<Map<String, Object>> executionResult) {
//        var uitgepakt = executionResult;
//        uitgepakt.subscribe(item -> log.debug(item.values().toString()), error -> {
//
//        });

        return executionResult.map(result -> Optional.ofNullable((Map<String, Object>) result)
                        .orElse(Map.of()))
                .map(ResponseMapper::unwrapRefs);
    }

    //Verwerk execution result, unwrap deze en return een nieuwe Map<String, Object>.
    //0..N resultaten
    public Flux<Map<String, Object>> processFindResult(Mono<Map<String, Object>> executionResult, String objectName) {
        return executionResult.map(e -> getCollectionResult(e, objectName))
                .flatMapMany(Flux::fromIterable)
                .map(ResponseMapper::unwrapRefs);

    }

    //Verwerk execution result, unwrap deze en return een nieuwe Map<String, Object>.
    //0..N resultaten
    public Flux<Map<String, Object>> processBatchResult(Mono<Map<String, Object>> executionResult, String objectName) {
        return null;

    }

    //Verkrijg een list met Maps<String, Object> vanuit een executionResult
    private List<Map<String, Object>> getCollectionResult(Map<String, Object> executionResult, String objectName) {
        List<Map<String, Object>> collectionResult = new ArrayList<>();

        // Assuming the executionResult contains a key corresponding to the objectName
        // Adjust this logic based on the actual structure of your executionResult
        Object objectData = executionResult.get(objectName);

        // Check if objectData is a List of Maps
        if (objectData instanceof List && !((List<?>) objectData).isEmpty() && ((List<?>) objectData).get(0) instanceof Map) {
            collectionResult = (List<Map<String, Object>>) objectData;
        }

        return collectionResult;

    }

    //Verkrijg een lijst met Maps<String, Object> vanuit een executionResult
    private List<Map<String, Object>> getBatchResult(Map<String, Object> executionResult, String objectName) {
        var data = executionResult;
        return null;
//        return ((Map<String, List<Map<String, Object>>>) data)
//                .get(uncapitalize(objectName) + config.getBatchSuffix());
    }

    //Kijk in de input of er values met de key "ref" of "refs" in een map zitten, en verkrijg hiervan de bijhorende value.
    //Return dan alleen alles uit de input met "ref" of "refs" erin.
    private static Map<String, Object> unwrapRefs(Map<String, Object> item) {
        return item;

//        return item.entrySet()
//                .stream()
//                .collect(HashMap::new, (acc, e) -> {
//                    var value = e.getValue();
//
//                    if (value instanceof Map<?, ?> mapValue) {
//                        if (mapValue.containsKey("ref")) {
//                            value = mapValue.get("ref");
//                        }
//
//                        if (mapValue.containsKey("refs")) {
//                            value = mapValue.get("refs");
//                        }
//                    }
//
//                    acc.put(e.getKey(), value);
//                }, HashMap::putAll);
   }
}
