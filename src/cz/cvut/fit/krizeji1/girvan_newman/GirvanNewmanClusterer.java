package cz.cvut.fit.krizeji1.girvan_newman;

import cz.cvut.fit.krizeji1.edge_betweenness.EdgeBetweenness;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import multicolour.attribute.GraphColorizer;
import org.gephi.clustering.api.Cluster;
import org.gephi.clustering.spi.Clusterer;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Node;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

public class GirvanNewmanClusterer implements Clusterer, LongTask {

    private static final int UNSEEN_CLUSTER = -1;
    private List<Cluster> result = new ArrayList<Cluster>();
    public static final String PLUGIN_NAME = "Girvan Newman";
    public static final String PLUGIN_DESCRIPTION = "Girvan Newman Clustering";
    public static final String CLUSTER = "cluster";
    public static final String PREV_CLUSTER = "prev_cluster";
    ProgressTicket progress = null;
    private static final Logger logger = Logger.getLogger(GirvanNewmanClusterer.class.getName());
    private Map<Integer, ArrayList<Cluster>> clusters = new HashMap<Integer, ArrayList<Cluster>>();
    boolean isCancelled = false;
    private GraphModel graphModel = null;
    private AttributeColumn clusterColumn;
    private Graph tempGraph;
    private AttributeColumn prevClusterColumn;
    private int selectedClustersCount;
    private int preferred = -1;
    private Vector<Integer> clusterCounts;

    @Override
    public void execute(GraphModel gm) {
        long startAlg = System.currentTimeMillis();
        
        this.graphModel = gm;
        this.isCancelled = false;
        if (progress != null) {
            this.progress.progress(NbBundle.getMessage(GirvanNewmanClusterer.class, "GirvanNewmanClusterer.setup"));
            this.progress.start();
        }

        GraphView view = graphModel.newView();
        tempGraph = graphModel.getGraph(view);

        long startInnerAlg = System.currentTimeMillis();
        this.clusterCounts = new Vector<Integer>();
        while (tempGraph.getEdgeCount() > 0) {
            // Count betweenness for all edges
            AttributeColumn col = recalculateBetweenness();

            if (isCancelled) {
                return;
            }

            // Remove edge with highest betweeness
            EdgeIterable edges = tempGraph.getEdges();
            Double maxBetw = Double.NEGATIVE_INFINITY;

            for (Edge e : edges) {
                Double centrality = (Double) e.getEdgeData().getAttributes().getValue(EdgeBetweenness.EDGE_BETWEENNESS);
                if (centrality > maxBetw) {
                    maxBetw = centrality;
                }
            }


            for (Edge maxEdge : findEdgesWithBetweenness(maxBetw)) {
                tempGraph.removeEdge(maxEdge);
            }

            // after removal of edges, find clusters
            int clusterCount = findClusters();
            if (getClusterCounts().isEmpty() || (getClusterCounts().lastElement() != clusterCount)) {
                getClusterCounts().add(clusterCount);
            }
        }
        long endInnerAlg = System.currentTimeMillis() - startInnerAlg;
        System.out.println("endInnerAlg: "+endInnerAlg);
        
        if (this.preferred > 0) {
            selectedClustersCount = findNearest(getClusterCounts());
            System.out.println("Cluster counts: "+getClusterCounts().toString());
        } else {
            SelectClustersPanel panel = new SelectClustersPanel(getClusterCounts());

            DialogDescriptor dd = new DialogDescriptor(panel, "Select number of clusters", true, null);
            if (DialogDisplayer.getDefault().notify(dd) == NotifyDescriptor.OK_OPTION) {
                selectedClustersCount = panel.getSelectedClustersCount();
            }
        }

        result = clusters.get(selectedClustersCount);

        AttributeTable nodeTable = Lookup.getDefault().lookup(AttributeController.class).getModel().getNodeTable();
        GraphColorizer c = new GraphColorizer(nodeTable);
        if (result != null && result.size() > 0) {
            c.colorizeGraph(result.toArray(new Cluster[selectedClustersCount]));
        }

        if (progress != null) {
            this.progress.finish(NbBundle.getMessage(GirvanNewmanClusterer.class, "GirvanNewmanClusterer.finished"));
        }

        long endAlg = System.currentTimeMillis() - startAlg;
        System.out.println("endAlg: "+endAlg);
    }

    @Override
    public Cluster[] getClusters() {
        if (result.isEmpty()) {
            return null;
        }
        return result.toArray(new Cluster[selectedClustersCount]);
    }

    @Override
    public boolean cancel() {
        this.progress.finish(NbBundle.getMessage(GirvanNewmanClusterer.class, "GirvanNewmanClusterer.cancelled"));
        return this.isCancelled = true;
    }

    @Override
    public void setProgressTicket(ProgressTicket pt) {
        this.progress = pt;
    }

    private AttributeColumn recalculateBetweenness() {
        if (tempGraph == null) {
            throw new IllegalStateException("tempGraph can not be null");
        }
        AttributeModel attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();

        return recalculateBetweenness(tempGraph.getGraphModel());
    }
    
    private AttributeColumn recalculateBetweenness(GraphModel model) {
        AttributeModel attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();

        EdgeBetweenness eb = new EdgeBetweenness();
        eb.execute(model, attributeModel);

        return attributeModel.getNodeTable().getColumn(EdgeBetweenness.EDGE_BETWEENNESS);
    }

    private Edge[] findEdgesWithBetweenness(Double maxBetw) {
        if (tempGraph == null) {
            throw new IllegalStateException("tempGraph can not be null");
        }
        ArrayList<Edge> res = new ArrayList<Edge>();

        for (Edge e : tempGraph.getEdges()) {
            double betw = (Double) e.getEdgeData().getAttributes().getValue(EdgeBetweenness.EDGE_BETWEENNESS);
            if (betw == maxBetw) {
                res.add(e);
            }
        }
        Edge[] resArr = new Edge[res.size()];
        return res.toArray(resArr);
    }

    private int findClusters() {
        if (tempGraph == null) {
            throw new IllegalStateException("tempGraph can not be null");
        }

        prevClusterColumn = copyClusterToPrev();
        clusterColumn = prepareClusterColumn(UNSEEN_CLUSTER);
        int clusterNumber = 0;

        ArrayList<Cluster> currCluster = new ArrayList<Cluster>();
        for (Node rootNode : tempGraph.getNodes()) {
            int nodeClusterId = getNodeClusterId(rootNode);

            if (nodeClusterId == UNSEEN_CLUSTER) {
                //doBFS
                GirvanNewmanClusterImpl tmpCluster = bfsMarkNodes(rootNode, clusterNumber++);
                currCluster.add(tmpCluster);
            }
        }

        int clustersCount = clusterNumber;
        clusters.put(clustersCount, currCluster);
        return clusterNumber;
    }

    private AttributeColumn prepareClusterColumn(int initialValue) {
        AttributeController ac = Lookup.getDefault().lookup(AttributeController.class);
        AttributeTable nodeTable = ac.getModel().getNodeTable();
        AttributeColumn column = nodeTable.getColumn(CLUSTER, AttributeType.INT);

        if (column != null) {
            nodeTable.removeColumn(column);
        }

        return nodeTable.addColumn(CLUSTER, CLUSTER, AttributeType.INT, AttributeOrigin.COMPUTED, new Integer(initialValue));
    }

    private AttributeColumn copyClusterToPrev() {
        AttributeController ac = Lookup.getDefault().lookup(AttributeController.class);
        AttributeTable nodeTable = ac.getModel().getNodeTable();
        if (nodeTable.hasColumn(CLUSTER)) {
            clusterColumn = nodeTable.getColumn(CLUSTER, AttributeType.INT);

            if (nodeTable.hasColumn(PREV_CLUSTER)) {
                nodeTable.removeColumn(nodeTable.getColumn(PREV_CLUSTER));
            }

            AttributeColumn duplicateColumn = Lookup.getDefault().lookup(org.gephi.datalab.api.AttributeColumnsController.class).duplicateColumn(nodeTable, clusterColumn, PREV_CLUSTER, AttributeType.INT);
            return duplicateColumn;
        }
        return null;
    }

    private int getNodeClusterId(Node node) {
        return getParamClusterId(node, clusterColumn);
    }

    private int getNodeClusterPrevId(Node node) {
        return getParamClusterId(node, prevClusterColumn);
    }

    private int getParamClusterId(Node node, AttributeColumn attr) {
        if (attr == null) {
            throw new IllegalStateException("cluster Column must be initialized before calling getParamClusterId");
        }
        AttributeRow row = (AttributeRow) node.getNodeData().getAttributes();
        return (Integer) row.getValue(attr);
    }

    private void setNodeClusterId(Node node, int newClusterId) {
        if (clusterColumn == null) {
            throw new IllegalStateException("clusterColumn must be initialized before calling setNodeClusterId");
        }
        AttributeRow row = (AttributeRow) node.getNodeData().getAttributes();

        row.setValue(clusterColumn, newClusterId);
    }

    private GirvanNewmanClusterImpl bfsMarkNodes(Node rootNode, int newClusterId) {
        if (tempGraph == null) {
            throw new IllegalStateException("tempGraph cannot be null");
        }

        GirvanNewmanClusterImpl cluster = new GirvanNewmanClusterImpl();
        LinkedList<Node> queue = new LinkedList<Node>();
        queue.addLast(rootNode);

        while (!queue.isEmpty()) {
            Node v = queue.removeFirst();
            // set node cluster id for node itself
            if (getNodeClusterId(v) == UNSEEN_CLUSTER) {
                setNodeClusterId(v, newClusterId);
                cluster.addNode(v);
                cluster.setName("Cluster " + Integer.toString(newClusterId + 1));
                if (prevClusterColumn != null) {
                    int prev = getNodeClusterPrevId(v);
                }
            }

            // set node cluster id for all its neighbors
            for (Node n : tempGraph.getNeighbors(v).toArray()) {
                if (getNodeClusterId(n) != newClusterId) {
                    queue.add(n);
                    setNodeClusterId(n, newClusterId);
                    cluster.addNode(n);
                }
            }
        }
        return cluster;
    }

    public void setPreferredNumberOfClusters(int clusters) {
        preferred = clusters;
    }

    private int findNearest(Vector<Integer> clusterCounts) {
        if (clusterCounts.contains(this.preferred)) {
            return this.preferred;
        }

        int bestDistance = Integer.MAX_VALUE;
        int nearest = -1;

        for (int i : clusterCounts) {
            int d = Math.abs(this.preferred - i);
            if (d < bestDistance) {
                nearest = i;
                bestDistance = d;
            }
        }
        return nearest;
    }

    /**
     * @return the clusterCounts
     */
    public Vector<Integer> getClusterCounts() {
        return clusterCounts;
    }
}
