package org.dotwebstack.orchestrate;

import static org.dotwebstack.orchestrate.TestFixtures.createModelMapping;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import java.util.Map;
import org.dotwebstack.orchestrate.engine.Orchestration;
import org.dotwebstack.orchestrate.engine.schema.SchemaFactory;
import org.dotwebstack.orchestrate.source.CollectionRequest;
import org.dotwebstack.orchestrate.source.DataRepository;
import org.dotwebstack.orchestrate.source.ObjectRequest;
import org.dotwebstack.orchestrate.source.Source;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.DefaultExecutionGraphQlService;
import org.springframework.graphql.execution.GraphQlSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Configuration
@EnableConfigurationProperties(GraphQlProperties.class)
public class GatewayConfiguration {

  @Bean
  public GraphQlSource graphQlSource() {
    var orchestration = Orchestration.builder()
        .modelMapping(createModelMapping())
        .source("bag", createSourceStub())
        .build();

    var graphQL = GraphQL.newGraphQL(SchemaFactory.create(orchestration)).build();

    return new GraphQlSource() {
      @Override
      public GraphQL graphQl() {
        return graphQL;
      }

      @Override
      public GraphQLSchema schema() {
        return graphQL.getGraphQLSchema();
      }
    };
  }

  @Bean
  public DefaultExecutionGraphQlService graphQlService(GraphQlSource graphQlSource) {
    return new DefaultExecutionGraphQlService(graphQlSource);
  }

  private Source createSourceStub() {
    return () -> new DataRepository() {
      @Override
      public Mono<Map<String, Object>> findOne(ObjectRequest objectRequest) {
        var typeName = objectRequest.getObjectType()
            .getName();

        return switch (typeName) {
          case "Nummeraanduiding" ->
              Mono.just(Map.of("identificatie", "0200200000075716", "huisnummer", 701, "postcode", "7334DP", "ligtAan",
                  Map.of("identificatie", "0200300022472362")));
          case "OpenbareRuimte" ->
              Mono.just(Map.of("naam", "Laan van Westenenk", "ligtIn", Map.of("identificatie", "3560")));
          case "Woonplaats" -> Mono.just(Map.of("naam", "Apeldoorn"));
          default -> Mono.error(() -> new RuntimeException("Error!"));
        };
      }

      @Override
      public Flux<Map<String, Object>> find(CollectionRequest collectionRequest) {
        var typeName = collectionRequest.getObjectType()
            .getName();

        return switch (typeName) {
          case "Nummeraanduiding" -> Flux.just(
              Map.of("identificatie", "0200200000075716", "huisnummer", 701, "postcode", "7334DP", "ligtAan", Map.of(
                  "identificatie", "0200300022472362")),
              Map.of("identificatie", "0200200000075717", "huisnummer", 702, "postcode", "7334DP", "ligtAan", Map.of(
                  "identificatie", "0200300022472362")),
              Map.of("identificatie", "0200200000075718", "huisnummer", 703, "postcode", "7334DP", "ligtAan", Map.of(
                  "identificatie", "0200300022472362")));
          case "Verblijfsobject" -> Flux.just(Map.of("identificatie", "0200010000130331"));
          default -> Flux.error(() -> new RuntimeException("Error!"));
        };
      }
    };
  }
}
