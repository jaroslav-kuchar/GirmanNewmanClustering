package cz.cvut.fit.krizeji1.girvan_newman;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import org.gephi.clustering.spi.Clusterer;
import org.gephi.clustering.spi.ClustererUI;

public class GirvanNewmanClustererUI implements ClustererUI {

  JPanel panel = null;
  GirvanNewmanClusterer clusterer = null;

  public GirvanNewmanClustererUI() {
    initComponents();
  }

  @Override
  public JPanel getPanel() {
    return panel;
  }

  @Override
  public void setup(Clusterer clstr) {
    this.clusterer = (GirvanNewmanClusterer) clusterer;
  }

  @Override
  public void unsetup() {
  }

  private void initComponents() {
    panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
  }
}
