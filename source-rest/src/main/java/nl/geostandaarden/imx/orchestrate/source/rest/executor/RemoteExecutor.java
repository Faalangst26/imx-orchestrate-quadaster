package nl.geostandaarden.imx.orchestrate.source.rest.executor;

import graphql.ExecutionResult;
import graphql.ExecutionResultImpl;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.geostandaarden.imx.orchestrate.source.rest.config.RestOrchestrateConfig;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class RemoteExecutor implements Executor{
    private static final String DATA = "data";

    private final WebClient webClient;

    //Maak een nieuwe RemoteExecutor, en een nieuwe configuratie in de RestWebClient
    public static RemoteExecutor create(RestOrchestrateConfig config) {
        return new RemoteExecutor(RestWebClient.create(config));
    }

    //Execute een input, return deze als ExecutionResult.
    @Override
    public Mono<ExecutionResult> execute(ExecutionInputRest input) {
        // ParameterizedTypeReference keeps the Generic Types
        var mapTypeRef = new ParameterizedTypeReference<Map<String, Object>>() {};
        var body = Map.of("query", input.getQuery(), "variables", input.getVariables());

        // Logging
        if (log.isDebugEnabled()) {
            log.debug("Sending request: \n\n{}\n", input.getQuery());
        }

        // Perform a GET request via the WebClient class, which was created in the constructor with the config
        // Map the results via the mapToResult() function
        // Return an ExecutionResult
        return this.webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("query", input.getQuery()) // replace with actual query parameters
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(mapTypeRef)
                .map(RemoteExecutor::mapToResult);
    }

    //doe iets
    private static ExecutionResult mapToResult(Map<String, Object> body) {
        return ExecutionResultImpl.newExecutionResult()
                .data(body.get(DATA))
                .build();
    }
}
