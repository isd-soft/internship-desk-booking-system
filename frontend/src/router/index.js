import { createRouter, createWebHistory } from "vue-router";
import LoginPage from "../components/LoginPage.vue";
import RegistrationPage from "../components/RegisterPage.vue";
import Dashboard from "../components/SidePanel.vue";
import Map from "../components/VisualFloorMap/Map.vue";
import { isAuthenticated } from "../utils/auth";
import StatisticsPage from "../components/StatisticsPage.vue";

const routes = [
  { path: "/", redirect: "/login" },

  { path: "/login", name: "Login", component: LoginPage },
  { path: "/register", name: "Register", component: RegistrationPage },

  { path: "/dashboard", name: "Dashboard", component: Dashboard },
  { path:"/map", name: "Map", component:Map},
    {path:"/statistics", name: "Statistics", component: StatisticsPage}
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to, from, next) => {
  const isAuth = isAuthenticated();
  const publicPages = ["/login", "/register"];
  const triesToAccessPublic = publicPages.includes(to.path);

  if (!isAuth && !triesToAccessPublic) return next("/login");

  if (isAuth && triesToAccessPublic) return next("/dashboard");

  next();
});

export default router;
