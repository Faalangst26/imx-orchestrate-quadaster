package nl.geostandaarden.imx.orchestrate.source.rest.executor;

import reactor.core.publisher.Mono;

import java.util.Map;

public interface ApiExecutor {

    //krijg dict
    //bestuurslaag => gemeente
    //code => GM0014
    //omzetten naar string
    //
<<<<<<< Updated upstream
    public Mono<Map<String, Object>> execute(Map<String, Object> input);
=======
    public Mono<Map<String, Object>> execute(Map<String, String> input);
>>>>>>> Stashed changes
}
