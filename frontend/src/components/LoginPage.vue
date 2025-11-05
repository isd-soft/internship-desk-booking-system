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
      <div class="logo-section text-center pt-8 pt-sm-10 pb-4">
        <div class="logo-container mx-auto mb-4">
          <img src="../assets/isd-logo.webp" alt="Logo" class="logo-fallback" />
        </div>
      </div>

      <v-card-title
        class="text-h4 text-sm-h3 font-weight-bold text-center pb-2 main-title px-4"
      >
        Sign in
      </v-card-title>

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
            class="mb-4 custom-input"
            color="orange-darken-2"
          />

          <v-text-field
            v-model="password"
            label="Password"
            :type="showPassword ? 'text' : 'password'"
            prepend-inner-icon="mdi-lock-outline"
            :append-inner-icon="showPassword ? 'mdi-eye-off-outline' : 'mdi-eye-outline'"
            @click:append-inner="showPassword = !showPassword"
            variant="outlined"
            density="comfortable"
            rounded="lg"
            :rules="passwordRules"
            class="mb-6 custom-input"
            color="orange-darken-2"
          />

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

      <!-- Snackbar -->
      <v-snackbar
        v-model="snackbar.show"
        :color="snackbar.color"
        timeout="2500"
        rounded="lg"
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

const usernameRules = [
  (v) => !!v || "Email is required",
  (v) => /.+@.+\..+/.test(v) || "Email format is invalid",
];

const passwordRules = [
  (v) => !!v || "Password is required",
];

const handleLogin = async () => {
  const isValid = await form.value.validate();
  if (!isValid) {
    snackbar.value = {
      show: true,
      message: "Please fill all fields correctly",
      color: "error",
    };
    return;
  }

  try {
    const payload = { email: username.value, password: password.value };
    const response = await axios.post("http://localhost:8080/auth/login", payload);

    localStorage.setItem("token", response.data.token);
    localStorage.setItem("email", response.data.email);
    localStorage.setItem("role", response.data.role);

    snackbar.value = {
      show: true,
      message: "✅ Login successful! Redirecting...",
      color: "success",
    };

    setTimeout(() => router.push("/"), 1200);

  } catch (error) {
    snackbar.value = {
      show: true,
      message: "❗ " + (error.response?.data?.message || "Invalid email or password"),
      color: "error",
    };
  }
};
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap");

* {
  font-family: "Inter", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
}

.login-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #fff5f0 0%, #ffffff 50%, #fff8f4 100%);
  position: relative;
  overflow: hidden;
}

.login-container::before,
.login-container::after {
  content: "";
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
}

.login-container::before {
  top: -50%;
  right: -20%;
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, rgba(255, 152, 0, 0.08) 0%, transparent 70%);
}

.login-container::after {
  bottom: -30%;
  left: -10%;
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, rgba(255, 183, 77, 0.06) 0%, transparent 70%);
}

.login-card {
  border: 2px solid rgba(255, 152, 0, 0.1);
  background: #ffffff;
  box-shadow: 0 8px 32px rgba(255, 152, 0, 0.12);
  transition: 0.4s;
}

.login-card:hover {
  box-shadow: 0 16px 48px rgba(255, 152, 0, 0.18);
  transform: translateY(-4px);
}

/* ✅ Вернули как раньше + сделали чуть больше */
.logo-fallback {
  width: 140px;
  height: 140px;
  object-fit: contain;
  display: block;
  margin: 0 auto;
  transition: transform 0.3s ease;
}

.logo-fallback:hover {
  transform: scale(1.05);
}

/* Адаптив на мобилку */
@media (max-width: 599px) {
  .logo-fallback {
    width: 110px;
    height: 110px;
  }
}

.main-title {
  color: #2c2c2c;
  letter-spacing: -0.5px;
}

.custom-input :deep(.v-field) {
  background-color: #fff9f5;
  border-radius: 12px;
  border: 1.5px solid rgba(255, 152, 0, 0.15);
}

.login-btn {
  background: linear-gradient(135deg, #ff9800 0%, #f57c00 100%);
  font-weight: 600;
  box-shadow: 0 4px 16px rgba(255, 152, 0, 0.4);
}
</style>
