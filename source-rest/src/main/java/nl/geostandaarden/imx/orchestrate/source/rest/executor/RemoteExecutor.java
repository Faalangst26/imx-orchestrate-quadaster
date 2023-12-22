package nl.geostandaarden.imx.orchestrate.source.rest.executor;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.geostandaarden.imx.orchestrate.engine.exchange.AbstractDataRequest;
import nl.geostandaarden.imx.orchestrate.source.rest.config.RestOrchestrateConfig;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public class RemoteExecutor implements ApiExecutor{
    private static final String DATA = "data";

    private final WebClient webClient;

    private static AbstractDataRequest objectRequest;

    //Maak een nieuwe RemoteExecutor, en een nieuwe configuratie in de RestWebClient
    public static RemoteExecutor create(RestOrchestrateConfig config) {
        return new RemoteExecutor(RestWebClient.create(config));
    }

    @Override
    public Mono<Map<String, Object>> execute(Map<String, Object> input, AbstractDataRequest objectRequest) {
        var mapTypeRef = new ParameterizedTypeReference<Map<String, Object>>() {};



        this.objectRequest = objectRequest;

        return this.webClient.get()
                .accept(MediaType.valueOf("application/hal+json"))
                .retrieve()
                .bodyToMono(mapTypeRef)
                .map(RemoteExecutor::mapToResult);

    }

    private static Map<String, Object> mapToResult(Map<String, Object> body) {
        Map<String, Object> output = new HashMap<String, Object>();
        for (var item : body.values() ){
            var values = item.toString();
            for (var prop : objectRequest.getSelectedProperties()){


                int foundIndex = values.indexOf(prop.toString() + "=");
                String val = values.substring(foundIndex + prop.toString().length() + 1, values.indexOf(',', foundIndex));

                log.debug("Looking for property: " + prop + "  value: " + val + "\n");

                output.put(prop.toString() , val);
            }

            log.debug(item + "\n");
            break;
        }
        //TODO: AAHHHHHHHHHH
        output.put("id", "A0001");
        for (var outputitem : output.keySet()){
            log.debug("output map: " + outputitem + ":" + output.get(outputitem));
        }

        return output;


    }
}
