# Regression Model

# Features
+ Calculate the regression score with given data
+ 2 types of algorithm supported 
  + Linear regression
  + Logistic regression  

# How to Use 
## Algorithm Parameters Setting
+ Describe "Input Parameters" in Json string 
  + "target"    <-- Name of target variables
  + "outputKey" <-- Name of keys for outputs (The key and its value will be inserted in to the "record")
  + "type"  <-- Type of regression (linear, logistic)
  + "weights"  <-- Weights for each independent variables
  + "error"  <-- Error value

```json
{
    "jobs" : [
        {
            "input" : [],
            "output": [],
            "task" : [
                {
                    "name": "regression",
                    "params": {
                        "type"      : "logistic",
                        "error"     : 0.5,
                        "weights"   : [1.0, 0.5]
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
+ Case 1 : "outrecord" : ["/records/*/Out"]
```json
{
    "records"    : [
        {"A" : 1.1, "B": 2.2, "Out" : 1},
        {"A" : 1.2, "B": 2.1, "Out" : 1},
        {"A" : 1.3, "B": 2.0, "Out" : 1},
        {"A" : 1.2, "B": 2.1, "Out" : 1}
    ]
}
```
+ Case 2 : "outrecord" : ["/Out"]
```json
{
    "records"    : [
        {"A" : 1.1, "B": 2.2},
        {"A" : 1.2, "B": 2.1},
        {"A" : 1.3, "B": 2.0},
        {"A" : 1.2, "B": 2.1}
    ],
    "Out" : [1, 1, 1, 1]
}
```
# Sequence Diagram (TBD)
