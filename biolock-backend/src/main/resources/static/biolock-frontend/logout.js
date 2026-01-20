// File: js/logout.js

document.getElementById("logoutBtn").addEventListener("click", () => {
  // Sign out from Firebase (optional)
  firebase.auth().signOut().catch(err => console.error("Logout error:", err));

  // Remove user data from local storage
  localStorage.removeItem("biolockUser");

  // Redirect to login
  window.location.href = "index.html";
});
