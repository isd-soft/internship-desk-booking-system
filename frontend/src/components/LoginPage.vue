<template>
  <v-container
    fluid
    class="login-container d-flex align-center justify-center pa-0"
  >
    <transition name="slide-down">
      <v-btn
        v-if="isUserAuthenticated"
        icon
        variant="flat"
        class="logout-fab"
        color="rgba(255, 255, 255, 0.95)"
        size="large"
        elevation="4"
        @click="handleLogout"
      >
        <v-icon color="orange-darken-2">mdi-logout</v-icon>
        <v-tooltip activator="parent" location="left">Logout</v-tooltip>
      </v-btn>
    </transition>

    <div class="bg-decoration">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="circle circle-3"></div>
    </div>

    <transition name="fade">
      <v-card
        v-if="showCard"
        class="login-card elevation-12"
        max-width="480px"
        width="100%"
        rounded="xl"
      >
        <div class="logo-section text-center pt-10 pb-6">
          <div class="logo-wrapper">
            <img
              src="../assets/isd-logo.webp"
              alt="ISD Logo"
              class="logo-img"
            />
          </div>
          <h1 class="welcome-text mt-4">Welcome to ISD Desk System</h1>
          <p class="welcome-subtitle">Please sign in to continue.</p>
        </div>

        <v-card-text class="px-8 pb-8">
          <v-form
            ref="form"
            v-model="valid"
            lazy-validation
            @submit.prevent="handleLogin"
          >
            <div class="input-group mb-4">
              <label class="input-label">Email Address</label>
              <v-text-field
                v-model="username"
                placeholder="you@example.com"
                prepend-inner-icon="mdi-email-outline"
                type="email"
                variant="outlined"
                density="comfortable"
                rounded="lg"
                :rules="usernameRules"
                color="orange-darken-2"
                autocomplete="username"
                class="modern-input"
                @keyup.enter="handleLogin"
              />
            </div>

            <div class="input-group mb-6">
              <label class="input-label">Password</label>
              <v-text-field
                v-model="password"
                placeholder="Enter your password"
                :type="showPassword ? 'text' : 'password'"
                prepend-inner-icon="mdi-lock-outline"
                :append-inner-icon="
                  showPassword ? 'mdi-eye-off-outline' : 'mdi-eye-outline'
                "
                @click:append-inner="showPassword = !showPassword"
                variant="outlined"
                density="comfortable"
                rounded="lg"
                :rules="passwordRules"
                color="orange-darken-2"
                autocomplete="current-password"
                class="modern-input"
                @keyup.enter="handleLogin"
              />
            </div>

            <v-btn
              block
              type="button"
              class="login-btn text-white font-weight-bold"
              size="x-large"
              rounded="lg"
              elevation="0"
              :loading="isLoading"
              :disabled="isLoading"
              @click="handleLogin"
            >
              <v-icon left class="mr-2">mdi-login</v-icon>
              Sign In
            </v-btn>

            <div class="text-center mt-6">
              <p class="helper-text">
                Don't have an account?
                <a href="/register" class="register-link">Sign up here</a>
              </p>
            </div>

            <div class="text-center mt-4">
              <p class="helper-text">
                <v-icon size="16" class="mr-1">mdi-shield-check</v-icon>
                Secure authentication via ISD system
              </p>
            </div>
          </v-form>
        </v-card-text>
      </v-card>
    </transition>

    <v-snackbar
      v-model="snackbar.show"
      :color="snackbar.color"
      timeout="2500"
      location="top"
      elevation="12"
      rounded="xl"
      class="custom-snackbar"
    >
      <div class="d-flex align-center">
        <v-icon class="mr-3" size="24">
          {{
            snackbar.color === "success"
              ? "mdi-check-circle"
              : snackbar.color === "info"
              ? "mdi-information"
              : "mdi-alert-circle"
          }}
        </v-icon>
        <span class="snackbar-text">{{ snackbar.message }}</span>
      </div>
    </v-snackbar>
  </v-container>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import api from "../plugins/axios";
import { logout, isAuthenticated } from "../utils/auth";

const router = useRouter();
const valid = ref(false);
const username = ref("");
const password = ref("");
const showPassword = ref(false);
const form = ref(null);
const showCard = ref(true);
const isLoading = ref(false);
const isUserAuthenticated = ref(false);

const snackbar = ref({
  show: false,
  message: "",
  color: "error",
});

const usernameRules = [
  (v) => !!v || "Email is required",
  (v) => /.+@.+\..+/.test(v) || "Email format is invalid",
];
const passwordRules = [(v) => !!v || "Password is required"];

onMounted(() => {
  isUserAuthenticated.value = isAuthenticated();
});

const handleLogout = async () => {
  snackbar.value = {
    show: true,
    message: "Logging out...",
    color: "info",
  };

  setTimeout(async () => {
    await logout(router);
    isUserAuthenticated.value = false;
    username.value = "";
    password.value = "";
  }, 500);
};

const handleLogin = async () => {
  const { valid: isValid } = await form.value.validate();
  if (!isValid) {
    snackbar.value = {
      show: true,
      message: "Please fill in all required fields.",
      color: "error",
    };
    return;
  }

  isLoading.value = true;
  try {
    const payload = { email: username.value, password: password.value };
    const response = await api.post("/auth/login", payload);

    if (response.data?.token) {
      localStorage.setItem("token", response.data.token);
      localStorage.setItem("email", response.data.email);
      localStorage.setItem("role", response.data.role);

      snackbar.value = {
        show: true,
        message: "Successfully logged in!",
        color: "success",
      };

      setTimeout(() => {
        showCard.value = false;
        setTimeout(() => router.push("/dashboard"), 400);
      }, 1500);
    } else {
      snackbar.value = {
        show: true,
        message: "Unexpected response from server.",
        color: "error",
      };
    }
  } catch (error) {
    snackbar.value = {
      show: true,
      message:
        error.response?.data?.message ||
        "Invalid email or password. Please try again.",
      color: "error",
    };
  } finally {
    isLoading.value = false;
  }
};
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap");

* {
  font-family: "Inter", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto,
    sans-serif;
}

.login-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #fff5ed 0%, #ffffff 50%, #fff8f0 100%);
  position: relative;
  overflow: hidden;
}

.bg-decoration {
  position: absolute;
  width: 100%;
  height: 100%;
  overflow: hidden;
  z-index: 0;
}

.circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.1;
  animation: float 20s infinite ease-in-out;
}

.circle-1 {
  width: 500px;
  height: 500px;
  background: linear-gradient(135deg, #ff9800, #f57c00);
  top: -10%;
  right: -10%;
  animation-delay: 0s;
}

.circle-2 {
  width: 350px;
  height: 350px;
  background: linear-gradient(135deg, #ffa726, #ff6f00);
  bottom: -5%;
  left: -8%;
  animation-delay: 5s;
}

.circle-3 {
  width: 250px;
  height: 250px;
  background: linear-gradient(135deg, #ffb74d, #f57c00);
  top: 50%;
  left: 50%;
  animation-delay: 10s;
}

@keyframes float {
  0%,
  100% {
    transform: translate(0, 0) scale(1);
  }
  33% {
    transform: translate(30px, -30px) scale(1.1);
  }
  66% {
    transform: translate(-20px, 20px) scale(0.9);
  }
}

.logout-fab {
  position: fixed;
  top: 24px;
  right: 24px;
  z-index: 1000;
  backdrop-filter: blur(10px);
  border: 2px solid rgba(255, 152, 0, 0.2);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.logout-fab:hover {
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 8px 24px rgba(255, 152, 0, 0.3);
  border-color: rgba(255, 152, 0, 0.4);
}

.login-card {
  position: relative;
  z-index: 1;
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 152, 0, 0.1);
  box-shadow: 0 20px 60px rgba(255, 152, 0, 0.15);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  animation: slideUp 0.6s ease;
}

.login-card:hover {
  box-shadow: 0 24px 72px rgba(255, 152, 0, 0.2);
  transform: translateY(-4px);
}

.logo-section {
  position: relative;
}

.logo-wrapper {
  position: relative;
  display: inline-block;
}

.logo-img {
  width: 120px;
  height: auto;
  position: relative;
  z-index: 1;
  transition: all 0.4s ease;
}

.logo-img:hover {
  transform: scale(1.05) rotate(2deg);
}

.welcome-text {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a1a;
  letter-spacing: -0.5px;
  margin: 0;
}

.welcome-subtitle {
  font-size: 14px;
  color: #666;
  font-weight: 500;
  margin-top: 8px;
}

.input-group {
  margin-bottom: 20px;
}

.input-label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: #2c2c2c;
  margin-bottom: 8px;
  letter-spacing: 0.2px;
}

.modern-input :deep(.v-field) {
  background: linear-gradient(135deg, #fffbf5 0%, #fff9f0 100%);
  border: 2px solid rgba(255, 152, 0, 0.15);
  transition: all 0.3s ease;
}

.modern-input :deep(.v-field:hover) {
  border-color: rgba(255, 152, 0, 0.3);
  background: #ffffff;
}

.modern-input :deep(.v-field--focused) {
  border-color: #ff9800 !important;
  box-shadow: 0 0 0 3px rgba(255, 152, 0, 0.1);
  background: #ffffff;
}

.modern-input :deep(.v-field__input) {
  font-weight: 500;
}

.modern-input :deep(.v-field__input::placeholder) {
  color: #999;
  opacity: 1;
}

.login-btn {
  background: linear-gradient(135deg, #ff9800 0%, #f57c00 100%);
  font-weight: 700;
  height: 56px !important;
  font-size: 16px;
  text-transform: none;
  letter-spacing: 0.5px;
  box-shadow: 0 6px 20px rgba(255, 152, 0, 0.35);
  position: relative;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.login-btn::before {
  content: "";
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(255, 255, 255, 0.3),
    transparent
  );
  transition: left 0.5s;
}

.login-btn:hover::before {
  left: 100%;
}

.login-btn:hover {
  box-shadow: 0 8px 28px rgba(255, 152, 0, 0.45);
  transform: translateY(-2px);
}

.login-btn:active {
  transform: translateY(0);
}

.helper-text {
  font-size: 12px;
  color: #888;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.register-link {
  color: #ff9800;
  font-weight: 600;
  text-decoration: none;
  transition: all 0.2s ease;
  margin-left: 4px;
}

.register-link:hover {
  color: #f57c00;
  text-decoration: underline;
}

.custom-snackbar {
  font-weight: 600;
}

.snackbar-text {
  font-size: 15px;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.4s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.slide-down-enter-active,
.slide-down-leave-active {
  transition: all 0.3s ease;
}

.slide-down-enter-from,
.slide-down-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(40px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 768px) {
  .login-card {
    margin: 20px;
    max-width: calc(100% - 40px);
  }

  .logo-img {
    width: 100px;
  }

  .welcome-text {
    font-size: 24px;
  }

  .logout-fab {
    top: 16px;
    right: 16px;
  }

  .circle-1 {
    width: 350px;
    height: 350px;
  }

  .circle-2 {
    width: 250px;
    height: 250px;
  }
}

@media (max-width: 480px) {
  .login-card {
    margin: 16px;
    padding: 0;
  }

  .logo-section {
    padding-top: 32px !important;
    padding-bottom: 24px !important;
  }

  .welcome-text {
    font-size: 22px;
  }

  .welcome-subtitle {
    font-size: 13px;
  }

  .v-card-text {
    padding: 24px !important;
  }

  .login-btn {
    height: 52px !important;
    font-size: 15px;
  }
}
</style>
