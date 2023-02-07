package org.dotwebstack.orchestrate.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;
import org.dotwebstack.orchestrate.model.types.ObjectTypeRef;

@Getter
@ToString(exclude = {"propertyMap"})
public final class ObjectType {

  private final String name;

  private final List<Property> properties;

  private final List<Property> identityProperties;

  private final Map<String, Property> propertyMap;

  @Builder(toBuilder = true)
  private ObjectType(String name, @Singular List<Property> properties) {
    this.name = name;
    this.properties = Collections.unmodifiableList(properties);
    propertyMap = properties.stream()
        .collect(Collectors.toUnmodifiableMap(Property::getName, Function.identity()));
    identityProperties = properties.stream()
        .filter(Property::isIdentifier)
        .toList();
  }

  public <T extends Property> List<T> getProperties(Class<T> propertyClass) {
    return properties.stream()
        .filter(propertyClass::isInstance)
        .map(propertyClass::cast)
        .toList();
  }

  public <T extends Property> List<T> getIdentityProperties(Class<T> propertyClass) {
    return identityProperties.stream()
        .filter(propertyClass::isInstance)
        .map(propertyClass::cast)
        .toList();
  }

  public Property getProperty(String name) {
    return Optional.ofNullable(propertyMap.get(name))
        .orElseThrow(() -> new ModelException("Attribute not found: " + name));
  }

  public ObjectTypeRef getRef() {
    return ObjectTypeRef.forType(name);
  }

  public boolean hasIdentity() {
    return !identityProperties.isEmpty();
  }
}
