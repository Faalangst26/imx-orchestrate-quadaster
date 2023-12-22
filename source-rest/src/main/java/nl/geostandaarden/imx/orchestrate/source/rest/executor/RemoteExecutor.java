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
                .accept(MediaType.valueOf("application/hal+json"))
                .retrieve()
                .bodyToMono(mapTypeRef)
                .map(RemoteExecutor::mapToResult);

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


//    Map<String, Object> output = new HashMap<String, Object>();
//        for (var item : body.values() ){
//                var values = item.toString();
//                var luc = objectRequest.getSelectedProperties();
//                for (var prop : objectRequest.getSelectedProperties()){
//
//
//                int foundIndex = values.indexOf(prop.toString() + "=");
//                String val = values.substring(foundIndex + prop.toString().length() + 1, values.indexOf(',', foundIndex));
//
//                log.debug("Looking for property: " + prop + "  value: " + val + "\n");
//
//                output.put(prop.toString() , val);
//                }
//
//                log.debug(item + "\n");
//                break;
//                }
//                //TODO: AAHHHHHHHHHH
//                output.put("id", "A0001");
//                for (var outputitem : output.keySet()){
//                log.debug("output map: " + outputitem + ":" + output.get(outputitem));
//                }
//
//                return output;

