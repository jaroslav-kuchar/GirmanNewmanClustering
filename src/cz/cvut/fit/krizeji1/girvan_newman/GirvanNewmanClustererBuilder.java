package cz.cvut.fit.krizeji1.girvan_newman;

import org.gephi.clustering.spi.Clusterer;
import org.gephi.clustering.spi.ClustererBuilder;
import org.gephi.clustering.spi.ClustererUI;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = ClustererBuilder.class)
public class GirvanNewmanClustererBuilder<T> implements ClustererBuilder {

  @Override
  public Clusterer getClusterer() {
    return new GirvanNewmanClusterer();
  }

  @Override
  public String getName() {
    return GirvanNewmanClusterer.PLUGIN_NAME;
  }

  @Override
  public String getDescription() {
    return GirvanNewmanClusterer.PLUGIN_DESCRIPTION;
  }

  @Override
  public Class getClustererClass() {
    return GirvanNewmanClusterer.class;
  }

  @Override
  public ClustererUI getUI() {
    return new GirvanNewmanClustererUI();
  }
}
