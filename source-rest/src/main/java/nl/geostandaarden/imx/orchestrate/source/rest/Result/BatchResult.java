package nl.geostandaarden.imx.orchestrate.source.rest.Result;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.LinkedHashMap;
import java.util.List;

public class BatchResult extends AbstractResult<List<LinkedHashMap<String, ObjectNode>>> {

    public final List<LinkedHashMap<String, ObjectNode>> data;
    public BatchResult(List<LinkedHashMap<String, ObjectNode>> data) {
        super(data,"Batch");
        this.data = data;
    }
}
