import Group from "../models/Group.model.js";

export async function getGroupById(req, res) {
  try {
    const { groupId } = req.params;

    if (!groupId) {
      return res.status(400).json({ error: "groupId is required" });
    }

    const group = await Group.findById(groupId)
      .populate("members.studentId", "name email")
      .lean();

    if (!group) {
      return res.status(404).json({ error: "Group not found" });
    }

    return res.json(group);
  } catch (err) {
    console.error("getGroupById error:", err);
    return res.status(500).json({ error: "Server error" });
  }
}

// GET /api/groups/by-student/:studentId
export async function getGroupsByStudent(req, res) {
  try {
    const { studentId } = req.params;
    if (!studentId) return res.status(400).json({ error: "studentId is required" });

    const groups = await Group.find({ "members.studentId": studentId })
      .sort({ createdAt: -1 })
      .lean();

    return res.json({ studentId, groupsCount: groups.length, groups });
  } catch (err) {
    console.error("getGroupsByStudent error:", err);
    return res.status(500).json({ error: "Server error", details: err.message });
  }
}

// PATCH /api/groups/:groupId/decision
export async function updateMemberDecision(req, res) {
  try {
    const { groupId } = req.params;
    const { studentId, decision, rejectionReason } = req.body;

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

    const member = group.members.find((m) => String(m.studentId) === String(studentId));
    if (!member) return res.status(404).json({ error: "Member not found in this group" });

    member.decision = decision;
    if (decision === "rejected") {
      member.rejectionReason = rejectionReason || "";
    } else {
      member.rejectionReason = undefined;
    }


    const allApproved = group.members.length > 0 && group.members.every((m) => m.decision === "approved");
    group.status = allApproved ? "final" : "draft";

    await group.save();

    return res.json({
      ok: true,
      groupId: group._id,
      updatedStudentId: studentId,
      decision,
      groupStatus: group.status,
      group,
    });
  } catch (err) {
    console.error("updateMemberDecision error:", err);
    return res.status(500).json({ error: "Server error", details: err.message });
  }
}
