package nl.geostandaarden.imx.orchestrate.source.rest.executor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.rpc.Help;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.geostandaarden.imx.orchestrate.engine.exchange.AbstractDataRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.CollectionRequest;
import nl.geostandaarden.imx.orchestrate.source.rest.Result.AbstractResult;
import nl.geostandaarden.imx.orchestrate.source.rest.Result.BatchResult;
import nl.geostandaarden.imx.orchestrate.source.rest.Result.CollectionResult;
import nl.geostandaarden.imx.orchestrate.source.rest.Result.ObjectResult;
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

    private static AbstractResult requesType;

    //Maak een nieuwe RemoteExecutor, en een nieuwe configuratie in de RestWebClient
    public static RemoteExecutor create(RestOrchestrateConfig config) {
        return new RemoteExecutor(RestWebClient.create(config));
    }

    @Override
    public Mono<AbstractResult> execute(Map<String, Object> input, AbstractDataRequest objectRequest) {
    requesType=getRequestType(objectRequest);
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

    private static AbstractResult getRequestType(AbstractDataRequest objectRequest) {
        if (objectRequest == null) {
            return null;
        }

        switch (objectRequest.getClass().getName()) {
            case "nl.geostandaarden.imx.orchestrate.engine.exchange.CollectionRequest":
                return new CollectionResult(null);

            case "nl.geostandaarden.imx.orchestrate.engine.exchange.ObjectRequest":
                return new ObjectResult(null);

            case "nl.geostandaarden.imx.orchestrate.engine.exchange.BatchRequest":
                return new BatchResult(null);


            default:
                return null;
        }
    }

    private static String createUri(Map<String, Object> input) {
        if (input.isEmpty())
            return "";
        return "";
        //Haal het eerste item uit de map, je kunt immers maar 1 ding in het URI plakken om op te zoeken
//        Map.Entry<String, Object> entry = input.entrySet().iterator().next();
//        String key = entry.getKey();
//        var value = entry.getValue();
//        System.out.println("/" + value.toString());
//        return ("/" + value.toString());
    }

//ArrayList<LinkedHashMap<String, Object>>
    private static AbstractResult mapToResult(Map<String, Object> body) {
        AbstractResult result;
        ArrayList<LinkedHashMap<String, Object>> resultlist = new ArrayList<>();
        if (requesType instanceof CollectionResult) {
            result = new CollectionResult(null);
        } else if (requesType instanceof BatchResult) {
            result = new BatchResult(null);
        } else if (requesType instanceof ObjectResult) {
            result = new ObjectResult(null);
        } else {
            result = null;
        }
        if (body.containsKey("_embedded")) {
            Object embeddedObject = body.get("_embedded");

            if (embeddedObject instanceof LinkedHashMap) {
                LinkedHashMap<String, Object> embeddedMap = (LinkedHashMap<String, Object>) embeddedObject;

                if (embeddedMap.containsKey("bestuurlijkeGebieden") || embeddedMap.containsKey("openbareLichamen")) {
                    String keyToCheck = embeddedMap.containsKey("bestuurlijkeGebieden") ? "bestuurlijkeGebieden" : "openbareLichamen";
                    Object bodyListObject = embeddedMap.get(keyToCheck);

                    if (bodyListObject instanceof ArrayList) {
                        ArrayList<?> bodyList = (ArrayList<?>) bodyListObject;

                        // Assuming each element in the body list is a linked hash map
                        for (Object item : bodyList) {
                            if (item instanceof LinkedHashMap) {
                                resultlist.add((LinkedHashMap<String, Object>) item);


                            }
                        }
                    }
                }
            }
        }

        result.data = resultlist;
        return result;
    }
}



