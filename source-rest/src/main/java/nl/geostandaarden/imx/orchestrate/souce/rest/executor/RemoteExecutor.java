package nl.geostandaarden.imx.orchestrate.souce.rest.executor;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.ExecutionResultImpl;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.geostandaarden.imx.orchestrate.souce.rest.config.RestOrchestrateConfig;
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

    public static RemoteExecutor create(RestOrchestrateConfig config) {
        return new RemoteExecutor(RestWebClient.create(config));
    }

    @Override
    public Mono<ExecutionResult> execute(ExecutionInput input) {
        var mapTypeRef = new ParameterizedTypeReference<Map<String, Object>>() {};
        var body = Map.of("query", input.getQuery(), "variables", input.getVariables());

        if (log.isDebugEnabled()) {
            log.debug("Sending request: \n\n{}\n", input.getQuery());
        }

        return this.webClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(body))
                .retrieve()
                .bodyToMono(mapTypeRef)
                .map(RemoteExecutor::mapToResult);
    }

    private static ExecutionResult mapToResult(Map<String, Object> body) {
        return ExecutionResultImpl.newExecutionResult()
                .data(body.get(DATA))
                .build();
    }
}
