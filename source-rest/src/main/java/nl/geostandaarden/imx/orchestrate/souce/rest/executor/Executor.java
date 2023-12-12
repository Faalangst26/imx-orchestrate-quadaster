package nl.geostandaarden.imx.orchestrate.souce.rest.executor;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import reactor.core.publisher.Mono;

public interface Executor {
    Mono<ExecutionResult> execute(ExecutionInput input);
}
