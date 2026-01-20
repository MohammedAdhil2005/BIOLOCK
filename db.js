const { MongoClient } = require('mongodb');

// Replace <password> with your actual MongoDB Atlas user password
const uri = "mongodb+srv://BiolockCluster:myadilat04102005@cluster0.jqtzlut.mongodb.net/?retryWrites=true&w=majority";

const client = new MongoClient(uri);

async function connectDB() {
  try {
    await client.connect();
    console.log("✅ Connected to MongoDB!");
    return client.db("BioLockDB"); // Name of your database
  } catch (err) {
    console.error("❌ Connection failed:", err);
  }
}

module.exports = connectDB;
