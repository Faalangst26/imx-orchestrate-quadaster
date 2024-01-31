package nl.geostandaarden.imx.orchestrate.source.rest.executor;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import nl.geostandaarden.imx.orchestrate.engine.exchange.AbstractDataRequest;
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

@SuppressWarnings("SpellCheckingInspection")
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public class RemoteExecutor implements ApiExecutor {

    private final WebClient webClient;

    static AbstractResult requestType;

    public static RemoteExecutor create(RestOrchestrateConfig config) {
        return new RemoteExecutor(RestWebClient.create(config));
    }

    @Override
    public Mono<AbstractResult> execute(Map<String, Object> requestedData, AbstractDataRequest objectRequest) {
    requestType = getRequestType(objectRequest);
        var mapTypeRef = new ParameterizedTypeReference<Map<String, Object>>() {};

        return this.webClient.get()
                .uri(createUri(requestedData))
                .accept(MediaType.valueOf("application/hal+json"))
                .retrieve()
                .bodyToMono(mapTypeRef)
                .map(RemoteExecutor::mapToResult);

    }

     static AbstractResult getRequestType(AbstractDataRequest objectRequest) {
        if (objectRequest == null) {
            return null;
        }

        return switch (objectRequest.getClass().getName()) {
            case "nl.geostandaarden.imx.orchestrate.engine.exchange.CollectionRequest" -> new CollectionResult(null);
            case "nl.geostandaarden.imx.orchestrate.engine.exchange.ObjectRequest" -> new ObjectResult(null);
            case "nl.geostandaarden.imx.orchestrate.engine.exchange.BatchRequest" -> new BatchResult(null);
            default -> null;
        };
    }

    static String createUri(Map<String, Object> requestedData) {
        if (requestedData == null)
            return "";

        if (requestedData.size() != 1)
            return "";

        //Haal het eerste item uit de map, je kunt immers maar 1 ding in het URI plakken om op te zoeken
        Map.Entry<String, Object> entry = requestedData.entrySet().iterator().next();
        var key = requestedData.keySet().iterator().next();
        var value = entry.getValue();


        if(key.equals("identificatie")){
            value = "?openbaarLichaam=" + value;
        } else {
            value = "/" + value;
        }
        return (value.toString());
    }

    static AbstractResult mapToResult(Map<String, Object> body) {
        if(isValidInput(body)) {
            AbstractResult result;

            if (isSingleObjectResult(body)) {
                return createSingleObjectResult((LinkedHashMap<String, Object>)body);
            }
            ArrayList<LinkedHashMap<String, Object>> multipleObjectResults = createMultipleObjectResults(body);
            result = determineResultType();

            if (result != null) {
                result.data = multipleObjectResults;
                return result;
            }
        }
        return null;
    }
    static AbstractResult createSingleObjectResult(LinkedHashMap<String, Object> body) {
        ArrayList<LinkedHashMap<String, Object>> resultlist = new ArrayList<>();
        resultlist.add(body);
        AbstractResult result = new ObjectResult(null);
        result.data = resultlist;

        return result;
    }
    static ArrayList<LinkedHashMap<String, Object>> createMultipleObjectResults(Map<String, Object> body) {
        ArrayList<LinkedHashMap<String, Object>> resultlist = new ArrayList<>();
        Object embeddedObject = body.get("_embedded");
        if (embeddedObject instanceof LinkedHashMap) {
            LinkedHashMap<String, Object> embeddedMap = (LinkedHashMap<String, Object>) embeddedObject;

            Iterator<String> iterator = embeddedMap.keySet().iterator();
            Object bodyListObject = embeddedMap.get(iterator.hasNext() ? iterator.next() : "defaultKey");

            if (bodyListObject instanceof ArrayList) {
                ArrayList<?> bodyList = (ArrayList<?>) bodyListObject;

                for (Object item : bodyList) {
                    if (item instanceof LinkedHashMap) {
                        resultlist.add((LinkedHashMap<String, Object>) item);
                    }
                }
            }
        }
        return resultlist;
    }
    static boolean isValidInput(Map<String, Object> body)
    {
        return body.containsKey("_embedded");
    }

    static AbstractResult determineResultType() {
        if (requestType instanceof CollectionResult) {
            return new CollectionResult(null);
        } else if (requestType instanceof BatchResult) {
            return new BatchResult(null);
        } else if (requestType instanceof ObjectResult) {
            return new ObjectResult(null);
        }
        return null;
    }

    static boolean isSingleObjectResult(Map<String, Object> body) {
        //Als de return body meer als 2 items heeft (meer als _embedded en _links) dan is het een enkel resultaat uit een zoek query
        return body.size() > 2;
    }
}



