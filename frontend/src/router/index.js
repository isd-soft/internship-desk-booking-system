import { createRouter, createWebHistory } from "vue-router";
import LoginPage from "../components/LoginPage.vue";
import RegistrationPage from "../components/RegisterPage.vue";
import Dashboard from "../components/Dashboard.vue"; // <-- ВАЖНО: не SidePanel!
import Map from "../components/VisualFloorMap/OfficeMapOverlay.vue";
import { isAuthenticated } from "../utils/auth";

const routes = [
  { path: "/", redirect: "/login" },
  { path: "/login", name: "Login", component: LoginPage },
  { path: "/register", name: "Register", component: RegistrationPage },
  { path: "/dashboard", name: "Dashboard", component: Dashboard }, // <-- тут лейаут
  { path: "/map", name: "Map", component: Map },
];

const router = createRouter({ history: createWebHistory(), routes });

router.beforeEach((to, from, next) => {
  const publicPages = ["/login", "/register"];
  const isAuth = isAuthenticated();
  if (!isAuth && !publicPages.includes(to.path)) return next("/login");
  next();
});

export default router;
