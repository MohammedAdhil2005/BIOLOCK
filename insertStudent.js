const connectDB = require("./db");

async function run() {
  const db = await connectDB();

  const studentsCollection = db.collection("students");

  const newStudent = {
    studentId: "S1001",
    name: "Mohammed Adhil",
    course: "Computer Science",
    year: 3,
    contact: "9876543210"
  };

  const result = await studentsCollection.insertOne(newStudent);
  console.log("✅ Student Inserted:", result.insertedId);
}

run().catch(console.error);
