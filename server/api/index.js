// server/index.js

const express = require("express");
const cors = require("cors");

const { db } = require("./firebaseAdmin");

const app = express();
app.use(cors());
app.use(express.json());

// דף בית
app.get("/", (req, res) => {
  res.status(200).send("SmartGroup API is running. Try /ping");
});

// בדיקת חיים
app.get("/ping", (req, res) => {
  res.status(200).send("pong");
});

// ✅ בדיקה ש-Firestore מחובר ועובד
// ייצור מסמך חדש באוסף server_tests בכל פעם שקוראים לנתיב הזה
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
      error: err?.message || String(err),
    });
  }
});

// ===== כאן את מוסיפה את ה־REGISTER =====

app.post("/register", async (req, res) => {
  try {
    const { fullName, email, phone } = req.body;

    if (!fullName || !email) {
      return res
        .status(400)
        .json({ ok: false, message: "חסרים fullName או email" });
    }

    const ref = await db.collection("users").add({
      fullName,
      email,
      phone: phone || "",
      createdAt: new Date().toISOString(),
    });

    res.status(201).json({ ok: true, id: ref.id });
  } catch (err) {
    console.error("Register error:", err);
    res.status(500).json({ ok: false });
  }
});

// ===== סוף ה־ROUTES =====



// חובה ל-Render: להשתמש ב-PORT שהוא נותן
const port = process.env.PORT || 8080;
app.listen(port, () => {
  console.log("API running on port", port);
});
