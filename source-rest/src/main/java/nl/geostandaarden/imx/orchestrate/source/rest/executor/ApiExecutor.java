package nl.geostandaarden.imx.orchestrate.source.rest.executor;

import graphql.ExecutionResult;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Map;

public interface ApiExecutor {

    //krijg dict
    //bestuurslaag => gemeente
    //code => GM0014
    //omzetten naar string
    //
    public Mono<ExecutionResult> execute(Map<String, String> input);
}
