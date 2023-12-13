package nl.geostandaarden.imx.orchestrate.souce.rest;

import nl.geostandaarden.imx.orchestrate.engine.source.DataRepository;
import nl.geostandaarden.imx.orchestrate.engine.source.Source;
import nl.geostandaarden.imx.orchestrate.souce.rest.config.RestOrchestrateConfig;
import nl.geostandaarden.imx.orchestrate.souce.rest.executor.RemoteExecutor;
import nl.geostandaarden.imx.orchestrate.souce.rest.mapper.BatchRestMapper;
import nl.geostandaarden.imx.orchestrate.souce.rest.mapper.CollectionRestMapper;
import nl.geostandaarden.imx.orchestrate.souce.rest.mapper.ObjectRestMapper;
import nl.geostandaarden.imx.orchestrate.souce.rest.mapper.ResponseMapper;
import nl.geostandaarden.imx.orchestrate.souce.rest.repository.RestRepository;

public class RestSource implements Source {

  private final RestOrchestrateConfig config;

  RestSource(RestOrchestrateConfig config) {
    this.config = config;
  }

  @Override
  public DataRepository getDataRepository() {
    return new RestRepository(RemoteExecutor.create(config), new ObjectRestMapper(config),
      new CollectionRestMapper(config), new BatchRestMapper(config), new ResponseMapper(config));
  }
}
