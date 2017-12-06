# Moving Average

# Features
- Calculate the trend with given data
+ 4 types of algorithm supported 
  + Simple Moving Average
  + Weighted Moving Average
  + Exponential Moving Average(EMA)
  + Auto Regressive Moving Average(ARMA)  

# How to Use 
## Algorithm Parameters Setting
+ Describe "Input Parameters" in Json string 
  + "target"    <-- Name of target variables
  + "outputKey" <-- Name of keys for outputs (Length should be that same as target)
  + "interval"  <-- Interval of Windowing
    + "data"    <-- Length of Window
    + "time"    <-- Length of Time(Not supported yet)
  + "alpha"  <-- Smoothing factor for EMA (value will be automatically calculated if not described in case of EMA)
  + "ma"  <-- Moving Average Weights for WMA, ARMA
  + "ar"  <-- Auto Regression Weights for ARMA
  + "lags"  <-- Lagging Weights for ARMA

```json
{
    "target"    : ["A", "B"],
    "outputKey" : ["MA_A", "MA_B"],
    "interval"  : {
        "data"  : 5 
    },
    "alpha"     : 0.5,
    "ma"        : {
        "coefficients" : [1.0, 0.5]
    },
    "ar"        : {
        "coefficients" : [1.0, 0.5]
    },
    "lags"        : {
        "coefficients" : [1.0, 0.5]
    }
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
+ Assuming Window Length == 3
```json
{
    "records"    : [
        {"A" : 1.1, "B": 2.2},
        {"A" : 1.2, "B": 2.1},
        {"A" : 1.3, "B": 2.0},
        {"A" : 1.2, "B": 2.1}
    ],
    "MA_A" : [1.2, 1,2],
    "MA_B" : [2.1, 2.15]
}
```

# Sequence Diagram (TBD)
