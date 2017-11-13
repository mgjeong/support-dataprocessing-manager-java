# Linear (Interpolation) Model

# Features
+ Generate number of points between two time-series points
+ NOTE: The calculation will only be proceed after it receives two points (2 length windowing) 
+ Reference : https://en.wikipedia.org/wiki/Linear_interpolation

# How to Use 
## Algorithm Parameters Setting
+ Describe "Input Parameters" in Json string
  + "pointKey" : Name of time-stamp variable
  + "target" : Name of target (independent) variable key
  + "period" : Number of inter point to generate
  + "outputKey" <-- Name of keys for outputs 
 
```json
{
    "pointKey" : "Time",
    "target": ["KEY_A"],    
    "period" : 5,
    "outputKey" : ["IP_KEY_A"]
}
```

+ Deliver "Input Parameters" along with Job creation Request (POST "/v{#}/job")
  + Note : Order of the values in the array type of parameters should be kept

## In/Output Stream Data Sample
### Input Sample
```json
{
    "records"    : [
        {"Time" : 1502323595, "KEY_A" : 618.0, "KEY_B": 2.0},
        {"Time" : 1502323605, "KEY_A" : 615.1, "KEY_B": 2.2},
        {"Time" : 1502323615, "KEY_A" : 617.2, "KEY_B": 2.1}
    ]
}
```
### Output Sample
 - There're 3 slots that we could look into to find expected pattern
 - The slot with the lowest error value can be interpreted as the place where expected pattern exist 
```json
{
    "records"    : [
        {"Time" : 1502323595, "KEY_A" : 618.0, "KEY_B": 2.0},
        {"Time" : 1502323605, "KEY_A" : 615.1, "KEY_B": 2.2},
        {"Time" : 1502323615, "KEY_A" : 617.2, "KEY_B": 2.1}
    ],
    "IP_KEY_A" : [
        {"Time" : 1502323595, "KEY_A" : 618.0, "KEY_B": 2.0},
        {"Time" : 1502323597, "KEY_A" : 617.6, "KEY_B": 2.0},
        {"Time" : 1502323599, "KEY_A" : 617.2, "KEY_B": 2.0},
        {"Time" : 1502323601, "KEY_A" : 616.8, "KEY_B": 2.0},
        {"Time" : 1502323603, "KEY_A" : 615.4, "KEY_B": 2.0},
        {"Time" : 1502323605, "KEY_A" : 615.1, "KEY_B": 2.0},
        {"Time" : 1502323607, "KEY_A" : 615.5, "KEY_B": 2.0},
        {"Time" : 1502323609, "KEY_A" : 615.9, "KEY_B": 2.0},
        {"Time" : 1502323611, "KEY_A" : 616.3, "KEY_B": 2.0},
        {"Time" : 1502323613, "KEY_A" : 616.7, "KEY_B": 2.0},
        {"Time" : 1502323615, "KEY_A" : 617.2, "KEY_B": 2.0}
    ]
}
```

# Sequence Diagram (TBD)
