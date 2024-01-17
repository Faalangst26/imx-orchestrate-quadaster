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

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public class RemoteExecutor implements ApiExecutor {

    private final WebClient webClient;

    private static AbstractResult requestType;

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

    private static AbstractResult getRequestType(AbstractDataRequest objectRequest) {
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

    private static String createUri(Map<String, Object> requestedData) {
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

    private static AbstractResult mapToResult(Map<String, Object> body) {
        AbstractResult result;
        ArrayList<LinkedHashMap<String, Object>> resultlist = new ArrayList<>();
        //Als de return body meer als 2 items heeft (meer als _embedded  en _links) dan is het een enkel resultaat uit een zoek query
        if(body.size() > 2 ){
            resultlist.add((LinkedHashMap<String, Object>) body);
            result = new ObjectResult(null);
            result.data = resultlist;
            return result;
        }

        if (requestType instanceof CollectionResult) {
            result = new CollectionResult(null);
        } else if (requestType instanceof BatchResult) {
            result = new BatchResult(null);
        } else if (requestType instanceof ObjectResult) {
            result = new ObjectResult(null);
        } else {
            result = null;
        }
        if (body.containsKey("_embedded")) {
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
        }

        result.data = resultlist;
        return result;
    }
}



