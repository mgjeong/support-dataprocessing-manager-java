# SVM(Support Vector Machine) Model

# Features
+ Calculate the classification score with given data
  + Based on the libsvm library(https://github.com/cjlin1/libsvm)
# How to Use 
## Algorithm Parameters Setting
+ Describe "Input Parameters" in Json string 
  + "type" : SVM type (C_SVC(0), NU_SVC(1), ONE_CLASS(2), EPSILON_SVR(3), NU_SVR($))
  + "kernel" : Kernel function
    + "type" : Type of kernel function (Linear, Polynomial, radial, sigmoid)
    + "degree" : Value of degree(for polynomial type)
    + "gamma" : Value of gamma(for all kernel types)
    + "coef0" : Value of coefficient(for polynomial & sigmoid type)
  +"classInfo" : Information about the classes
    + "labels" : Labels for each classes
    + "nSV" : Number of support vectors for each classes
    + "rho" : Rho value for each classes
    + "sVectors" : Support Vectors for each classes
    + "svCoef" : Coefficients for each SVs
  + "inrecord"    <-- Name of target (independent) variables
  + "outrecord" <-- Name of keys for outputs (The key and its value will be inserted in to the "record")


```json
{
    "jobs" : [
        {
            "input" : [],
            "output": [],
            "task" : [
                {
                    "name"      : "svm",
                    "params"    : {
                        "type"      : 1,
                        "kernel"    : {
                            "type"  : "linear",
                            "degree": 2,
                            "gamma" : 0.5,
                            "coeff0": 1.0
                        },
                        "classInfo" : {
                            "labels": ["1", "2"],
                            "nSV"   : [2,2],
                            "rho"   : [0.2, 0.1],
                            "sVectors" : [
                                [1.9, 2.2, 3.3],
                                [1.9, 2.2, 3.3]
                            ],
                            "svCoef" : [ 1.1, 2.2 ]
                        }
                    },
                    "inrecord"    : ["/records/A", "/records/B"],
                    "outrecord" : ["/records/*/Out"]
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
        {"A" : 1.1, "B": 2.2},
        {"A" : 1.2, "B": 2.1},
        {"A" : 1.3, "B": 2.0},
        {"A" : 1.2, "B": 2.1}
    ]
}
```
### Output Sample

```json
{
    "records"    : [
        {"A" : 1.1, "B": 2.2, "Out" : "class1"},
        {"A" : 1.2, "B": 2.1, "Out" : "class1"},
        {"A" : 1.3, "B": 2.0, "Out" : "class2"},
        {"A" : 1.2, "B": 2.1, "Out" : "class2"}
    ]
}
```

# Sequence Diagram (TBD)