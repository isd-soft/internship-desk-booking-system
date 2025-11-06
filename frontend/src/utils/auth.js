// ❌ УДАЛИ ЭТУ СТРОКУ, если была:
// import router from "../router";

export const clearAuthData = () => {
  localStorage.removeItem("token");
  localStorage.removeItem("user");
  sessionStorage.clear();
  document.cookie.split(";").forEach((c) => {
    document.cookie = c
      .replace(/^ +/, "")
      .replace(/=.*/, "=;expires=" + new Date().toUTCString() + ";path=/");
  });
};

export const logout = async (router) => {
  try {
    clearAuthData();
    router.push("/login");
    return { success: true, message: "Logged out successfully" };
  } catch (error) {
    console.error("Logout error:", error);
    clearAuthData();
    router.push("/login");
    return { success: false, message: "Logout completed with errors" };
  }
};

export const isAuthenticated = () => {
  const token = localStorage.getItem("token");
  return token && token !== "null" && token !== "undefined" && token.trim() !== "";
};
