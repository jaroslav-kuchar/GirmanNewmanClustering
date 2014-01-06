package cz.cvut.fit.krizeji1.girvan_newman;

import java.util.ArrayList;
import java.util.List;
import org.gephi.clustering.api.Cluster;
import org.gephi.graph.api.Node;

public class GirvanNewmanClusterImpl implements Cluster {

  private List<Node> nodes = new ArrayList<Node>();
  private String clusterName = "untitled";
  private Node metaNode = null;

  public GirvanNewmanClusterImpl() {
  }

  public GirvanNewmanClusterImpl(List<Node> nodeList) {
    this.nodes = nodeList;
  }

  public void addNode(Node node) {
    this.nodes.add(node);
  }

  public void setName(String clusterName) {
    this.clusterName = clusterName;
  }

  @Override
  public Node[] getNodes() {
    return this.nodes.toArray(new Node[0]);
  }

  @Override
  public int getNodesCount() {
    return this.nodes.size();
  }

  @Override
  public String getName() {
    return clusterName;
  }

  @Override
  public Node getMetaNode() {
    return this.metaNode;
  }

  @Override
  public void setMetaNode(Node node) {
    this.metaNode = node;
  }
}
