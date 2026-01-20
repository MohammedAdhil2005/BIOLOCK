import { auth, db } from './firebase-config.js';
import { createUserWithEmailAndPassword, signInWithEmailAndPassword, updateProfile, onAuthStateChanged } 
  from "https://www.gstatic.com/firebasejs/10.12.0/firebase-auth.js";
import { setDoc, doc } 
  from "https://www.gstatic.com/firebasejs/10.12.0/firebase-firestore.js";

const BASE_BACKEND_URL = "http://localhost:8081";

let video, canvas, scanBtn;
let cameraStream = null;

window.addEventListener('DOMContentLoaded', () => {
  document.querySelector('.signin')?.classList.add('active');
  document.querySelector('.signup')?.classList.add('active');
});

// Start Camera
async function startCamera() {
  try {
    console.log("🔄 Requesting camera access...");
    cameraStream = await navigator.mediaDevices.getUserMedia({
      video: { facingMode: "user" }
    });
    video.srcObject = cameraStream;
    await video.play();
    console.log("📷 Camera started successfully");

    // Live zoom preview
    video.style.transform = "scale(2)"; // 2x zoom live
    video.style.objectFit = "cover";

  } catch (err) {
    alert("📵 Camera access denied.");
    console.error("Camera Error:", err);
  }
}

// Stop Camera
function stopCamera() {
  if (cameraStream) {
    cameraStream.getTracks().forEach((track) => track.stop());
    cameraStream = null;
    video.srcObject = null;
  }
}

// Capture Retina Image
function captureRetinaImage() {
  if (!video || video.readyState < 2) {
    alert("📷 Camera not ready!");
    return null;
  }

  canvas.width = video.videoWidth;


  
  canvas.height = video.videoHeight;
  const ctx = canvas.getContext("2d");

  ctx.drawImage(video, 0, 0, canvas.width, canvas.height);

  const retinaImageData = canvas.toDataURL("image/png");
  console.log("📸 Retina image captured:", retinaImageData);
  stopCamera();

  const isSignUp = document.getElementById("signupForm") !== null;
  const preview = document.getElementById(
    isSignUp ? "retinaPreviewSignUp" : "retinaPreviewSignIn"
  );
  if (preview) {
    preview.src = retinaImageData;
    preview.style.display = "block";
    preview.style.transform = "scale(2)";
    preview.style.objectFit = "cover";
    preview.classList.add("retina-preview");
    video.style.display = "none";
  }

  return retinaImageData;
}

// ✅ Validation
function isSignUpFormValid() {
  const username = document.getElementById("signupUsername")?.value.trim();
  const email = document.getElementById("signupEmail")?.value.trim();
  const password = document.getElementById("signupPassword")?.value.trim();
  const confirm = document.getElementById("confirmPassword")?.value.trim();

  if (!username || !email || !password || !confirm) {
    alert("⚠️ All fields are required.");
    return false;
  }
  if (password !== confirm) {
    alert("❌ Passwords do not match.");
    return false;
  }
  return true;
}

function isSignInFormValid() {
  const username = document.getElementById("loginUsername")?.value.trim();
  const email = document.getElementById("loginEmail")?.value.trim();
  const password = document.getElementById("loginPassword")?.value.trim();

  if (!username || !email || !password) {
    alert("⚠️ Please fill in all fields.");
    return false;
  }
  return true;
}

// ✅ Signup
async function signUpUser(username, email, password, retinaData) {
  const safeUsername = username.toLowerCase().replace(/[^a-z0-9]/g, "_");

  try {
    const res = await fetch(`${BASE_BACKEND_URL}/save-retina`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        username: safeUsername,
        email: email,
        image: retinaData.replace(/^data:image\/png;base64,/, "")
      })
    });

    const result = await res.text();
    console.log("Backend Response:", result);

    const userCredential = await createUserWithEmailAndPassword(auth, email, password);
    const user = userCredential.user;

    await setDoc(doc(db, "users", user.uid), {
      username,
      email
    });

    alert("✅ Account created successfully!");
    
    localStorage.setItem("biolockUser", JSON.stringify({
    uid: user.uid,
    email: user.email,
    username: username
    }));

    // ✅ Immediately update sidebar
const sidebarName = document.getElementById("sidebar-name");
if (sidebarName) sidebarName.textContent = username;

const sidebarEmail = document.getElementById("sidebar-email");
if (sidebarEmail) sidebarEmail.textContent = email;

const sidebarLogo = document.getElementById("sidebar-logo");
if (sidebarLogo) {
  const nameForLogo = username || email || "U";
  sidebarLogo.textContent = nameForLogo[0].toUpperCase();
}

    redirectToDashboard();

  } catch (err) {
    console.error("Signup Error:", err);
    alert("❌ Signup Failed: " + err.message);
  }
}

// ✅ Sign In
async function signInUserWithRetina(username, email, retinaData) {
  const safeUsername = username.toLowerCase().replace(/[^a-z0-9]/g, "_");
  const safeEmail = email.toLowerCase().replace(/[^a-z0-9]/g, "_"); // normalize email

  try {
    const res = await fetch(`${BASE_BACKEND_URL}/match-retina`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        username: safeUsername,
        email: safeEmail, // ✅ include email
        image: retinaData.replace(/^data:image\/png;base64,/, "")
      })
    });

    const data = await res.json();

    if (data.match === true) {
      alert("🔐 Retina match success!");

      const firebaseEmail = document.getElementById("loginEmail").value.trim();
      const password = document.getElementById("loginPassword").value.trim();

      try {
        const userCredential = await signInWithEmailAndPassword(auth, firebaseEmail, password);
        const user = userCredential.user;

        localStorage.setItem("biolockUser", JSON.stringify({
          uid: user.uid,
          email: user.email,
          username: username // optional
        }));

        // ✅ Update sidebar
        const sidebarName = document.getElementById("sidebar-name");
        if (sidebarName) sidebarName.textContent = username;

        const sidebarEmail = document.getElementById("sidebar-email");
        if (sidebarEmail) sidebarEmail.textContent = email;

        const sidebarLogo = document.getElementById("sidebar-logo");
        if (sidebarLogo) {
          const nameForLogo = username || email || "U";
          sidebarLogo.textContent = nameForLogo[0].toUpperCase();
        }

        redirectToDashboard();

      } catch (authErr) {
        console.error("Firebase Login Error:", authErr.code, authErr.message);
        if (authErr.code === "auth/user-not-found") {
          alert("⚠️ No account found with this email. Please sign up first.");
        } else if (authErr.code === "auth/wrong-password") {
          alert("❌ Incorrect password. Please try again.");
        } else {
          alert("⚠️ Firebase login error: " + authErr.message);
        }
      }

    } else {
      alert(`🚫 Retina match failed. ${data.error || ""}`);
    }

  } catch (err) {
    console.error("Match Error:", err);
    alert("⚠️ Server error: " + err.message);
  }
}


// ✅ Fetch and display all user data on dashboard
async function loadAllUserData() {
  const user = auth.currentUser;
  if (!user) return;

  const uid = user.uid;
  const logsContainer = document.getElementById("logs-container");
  if (!logsContainer) return;
  logsContainer.innerHTML = "";

  const endpoints = [
    { name: "Student", url: `${BASE_BACKEND_URL}/students?uid=${uid}` },
    { name: "Employee", url: `${BASE_BACKEND_URL}/employees?uid=${uid}` },
    { name: "Business", url: `${BASE_BACKEND_URL}/business?uid=${uid}` },
    { name: "Healthcare", url: `${BASE_BACKEND_URL}/healthcare?uid=${uid}` },
    { name: "Education", url: `${BASE_BACKEND_URL}/education?uid=${uid}` },
    { name: "Custom", url: `${BASE_BACKEND_URL}/custom?uid=${uid}` }
  ];

  for (const endpoint of endpoints) {
    try {
      const res = await fetch(endpoint.url);
      const data = await res.json();
      if (!data || data.length === 0) continue;

      data.reverse().forEach(record => {
        const card = document.createElement("div");
        card.classList.add("log-card");

        if (endpoint.name === "Custom") {
          const customFields = record.fields
            .map(f => `<p><strong>${f.fieldName}:</strong> ${f.fieldValue}</p>`)
            .join("");
           card.innerHTML = `<h3>${record.recordTitle}</h3>${customFields}<p><em>Loaded at: ${new Date().toLocaleString()}</em></p>`;
         }
        else {
          card.innerHTML = `<h3>${endpoint.name} Record</h3>${Object.entries(record).map(([k,v])=>`<p><strong>${k}:</strong> ${v}</p>`).join("")}<p><em>Loaded at: ${new Date().toLocaleString()}</em></p>`;
        }

        logsContainer.appendChild(card);
      });

    } catch (err) {
      console.error(`Error loading ${endpoint.name} data:`, err);
    }
  }
}

// ✅ Main Logic on DOM Ready
window.addEventListener("DOMContentLoaded", () => { 
  video = document.querySelector("#retinaCam");
  canvas =
    document.getElementById("retinaCanvasSign") ||
    document.getElementById("retinaCanvasLogin");
  scanBtn =
    document.getElementById("scanRetinaSignBtn") ||
    document.getElementById("scanRetinaLoginBtn");
  const createBtn = document.getElementById("createAccountBtn");

  scanBtn?.addEventListener("click", async (event) => {
    event.preventDefault();

    const isSignUp = document.getElementById("signupForm") !== null;
    const username = document.getElementById(isSignUp ? "signupUsername" : "loginUsername")?.value.trim();
    const email = document.getElementById(isSignUp ? "signupEmail" : "loginEmail")?.value.trim();
    const password = document.getElementById(isSignUp ? "signupPassword" : "loginPassword")?.value.trim();

    const isValid = isSignUp ? isSignUpFormValid() : isSignInFormValid();
    if (!isValid) return;

    if (!cameraStream) {
      await startCamera();
      alert("📷 Camera started. Capturing retina in 2 seconds...");

      setTimeout(async () => {
        const retinaData = captureRetinaImage();
        if (!retinaData) return;

        if (isSignUp) {
          await signUpUser(username, email, password, retinaData);
        } else {
          await signInUserWithRetina(username, email, retinaData);
        }
      }, 2000);

      return;
    }

    const retinaData = captureRetinaImage();
    if (!retinaData) return;

    if (isSignUp) {
      await signUpUser(username, email, password, retinaData);
    } else {
      await signInUserWithRetina(username, email, retinaData);
    }
  });

    // ✅ Create Account button click – just show success and go to Sign In
  createBtn?.addEventListener("click", (e) => {
    e.preventDefault();

    if (!isSignUpFormValid()) return;

    alert("✅ Account created successfully!");

    const username = document.getElementById("signupUsername")?.value.trim();
    const email = document.getElementById("signupEmail")?.value.trim();

    // Update sidebar if exists
    const sidebarName = document.getElementById("sidebar-name");
    if (sidebarName) sidebarName.textContent = username;

    const sidebarEmail = document.getElementById("sidebar-email");
    if (sidebarEmail) sidebarEmail.textContent = email;

    const sidebarLogo = document.getElementById("sidebar-logo");
    if (sidebarLogo) {
      const nameForLogo = username || email || "U";
      sidebarLogo.textContent = nameForLogo[0].toUpperCase();
    }

    // Redirect to Sign In page
    window.location.href = "index.html";
  });


  // --- General function to save any form module ---
  async function saveModuleData(formId, endpoint, getDataCallback) {
    const form = document.getElementById(formId);
    if (!form) return;

    form.addEventListener("submit", async (e) => {
      e.preventDefault();

      if (!auth.currentUser) {
        alert("⚠️ Please login first.");
        return;
      }

      const data = getDataCallback(auth.currentUser.uid);

      try {
        const res = await fetch(`${BASE_BACKEND_URL}/${endpoint}`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(data)
        });
        const result = await res.json();

        if (result.id) {
          alert(`✅ ${endpoint.charAt(0).toUpperCase() + endpoint.slice(1)} record saved!`);
          form.reset();
          loadAllUserData(); // Refresh dashboard logs
        } else {
          alert("❌ Save failed: " + result.message);
        }
      } catch (err) {
        console.error("Save Error:", err);
        alert("❌ Server error while saving " + endpoint);
      }
    });
  }

  // --- Student form ---
  saveModuleData("studentForm", "students", (uid) => ({
    uid,
    rollNo: document.getElementById("rollNo").value.trim(),
    name: document.getElementById("name").value.trim(),
    course: document.getElementById("course").value.trim(),
    year: parseInt(document.getElementById("year").value.trim())
  }));

  // --- Employee form ---
  saveModuleData("employeeForm", "employees", (uid) => ({
    uid,
    fullName: document.getElementById("empName").value.trim(),
    companyName: document.getElementById("companyName").value.trim(),
    designation: document.getElementById("designation").value.trim(),
    experience: parseInt(document.getElementById("experience").value.trim()),
    contactNumber: document.getElementById("empContact").value.trim(),
    address: document.getElementById("empAddress").value.trim()
  }));

  // --- Business form ---
  saveModuleData("businessForm", "business", (uid) => ({
    uid,
    businessName: document.getElementById("businessName").value.trim(),
    ownerName: document.getElementById("ownerName").value.trim(),
    type: document.getElementById("businessType").value.trim(),
    contactNumber: document.getElementById("businessContact").value.trim(),
    address: document.getElementById("businessAddress").value.trim()
  }));

  // --- Healthcare form ---
  saveModuleData("healthcareForm", "healthcare", (uid) => ({
    uid,
    patientName: document.getElementById("patientName").value.trim(),
    condition: document.getElementById("condition").value.trim(),
    doctorName: document.getElementById("doctorName").value.trim(),
    hospital: document.getElementById("hospital").value.trim(),
    contactNumber: document.getElementById("healthContact").value.trim()
  }));

  // --- Education form ---
  saveModuleData("educationForm", "education", (uid) => ({
    uid,
    studentName: document.getElementById("eduStudentName").value.trim(),
    institution: document.getElementById("institution").value.trim(),
    degree: document.getElementById("degree").value.trim(),
    year: parseInt(document.getElementById("eduYear").value.trim())
  }));

// --- Custom form ---
saveModuleData("customForm", "custom", (uid) => {
  const recordTitle = document.getElementById("recordTitle").value.trim();
  const container = document.getElementById("custom-fields");
  let fields = [];

  if (container) {
    const inputs = container.querySelectorAll("input");
    // loop through label-value pairs
    for (let i = 0; i < inputs.length; i += 2) {
      const label = inputs[i].value.trim();
      const value = inputs[i + 1].value.trim();
      if (label && value) fields.push({ fieldName: label, fieldValue: value });
    }
  }

  return { uid, recordTitle, fields };
});


});

// ✅ Redirect user to dashboard after login/signup success
function redirectToDashboard() {
  window.location.href = "dashboard.html";
}

// ✅ Toggle Dark/Light Mode
const toggleBtn = document.getElementById("modeToggle");
toggleBtn.addEventListener("click", () => {
  document.body.classList.toggle("dark-mode");
  toggleBtn.textContent = document.body.classList.contains("dark-mode") 
    ? "☀️ Light Mode" 
    : "🌙 Dark Mode";
});
//runned scrpt