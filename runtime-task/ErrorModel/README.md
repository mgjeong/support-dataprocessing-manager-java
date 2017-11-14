# Error Model

# Features
+ Calculate the error value
+ Two types of algorithms supported
  + MSE (Mean Square Error)
  + RMSE (Root Mean Square Error)
+ NOTE: The calculation will fail if the length of records inside the delivered data is smaller then that of pattern 

# How to Use 
## Algorithm Parameters Setting
+ Describe "Input Parameters" in Json string
  + "type" : Type of error calculation algorithms(mse, rmse)
  + "pattern" : Array of values which represents the expected pattern(array) for specific key value
    + ex) First Array represents the pattern for the "/records/KEY_A" key, and Second for the "/records/KEY_B"
  + "length" : Length of pattern(array)
  + "margin" : Marginal length of observed pattern(records inside the delivered data)
  + "default" : Value of error by default(for the case when not able to calculate error)
  + "inrecords" : Name of target (independent) variables
  + "outrecords" : Name of keys for outputs (The key and its value will be inserted in to the "record")
    + NOTE : If the # of outrecord is not equal to that of inrecord the name for the result will be as follows
    + FORMAT : outrecord[0] + "_" + # (ex: /error_1, /error_2)

 
```json
{
    "jobs" : [
        {
            "input" : [],
            "output": [],
            "task" : [
                {
                    "name"      : "error",
                    "params"    : {
                        "type" : "mse",
                        "pattern" : [
                            [ 1.0, 1.2, 1.25, 1.2 ],
                            [ 1.2, 1.0, 1.2, 1.0 ]
                         ],
                        "length" : 4,
                        "margin" : 2,
                        "default" : -1.0
                    },
                    "inrecord"  : ["/records/KEY_A", "/records/KEY_B"],
                    "outrecord" : ["/error"]
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
        {"KEY_A" : 1.0, "KEY_B": 2.0},
        {"KEY_A" : 1.1, "KEY_B": 2.2},
        {"KEY_A" : 1.2, "KEY_B": 2.1},
        {"KEY_A" : 1.3, "KEY_B": 2.0},
        {"KEY_A" : 1.2, "KEY_B": 2.1},
        {"KEY_A" : 1.1, "KEY_B": 2.0}
    ]
}
```
### Output Sample
 - There're 3 slots that we could look into to find expected pattern
 - The slot with the lowest error value can be interpreted as the place where expected pattern exist 
```json
{
    "records"    : [
        {"KEY_A" : 1.0, "KEY_B": 2.0},
        {"KEY_A" : 1.1, "KEY_B": 2.2},
        {"KEY_A" : 1.2, "KEY_B": 2.1},
        {"KEY_A" : 1.3, "KEY_B": 2.0},
        {"KEY_A" : 1.2, "KEY_B": 2.1},
        {"KEY_A" : 1.1, "KEY_B": 2.0}
    ],
    "error" : [3.3, 1.3, 2.3]
}
```

# Sequence Diagram (TBD)
