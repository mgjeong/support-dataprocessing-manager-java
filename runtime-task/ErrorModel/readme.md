# Regression Model

# Features
+ Calculate the regression score with given data
+ 2 types of algorithm supported 
  + Linear regression
  + Logistic regression  

# How to Use 
## Algorithm Parameters Setting
+ Describe "Input Parameters" in Json string 
  + "type"  <-- Type of error calculation (mse, rmse, mae, me)
  + "observation"  <-- Target value for which error should calculated with data with key name in the "inrecord"
  + "interval"  <-- windows size for error calculation

```json
{
    "jobs" : [
        {
            "input" : [],
            "output": [],
            "task" : [
                {
                    "name": "error",
                    "params": {
                        "type"      : "mse",
                        "observation"     : "/ACTUAL",
                        "interval"  : {
                            "data"  : 2
			}
                    },
                    "inrecord"    : ["/X1", "/X2"],
                    "outrecord" : ["/Yy1", "/Y2"]
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
- Case 1 : With single data : "inrecord" : ["/x1", "/x2"]
```json
{
    "ACTUAL" : 1.1, "X1": 2.2, "X2": 2.2
}
```

- With Set of data : "inrecord" : ["/records/x1", "/records/x2"]
```json
{
    "records"    : [
        {"ACTUAL" : 1.1, "X1": 2.2, "X2": 2.2},
        {"ACTUAL" : 1.2, "X1": 2.1, "X2": 2.2},
        {"ACTUAL" : 1.3, "X1": 2.0, "X2": 2.2},
        {"ACTUAL" : 1.2, "X1": 2.1, "X2": 2.2}
    ]
}
```
### Output Sample
+ Case 1 : "outrecord" : ["/out"]
```json
{
    "A" : 1.1, "B": 2.2, "out" : 1
}
```
+ Case 2-1 : "outrecord" : ["/records/*/out"]
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
+ Case 2-2 : "outrecord" : ["/out"]
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
