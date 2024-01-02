package nl.geostandaarden.imx.orchestrate.source.rest.executor;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.geostandaarden.imx.orchestrate.engine.exchange.AbstractDataRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.SelectedProperty;
import nl.geostandaarden.imx.orchestrate.source.rest.config.RestOrchestrateConfig;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
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
                .uri(createUri(input))
                .accept(MediaType.valueOf("application/hal+json"))
                .retrieve()
                .bodyToMono(mapTypeRef)
                .map(RemoteExecutor::mapToResult);

    }
    private static String createUri(Map<String, Object> input) {
        if(input.isEmpty())
            return "";

        //Haal het eerste item uit de map, je kunt immers maar 1 ding in het URI plakken om op te zoeken
        Map.Entry<String, Object> entry = input.entrySet().iterator().next();
        String key = entry.getKey();
        var value = entry.getValue();
        return ("/" + value.toString());
    }
    private static Map<String, Object> mapToResult(Map<String, Object> body) {
        Map<String, Object> resultMap = new HashMap<>();
        var berne = body.toString();
        for (SelectedProperty key : objectRequest.getSelectedProperties()) {
            if (containsKeyRecursive(body, key.toString())) {
                resultMap.put(key.toString(), getValueRecursive(body, key.toString()));
            }
        }
        var emir =resultMap;
        return resultMap;
    }
    private static boolean containsKeyRecursive(Map<String, Object> map, String key) {
        if (map.containsKey(key)) {
            return true;
        }

        for (Object value : map.values()) {
            if (value instanceof Map && containsKeyRecursive((Map<String, Object>) value, key)) {
                return true;
            } else if (value instanceof List) {
                for (Object listItem : (List<?>) value) {
                    if (listItem instanceof Map && containsKeyRecursive((Map<String, Object>) listItem, key)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private static Object getValueRecursive(Map<String, Object> map, String key) {
        if (map.containsKey(key)) {
            return map.get(key);
        }

        for (Object value : map.values()) {
            if (value instanceof Map) {
                Object result = getValueRecursive((Map<String, Object>) value, key);
                if (result != null) {
                    return result;
                }
            } else if (value instanceof List) {
                for (Object listItem : (List<?>) value) {
                    if (listItem instanceof Map) {
                        Object result = getValueRecursive((Map<String, Object>) listItem, key);
                        if (result != null) {
                            return result;
                        }
                    }
                }
            }
        }

        return null;
    }
}



