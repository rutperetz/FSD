
import MatchRound from "../models/MatchRound.model.js";
import Group from "../models/Group.model.js";

/**
 * פונקציה זמנית שעובדת בשיטה של MVP(גרסה מינימלית)  שיוצר קבוצות באופן אקראי עד לשלב יצירת אלגוריתם שאלון
 */
function shuffle(arr) {
  const a = [...arr];
  for (let i = a.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [a[i], a[j]] = [a[j], a[i]];
  }
  return a;
}

/**
 * POST /api/match-rounds
 * Body: { courseId, groupSize, studentIds }
 *
 * Creates a new match round and creates draft groups with "pending" decisions.
 * MVP version: random grouping (no questionnaire scoring yet).
 */
export async function createMatchRound(req, res) {
  try {
    const { courseId, groupSize, studentIds } = req.body;

    
    if (!courseId) return res.status(400).json({ error: "courseId is required" });

    const size = Number(groupSize);
    if (!size || size < 2) {
      return res.status(400).json({ error: "groupSize must be >= 2" });
    }

    if (!Array.isArray(studentIds) || studentIds.length < size) {
      return res.status(400).json({ error: "studentIds must include enough ids" });
    }

    
    const existingCount = await MatchRound.countDocuments({ courseId });
    const roundNumber = existingCount + 1;

   
    const round = await MatchRound.create({
      courseId,
      roundNumber,
      status: "processing",
      groupIds: [],
      createdAt: new Date(),
    });

    // Shuffle students and split into chunks of groupSize
    const shuffled = shuffle(studentIds);

    const groupsToCreate = [];
    for (let i = 0; i < shuffled.length; i += size) {
      const chunk = shuffled.slice(i, i + size);

     
      const members = chunk.map((id) => ({
        studentId: id,
        decision: "pending", // pending | approved | rejected
      }));

      groupsToCreate.push({
        courseId,
        roundId: round._id,
        status: "draft", // draft | active | closed (optional)
        members,
        createdAt: new Date(),
      });
    }

    //  Insert all groups
    const createdGroups = await Group.insertMany(groupsToCreate);

    // Update round with group IDs and mark completed
    round.groupIds = createdGroups.map((g) => g._id);
    round.status = "completed";
    await round.save();

    return res.status(201).json({
      roundId: round._id,
      roundNumber: round.roundNumber,
      status: round.status,
      groupIds: round.groupIds,
      groupsCreated: createdGroups.length,
      groups: createdGroups, // useful for client immediately
    });
  } catch (err) {
    console.error("createMatchRound error:", err);
    return res.status(500).json({ error: "Server error", details: err.message });
  }
}

/**
 * GET /api/match-rounds/:roundId/groups
 * Returns all groups for a specific match round.
 */
export async function getGroupsByRound(req, res) {
  try {
    const { roundId } = req.params;

    if (!roundId) return res.status(400).json({ error: "roundId is required" });

    const groups = await Group.find({ roundId }).lean();

    return res.json({ roundId, groupsCount: groups.length, groups });
  } catch (err) {
    console.error("getGroupsByRound error:", err);
    return res.status(500).json({ error: "Server error", details: err.message });
  }
}

/**
 * PATCH /api/groups/:groupId/decision
 * Body: { studentId, decision }
 * decision: "accepted" | "rejected" | "pending"
 */
export async function updateMemberDecision(req, res) {
  try {
    const { groupId } = req.params;
    const { studentId, decision } = req.body;

    if (!groupId) return res.status(400).json({ error: "groupId is required" });
    if (!studentId) return res.status(400).json({ error: "studentId is required" });

    const allowed = new Set(["pending", "approved", "rejected"]);
    if (!allowed.has(decision)) {
      return res
        .status(400)
        .json({ error: `decision must be one of: ${Array.from(allowed).join(", ")}` });
    }

    const group = await Group.findById(groupId);
    if (!group) return res.status(404).json({ error: "Group not found" });

    const member = group.members.find(
      (m) => String(m.studentId) === String(studentId)
    );
    if (!member) {
      return res.status(404).json({ error: "Member not found in this group" });
    }

    member.decision = decision;

    await group.save();

    return res.json({
      ok: true,
      groupId: group._id,
      updatedStudentId: studentId,
      decision,
      group,
    });
  } catch (err) {
    console.error("updateMemberDecision error:", err);
    return res.status(500).json({ error: "Server error", details: err.message });
  }
}
