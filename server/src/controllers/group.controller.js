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
