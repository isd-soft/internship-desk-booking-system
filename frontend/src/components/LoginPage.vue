<template>
  <v-container
    fluid
    class="login-container d-flex align-center justify-center pa-4 pa-sm-6"
  >
    <transition name="fade">
      <v-card
        v-if="showCard"
        class="login-card elevation-3"
        max-width="clamp(320px, 90vw, 500px)"
        width="100%"
        rounded="xl"
      >
        <div class="logo-section text-center pt-8 pt-sm-10 pb-4">
          <div class="logo-container mx-auto mb-4">
            <img
              src="../assets/isd-logo.webp"
              alt="Logo"
              class="logo-fallback"
            />
          </div>
        </div>

        <v-card-title
          class="text-h4 text-sm-h3 font-weight-bold text-center pb-2 main-title px-4"
        >
          Sign in
        </v-card-title>

        <v-card-text class="px-6 px-sm-10">
          <v-form
            ref="form"
            v-model="valid"
            lazy-validation
            @submit.prevent="handleLogin"
          >
            <v-text-field
              v-model="username"
              label="Email"
              prepend-inner-icon="mdi-email-outline"
              type="email"
              variant="outlined"
              density="comfortable"
              rounded="lg"
              :rules="usernameRules"
              class="mb-4 custom-input"
              color="orange-darken-2"
              autocomplete="username"
              @keyup.enter="handleLogin"
            />

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
              class="mb-4 custom-input"
              color="orange-darken-2"
              autocomplete="current-password"
              @keyup.enter="handleLogin"
            />

            <v-btn
              block
              type="button"
              color="orange-darken-2"
              class="mb-2 login-btn text-white font-weight-bold"
              :size="$vuetify.display.xs ? 'large' : 'x-large'"
              rounded="lg"
              elevation="0"
              :loading="isLoading"
              :disabled="isLoading"
              @click="handleLogin"
            >
              Sign In
            </v-btn>
          </v-form>
        </v-card-text>
      </v-card>
    </transition>

    <v-snackbar
      v-model="snackbar.show"
      :color="snackbar.color"
      timeout="2500"
      location="bottom"
      elevation="8"
      rounded="lg"
      class="snackbar-style"
    >
      <span class="snackbar-text">{{ snackbar.message }}</span>
    </v-snackbar>
  </v-container>
</template>

<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import api from "../plugins/axios";

const router = useRouter();
const valid = ref(false);
const username = ref("");
const password = ref("");
const showPassword = ref(false);
const form = ref(null);
const showCard = ref(true);
const isLoading = ref(false);

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
@import url("https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap");

* {
  font-family: "Inter", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto,
    sans-serif;
}

.login-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #fff5f0 0%, #ffffff 50%, #fff8f4 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: clamp(1rem, 3vw, 3rem);
}

.login-card {
  border: 2px solid rgba(255, 152, 0, 0.1);
  background: #ffffff;
  box-shadow: 0 8px 32px rgba(255, 152, 0, 0.12);
  transition: all 0.4s ease;
  animation: slideIn 0.6s ease;
}

.login-card:hover {
  box-shadow: 0 16px 48px rgba(255, 152, 0, 0.18);
  transform: translateY(-3px);
}

.logo-fallback {
  width: clamp(90px, 20vw, 140px);
  height: auto;
  object-fit: contain;
  margin: 0 auto;
  transition: transform 0.3s ease, opacity 0.4s ease;
}

.logo-fallback:hover {
  transform: scale(1.05);
  opacity: 0.9;
}

.main-title {
  color: #2c2c2c;
  letter-spacing: -0.5px;
  font-weight: 700;
  font-size: clamp(22px, 4vw, 30px);
}

.custom-input :deep(.v-field) {
  background-color: #fff9f5;
  border-radius: 12px;
  border: 1.5px solid rgba(255, 152, 0, 0.15);
}

.login-btn {
  background: linear-gradient(135deg, #ff9800 0%, #f57c00 100%);
  font-weight: 700;
  box-shadow: 0 4px 16px rgba(255, 152, 0, 0.4);
  text-transform: none;
  font-size: clamp(14px, 2vw, 16px);
}

.snackbar-style {
  font-weight: 700;
  font-size: 15px;
  text-align: center;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.4s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 768px) {
  .login-card {
    padding: 1rem;
  }

  .v-card-text {
    padding: 1rem !important;
  }

  .login-btn {
    font-size: 15px;
  }
}

@media (max-width: 480px) {
  .login-container {
    padding: 1rem;
  }

  .login-card {
    border-radius: 18px;
  }

  .main-title {
    font-size: 22px;
  }

  .login-btn {
    font-size: 14px;
  }
}
</style>
