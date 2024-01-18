package nl.geostandaarden.imx.orchestrate.source.rest.Result;

public abstract class AbstractResult<T>{
    public T data;

    public String queryType;

    public AbstractResult(T data, String queryType) {
        this.data = data;
        this.queryType = queryType;
    }

    public T getData() {
        if(data != null) return data;
        return null;
    }


}
