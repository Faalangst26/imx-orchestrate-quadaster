package nl.geostandaarden.imx.orchestrate.source.rest.Result;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.LinkedHashMap;
import java.util.List;

public class CollectionResult extends AbstractResult {
    public List<LinkedHashMap<String, Object>> data;
    public CollectionResult(List<LinkedHashMap<String, Object>> data) {
        super( "Collection");
        this.data = data;
    }

}
