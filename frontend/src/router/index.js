import { createRouter, createWebHistory } from "vue-router";
import LoginPage from "../components/LoginPage.vue";
import RegistrationPage from "../components/RegisterPage.vue";
import Dashboard from "../components/Dashboard.vue";
import Map from "../components/VisualFloorMap/OfficeMapOverlay.vue";
import { isAuthenticated } from "../utils/auth";
import StatisticsPage from "../components/StatisticsPage.vue";
import AdminDesks from "../components/AdminDesks.vue";
import AdminBookings from "../components/AdminBookings.vue";
import AdminDashboard from "../components/AdminDashboard.vue";
import SettingsPage from "../components/SettingsPage.vue";

const routes = [
  { path: "/", redirect: "/login" },
  { path: "/login", name: "Login", component: LoginPage },
  { path: "/register", name: "Register", component: RegistrationPage },
  { path: "/dashboard", name: "Dashboard", component: Dashboard },
    {
        path: "/admin-dashboard",
        component: AdminDashboard,
        meta: {requiresAdmin: true},
        children: [
            {path: "bookings", name: "AdminBookings", component: AdminBookings},
            {path: "desks", name: "AdminDesks", component: AdminDesks},
            {path: "statistics", name: "Statistics", component: StatisticsPage},
            {path: "settings", name: "Settings", component: SettingsPage},
            {path: "map", name: "Map", component: Map},
        ],
    },
];

const router = createRouter({ history: createWebHistory(), routes });

router.beforeEach((to, from, next) => {
  const publicPages = ["/login", "/register"];
  const isAuth = isAuthenticated();
  if (!isAuth && !publicPages.includes(to.path)) return next("/login");
  next();
});

export default router;
