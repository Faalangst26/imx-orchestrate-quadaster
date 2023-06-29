package org.dotwebstack.orchestrate.ext.spatial;

import java.util.Map;
import org.dotwebstack.orchestrate.model.ModelException;
import org.dotwebstack.orchestrate.model.types.ValueTypeFactory;

public class GeometryTypeFactory implements ValueTypeFactory<GeometryType> {

  @Override
  public String getTypeName() {
    return GeometryType.TYPE_NAME;
  }

  @Override
  public GeometryType create(Map<String, Object> options) {
    var srid = options.getOrDefault("srid", GeometryType.DEFAULT_SRID);

    if (srid instanceof Integer sridInt) {
      return new GeometryType(sridInt);
    }

    throw new ModelException("SRID is not an integer.");
  }
}
