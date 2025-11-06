<template>
  <v-container
    fluid
    class="register-container d-flex align-center justify-center pa-0"
  >
    <!-- Animated Background Elements -->
    <div class="bg-decoration">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="circle circle-3"></div>
    </div>

    <transition name="fade">
      <v-card
        v-if="showCard"
        class="register-card elevation-12"
        max-width="520px"
        width="100%"
        rounded="xl"
      >
        <div class="logo-section text-center pt-10 pb-6">
          <div class="logo-wrapper">
            <div class="logo-glow"></div>
            <img
              src="../assets/isd-logo.webp"
              alt="ISD Logo"
              class="logo-img"
            />
          </div>
          <h1 class="welcome-text mt-4">Create Your Account</h1>
          <p class="welcome-subtitle">Join the ISD Desk System today.</p>
        </div>

        <v-card-text class="px-8 pb-8">
          <v-form
            ref="form"
            v-model="valid"
            lazy-validation
            @submit.prevent="handleRegister"
          >
            <!-- First Name Field -->
            <div class="input-group mb-4">
              <label class="input-label">First Name</label>
              <v-text-field
                v-model="firstName"
                placeholder="John"
                prepend-inner-icon="mdi-account-outline"
                type="text"
                variant="outlined"
                density="comfortable"
                rounded="lg"
                :rules="firstNameRules"
                color="orange-darken-2"
                autocomplete="given-name"
                class="modern-input"
              />
            </div>

            <!-- Last Name Field -->
            <div class="input-group mb-4">
              <label class="input-label">Last Name</label>
              <v-text-field
                v-model="lastName"
                placeholder="Doe"
                prepend-inner-icon="mdi-account-outline"
                type="text"
                variant="outlined"
                density="comfortable"
                rounded="lg"
                :rules="lastNameRules"
                color="orange-darken-2"
                autocomplete="family-name"
                class="modern-input"
              />
            </div>

            <!-- Email Field -->
            <div class="input-group mb-4">
              <label class="input-label">Email Address</label>
              <v-text-field
                v-model="email"
                placeholder="you@example.com"
                prepend-inner-icon="mdi-email-outline"
                type="email"
                variant="outlined"
                density="comfortable"
                rounded="lg"
                :rules="emailRules"
                color="orange-darken-2"
                autocomplete="email"
                class="modern-input"
              />
            </div>

            <!-- Password Field -->
            <div class="input-group mb-4">
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
                autocomplete="new-password"
                class="modern-input"
              />
            </div>

            <!-- Confirm Password Field -->
            <div class="input-group mb-6">
              <label class="input-label">Confirm Password</label>
              <v-text-field
                v-model="confirmPassword"
                placeholder="Confirm your password"
                :type="showConfirmPassword ? 'text' : 'password'"
                prepend-inner-icon="mdi-lock-check-outline"
                :append-inner-icon="
                  showConfirmPassword ? 'mdi-eye-off-outline' : 'mdi-eye-outline'
                "
                @click:append-inner="showConfirmPassword = !showConfirmPassword"
                variant="outlined"
                density="comfortable"
                rounded="lg"
                :rules="confirmPasswordRules"
                color="orange-darken-2"
                autocomplete="new-password"
                class="modern-input"
              />
            </div>

            <!-- Register Button -->
            <v-btn
              block
              type="button"
              class="register-btn text-white font-weight-bold"
              size="x-large"
              rounded="lg"
              elevation="0"
              :loading="isLoading"
              :disabled="isLoading"
              @click="handleRegister"
            >
              <v-icon left class="mr-2">mdi-account-plus</v-icon>
              Create Account
            </v-btn>

            <!-- Login Link -->
            <div class="text-center mt-6">
              <p class="helper-text">
                Already have an account?
                <a href="/login" class="login-link">Sign in here</a>
              </p>
            </div>

            <!-- Additional Info -->
            <div class="text-center mt-4">
              <p class="helper-text">
                <v-icon size="16" class="mr-1">mdi-shield-check</v-icon>
                Your information is secure with us
              </p>
            </div>
          </v-form>
        </v-card-text>
      </v-card>
    </transition>

    <!-- Snackbar -->
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
import { ref } from "vue";
import { useRouter } from "vue-router";
import api from "../plugins/axios";

const router = useRouter();
const valid = ref(false);
const firstName = ref("");
const lastName = ref("");
const email = ref("");
const password = ref("");
const confirmPassword = ref("");
const showPassword = ref(false);
const showConfirmPassword = ref(false);
const form = ref(null);
const showCard = ref(true);
const isLoading = ref(false);

const snackbar = ref({
  show: false,
  message: "",
  color: "error",
});

const firstNameRules = [
  (v) => !!v || "First name is required",
  (v) => (v && v.length >= 2) || "First name must be at least 2 characters",
  (v) => (v && v.length <= 40) || "First name must not exceed 40 characters",
];

const lastNameRules = [
  (v) => !!v || "Last name is required",
  (v) => (v && v.length >= 2) || "Last name must be at least 2 characters",
  (v) => (v && v.length <= 40) || "Last name must not exceed 40 characters",
];

const emailRules = [
  (v) => !!v || "Email is required",
  (v) => /.+@.+\..+/.test(v) || "Email format is invalid",
];

const passwordRules = [
  (v) => !!v || "Password is required",
  (v) => (v && v.length >= 6) || "Password must be at least 6 characters",
  (v) => (v && v.length <= 64) || "Password must not exceed 64 characters",
];

const confirmPasswordRules = [
  (v) => !!v || "Please confirm your password",
  (v) => v === password.value || "Passwords do not match",
];

const handleRegister = async () => {
  const { valid: isValid } = await form.value.validate();
  if (!isValid) {
    snackbar.value = {
      show: true,
      message: "Please fill in all required fields correctly.",
      color: "error",
    };
    return;
  }

  isLoading.value = true;
  try {
    const payload = {
      firstName: firstName.value,
      lastName: lastName.value,
      email: email.value,
      password: password.value,
    };
    
    const response = await api.post("/auth/register", payload);

    if (response.data) {
      snackbar.value = {
        show: true,
        message: "Account created successfully! Redirecting to login...",
        color: "success",
      };

      setTimeout(() => {
        showCard.value = false;
        setTimeout(() => router.push("/login"), 400);
      }, 1500);
    }
  } catch (error) {
    snackbar.value = {
      show: true,
      message:
        error.response?.data?.message ||
        "Registration failed. Please try again.",
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

.register-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #fff5ed 0%, #ffffff 50%, #fff8f0 100%);
  position: relative;
  overflow: hidden;
}

/* Animated Background */
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
  0%, 100% {
    transform: translate(0, 0) scale(1);
  }
  33% {
    transform: translate(30px, -30px) scale(1.1);
  }
  66% {
    transform: translate(-20px, 20px) scale(0.9);
  }
}

/* Register Card */
.register-card {
  position: relative;
  z-index: 1;
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 152, 0, 0.1);
  box-shadow: 0 20px 60px rgba(255, 152, 0, 0.15);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  animation: slideUp 0.6s ease;
}

.register-card:hover {
  box-shadow: 0 24px 72px rgba(255, 152, 0, 0.2);
  transform: translateY(-4px);
}

/* Logo Section */
.logo-section {
  position: relative;
}

.logo-wrapper {
  position: relative;
  display: inline-block;
}

.logo-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 160px;
  height: 160px;
  background: radial-gradient(circle, rgba(255, 152, 0, 0.2), transparent 70%);
  border-radius: 50%;
  animation: pulse 3s infinite;
}

.logo-img {
  width: 120px;
  height: auto;
  position: relative;
  z-index: 1;
  filter: drop-shadow(0 4px 12px rgba(255, 152, 0, 0.2));
  transition: all 0.4s ease;
}

.logo-img:hover {
  transform: scale(1.05) rotate(2deg);
}

@keyframes pulse {
  0%, 100% {
    transform: translate(-50%, -50%) scale(1);
    opacity: 0.5;
  }
  50% {
    transform: translate(-50%, -50%) scale(1.2);
    opacity: 0.3;
  }
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

/* Input Styling */
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

/* Register Button */
.register-btn {
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

.register-btn::before {
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

.register-btn:hover::before {
  left: 100%;
}

.register-btn:hover {
  box-shadow: 0 8px 28px rgba(255, 152, 0, 0.45);
  transform: translateY(-2px);
}

.register-btn:active {
  transform: translateY(0);
}

/* Helper Text & Login Link */
.helper-text {
  font-size: 12px;
  color: #888;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.login-link {
  color: #ff9800;
  font-weight: 600;
  text-decoration: none;
  transition: all 0.2s ease;
  margin-left: 4px;
}

.login-link:hover {
  color: #f57c00;
  text-decoration: underline;
}

/* Snackbar */
.custom-snackbar {
  font-weight: 600;
}

.snackbar-text {
  font-size: 15px;
}

/* Animations */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.4s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
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

/* Responsive Design */
@media (max-width: 768px) {
  .register-card {
    margin: 20px;
    max-width: calc(100% - 40px);
  }

  .logo-img {
    width: 100px;
  }

  .welcome-text {
    font-size: 24px;
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
  .register-card {
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

  .register-btn {
    height: 52px !important;
    font-size: 15px;
  }
}
</style>