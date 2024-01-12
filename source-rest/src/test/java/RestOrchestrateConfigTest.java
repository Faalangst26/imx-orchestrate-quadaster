import nl.geostandaarden.imx.orchestrate.source.rest.config.RestOrchestrateConfig;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RestOrchestrateConfigTest {

    @Test
    void build_returnsNewGraphQlOrchestrateConfig_forSuffixValues() {
        var result = RestOrchestrateConfig
                .builder()
                .collectionSuffix("Collection")
                .batchSuffix("Batch")
                .build();

        assertThat(result.getAuthToken()).isNull();
        assertThat(result.getBaseUrl()).isNull();
        assertThat(result.getBatchSuffix()).hasToString("Batch");
        assertThat(result.getCollectionSuffix()).hasToString("Collection");
    }
}
