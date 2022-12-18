package org.dotwebstack.orchestrate.engine.fetch;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

@Getter
@Builder(toBuilder = true)
public final class FetchRequest {

  private final String objectType;

  @Singular
  private final List<SelectedField> selectedFields;
}
