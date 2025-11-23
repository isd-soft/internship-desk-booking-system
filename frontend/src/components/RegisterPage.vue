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
      <v-card v-if="showCard" class="register-card elevation-12">
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

            <!-- BUTTON -->
            <v-btn
              block
              type="button"
              class="register-btn text-white font-weight-bold"
              size="large"
              rounded="lg"
              elevation="0"
              :loading="isLoading"
              :disabled="isLoading"
              @click="handleRegister"
            >
              <v-icon left class="mr-2">mdi-account-plus</v-icon>
              create
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
const emit = defineEmits(["success", "cancel"]);

const valid = ref(false);

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
  return encryptor.encrypt(rawPassword);
};

const emailRules = [
  (v) => !!v || "Email is required",
  (v) => /.+@.+\..+/.test(v) || "Email format is invalid",
];

const passwordRules = [
  (v) => !!v || "Password is required",
  (v) => v.length >= 6 || "Password must be at least 6 characters",
  (v) => v.length <= 64 || "Password must not exceed 64 characters",
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
      email: email.value,
      password: encryptedPassword,
      confirmPassword: encryptedConfirmPassword,
    };

    await api.post("auth/register", payload);

    snackbar.value = {
      show: true,
      message: "Account created successfully! Redirecting...",
      color: "success",
    };

    emit("success");

    if (router.currentRoute.value.path === "/register") {
      setTimeout(() => {
        showCard.value = false;
        setTimeout(() => router.push("/login"), 400);
      }, 1500);
    }
  } catch (error) {
    snackbar.value = {
      show: true,
      message: error.response?.data?.message || "Registration failed.",
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
  width: 100%;
  height: 100vh;

  display: flex;
  justify-content: center;
  align-items: center;

  padding: 24px;

  background: linear-gradient(135deg, #fff5ed 0%, #ffffff 50%, #fff8f0 100%);
  position: relative;
  overflow: hidden;
}

.bg-decoration {
  position: absolute;
  width: 120%;
  height: 120%;
  z-index: 0;
  pointer-events: none;
}

.circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.18;
  filter: blur(2px);
  animation: float 18s infinite ease-in-out;
}

.circle-1 {
  width: 520px;
  height: 520px;
  background: linear-gradient(135deg, #ff9800, #f57c00);
  top: -12%;
  right: -12%;
}

.circle-2 {
  width: 350px;
  height: 350px;
  background: linear-gradient(135deg, #ffa726, #ff6f00);
  bottom: -10%;
  left: -10%;
  animation-delay: 4s;
}

.circle-3 {
  width: 260px;
  height: 260px;
  background: linear-gradient(135deg, #ffcc80, #fb8c00);
  top: 40%;
  left: 50%;
  animation-delay: 8s;
}

@keyframes float {
  0%,
  100% {
    transform: translate(0, 0) scale(1);
  }
  50% {
    transform: translate(20px, -25px) scale(1.06);
  }
}

/* CARD */
.register-card {
  position: relative;
  z-index: 2;

  width: 100%;
  max-width: 390px;

  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(16px);

  border-radius: 22px;
  border: 1px solid rgba(255, 152, 0, 0.15);

  box-shadow: 0 18px 52px rgba(255, 152, 0, 0.18);

  animation: slideUp 0.6s ease;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(25px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.logo-section {
  padding: 40px 30px 20px;
  text-align: center;
}

.logo-img {
  width: 105px;
  transition: 0.3s ease;
}

.logo-img:hover {
  transform: scale(1.05);
}

.welcome-text {
  font-size: 26px;
  font-weight: 700;
  margin-top: 14px;
}

.welcome-subtitle {
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

.form-content {
  padding: 0 32px 32px !important;
}

.input-group {
  margin-bottom: 18px;
}

.input-label {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 8px;
  color: #333;
}

.modern-input :deep(.v-field) {
  background: #fffdf9;
  border: 2px solid rgba(255, 152, 0, 0.2);
  transition: 0.25s ease;
  border-radius: 14px;
}

.modern-input :deep(.v-field:hover) {
  border-color: rgba(255, 152, 0, 0.35);
}

.modern-input :deep(.v-field--focused) {
  border-color: #ff9800 !important;
  box-shadow: 0 0 0 3px rgba(255, 152, 0, 0.12);
}

.modern-input :deep(.v-field__input) {
  font-size: 15px;
  font-weight: 500;
}

.register-btn {
  background: linear-gradient(135deg, #ff9800, #f57c00);
  height: 50px !important;
  font-size: 16px;
  font-weight: 700;
  border-radius: 14px;
  margin-top: 10px;
  box-shadow: 0 5px 16px rgba(255, 152, 0, 0.35);
}

.register-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 7px 22px rgba(255, 152, 0, 0.45);
}

.helper-section {
  margin-top: 18px;
}

.helper-text {
  font-size: 13px;
  color: #777;
}

@media (max-width: 992px) {
  .register-card {
    max-width: 360px;
  }
}

/* PHONES */
@media (max-width: 768px) {
  .register-card {
    max-width: 330px;
  }

  .welcome-text {
    font-size: 22px;
  }

  .logo-img {
    width: 90px;
  }

  .form-content {
    padding: 0 26px 26px !important;
  }

  .circle-1 {
    width: 360px;
    height: 360px;
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

/* SMALL PHONES */
@media (max-width: 480px) {
  .register-card {
    max-width: 100%;
  }
}

/* VERY SMALL */
@media (max-width: 360px) {
  .register-card {
    max-width: 92%;
  }

  .welcome-text {
    font-size: 20px;
  }

  .logo-img {
    width: 80px;
  }
}
</style>
