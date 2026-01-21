const express = require("express");
const cors = require("cors");
const db = require("./firebaseAdmin");

const app = express();
app.use(cors());
app.use(express.json());

app.get("/", (req, res) => {
  res.status(200).send("SmartGroup API is running. Try /ping");
});

app.get("/ping", (req, res) => {
  res.status(200).send("pong");
});

// בדיקת חיבור ל-Firestore
app.get("/firestore-test", async (req, res) => {
  try {
    const snapshot = await db.collection("test").get();
    res.status(200).json({
      message: "Firestore connected ✅",
      docsCount: snapshot.size,
    });
  } catch (err) {
    console.error(err);
    res.status(500).json({
      error: "Firestore connection failed ❌",
    });
  }
});

const port = process.env.PORT || 8080;
app.listen(port, () => {
  console.log("API running on port", port);
});
