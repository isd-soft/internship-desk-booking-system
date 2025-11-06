import { createRouter, createWebHistory } from "vue-router";
import LoginPage from "../components/LoginPage.vue";
import Dashboard from "../components/SidePanel.vue";
import { isAuthenticated } from "../utils/auth";

const routes = [
  { path: "/", redirect: "/login" },
  { path: "/login", name: "Login", component: LoginPage },
  { path: "/dashboard", name: "Dashboard", component: Dashboard },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to, from, next) => {
  const authenticated = isAuthenticated();

  if (!authenticated && to.path !== "/login") {
    next("/login");
  } else if (authenticated && to.path === "/login") {
    next("/dashboard");
  } else {
    next();
  }
});

export default router;
