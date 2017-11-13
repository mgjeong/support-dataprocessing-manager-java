# Arithmetic Operation Model

# Features
+ Proceed the arithmetic operations
+ Five types of operation supported
  + Addition(add, +)
  + Subtraction(sub, -)
  + Multiplication(mul, *)
  + Division(div,/)
  + Modulo(mod,%)  
# How to Use 
## Algorithm Parameters Setting
+ Describe "Input Parameters" in Json string
  + "operations" : List of operations
  + "type" : Type of operand(single or array variable)
  + "leftOperand" : Name of left operand key
  + "rightOperand" : Name of right operand key
  + "outputKey" <-- Name of keys for outputs 
 
```json
{
    "jobs" : [
        {
            "input" : [],
            "output": [],
            "task" : [
                {
                    "name"      : "arithmetic",
                    "params"    : {
                        "operations" : [
                            {
                                "leftOperand": "/KEY_A",
                                "rightOperand": "/KEY_B",
                                "operator" : "add",
                                "type" : "array",
                                "outputKey": "/add"
                            }
                        ]
                    },
                    "inrecord"    : [],
                    "outrecord" : []
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
        {"KEY_A" : 1.1, "KEY_B": 2.2},
        {"KEY_A" : 1.2, "KEY_B": 2.1},
        {"KEY_A" : 1.3, "KEY_B": 2.0},
        {"KEY_A" : 1.2, "KEY_B": 2.1}
    ]
}
```
### Output Sample

```json
{
    "records"    : [
        {"KEY_A" : 1.1, "KEY_B": 2.2},
        {"KEY_A" : 1.2, "KEY_B": 2.1},
        {"KEY_A" : 1.3, "KEY_B": 2.0},
        {"KEY_A" : 1.2, "KEY_B": 2.1}
    ],
    "add" : [3.3, 3.3, 3.3, 3.3]
}
```

# Sequence Diagram (TBD)