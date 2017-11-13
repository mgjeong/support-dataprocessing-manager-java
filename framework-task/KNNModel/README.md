# KNN (Kth Nearest Neighbor) Model

# Features
+ Find outlier(s) base on the distance between k-th nearest neighbors
+ Reference : http://teacher.nsrl.rochester.edu/phy_labs/AppendixB/AppendixB.html

# How to Use 
## Algorithm Parameters Setting
+ Describe "Input Parameters" in Json string
  + "distanceMeasure" : Type of distance measure (euclidean, manhattan, abs-relative)
  + "knn" : Number of nearest neighbors
  + "threshold" : Value of threshold for outlier detection (Score will be included as a result if not inserted)
  + "clusterVectors" : Vector points of the cluster
  + "target" <-- Name of keys for calculation (Length of the keys should be equal to that of clusterVector)
  + "outputKey" <-- Name of keys for outputs 
 
```json
{
    "jobs" : [
        {
            "input" : [],
            "output": [],
            "task" : [
                {
                    "name"      : "outlier_knn",
                    "params"    : {

                        "distanceMeasure" : "euclidean",
                        "knn": 2,
                        "threshold" : 2.0,
                        "clusterVectors" : [
                            [1.0, 1.1],
                            [2.0, 2.1],
                            [3.0, 3.1]
                        ]
                    },
                    "inrecord"    : ["/records/A", "/records/B"],
                    "outrecord" : ["/records/*/knn_score"]
                }
            ]
        }
    ]
}
```

+ Deliver "Input Parameters" along with Job creation Request (POST "/v{#}/job")
  + Note : Order of the values in the array type of parameters should be kept

## In/Output Stream Data Sample
### Input Sample
```json
{
    "records"    : [
        {"Time" : 1502323595, "KEY_A" : 1.0, "KEY_B": 2.0},
        {"Time" : 1502323595, "KEY_A" : 11.0, "KEY_B": 12.0}
    ]
}
```
### Output Sample
  
+ Case 1 : "outrecord" : ["/knn_score"]
```json
{
    "records"    : [
        {"Time" : 1502323595, "KEY_A" : 1.0, "KEY_B": 2.0},
        {"Time" : 1502323595, "KEY_A" : 11.0, "KEY_B": 12.0}
    ],
    "knn_score" : [        
        3.75720857287361,
        11.6617669294999997    
    ]
}
```
+ Case 2 : "outrecord" : ["/records/*/knn_score"]
```json
{
    "records"    : [
        {"Time" : 1502323595, "KEY_A" : 1.0, "KEY_B": 2.0, "knn_score" : 3.75720857287361},
        {"Time" : 1502323595, "KEY_A" : 11.0, "KEY_B": 12.0, "knn_score" : 11.6617669294999997}
    ]
}
```

# Sequence Diagram (TBD)
