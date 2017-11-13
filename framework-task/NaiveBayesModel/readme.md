# Naive Bayes Model

# Features
+ Calculate the classification score with given data

# How to Use 
## Algorithm Parameters Setting
+ Describe "Input Parameters" in Json string  
  + "kernel" : Kernel function
    + "type" : Type of kernel function (gaussian, exponential, kernel_density_est)
    + "bandwidth" : Bandwidth value for the kernel density estimation type of kernel
  + "classInfo" : Information about the classes
    + "labels" : Labels for each classes
    + "possibilities" : Value of possibility on each classes    
    + "rVectors" : Support Vectors for each classes
    + "means" : Value of means for each classes
    + "variances" : Value of variances for each classes
  + "target"    : Name of target (independent) variables
  + "outputKey" : Name of keys for outputs (The key and its value will be inserted in to the "record")


```json
{
    "jobs" : [
        {
            "input" : [],
            "output": [],
            "task" : [
                {
                    "name": "NaiveBayes",
                    "params" : {
                        "kernel"    : {
                            "type"  : "gaussian",
                            "bandwidth"  :  5.5
                        },
                        "classInfo" : {
                            "labels": ["1", "2"],
                            "possibilities"   : [0.5, 0.5],
                            "rVectors" : [
                                [1.9, 2.2, 3.3],
                                [1.9, 2.2, 3.3]
                            ],
                            "means" : [
                                [1.9, 2.2, 3.3],
                                [1.9, 2.2, 3.3]
                            ],
                            "variances" : [
                                [1.9, 2.2, 3.3],
                                [1.9, 2.2, 3.3]
                            ]
                        }
                    },
                    "inrecord"    : ["/records/A", "/records/B"],
                    "outrecord" : ["/Out"]
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
        {"A" : 1.1, "B": 2.2, "C": 3.3},
        {"A" : 1.2, "B": 2.1, "C": 3.2},
        {"A" : 1.3, "B": 2.0, "C": 3.0},
        {"A" : 1.2, "B": 2.1, "C": 3.2}
    ]
}
```
### Output Sample
+ Case 1 : "outrecord" : ["/records/*/Out"]
```json
{
    "records"    : [
        {"A" : 1.1, "B": 2.2, "C": 3.3, "Out" : "class1"},
        {"A" : 1.2, "B": 2.1, "C": 3.2, "Out" : "class1"},
        {"A" : 1.3, "B": 2.0, "C": 3.0, "Out" : "class2"},
        {"A" : 1.2, "B": 2.1, "C": 3.2, "Out" : "class2"}
    ]
}
```
+ Case 2 : "outrecord" : ["/Out"]
```json
{
    "records"    : [
        {"A" : 1.1, "B": 2.2, "C": 3.3},
        {"A" : 1.2, "B": 2.1, "C": 3.2},
        {"A" : 1.3, "B": 2.0, "C": 3.0},
        {"A" : 1.2, "B": 2.1, "C": 3.2}
    ],
    "Out" : ["class1","class1","class2","class2"]
}
```
# Sequence Diagram (TBD)
