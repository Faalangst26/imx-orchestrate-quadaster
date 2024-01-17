package nl.geostandaarden.imx.orchestrate.source.rest.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.geostandaarden.imx.orchestrate.engine.exchange.BatchRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.CollectionRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.DataRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.ObjectRequest;
import nl.geostandaarden.imx.orchestrate.engine.source.DataRepository;
import nl.geostandaarden.imx.orchestrate.model.ObjectType;
import nl.geostandaarden.imx.orchestrate.source.rest.executor.ApiExecutor;
import nl.geostandaarden.imx.orchestrate.source.rest.mapper.BatchRestMapper;
import nl.geostandaarden.imx.orchestrate.source.rest.mapper.CollectionRestMapper;
import nl.geostandaarden.imx.orchestrate.source.rest.mapper.ObjectRestMapper;
import nl.geostandaarden.imx.orchestrate.source.rest.mapper.ResponseMapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@RequiredArgsConstructor
@Slf4j
public class RestRepository implements DataRepository {

    private final ApiExecutor executor;

    private final ObjectRestMapper objectRestMapper;

    private final CollectionRestMapper collectionRestMapper;

    private final BatchRestMapper batchRestMapper;

    private final ResponseMapper responseMapper;

    @Override
    public Mono<Map<String, Object>> findOne(ObjectRequest objectRequest) {
        var rest = objectRestMapper.convert(objectRequest);
        return responseMapper.processFindOneResult(this.executor.execute(rest, objectRequest));
    }

    @Override
    public Flux<Map<String, Object>> find(CollectionRequest collectionRequest) {
        var rest = collectionRestMapper.convert(collectionRequest);
        return responseMapper.processFindResult(this.executor.execute(rest, collectionRequest), getName(collectionRequest));
    }

    @Override
    public Flux<Map<String, Object>> findBatch(BatchRequest batchRequest) {
        var rest = batchRestMapper.convert(batchRequest);
        return responseMapper.processBatchResult(this.executor.execute(rest, batchRequest), getName(batchRequest));
    }

    @Override
    public boolean supportsBatchLoading(ObjectType objectType) {
       return false;
    }

    private String getName(DataRequest request) {
        return request.getObjectType()
                .getName();
    }
}


