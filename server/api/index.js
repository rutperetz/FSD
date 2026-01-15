const express = require("express");
const cors = require("cors");

const app = express();
app.use(cors());
app.use(express.json());

app.get("/", (req, res) => {
  res.status(200).send("SmartGroup API is running. Try /ping");
});

// בדיקת חיים
app.get("/ping", (req, res) => {
  res.status(200).send("pong");
});

const port = process.env.PORT || 8080;
app.listen(port, () => {
  console.log("API running on port", port);
});
