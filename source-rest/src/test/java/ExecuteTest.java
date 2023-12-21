
import nl.geostandaarden.imx.orchestrate.source.rest.executor.RemoteExecutor;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.Map;

public class ExecuteTest {
    public ExecuteTest() {
    }

//    @Test
//    public void testExecute() {
//        RemoteExecutor remoteExecutor = new RemoteExecutor(WebClient.builder().build());
//        Map<String, String> inputMap = new HashMap();
//        inputMap.put("code", "GM0014");
//        inputMap.put("bestuurslaag", "gemeente");
//
//        HashMap<String, Object> expected = new HashMap<>();
//        expected.put("gemeente", "GM0014");
//
//        Mono<Map<String, Object>> result = remoteExecutor.execute(inputMap);
//
//
//        StepVerifier.create(result)
//                .expectNext(expected)
//                .expectComplete()
//                .verify();
//    }
}

