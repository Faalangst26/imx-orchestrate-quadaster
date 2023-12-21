package nl.geostandaarden.imx.orchestrate.source.rest.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
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

import java.util.HashMap;
import java.util.Map;



@RequiredArgsConstructor
@Slf4j
public class RestRepository implements DataRepository {
    //Executor van RemoteExecutor
    private final ApiExecutor executor;

    private final ObjectRestMapper objectRestMapper;

    private final CollectionRestMapper collectionRestMapper;

    private final BatchRestMapper batchRestMapper;

    private final ResponseMapper responseMapper;

    @Override
    public Mono<Map<String, Object>> findOne(ObjectRequest objectRequest) {
        var rest = objectRestMapper.convert(objectRequest);

        //TODO: Deze ramp weghalen en vervangen door iets goeds.
        log.debug("findOne bereikt!");
        Map<String, Object> output = new HashMap<String, Object>();
        var emir = responseMapper.processFindOneResult(this.executor.execute(rest));
        var uitgepakt = emir;
        uitgepakt.subscribe(items -> {
            for (var item : items.values() ){
                var values = item.toString();
                for (var prop : objectRequest.getSelectedProperties()){


                    int foundIndex = values.indexOf(prop.toString() + "=");
                    String val = values.substring(foundIndex + prop.toString().length() + 1, values.indexOf(',', foundIndex));

                    log.debug("Looking for property: " + prop + "  value: " + val + "\n");

                    output.put(prop.toString() , val);
                }

                log.debug(item + "\n");
                break;
            }
            output.put("id", "A0001");
            for (var outputitem : output.keySet()){
                log.debug("output map: " + outputitem + ":" + output.get(outputitem));
            }

        }, error -> {

        });




        return Mono.justOrEmpty(output);
       // return emir;
    }

    @Override
    public Flux<Map<String, Object>> find(CollectionRequest collectionRequest) {
        var rest = collectionRestMapper.convert(collectionRequest);
        return responseMapper.processFindResult(this.executor.execute(rest), getName(collectionRequest));
    }

    @Override
    public Flux<Map<String, Object>> findBatch(BatchRequest batchRequest) {
        var rest = batchRestMapper.convert(batchRequest);
        return responseMapper.processBatchResult(this.executor.execute(rest), getName(batchRequest));
    }

    @Override
    public boolean supportsBatchLoading(ObjectType objectType) {
       return true;
    }

    private String getName(DataRequest request) {
        return request.getObjectType()
                .getName();
    }
}


