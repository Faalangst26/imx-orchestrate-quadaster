package nl.geostandaarden.imx.orchestrate.source.rest.executor;

import graphql.ExecutionResult;
import graphql.ExecutionResultImpl;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.geostandaarden.imx.orchestrate.source.rest.config.RestOrchestrateConfig;
import nl.geostandaarden.imx.orchestrate.source.rest.mapper.ResponseMapper;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class RemoteExecutor implements ApiExecutor{
    private static final String DATA = "data";

    private final WebClient webClient;

    //Maak een nieuwe RemoteExecutor, en een nieuwe configuratie in de RestWebClient
    public static RemoteExecutor create(RestOrchestrateConfig config) {
        return new RemoteExecutor(RestWebClient.create(config));
    }

    @Override
    public Mono<ExecutionResult> execute(Map<String, String> input) {

        var body = input;
        var mapTypeRef = new ParameterizedTypeReference<Map<String, Object>>() {};

        return this.webClient.get()

                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(mapTypeRef)
                .map(RemoteExecutor::mapToResult);


//        return this.webClient.get()
//                .contentType(MediaType.APPLICATION_JSON)
//                .accept(MediaType.APPLICATION_JSON)
//                .body(BodyInserters.fromValue(body))
//                .retrieve()
//                .bodyToMono(mapTypeRef)
//                .map(RemoteExecutor::mapToResult);

    }

    private static ExecutionResult mapToResult(Map<String, Object> body) {
        return ExecutionResultImpl.newExecutionResult()
                .data(body.get(DATA))
                .build();
    }


}
