import axios from "axios";
import router from "../router";

const api = axios.create({
  baseURL: "http://localhost:8000/api/v1",
  headers: { "Content-Type": "application/json" },
});

// === REQUEST INTERCEPTOR ===
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

let isRefreshing = false;
let failedQueue = [];

const processQueue = (error, token = null) => {
  failedQueue.forEach((prom) => {
    if (error) prom.reject(error);
    else prom.resolve(token);
  });

  failedQueue = [];
};

api.interceptors.response.use(
  (response) => response,

  async (error) => {
    const originalRequest = error.config;

  if (
  error.response &&
  error.response.status === 403 &&
  error.response.data?.code === "ROLE_CHANGED"
) {

      const refreshToken = localStorage.getItem("refresh");

      if (!refreshToken) {
        localStorage.clear();
        return router.push("/login");
      }

      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        })
          .then((newToken) => {
            originalRequest.headers.Authorization = "Bearer " + newToken;
            return api(originalRequest);
          })
          .catch((err) => Promise.reject(err));
      }

      isRefreshing = true;

      try {
        const res = await api.post("/auth/refresh", {
          refreshToken: refreshToken,
        });

        const newAccessToken = res.data.token;

        localStorage.setItem("token", newAccessToken);

        isRefreshing = false;
        processQueue(null, newAccessToken);

        originalRequest.headers.Authorization = "Bearer " + newAccessToken;
        return api(originalRequest);
      } catch (err) {
        isRefreshing = false;
        processQueue(err, null);

        localStorage.clear();
        return router.push("/login");
      }
    }

    if (error.response && error.response.status === 401) {
      localStorage.clear();
      router.push("/login");
    }

    return Promise.reject(error);
  }
);

export default api;
