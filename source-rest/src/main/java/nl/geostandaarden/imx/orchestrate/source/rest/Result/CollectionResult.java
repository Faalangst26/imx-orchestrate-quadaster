package nl.geostandaarden.imx.orchestrate.source.rest.Result;

import java.util.LinkedHashMap;
import java.util.List;

public class CollectionResult extends AbstractResult<List<LinkedHashMap<String, Object>>> {
    public final List<LinkedHashMap<String, Object>> data;
    public CollectionResult(List<LinkedHashMap<String, Object>> data) {
        super(data, "Collection");
        this.data = data;
    }

}
