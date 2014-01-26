# Girvan Newman Clustering
Author: Jiri Krizek

Supervisor: Jaroslav Kuchar

The Girvan Newman Clustering plugin for <a href="http://www.gephi.org">Gephi</a>. 
This plugin finds clusters in graph, which can be used in Social Network Analysis. 

The Girvan–Newman algorithm detects communities by progressively removing edges from the original network. The connected components of the remaining network are the comunnities. Instead of trying to construct a measure that tells us which edges are the most central to communities, the Girvan–Newman algorithm focuses on edges that are most likely "between" communities.


## Tutorial
You can start cluster finding using "Clustering" panel. This panel is usually on the left part of Gephi window. 
If you don't see this panel, enable it using "Window/Clustering" from the main menu.

From dropdown menu, select **Girvan Newman** and start computation using **Run** button in the Clustering panel.

Then window will popup and you can select number of clusters. 

![gnparams](https://raw.github.com/jaroslav-kuchar/GirmanNewmanClustering/master/images/gn.png)

### Parameters
* algorithm has no parameters

## Gephi Toolkit
Algorithm can be used with Gephi toolkit, just use method `setPreferredNumberOfClusters(int clusters)` before calling method `execute(GraphModel gm)`

## License
The GPL version 3, http://www.gnu.org/licenses/gpl-3.0.txt
