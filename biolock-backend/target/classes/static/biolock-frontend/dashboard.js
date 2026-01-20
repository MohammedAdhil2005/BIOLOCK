// // File: dashboard.js

// // ✅ Check if user is logged in
// const user = JSON.parse(localStorage.getItem("biolockUser"));
// if (!user) {
//   window.location.href = "../index.html"; // Redirect to login if not logged in
// } else {
//   document.getElementById("sidebarUsername").textContent = user.username || "BioLock User";
//   document.getElementById("sidebarEmail").textContent = user.email || "";
// }

// // ✅ Save section data to Firebase Realtime Database
// function saveSection(section, dataObj) {
//   const uid = user.uid || user.username;
//   firebase.database()
//     .ref(`biolock_users/${uid}/${section}`)
//     .set(dataObj)
//     .then(() => alert("✅ Saved successfully!"))
//     .catch(err => alert("❌ Error saving: " + err.message));
// }

// // ✅ Add Save button logic for each form section
// document.querySelectorAll(".form-block").forEach(block => {
//   const btn = block.querySelector("button");
//   btn.addEventListener("click", () => {
//     const inputs = block.querySelectorAll("input, textarea");
//     const payload = {};

//     inputs.forEach(el => {
//       const key = el.name || el.placeholder.trim().replace(/\s+/g, "_").toLowerCase();
//       payload[key] = el.value.trim();
//     });

//     const sectionName = block.id.replace("Section", "").toLowerCase();
//     saveSection(sectionName, payload);
//   });
// });

// // ✅ Logout Function
// function logoutU() {
//   localStorage.removeItem("biolockUser");
//   firebase.auth().signOut().then(() => {
//     window.location.href = "../index.html";
//   }).catch(error => {
//     alert("❌ Logout failed: " + error.message);
//   });
// }

// // ✅ Optional: Warn before leaving the page with unsaved changes
// window.addEventListener("beforeunload", function (e) {
//   e.preventDefault();
//   e.returnValue = "You may have unsaved data. Are you sure you want to leave?";
// });
