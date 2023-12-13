package nl.geostandaarden.imx.orchestrate.souce.rest.mapper;

import graphql.language.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import nl.geostandaarden.imx.orchestrate.engine.source.SourceException;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class ValueMapper {

  static Value<?> mapToValue(Object value) {
    if (value instanceof String s) {
      return StringValue.of(s);
    }
    if (value instanceof Integer i) {
      return IntValue.of(i);
    }
    if (value instanceof Boolean b) {
      return BooleanValue.of(b);
    }
    if (value instanceof List<?> l) {
      var array = ArrayValue.newArrayValue();
      l.forEach(item -> array.value(mapToValue(item)));
      return array.build();
    }

    throw new SourceException(String.format("Value type '%s' is unsupported.", value.getClass()));
  }
}
