import { createRouter, createWebHistory } from "vue-router";
import LoginPage from "../components/LoginPage.vue";
import RegistrationPage from "../components/RegisterPage.vue";
import Dashboard from "../components/SidePanel.vue";
import { isAuthenticated } from "../utils/auth";

const routes = [
  { path: "/", redirect: "/login" },
  { path: "/login", name: "Login", component: LoginPage },
    { path: "/register", name: "Register", component: RegistrationPage },

  { path: "/dashboard", name: "Dashboard", component: Dashboard },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to, from, next) => {
  const authenticated = isAuthenticated();

  if (to.path === "/login" || to.path === "/register") {
    if (authenticated && to.path === "/login") {
      next("/dashboard");
    } else {
      next();
    }
    return;
  }

  if (!authenticated) {
    next("/login");
  } else {
    next();
  }
});


export default router;
