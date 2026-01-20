const computeCompatibility = require('../similarityScore.js');

function buildSimilarityMatrix(vectors, weights, schema) {
    const n = vectors.length;

    //empty matrix
    const matrix = Array.from({ length: n }, () => Array(n).fill(0));

    
    for (let i = 0; i < n; i++) {
        for (let j = i + 1; j < n; j++) {

            // Similarity score
            const score = computeCompatibility(
                vectors[i],
                vectors[j],
                weights,
                schema
            );

            matrix[i][j] = score;
            matrix[j][i] = score;
        }
    }

    return matrix ;
}
module.exports = buildSimilarityMatrix;