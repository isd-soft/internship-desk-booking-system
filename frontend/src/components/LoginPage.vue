<template>
  <v-container
    fluid
    class="login-container d-flex align-center justify-center pa-4 pa-sm-6"
  >
    <v-card
      class="login-card elevation-3"
      :class="{ 'mobile-card': $vuetify.display.xs }"
      max-width="500"
      width="100%"
      rounded="xl"
    >
      <!-- Logo Section -->
      <div class="logo-section text-center pt-8 pt-sm-10 pb-4">
        <div class="logo-container mx-auto mb-4">
          <img src="../assets/isd-logo.webp" alt="Logo" class="logo-fallback" />
        </div>
      </div>

      <v-card-title
        class="text-h4 text-sm-h3 font-weight-bold text-center pb-2 main-title px-4"
      >
        Welcome Back
      </v-card-title>
      <v-card-subtitle class="text-center subtitle-text pb-6 pb-sm-8 px-4">
        Sign in to continue to your account
      </v-card-subtitle>

      <v-card-text class="px-6 px-sm-10">
        <v-form ref="form" v-model="valid" lazy-validation>
          <v-text-field
            v-model="username"
            label="Email"
            prepend-inner-icon="mdi-email-outline"
            type="email"
            variant="outlined"
            density="comfortable"
            rounded="lg"
            :rules="usernameRules"
            :error="loginError"
            :error-messages="loginError ? errorMessage : []"
            class="mb-4 custom-input"
            color="orange-darken-2"
            @input="loginError = false"
          ></v-text-field>

          <v-text-field
            v-model="password"
            label="Password"
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
            :error="loginError"
            :error-messages="loginError ? ' ' : []"
            class="mb-6 custom-input"
            color="orange-darken-2"
            @input="loginError = false"
          ></v-text-field>

          <v-btn
            block
            color="orange-darken-2"
            class="mb-6 login-btn text-white"
            :size="$vuetify.display.xs ? 'large' : 'x-large'"
            rounded="lg"
            elevation="0"
            @click="handleLogin"
          >
            Sign In
          </v-btn>
        </v-form>
      </v-card-text>

      <v-snackbar
        v-model="snackbar.show"
        :color="snackbar.color"
        timeout="2500"
        top
        right
        elevation="4"
      >
        {{ snackbar.message }}
      </v-snackbar>
    </v-card>
  </v-container>
</template>

<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import axios from "axios";

const router = useRouter();
const valid = ref(false);
const username = ref("");
const password = ref("");
const showPassword = ref(false);
const form = ref(null);

const snackbar = ref({
  show: false,
  message: "",
  color: "error",
});

const loginError = ref(false);
const errorMessage = ref("");

const usernameRules = [
  (v) => !!v || "Email is required",
  (v) => /.+@.+\..+/.test(v) || "Email format is invalid",
];

const passwordRules = [
  (v) => !!v || "Password is required",
  (v) =>
    (v && v.length >= 8 && v.length <= 12) ||
    "Password must be 8–12 characters",
];

const handleLogin = async () => {
  if (!form.value.validate()) return;

  loginError.value = false;
  errorMessage.value = "";

  try {
    const payload = { email: username.value, password: password.value };
    const response = await axios.post(
      "http://localhost:8080/auth/sign-in",
      payload
    );

    localStorage.setItem("token", response.data.token);

    snackbar.value = {
      show: true,
      message: "Login successful! Redirecting...",
      color: "success",
    };

    setTimeout(() => {
      router.push("/");
    }, 2500);
  } catch (error) {
    loginError.value = true;

    if (error.response?.status === 401) {
      errorMessage.value = "Invalid email or password";
    } else if (error.response?.status === 404) {
      errorMessage.value = "User not found";
    } else {
      errorMessage.value =
        error.response?.data?.message || "Login error. Please try again.";
    }

    snackbar.value = {
      show: true,
      message: errorMessage.value,
      color: "error",
    };
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
  background: linear-gradient(135deg, #fff5f0 0%, #ffffff 50%, #fff8f4 100%);
  position: relative;
  overflow: hidden;
}

.login-container::before {
  content: "";
  position: absolute;
  top: -50%;
  right: -20%;
  width: 600px;
  height: 600px;
  background: radial-gradient(
    circle,
    rgba(255, 152, 0, 0.08) 0%,
    transparent 70%
  );
  border-radius: 50%;
  pointer-events: none;
}

.login-container::after {
  content: "";
  position: absolute;
  bottom: -30%;
  left: -10%;
  width: 500px;
  height: 500px;
  background: radial-gradient(
    circle,
    rgba(255, 183, 77, 0.06) 0%,
    transparent 70%
  );
  border-radius: 50%;
  pointer-events: none;
}

.login-card {
  border: 2px solid rgba(255, 152, 0, 0.1);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  background: #ffffff;
  box-shadow: 0 8px 32px rgba(255, 152, 0, 0.12);
  position: relative;
  z-index: 1;
}

.login-card:hover {
  box-shadow: 0 16px 48px rgba(255, 152, 0, 0.18) !important;
  transform: translateY(-4px);
  border-color: rgba(255, 152, 0, 0.2);
}

.mobile-card {
  max-width: 100%;
  margin: 0;
  border-radius: 0 !important;
  min-height: 100vh;
}

.logo-section {
  animation: fadeInDown 0.6s ease-out;
}

.logo-container {
  width: 120px;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 20px;
  overflow: hidden;
  background: transparent;
  transition: transform 0.3s ease;
}

.logo-container:hover {
  transform: scale(1.05);
}

.logo-video {
  width: 100%;
  height: 100%;
  object-fit: contain;
  display: block;
}

.logo-fallback {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.main-title {
  color: #2c2c2c;
  letter-spacing: -0.5px;
  animation: fadeInUp 0.6s ease-out 0.1s both;
}

.subtitle-text {
  font-size: 0.95rem;
  font-weight: 400;
  color: #666;
  animation: fadeInUp 0.6s ease-out 0.2s both;
}

.custom-input {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  animation: fadeInUp 0.6s ease-out 0.3s both;
}

.custom-input :deep(.v-field) {
  border-radius: 12px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background-color: #fff9f5;
  border: 1.5px solid rgba(255, 152, 0, 0.15);
}

.custom-input :deep(.v-field--focused) {
  background-color: #ffffff;
  box-shadow: 0 0 0 3px rgba(255, 152, 0, 0.1);
  border-color: #ff9800;
}

.custom-input :deep(.v-field--error) {
  border-color: #f44336;
  animation: shake 0.4s ease-out;
}

.custom-input :deep(input) {
  font-weight: 500;
  color: #2c2c2c;
}

.custom-input :deep(.v-label) {
  font-weight: 500;
  color: #666;
}

.custom-input :deep(.v-icon) {
  color: #ff9800;
}

.login-btn {
  text-transform: none;
  font-weight: 600;
  font-size: 1rem;
  letter-spacing: 0.5px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 16px rgba(255, 152, 0, 0.4);
  background: linear-gradient(135deg, #ff9800 0%, #f57c00 100%);
  animation: fadeInUp 0.6s ease-out 0.4s both;
}

.login-btn:hover {
  box-shadow: 0 6px 24px rgba(255, 152, 0, 0.5);
  transform: translateY(-2px);
}

.login-btn:active {
  transform: translateY(0);
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeInDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes shake {
  0%,
  100% {
    transform: translateX(0);
  }
  25% {
    transform: translateX(-8px);
  }
  50% {
    transform: translateX(8px);
  }
  75% {
    transform: translateX(-6px);
  }
}

:deep(input) {
  caret-color: #ff9800;
}

/* Mobile Responsive */
@media (max-width: 599px) {
  .main-title {
    font-size: 1.75rem !important;
  }

  .logo-container {
    width: 100px;
    height: 100px;
  }

  .login-card {
    box-shadow: none !important;
  }
}
</style>
