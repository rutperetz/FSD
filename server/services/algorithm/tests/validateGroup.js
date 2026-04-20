function validateGroups(result, minSize, maxSize) {

    const seen = new Set();

    result.matchGroups.forEach(group => {

        const members = group.memberIds;

        if (members.length < minSize || members.length > maxSize) {
            throw new Error("Invalid group size");
        }

        members.forEach(s => {

            if (seen.has(s))
                throw new Error("Student appears twice");

            seen.add(s);

        });

    });

    console.log("Groups structure OK");
}

module.exports = validateGroups;