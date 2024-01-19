package nl.geostandaarden.imx.orchestrate.source.rest.Result;

import java.util.Map;

public class ObjectResult extends AbstractResult<Map<String, Object>> {
    public Map<String, Object> data;
    public ObjectResult(Map<String, Object> data) {
        super(data, "Object");
    }

}
