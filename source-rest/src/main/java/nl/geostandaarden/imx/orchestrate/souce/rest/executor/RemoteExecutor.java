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

    //Maak een nieuwe RemoteExecutor, en een nieuwe configuratie in de RestWebClient
    public static RemoteExecutor create(RestOrchestrateConfig config) {
        return new RemoteExecutor(RestWebClient.create(config));
    }

    //Execute een input, return deze als ExecutionResult.
    @Override
    public Mono<ExecutionResult> execute(ExecutionInputRest input) {
        //ParameterizedTypeReference zorgt ervoor dat de Generic Types bewaard kunnen blijven
        var mapTypeRef = new ParameterizedTypeReference<Map<String, Object>>() {};
        var body = Map.of("query", input.getQuery(), "variables", input.getVariables());

        //Logging
        if (log.isDebugEnabled()) {
            log.debug("Sending request: \n\n{}\n", input.getQuery());
        }


        //Doe een post request via de webClient klasse. Deze webClient wordt aangemaakt in de constructor met de config
        //Map de results via de mapToResult() functie
        //Return een ExecutionResult
        return this.webClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(body))
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
