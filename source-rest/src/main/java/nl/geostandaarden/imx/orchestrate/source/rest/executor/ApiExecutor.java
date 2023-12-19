package nl.geostandaarden.imx.orchestrate.source.rest.executor;

import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Map;

public interface ApiExecutor {

    //krijg dict
    //bestuurslaag => gemeente
    //code => GM0014
    //omzetten naar string
    //
    public Mono<String> execute(Map<String, String> input);
}
