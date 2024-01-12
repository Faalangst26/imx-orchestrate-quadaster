
import nl.geostandaarden.imx.orchestrate.engine.exchange.BatchRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.CollectionRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.ObjectRequest;
import nl.geostandaarden.imx.orchestrate.model.Attribute;
import nl.geostandaarden.imx.orchestrate.model.Model;
import nl.geostandaarden.imx.orchestrate.model.ObjectType;
import nl.geostandaarden.imx.orchestrate.model.types.ScalarTypes;
import nl.geostandaarden.imx.orchestrate.source.rest.executor.ApiExecutor;
import nl.geostandaarden.imx.orchestrate.source.rest.mapper.BatchRestMapper;
import nl.geostandaarden.imx.orchestrate.source.rest.mapper.CollectionRestMapper;
import nl.geostandaarden.imx.orchestrate.source.rest.mapper.ObjectRestMapper;
import nl.geostandaarden.imx.orchestrate.source.rest.mapper.ResponseMapper;
import nl.geostandaarden.imx.orchestrate.source.rest.repository.RestRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class RestRepositoryTest {

    @Mock
    private ApiExecutor executor;

    @Mock
    private ObjectRestMapper objectRestMapper;

    @Mock
    private CollectionRestMapper collectionRestMapper;

    @Mock
    private BatchRestMapper batchRestMapper;

    @Mock
    private ResponseMapper responseMapper;

    @InjectMocks
    private RestRepository repository;

    @Test
    void findOne_usesCorrectMappers() {
        this.repository.findOne(getObjectRequest());

        verify(objectRestMapper, times(1)).convert(any(ObjectRequest.class));
        verifyNoInteractions(collectionRestMapper);
        verifyNoInteractions(batchRestMapper);

        verify(responseMapper, times(1)).processFindOneResult(any());
        verify(responseMapper, never()).processFindResult(any(), any());
        verify(responseMapper, never()).processBatchResult(any(), any());
    }

    @Test
    void find_usesCorrectMappers() {
        this.repository.find(getCollectionRequest());

        verifyNoInteractions(objectRestMapper);
        verify(collectionRestMapper, times(1)).convert(any(CollectionRequest.class));
        verifyNoInteractions(batchRestMapper);

        verify(responseMapper, never()).processFindOneResult(any());
        verify(responseMapper, times(1)).processFindResult(any(), any());
        verify(responseMapper, never()).processBatchResult(any(), any());
    }

    @Test
    void findBatch_usesCorrectMappers() {
        this.repository.findBatch(getBatchRequest());

        verifyNoInteractions(objectRestMapper);
        verifyNoInteractions(collectionRestMapper);
        verify(batchRestMapper, times(1)).convert(any(BatchRequest.class));

        verify(responseMapper, never()).processFindOneResult(any());
        verify(responseMapper, never()).processFindResult(any(), any());
        verify(responseMapper, times(1)).processBatchResult(any(), any());
    }

    private ObjectRequest getObjectRequest() {
        return ObjectRequest.builder(createModel())
                .objectType("abc")
                .objectKey(Map.of("id", "123"))
                .selectProperty("attr")
                .build();
    }

    private CollectionRequest getCollectionRequest() {
        return CollectionRequest.builder(createModel())
                .objectType("abc")
                .selectProperty("attr")
                .build();
    }

    private BatchRequest getBatchRequest() {
        return BatchRequest.builder(createModel())
                .objectType("abc")
                .objectKey(Map.of("id", "123"))
                .objectKey(Map.of("id", "456"))
                .selectProperty("attr")
                .build();
    }

    private Model createModel() {
        return Model.builder()
                .objectType(ObjectType.builder()
                        .name("abc")
                        .property(Attribute.builder()
                                .name("attr")
                                .type(ScalarTypes.STRING)
                                .build())
                        .build())
                .build();
    }
}

