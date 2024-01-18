package nl.geostandaarden.imx.orchestrate.source.rest.executor;


import nl.geostandaarden.imx.orchestrate.source.rest.Result.AbstractResult;
import nl.geostandaarden.imx.orchestrate.source.rest.Result.CollectionResult;
import nl.geostandaarden.imx.orchestrate.source.rest.Result.ObjectResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@MockitoSettings(strictness = Strictness.LENIENT)
class RemoteExecutorTest {

    @Mock
    private RemoteExecutor executor;

    @BeforeEach
    public void setup() {
        ClientResponse mockResponse = mock();
        when(mockResponse.statusCode()).thenReturn(HttpStatus.OK);
        when(mockResponse.bodyToMono(Void.class)).thenReturn(Mono.empty());

        RemoteExecutor remoteExecutor = mock();
        executor = remoteExecutor;
    }

    @Test
    void mapToResultReturnsObject() {
        var result = mockBody();

        RemoteExecutor.requestType = new ObjectResult(null);
        var mappedResult = RemoteExecutor.mapToResult(result);

        AbstractResult objectResult = new ObjectResult(null);
        var resultMap = new ArrayList<>();
        resultMap.add(Map.of("testKey","testValue"));
        objectResult.data = resultMap;

        assertInstanceOf(ObjectResult.class, mappedResult);
        assertThat(mappedResult.data).isEqualTo(objectResult.data);
    }

    @Test
    void mapToResultReturnsCollection() {
        var result = mockBody();

        RemoteExecutor.requestType = new CollectionResult(null);
        var mappedResult = RemoteExecutor.mapToResult(result);

        AbstractResult collectionResult = new CollectionResult(null);
        var resultMap = new ArrayList<>();
        resultMap.add(Map.of("testKey","testValue"));
        collectionResult.data = resultMap;

        assertInstanceOf(CollectionResult.class, mappedResult);
        assertThat(mappedResult.data).isEqualTo(collectionResult.data);
    }



    LinkedHashMap<String, Object> mockBody(){
        var embedded = new LinkedHashMap<String, Object>();
        var bestuurlijkeGebieden = new LinkedHashMap<String, Object>();
        var testList = new ArrayList<>();
        var testItem = new LinkedHashMap<String, Object>();
        testItem.put("testKey","testValue");
        testList.add(testItem);
        bestuurlijkeGebieden.put("bestuurlijkeGebieden", testList);
        embedded.put("_embedded", bestuurlijkeGebieden);
        return embedded;
    }

}
