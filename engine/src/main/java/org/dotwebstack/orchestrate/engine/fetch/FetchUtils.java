package org.dotwebstack.orchestrate.engine.fetch;

import static graphql.introspection.Introspection.INTROSPECTION_SYSTEM_FIELDS;

import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.dotwebstack.orchestrate.model.types.Field;
import org.dotwebstack.orchestrate.model.types.ObjectType;
import org.dotwebstack.orchestrate.source.SelectedField;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class FetchUtils {

  public static UnaryOperator<Map<String, Object>> keyExtractor(ObjectType objectType) {
    return data -> objectType.getIdentityFields()
        .stream()
        .collect(Collectors.toMap(Field::getName, field -> data.get(field.getName())));
  }

  public static List<SelectedField> selectIdentifyFields(ObjectType objectType) {
    return objectType.getIdentityFields()
        .stream()
        .map(SelectedField::new)
        .toList();
  }

  public static boolean isIntrospectionField(graphql.schema.SelectedField selectedField) {
    return INTROSPECTION_SYSTEM_FIELDS.contains(selectedField.getName());
  }
}
