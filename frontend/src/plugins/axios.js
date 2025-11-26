import axios from "axios";
import router from "../router";

const api = axios.create({
  baseURL: "/api/v1",//http://localhost:8000
  headers: { "Content-Type": "application/json" },
});

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("token");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

api.interceptors.response.use(
  (response) => response,

  async (error) => {
    const originalRequest = error.config;

    if (
      error.response &&
      error.response.status === 403 &&
      error.response.data?.code === "ROLE_CHANGED"
    ) {
      const refreshToken = localStorage.getItem("refreshToken");

      return new Promise(async (resolve, reject) => {
        try {
          const res = await api.post("/auth/refresh", { refreshToken });
          const newAccessToken = res.data.token;
          const newRole = res.data.role;

          localStorage.setItem("token", newAccessToken);
          localStorage.setItem("role", newRole);
          window.location.reload();
          return;
        } catch (err) {
          localStorage.clear();
          router.push("/login");
          reject(err);
        }
      });
    }

    return Promise.reject(error);
  }
);

export default api;
