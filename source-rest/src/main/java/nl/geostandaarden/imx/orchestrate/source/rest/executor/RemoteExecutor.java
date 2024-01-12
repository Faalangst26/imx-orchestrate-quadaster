package nl.geostandaarden.imx.orchestrate.source.rest.executor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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


    private static AbstractResult mapToResult(Map<String, Object> body) {
        if (body.containsKey("_embedded")) {
            Object embeddedObject = body.get("_embedded");

            // Assuming the embeddedObject is a LinkedHashMap
            if (embeddedObject instanceof LinkedHashMap) {
                LinkedHashMap<?, ?> embeddedMap = (LinkedHashMap<?, ?>) embeddedObject;

                var data = new ArrayList<LinkedHashMap<String, Object>>();

                // Iterate over the entries of the embeddedMap
                for (Map.Entry<?, ?> entry : embeddedMap.entrySet()) {
                    // Assuming each entry value is a List
                    if (entry.getValue() instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> itemList = (List<Map<String, Object>>) entry.getValue();

                        // Convert each item in the list to LinkedHashMap<String, ObjectNode>
                        for (Map<String, Object> itemMap : itemList) {
                            LinkedHashMap<String, Object> itemData = convertToItemData(itemMap);
                            data.add(itemData);
                        }
                    }
                }
                // Create CollectionResult with the populated data
                AbstractResult result = new CollectionResult(data);
                return result;
            }
        }

        return null;
    }

    private static LinkedHashMap<String, Object> convertToItemData(Map<String, Object> itemMap) {
        LinkedHashMap<String, Object> itemData = new LinkedHashMap<>();

        // Use Jackson's ObjectMapper to create ObjectNode
        ObjectMapper objectMapper = new ObjectMapper();

        // Iterate over the entries of the itemMap
        for (Map.Entry<String, Object> entry : itemMap.entrySet()) {
            // Convert each entry value to ObjectNode
            JsonNode jsonNode = objectMapper.valueToTree(entry.getValue());
            if (jsonNode instanceof Object) {
                itemData.put(entry.getKey(), (Object) jsonNode);
            }
        }

        return itemData;
    }
}



