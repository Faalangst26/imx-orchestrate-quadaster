package nl.geostandaarden.imx.orchestrate.source.rest.config;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class RestOrchestrateConfig {
    private char[] authToken;

    private String baseUrl;

    private String apiKey;

    @Builder.Default
    private String collectionSuffix = "Collection";

    @Builder.Default
    private String batchSuffix = "Batch";
}
