package nl.geostandaarden.imx.orchestrate.source.rest.Result;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractResult<T>{
    public T data;
//    private final Map<Object, Object> extensions;
//    private final Map<Object, Object> localContext;
//    private final ResultPath queryPath;
    private final String queryType;

    public AbstractResult(T data, String queryType) {
        this.data = data;
        this.queryType = queryType;
    }

    public <T> T getData() {
        if(data != null) return (T) data;
        return null;
    }

    public boolean isDataPresent() {
        if(data != null) return true;
        return false;
    }

    public Map<String, Object> toOne(T data) {
        try{return (Map<String, Object>)data;}
        catch (Exception e){
            System.out.print("ToOneError");
            return null;
        }
    }

    public List<LinkedHashMap<String, ObjectNode>> ToColl(T data){
        try{return (List<LinkedHashMap<String, ObjectNode>>)data;}
        catch (Exception e){
            System.out.print("ToCollError");
            return null;
        }
    }
}
