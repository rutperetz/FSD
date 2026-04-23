// ------------------------------------------------------------
// Algorithm configuration
// Controls global behavior of the grouping algorithm:
//   - threshold: minimum group compatibility score
//   - weights: importance of each field in similarity calculation
//   - maxRounds: maximum number of grouping iterations allowed
// ------------------------------------------------------------

const algorithmConfig = {
        "threshold": 0.7,
        "weights": {
            "availability": 0.4,
            "workMode": 0.1,
            "workStyle": 0.2,
            "language": 0.15,
            "taskPreference": 0.15
    },
           "maxRounds": 3
}

module.exports = algorithmConfig;
