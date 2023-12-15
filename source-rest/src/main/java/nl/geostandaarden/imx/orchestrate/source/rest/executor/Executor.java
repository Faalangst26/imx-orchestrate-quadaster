package nl.geostandaarden.imx.orchestrate.source.rest.executor;

import graphql.ExecutionResult;
import reactor.core.publisher.Mono;

public interface Executor {
    Mono<ExecutionResult> execute(ExecutionInputRest input);
}
