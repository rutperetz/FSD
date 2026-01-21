const express = require("express");
const cors = require("cors");
const { db } = require("../firebaseAdmin");

const app = express();
app.use(cors());
app.use(express.json());

// בדיקת שרת
app.get("/", (req, res) => {
  res.status(200).send("SmartGroup API is running. Try /ping");
});

// בדיקת חיים
app.get("/ping", (req, res) => {
  res.status(200).send("pong");
});

// 🔥 בדיקת חיבור ל-Firestore
app.get("/firestore-test", async (req, res) => {
  try {
    const ref = await db.collection("server_tests").add({
      message: "Hello from Render server",
      createdAt: new Date().toISOString(),
    });

    res.status(200).json({
      ok: true,
      documentId: ref.id,
    });
  } catch (err) {
    console.error("Firestore error:", err);
    res.status(500).json({
      ok: false,
      error: err.message,
    });
  }
});

const port = process.env.PORT || 8080;
app.listen(port, () => {
  console.log("API running on port", port);
});
