package nl.geostandaarden.imx.orchestrate.source.rest;

import nl.geostandaarden.imx.orchestrate.engine.source.DataRepository;
import nl.geostandaarden.imx.orchestrate.engine.source.Source;
import nl.geostandaarden.imx.orchestrate.source.rest.config.RestOrchestrateConfig;
import nl.geostandaarden.imx.orchestrate.source.rest.executor.RemoteExecutor;
import nl.geostandaarden.imx.orchestrate.source.rest.mapper.BatchRestMapper;
import nl.geostandaarden.imx.orchestrate.source.rest.mapper.CollectionRestMapper;
import nl.geostandaarden.imx.orchestrate.source.rest.mapper.ObjectRestMapper;
import nl.geostandaarden.imx.orchestrate.source.rest.mapper.ResponseMapper;
import nl.geostandaarden.imx.orchestrate.source.rest.repository.RestRepository;

public class RestSource implements Source {

  private final RestOrchestrateConfig config;
  public RestSource(RestOrchestrateConfig config) {
    this.config = config;
  }

  @Override
  public DataRepository getDataRepository() {
    return new RestRepository(RemoteExecutor.create(config), new ObjectRestMapper(),
      new CollectionRestMapper(), new BatchRestMapper(), new ResponseMapper());
  }
}
