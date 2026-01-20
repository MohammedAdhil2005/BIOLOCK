const connectDB = require('./db');

async function test() {
  const db = await connectDB();
  console.log("DB object:", db);
  process.exit();
}

test();
