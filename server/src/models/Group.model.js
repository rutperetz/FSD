import mongoose from "mongoose";

const groupMemberSchema = new mongoose.Schema({
  studentId: {
    type: mongoose.Schema.Types.ObjectId,
    ref: "User",
    required: true,
  },

  decision: {
    type: String,
    enum: ["pending", "approved", "rejected"],
    default: "pending",
  },

  rejectionReason: {
    type: String,
  },
});

const groupSchema = new mongoose.Schema(
  {
    courseId: {
      type: mongoose.Schema.Types.ObjectId,
      ref: "Course",
      required: true,
    },

    roundId: {
      type: mongoose.Schema.Types.ObjectId,
      ref: "MatchRound",
      required: true,
    },

    status: {
      type: String,
      enum: ["draft", "final"],
      default: "draft",
    },

    members: [groupMemberSchema],
  },
  {
    timestamps: true,
  }
);

export default mongoose.model("Group", groupSchema);
