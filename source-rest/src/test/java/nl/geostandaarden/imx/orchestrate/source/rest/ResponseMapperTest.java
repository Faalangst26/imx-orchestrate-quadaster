package nl.geostandaarden.imx.orchestrate.source.rest;

import nl.geostandaarden.imx.orchestrate.source.rest.Result.AbstractResult;
import nl.geostandaarden.imx.orchestrate.source.rest.Result.CollectionResult;
import nl.geostandaarden.imx.orchestrate.source.rest.Result.ObjectResult;
import nl.geostandaarden.imx.orchestrate.source.rest.mapper.ResponseMapper;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class ResponseMapperTest {

    @Test
    void processFindOneResult_returnsMono() {
        AbstractResult objectResult = new ObjectResult(null);
        var resultMap = new ArrayList<>();
        resultMap.add(Map.of("testKey","testValue"));
        objectResult.data = resultMap;
        var executionResult = Mono.just(objectResult);
        var result = new ResponseMapper().processFindOneResult(executionResult);

        assertThat(result.block()).isEqualTo(Map.of("testKey", "testValue"));
    }

    @Test
    void processFindOneResult_returnsMono_forNull() {
        AbstractResult objectResult = new ObjectResult(null);
        var executionResult = Mono.just(objectResult);
        var result = new ResponseMapper().processFindOneResult(executionResult);

        assertThat(result.block()).isEqualTo(null);
    }

    @Test
    void processFindResult_returnsFlux() {
        AbstractResult collectionResult = new CollectionResult(null);
        var resultMap = new ArrayList<>();
        resultMap.add(Map.of("testKey","testValue"));
        collectionResult.data = resultMap;

        var executionResult = Mono.just(collectionResult);

        var result = new ResponseMapper().processFindResult(executionResult);

        assertThat(result.blockFirst()).isEqualTo(Map.of("testKey", "testValue"));
    }

}
