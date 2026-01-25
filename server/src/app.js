import express from "express";
import mongoose from "mongoose";
import cors from "cors";
import dotenv from "dotenv";
import groupRoutes from "./routes/group.routes.js";
import matchRoundRoutes from "./routes/matchRound.routes.js";

dotenv.config();

const app = express();

// middlewares
app.use(cors());
app.use(express.json());

// test route
app.get("/api/health", (req, res) => {
  res.json({ ok: true, message: "Server is running" });
});

app.use("/api/groups", groupRoutes);
app.use("/api/match-rounds", matchRoundRoutes);


// port
const PORT = process.env.PORT || 3000;

// database connection + server start
mongoose.connect(process.env.MONGO_URI)
  .then(() => {
    console.log("MongoDB connected");
    app.listen(PORT, () => {
      console.log(`Server running on port ${PORT}`);
    });
  })
  .catch((err) => {
    console.error("MongoDB connection error:", err.message);
  });
