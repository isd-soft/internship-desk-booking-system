import axios from "axios";
import { clearAuthData } from "../utils/auth";
import router from "../router";

const api = axios.create({
  baseURL: "http://localhost:8080/api/v1",
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
  (error) => {
    if (error.response && error.response.status === 401) {
      clearAuthData();
      router.push("/login");
    }
    return Promise.reject(error);
  }
);

export default api;
