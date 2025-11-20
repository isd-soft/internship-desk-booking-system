<template>
  <v-container
    fluid
    class="register-container d-flex align-center justify-center pa-0"
  >
    <div class="bg-decoration">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="circle circle-3"></div>
    </div>

    <transition name="fade">
      <v-card
        v-if="showCard"
        class="register-card elevation-12"
      >
        <div class="logo-section text-center">
          <div class="logo-wrapper">
            <img
              src="../assets/isd-logo.webp"
              alt="ISD Logo"
              class="logo-img"
            />
          </div>
          <h1 class="welcome-text">Create Your Account</h1>
          <p class="welcome-subtitle">Join the ISD Desk System today.</p>
        </div>

        <v-card-text class="form-content">
          <v-form
            ref="form"
            v-model="valid"
            lazy-validation
            @submit.prevent="handleRegister"
          >
            <div class="input-group">
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
                @keyup.enter="handleRegister"
              />
            </div>

            <div class="input-group">
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
                @keyup.enter="handleRegister"
              />
            </div>

            <div class="input-group">
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
                @keyup.enter="handleRegister"
              />
            </div>

            <div class="input-group">
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
                @keyup.enter="handleRegister"
              />
            </div>

            <div class="input-group">
              <label class="input-label">Confirm Password</label>
              <v-text-field
                v-model="confirmPassword"
                placeholder="Confirm your password"
                :type="showConfirmPassword ? 'text' : 'password'"
                prepend-inner-icon="mdi-lock-check-outline"
                :append-inner-icon="
                  showConfirmPassword
                    ? 'mdi-eye-off-outline'
                    : 'mdi-eye-outline'
                "
                @click:append-inner="showConfirmPassword = !showConfirmPassword"
                variant="outlined"
                density="comfortable"
                rounded="lg"
                :rules="confirmPasswordRules"
                color="orange-darken-2"
                autocomplete="new-password"
                class="modern-input"
                @keyup.enter="handleRegister"
              />
            </div>

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

            <div class="text-center helper-section">
              <p class="helper-text">
                <v-icon size="16" class="mr-1">mdi-shield-check</v-icon>
                Your information is secure with us
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
import { ref } from "vue";
import { useRouter } from "vue-router";
import api from "../plugins/axios";
import JSEncrypt from "jsencrypt";

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

const PUBLIC_KEY = `-----BEGIN PUBLIC KEY-----
MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCH/SI4N4hq8NiCb75OCODqNj84
tCY9cO9KnYlb1jy6wX3AwRDLDQoRAKWzKddmZZTfZhi1vlvm99PqR0ShVJCQrQIt
m0DTAfIMtiXAIT6hN65Ky31RYTcvoLyA+GrpWTIm1jFN13I39RIeWAvM1qyRnEpQ
GQ64Pn+sFnqrXROQ5QIDAQAB
-----END PUBLIC KEY-----`;

const encryptPassword = (rawPassword) => {
  const encryptor = new JSEncrypt();
  encryptor.setPublicKey(PUBLIC_KEY);

  const encrypted = encryptor.encrypt(rawPassword);
  return encrypted;
};

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
    const encryptedPassword = encryptPassword(password.value);
    const encryptedConfirmPassword = encryptPassword(confirmPassword.value);

    const payload = {
      firstName: firstName.value,
      lastName: lastName.value,
      email: email.value,
      password: encryptedPassword,
      confirmPassword: encryptedConfirmPassword,
    };

    const response = await api.post("auth/register", payload);

    snackbar.value = {
      show: true,
      message: "Account created successfully! Redirecting to login...",
      color: "success",
    };

    setTimeout(() => {
      showCard.value = false;
      setTimeout(() => router.push("/login"), 400);
    }, 1500);
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
  padding: 60px 40px;
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

.register-card {
  position: relative;
  z-index: 1;
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 152, 0, 0.1);
  box-shadow: 0 20px 60px rgba(255, 152, 0, 0.15);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  animation: slideUp 0.6s ease;
  max-width: 520px;
  width: 100%;
}

.register-card:hover {
  box-shadow: 0 24px 72px rgba(255, 152, 0, 0.2);
  transform: translateY(-4px);
}

.logo-section {
  position: relative;
  padding: 48px 32px 32px;
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
  font-size: 32px;
  font-weight: 700;
  color: #1a1a1a;
  letter-spacing: -0.5px;
  margin: 20px 0 0 0;
}

.welcome-subtitle {
  font-size: 16px;
  color: #666;
  font-weight: 500;
  margin-top: 12px;
}

.form-content {
  padding: 0 40px 40px !important;
}

.input-group {
  margin-bottom: 24px;
}

.input-label {
  display: block;
  font-size: 15px;
  font-weight: 600;
  color: #2c2c2c;
  margin-bottom: 10px;
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
  font-size: 16px;
}

.modern-input :deep(.v-field__input::placeholder) {
  color: #999;
  opacity: 1;
}

.register-btn {
  background: linear-gradient(135deg, #ff9800 0%, #f57c00 100%);
  font-weight: 700;
  height: 56px !important;
  font-size: 17px;
  text-transform: none;
  letter-spacing: 0.5px;
  box-shadow: 0 6px 20px rgba(255, 152, 0, 0.35);
  position: relative;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  margin-top: 8px;
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

.helper-section {
  margin-top: 24px;
}

.helper-text {
  font-size: 14px;
  color: #888;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
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

/* Планшеты */
@media (max-width: 1024px) {
  .register-container {
    padding: 40px 32px;
  }

  .register-card {
    max-width: 500px;
  }

  .logo-section {
    padding: 40px 28px 28px;
  }

  .logo-img {
    width: 110px;
  }

  .welcome-text {
    font-size: 28px;
  }

  .welcome-subtitle {
    font-size: 15px;
  }

  .form-content {
    padding: 0 32px 32px !important;
  }
}

/* Мобильные устройства */
@media (max-width: 768px) {
  .register-container {
    padding: 30px 20px;
  }

  .register-card {
    max-width: 460px;
  }

  .logo-section {
    padding: 36px 24px 24px;
  }

  .logo-img {
    width: 95px;
  }

  .welcome-text {
    font-size: 26px;
  }

  .welcome-subtitle {
    font-size: 14px;
  }

  .form-content {
    padding: 0 28px 28px !important;
  }

  .input-group {
    margin-bottom: 20px;
  }

  .input-label {
    font-size: 14px;
  }

  .modern-input :deep(.v-field__input) {
    font-size: 15px;
  }

  .register-btn {
    height: 52px !important;
    font-size: 16px;
  }

  .circle-1 {
    width: 400px;
    height: 400px;
  }

  .circle-2 {
    width: 280px;
    height: 280px;
  }

  .circle-3 {
    width: 200px;
    height: 200px;
  }
}

/* Маленькие мобильные */
@media (max-width: 480px) {
  .register-container {
    padding: 24px 16px;
  }

  .register-card {
    max-width: 100%;
  }

  .logo-section {
    padding: 32px 20px 20px;
  }

  .logo-img {
    width: 85px;
  }

  .welcome-text {
    font-size: 24px;
  }

  .welcome-subtitle {
    font-size: 13px;
  }

  .form-content {
    padding: 0 24px 24px !important;
  }

  .input-group {
    margin-bottom: 18px;
  }

  .input-label {
    font-size: 13px;
    margin-bottom: 8px;
  }

  .modern-input :deep(.v-field__input) {
    font-size: 14px;
  }

  .register-btn {
    height: 50px !important;
    font-size: 15px;
  }

  .helper-text {
    font-size: 12px;
  }

  .circle-1 {
    width: 320px;
    height: 320px;
  }

  .circle-2 {
    width: 240px;
    height: 240px;
  }

  .circle-3 {
    width: 180px;
    height: 180px;
  }
}

/* Очень маленькие экраны */
@media (max-width: 360px) {
  .register-container {
    padding: 20px 12px;
  }

  .logo-section {
    padding: 28px 16px 16px;
  }

  .logo-img {
    width: 75px;
  }

  .welcome-text {
    font-size: 22px;
  }

  .welcome-subtitle {
    font-size: 12px;
  }

  .form-content {
    padding: 0 20px 20px !important;
  }

  .input-group {
    margin-bottom: 16px;
  }

  .input-label {
    font-size: 12px;
  }

  .modern-input :deep(.v-field__input) {
    font-size: 13px;
  }

  .register-btn {
    height: 48px !important;
    font-size: 14px;
  }

  .helper-text {
    font-size: 11px;
  }
}
</style>