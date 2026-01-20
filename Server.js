const express = require("express");
const bodyParser = require("body-parser");
const cors = require("cors");
const mongoose = require("mongoose");

const app = express();
app.use(cors());
app.use(bodyParser.json());

// ====================
// Connect to Local MongoDB
// ====================
mongoose.connect(
  "mongodb://localhost:27017/biolockdb",
  { useNewUrlParser: true, useUnifiedTopology: true }
)
.then(() => console.log("✅ MongoDB Connected!"))
.catch(err => console.error("❌ MongoDB connection error:", err));

// ====================
// Schemas & Models
// ====================
const studentSchema = new mongoose.Schema({
  studentId: String,
  fullName: String,
  aadharNumber: String,
  contactNumber: String,
  dob: Date,
  collegeName: String,
  course: String,
  year: Number,
  grade: String,
  address: String,
  uid: String
}, { timestamps: true });
const Student = mongoose.model("Student", studentSchema);

const employeeSchema = new mongoose.Schema({
  employeeId: String,
  fullName: String,
  companyName: String,
  designation: String,
  experience: Number,
  contactNumber: String,
  address: String,
  uid: String
}, { timestamps: true });
const Employee = mongoose.model("Employee", employeeSchema);

const businessSchema = new mongoose.Schema({
  businessId: String,
  businessName: String,
  industryType: String,
  established: Date,
  address: String,
  uid: String
}, { timestamps: true });
const Business = mongoose.model("Business", businessSchema);

const customSchema = new mongoose.Schema({
  recordTitle: String,
  fields: [{ label: String, value: String }],
  uid: String
}, { timestamps: true });
const Custom = mongoose.model("Custom", customSchema);

const healthcareSchema = new mongoose.Schema({
  recordId: String,
  patientName: String,
  aadharNumber: String,
  hospital: String,
  diagnosis: String,
  doctor: String,
  date: Date,
  contactNumber: String,
  uid: String
}, { timestamps: true });
const Healthcare = mongoose.model("Healthcare", healthcareSchema);

const educationSchema = new mongoose.Schema({
  recordId: String,
  studentName: String,
  institution: String,
  course: String,
  marks: String,
  yearOfPassing: Number,
  grade: String,
  uid: String
}, { timestamps: true });
const Education = mongoose.model("Education", educationSchema);

// ====================
// POST APIs
// ====================
app.post("/students", async (req, res) => {
  try {
    const s = new Student(req.body);
    const saved = await s.save();
    res.json({ success: true, student: saved });
  } catch (err) {
    res.status(500).json({ success: false, message: err.message });
  }
});

app.post("/employees", async (req, res) => {
  try {
    const e = new Employee(req.body);
    const saved = await e.save();
    res.json({ success: true, employee: saved });
  } catch (err) {
    res.status(500).json({ success: false, message: err.message });
  }
});

app.post("/business", async (req, res) => {
  try {
    const b = new Business(req.body);
    const saved = await b.save();
    res.json({ success: true, business: saved });
  } catch (err) {
    res.status(500).json({ success: false, message: err.message });
  }
});

app.post("/custom", async (req, res) => {
  try {
    const c = new Custom(req.body);
    const saved = await c.save();
    res.json({ success: true, custom: saved });
  } catch (err) {
    res.status(500).json({ success: false, message: err.message });
  }
});

app.post("/healthcare", async (req, res) => {
  try {
    const h = new Healthcare(req.body);
    const saved = await h.save();
    res.json({ success: true, healthcare: saved });
  } catch (err) {
    res.status(500).json({ success: false, message: err.message });
  }
});

app.post("/education", async (req, res) => {
  try {
    const ed = new Education(req.body);
    const saved = await ed.save();
    res.json({ success: true, education: saved });
  } catch (err) {
    res.status(500).json({ success: false, message: err.message });
  }
});

// ====================
// GET APIs
// ====================
app.get("/students", async (req, res) => {
  try {
    const uid = req.query.uid;
    const s = await Student.find({ uid }).sort({ createdAt: -1 });
    res.json(s);
  } catch (err) {
    res.status(500).json({ success: false, message: err.message });
  }
});

app.get("/employees", async (req, res) => {
  try {
    const uid = req.query.uid;
    const e = await Employee.find({ uid }).sort({ createdAt: -1 });
    res.json(e);
  } catch (err) {
    res.status(500).json({ success: false, message: err.message });
  }
});

app.get("/business", async (req, res) => {
  try {
    const uid = req.query.uid;
    const b = await Business.find({ uid }).sort({ createdAt: -1 });
    res.json(b);
  } catch (err) {
    res.status(500).json({ success: false, message: err.message });
  }
});

app.get("/custom", async (req, res) => {
  try {
    const uid = req.query.uid;
    const c = await Custom.find({ uid }).sort({ createdAt: -1 });
    res.json(c);
  } catch (err) {
    res.status(500).json({ success: false, message: err.message });
  }
});

app.get("/healthcare", async (req, res) => {
  try {
    const uid = req.query.uid;
    const h = await Healthcare.find({ uid }).sort({ createdAt: -1 });
    res.json(h);
  } catch (err) {
    res.status(500).json({ success: false, message: err.message });
  }
});

app.get("/education", async (req, res) => {
  try {
    const uid = req.query.uid;
    const ed = await Education.find({ uid }).sort({ createdAt: -1 });
    res.json(ed);
  } catch (err) {
    res.status(500).json({ success: false, message: err.message });
  }
});

// ====================
// Start Server
// ====================
const PORT = 5000;
app.listen(PORT, () => console.log(`✅ Server running on http://localhost:${PORT}`));
