function roundManager(courseId,student,vectors,baseWeights,minSize,maxSize,roundNum) {
    const n = vectors.length;
    if (roundNum === 1) {
        id=courseId + roundNum;//??
        // 1. build similarity matrix
        let matrix_1 = buildSimilarityMatrix(vectors, baseWeights, schema);
     
        const used = new Array(n).fill(false);

        // 2. run window clustering
   
        let { groups_1, unassigned_1 } = windowClustering(vectors, matrix_1, minSize, maxSize, threshold, used);
        // 3. save round data
        matchRound.push({
            roundId: id,
            courseId,
            roundNumber: roundNum,
            matrix_1,
            groups: groups_1,
            unassigned: unassigned_1,
            baseWeights,
            feedback: {}
        });
    }
    else {
        //1. load previous round data
        const previousRound = matchRound.find(r => r.courseId === courseId && r.roundNumber === roundNum - 1);
        let { feedback, groups, baseWeights  } = previousRound;
        //2. process feedback
        const used = new Array(n).fill(false);
        let { matrix, lockedGroups, newWeights } = feedbackProcessor(groups, feedback, minSize, used, n);
        //3. run window clustering again
        let { newGroups, newUnassigned } = windowClustering(vectors, matrix, minSize, maxSize, threshold, used);
        //merge locked groups and new groups
        let finalGroups = lockedGroups.concat(newGroups);
//4. save round data
        id=courseId + roundNum;//??
        matchRound.push({
            roundId: id,
            courseId,
            roundNumber: roundNum,
            matrix,
            groups: finalGroups,
            unassigned: newUnassigned,
            newWeights,
            feedback: {}
        });
    }
    return matchRound
}