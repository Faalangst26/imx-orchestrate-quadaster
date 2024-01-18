package nl.geostandaarden.imx.orchestrate.source.rest.mapper;

import java.util.*;
import lombok.RequiredArgsConstructor;
import nl.geostandaarden.imx.orchestrate.source.rest.Result.AbstractResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ResponseMapper {

    public <T extends AbstractResult> Mono<Map<String, Object>> processFindOneResult(Mono<T> executionResult) {
        return executionResult
                .flatMap(result -> {
                    List<Map<String, Object>> dataList = result.getData() != null ? (ArrayList<Map<String, Object>>) result.getData() : new ArrayList<>();
                    return Flux.fromIterable(dataList).next(); // Gebruik .next() om een Mono te maken
                });
    }

    public <T extends AbstractResult> Flux<Map<String, Object>> processFindResult(Mono<T> executionResult) {
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
