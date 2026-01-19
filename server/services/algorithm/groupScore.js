// ---------------------------
// Group score
// ---------------------------
function computeGroupScore(groupIndices, matrix) {
    let sum = 0;
    let count = 0;

    for (let i = 0; i < groupIndices.length; i++) {
        for (let j = i + 1; j < groupIndices.length; j++) {
            sum += matrix[groupIndices[i]][groupIndices[j]];
            count++;
        }
    }

    return count === 0 ? 0 : sum / count;
}
module.exports = computeGroupScore;