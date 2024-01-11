package nl.geostandaarden.imx.orchestrate.gateway;

import static java.util.stream.Collectors.toUnmodifiableMap;
import static java.util.stream.Collectors.toUnmodifiableSet;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import nl.geostandaarden.imx.orchestrate.engine.OrchestrateEngine;
import nl.geostandaarden.imx.orchestrate.engine.source.Source;
import nl.geostandaarden.imx.orchestrate.engine.source.SourceType;
import nl.geostandaarden.imx.orchestrate.ext.spatial.SpatialExtension;
import nl.geostandaarden.imx.orchestrate.gateway.schema.SchemaFactory;
import nl.geostandaarden.imx.orchestrate.model.ComponentRegistry;
import nl.geostandaarden.imx.orchestrate.model.Model;
import nl.geostandaarden.imx.orchestrate.model.loader.ModelLoader;
import nl.geostandaarden.imx.orchestrate.model.loader.ModelLoaderRegistry;
import nl.geostandaarden.imx.orchestrate.model.types.ValueTypeRegistry;
import nl.geostandaarden.imx.orchestrate.parser.yaml.YamlModelMappingParser;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.DefaultExecutionGraphQlService;
import org.springframework.graphql.execution.GraphQlSource;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(GraphQlProperties.class)
public class GatewayConfiguration {

  private final GatewayProperties gatewayProperties;

  @Bean
  public GraphQlSource graphQlSource() throws IOException {
    var extensions = Set.of(new SpatialExtension());

    var componentRegistry = new ComponentRegistry();
    extensions.forEach(extension -> extension.registerComponents(componentRegistry));

    var modelLoaderRegistry = new ModelLoaderRegistry();
    resolveModelLoaders().forEach(modelLoaderRegistry::register);

    var valueTypeRegistry = new ValueTypeRegistry();
    extensions.forEach(extension -> extension.registerValueTypes(valueTypeRegistry));

    var modelMapping = new YamlModelMappingParser(componentRegistry, modelLoaderRegistry, valueTypeRegistry)
        .parse(new FileInputStream(gatewayProperties.getMapping()));

    var sourceModelMap = modelMapping.getSourceModels().stream()
        .collect(toUnmodifiableMap(Model::getAlias, Function.identity()));

    var sources = gatewayProperties.getSources()
        .entrySet()
        .stream()
        .collect(Collectors.toMap(Map.Entry::getKey, e -> resolveSource(e.getKey(), e.getValue(), sourceModelMap)));

    var engine = OrchestrateEngine.builder()
        .modelMapping(modelMapping)
        .sources(sources)
        .extensions(extensions)
        .build();

    var graphQL = GraphQL.newGraphQL(SchemaFactory.create(engine)).build();

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

  private Set<ModelLoader> resolveModelLoaders() {
    return ServiceLoader.load(ModelLoader.class)
        .stream()
        .map(ServiceLoader.Provider::get)
        .collect(toUnmodifiableSet());
  }

  private Source resolveSource(String dataset, GatewaySource source, Map<String, Model> sourceModels) {
    if (!sourceModels.containsKey(dataset)) {
      throw new GatewayException(String.format("No model with alias `%s` configured in model mapping.", dataset));
    }

    ServiceLoader<SourceType> loader = ServiceLoader.load(SourceType.class);

    return loader.stream()
        .map(ServiceLoader.Provider::get)
        .filter(e -> e.getName().equals(source.getType()))
        .findFirst()
        .map(s -> s.create(sourceModels.get(dataset), source.getOptions()))
        .orElseThrow(() -> new GatewayException(String.format("Source type '%s' not found.", source.getType())));
  }

  @Bean
  public DefaultExecutionGraphQlService graphQlService(GraphQlSource graphQlSource) {
    return new DefaultExecutionGraphQlService(graphQlSource);
  }
}
