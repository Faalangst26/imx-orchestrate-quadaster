package nl.geostandaarden.imx.orchestrate.source.rest.mapper;

import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.geostandaarden.imx.orchestrate.source.rest.Result.BatchResult;
import nl.geostandaarden.imx.orchestrate.source.rest.Result.CollectionResult;
import nl.geostandaarden.imx.orchestrate.source.rest.Result.ObjectResult;
import nl.geostandaarden.imx.orchestrate.source.rest.config.RestOrchestrateConfig;
import nl.geostandaarden.imx.orchestrate.source.rest.Result.AbstractResult;
import org.reactivestreams.Publisher;
import org.springframework.beans.BeanUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Slf4j
public class ResponseMapper {

    //Configuratie gegevens, worden geset de in RestSourceType klasse
    private final RestOrchestrateConfig config;
    private final ObjectMapper objectMapper = new ObjectMapper();

    //Verwerk execution result, unwrap deze en return een nieuwe Map<String, Object>.
    //0..1 resultaten
    public Mono<Map<String, Object>> processFindOneResult(Mono<AbstractResult> executionResult) {
        return executionResult
                .flatMap(result -> {
                    List<Map<String, Object>> dataList = result != null ? (ArrayList<Map<String, Object>>) result.getData() : new ArrayList<>();
                    return Flux.fromIterable(dataList).next(); // Gebruik .next() om een Mono te maken
                });
    }
    private Iterable<AbstractResult> getBatchItems(AbstractResult result, String objectName) {
        return Collections.emptyList();
    }
    private Iterable<AbstractResult> getItems(AbstractResult result, String objectName) {
        return Collections.emptyList();
    }
    //Verwerk execution result, unwrap deze en return een nieuwe Map<String, Object>.
    //0..N resultaten
    public Flux<Map<String, Object>> processFindResult(Mono<AbstractResult> executionResult, String objectName) {
        return executionResult
                .flatMapMany(result -> {
                    List<Map<String, Object>> dataList = result != null ? (ArrayList<Map<String, Object>>) result.getData() : new ArrayList<>();
                    return Flux.fromIterable(dataList);
                });

    }
    private Map<String, Object> convertToMap(ObjectNode objectNode) {
        return objectMapper.convertValue(objectNode, Map.class);
    }


    //Verwerk execution result, unwrap deze en return een nieuwe Map<String, Object>.
    //0..N resultaten
    public Flux<Map<String, Object>> processBatchResult(Mono<AbstractResult> executionResult, String objectName) {
        return executionResult
                .flatMapMany(result -> {
                    List<Map<String, Object>> dataList = result != null ? (ArrayList<Map<String, Object>>) result.getData() : new ArrayList<>();
                    return Flux.fromIterable(dataList);
                });

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

    private static Map<String, Object> unwrapColl(Map<String, Object> item){
        return null;
    }
}
