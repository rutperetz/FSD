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

    // For each user, get sorted list of neighbors
    const neighbors = new Array(n);
    for (let i = 0; i < n; i++) {
        const row = matrix[i];

        neighbors[i] = row
            .map((score, idx) => ({ idx, score })) // Create array of {idx, score} pairs
            .filter(x => x.idx !== i && x.score > 0) // Exclude self and zero scores
            .sort((a, b) => b.score - a.score);// Sort by val descending

    }

    return { matrix, neighbors };
}
module.exports = buildSimilarityMatrix;