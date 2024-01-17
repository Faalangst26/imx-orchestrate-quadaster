package nl.geostandaarden.imx.orchestrate.source.rest.mapper;

import java.util.*;
import lombok.RequiredArgsConstructor;
import nl.geostandaarden.imx.orchestrate.source.rest.Result.AbstractResult;
import nl.geostandaarden.imx.orchestrate.source.rest.config.RestOrchestrateConfig;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ResponseMapper {

    private final RestOrchestrateConfig config;

    public Mono<Map<String, Object>> processFindOneResult(Mono<AbstractResult> executionResult) {
        return executionResult
                .flatMap(result -> {
                    List<Map<String, Object>> dataList = result != null ? (ArrayList<Map<String, Object>>) result.getData() : new ArrayList<>();
                    return Flux.fromIterable(dataList).next(); // Gebruik .next() om een Mono te maken
                });
    }

    public Flux<Map<String, Object>> processFindResult(Mono<AbstractResult> executionResult, String objectName) {
        return executionResult
                .flatMapMany(result -> {
                    List<Map<String, Object>> dataList = result != null ? (ArrayList<Map<String, Object>>) result.getData() : new ArrayList<>();
                    return Flux.fromIterable(dataList);
                });

    }

    //TODO: Batch compatibility toevoegen?
    public Flux<Map<String, Object>> processBatchResult(Mono<AbstractResult> executionResult, String objectName) {
        return executionResult
                .flatMapMany(result -> {
                    List<Map<String, Object>> dataList = result != null ? (ArrayList<Map<String, Object>>) result.getData() : new ArrayList<>();
                    return Flux.fromIterable(dataList);
                });

    }

}
