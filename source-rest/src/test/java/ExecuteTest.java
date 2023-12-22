
import nl.geostandaarden.imx.orchestrate.source.rest.executor.RemoteExecutor;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ExecuteTest {
    public ExecuteTest() {
    }

    @Test
    public void testMapResult() {
        RemoteExecutor remoteExecutor = new RemoteExecutor(WebClient.builder().build());
        Set query = new HashSet<>();
        query.add("id");
        query.add("gemeente");
        Map data = new HashMap<>();




        StepVerifier.create(result)
                .expectNext(expected)
                .expectComplete()
                .verify();
    }
}

