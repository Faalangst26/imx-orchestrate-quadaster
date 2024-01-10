package nl.geostandaarden.imx.orchestrate.source.rest.executor;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.geostandaarden.imx.orchestrate.engine.exchange.AbstractDataRequest;
import nl.geostandaarden.imx.orchestrate.source.rest.Result.AbstractResult;
import nl.geostandaarden.imx.orchestrate.source.rest.config.RestOrchestrateConfig;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;


@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public class RemoteExecutor implements ApiExecutor {
    private static final String DATA = "data";

    private final WebClient webClient;

    private static AbstractDataRequest objectRequest;

    //Maak een nieuwe RemoteExecutor, en een nieuwe configuratie in de RestWebClient
    public static RemoteExecutor create(RestOrchestrateConfig config) {
        return new RemoteExecutor(RestWebClient.create(config));
    }

    @Override
    public Mono<AbstractResult> execute(Map<String, Object> input, AbstractDataRequest objectRequest) {
        var mapTypeRef = new ParameterizedTypeReference<Map<String, Object>>() {
        };

        this.objectRequest = objectRequest;

        return this.webClient.get()
                .uri(createUri(input))
                .accept(MediaType.valueOf("application/hal+json"))
                .retrieve()
                .bodyToMono(mapTypeRef)
                .map(RemoteExecutor::mapToResult);

    }

    private static String createUri(Map<String, Object> input) {
        if(input.isEmpty())
            return "";
        return "";
        //Haal het eerste item uit de map, je kunt immers maar 1 ding in het URI plakken om op te zoeken
//        Map.Entry<String, Object> entry = input.entrySet().iterator().next();
//        String key = entry.getKey();
//        var value = entry.getValue();
//        System.out.println("/" + value.toString());
//        return ("/" + value.toString());
    }


    private static AbstractResult mapToResult(Map<String, Object> body) {
        return null;
    }
}



