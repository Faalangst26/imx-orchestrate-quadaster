package nl.geostandaarden.imx.orchestrate.source.rest.Result;

import java.util.Map;

public class ObjectResult extends AbstractResult {
    public Map<String, Object> data;
    public String QueryType;
    public ObjectResult(Map<String, Object> data) {
        super(data, "Object");
    }
}
