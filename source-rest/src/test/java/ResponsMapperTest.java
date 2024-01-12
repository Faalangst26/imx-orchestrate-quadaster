import nl.geostandaarden.imx.orchestrate.source.rest.config.RestOrchestrateConfig;
import org.junit.jupiter.api.Test;
import nl.geostandaarden.imx.orchestrate.source.rest.mapper.ResponseMapper;
import nl.geostandaarden.imx.orchestrate.source.rest.Result.AbstractResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ResponsMapperTest {
    RestOrchestrateConfig mockConfig = mock(RestOrchestrateConfig.class);
    ResponseMapper responseMapper = new ResponseMapper(mockConfig);

    public ResponsMapperTest(){
    }

    @Test
    void testProcessFindResult() {
        AbstractResult mockResult = mock(AbstractResult.class);
        Mono<AbstractResult> mockExecutionResult = Mono.just(mockResult);
        String objectName = "yourObjectName";
        Flux<Map<String, Object>> resultFlux = responseMapper.processFindResult(mockExecutionResult, objectName);

        ((Flux<?>) resultFlux)
                .collectList()
                .block();
    }

}
