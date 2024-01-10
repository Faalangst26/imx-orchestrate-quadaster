package nl.geostandaarden.imx.orchestrate.source.rest.executor;

import nl.geostandaarden.imx.orchestrate.engine.exchange.AbstractDataRequest;
import nl.geostandaarden.imx.orchestrate.source.rest.Result.AbstractResult;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface ApiExecutor {

    //krijg dict
    //bestuurslaag => gemeente
    //code => GM0014
    //omzetten naar string
    public Mono<AbstractResult> execute(Map<String, Object> input, AbstractDataRequest objectRequest);
}
