# Kmeans Clustering Model

# Features
+ Partition n observations into k clusters in which each observation belongs to the cluster with the nearest mean
+ Reference : https://en.wikipedia.org/wiki/K-means_clustering

# How to Use 
## Algorithm Parameters Setting
+ Describe "Input Parameters" in Json string
  + "cluster_num" : Number of clusters
  + "cluster_name" : Names of clusters
  + "trainInstances" : train data for clustering 
  + "centroids" : List of centroids of each clusters 
 
```json
{
    "cluster_num" : 3,
    "cluster_name": [
	"Iris-setosa",
        "Iris-versicolor",
        "Iris-virginica"
    ],
    "trainInstances" : [
        [5.1, 3.5, 1.4, 0.2],
        [4.9, 3.0, 1.4, 0.2],
        [4.7, 3.2, 1.3, 0.2],
        [4.6, 3.1, 1.5, 0.2],
        [5.0, 3.6, 1.4, 0.2],
        [5.4, 3.9, 1.7, 0.4],
        [4.6, 3.4, 1.4, 0.3],
        [5.0, 3.4, 1.5, 0.2],
        [4.4, 2.9, 1.4, 0.2]
    ],
    "centroids" : [
        [5.006000, 3.428000, 1.462000, 0.246000],
        [5.901613, 2.748387, 4.393548, 1.433871],
        [6.850000, 3.073684, 5.742105, 2.071053]        
    ],
    "inrecord" : ["records/A", "records/B", "records/C", "records/D"],
    "outrecord" : ["/cluster_name"]
}
```

+ Deliver "Input Parameters" along with Job creation Request (POST "/v{#}/job")
  + Note : Order of the values in the array type of parameters should be kept

## In/Output Stream Data Sample
### Input Sample
```json
{
    "records"    : [
        {"Time" : 1502323595, "A" : 5.1, "B": 3.5, "C": 1.4, "D": 0.2}
    ]
}
```
### Output Sample
  
```json
{
    "records"    : [
        {"Time" : 1502323595, "A" : 5.1, "B": 3.5, "C": 1.4, "D": 0.2}
    ],
    "cluster_name" : [        
        "Iris-setosa"
    ]
}
```

# Sequence Diagram (TBD)
