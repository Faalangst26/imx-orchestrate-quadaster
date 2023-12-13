package nl.geostandaarden.imx.orchestrate.souce.rest.repository;

import lombok.RequiredArgsConstructor;
import nl.geostandaarden.imx.orchestrate.engine.exchange.BatchRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.CollectionRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.ObjectRequest;
import nl.geostandaarden.imx.orchestrate.engine.source.DataRepository;
import nl.geostandaarden.imx.orchestrate.model.ObjectType;
import nl.geostandaarden.imx.orchestrate.souce.rest.executor.Executor;
import nl.geostandaarden.imx.orchestrate.souce.rest.mapper.BatchRestMapper;
import nl.geostandaarden.imx.orchestrate.souce.rest.mapper.CollectionRestMapper;
import nl.geostandaarden.imx.orchestrate.souce.rest.mapper.ObjectRestMapper;
import nl.geostandaarden.imx.orchestrate.souce.rest.mapper.ResponseMapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;


@RequiredArgsConstructor
public class RestRepository implements DataRepository
{
    //Executor van RemoteExecutor
    private final Executor executor;

    private final ObjectRestMapper objectRestMapper;

    private final CollectionRestMapper collectionRestMapper;

    private final BatchRestMapper batchRestMapper;

    private final ResponseMapper responseMapper;

    @Override
    public Mono<Map<String, Object>> findOne(ObjectRequest objectRequest) {
        return null;
    }

    @Override
    public Flux<Map<String, Object>> find(CollectionRequest collectionRequest) {
        return null;
    }

    @Override
    public Flux<Map<String, Object>> findBatch(BatchRequest batchRequest) {
        return DataRepository.super.findBatch(batchRequest);
    }

    @Override
    public boolean supportsBatchLoading(ObjectType objectType) {
        return DataRepository.super.supportsBatchLoading(objectType);
    }
}
