import { initializeApp } from "https://www.gstatic.com/firebasejs/10.12.0/firebase-app.js";
import { getAuth, onAuthStateChanged } from "https://www.gstatic.com/firebasejs/10.12.0/firebase-auth.js";
import { getFirestore } from "https://www.gstatic.com/firebasejs/10.12.0/firebase-firestore.js";

const firebaseConfig = {
  apiKey: "AIzaSyBJjNhYqY77F5DQd-WManPuup-rE4QQ9kQ",
  authDomain: "biolockweb.firebaseapp.com",
  projectId: "biolockweb",
  storageBucket: "biolockweb.appspot.com",
  messagingSenderId: "350147918503",
  appId: "1:350147918503:web:f5967c386c6e1886db7982",
  measurementId: "G-KQHMHMPPVR"
};

const app = initializeApp(firebaseConfig);
const auth = getAuth(app);
const db = getFirestore(app);

// Export them so other modules can use
export { app, auth, db, onAuthStateChanged };

// ✅ Make them global
window.auth = auth;
window.db = db;
window.onAuthStateChanged = onAuthStateChanged;
