<template>
  <div class="panel-header pa-4 pb-3">
    <div class="header-inner">
      <div class="head-left">
        <div class="user-identity-pill animate-fade" v-if="userEmail">
          <div class="id-icon">
            <img src="../../assets/isd-logo.webp" alt="User" />
          </div>
          <span class="id-text">{{ userEmail }}</span>
          <div class="id-status">
            <span class="pulse-dot"></span>
            <span class="status-text">Online</span>
          </div>
        </div>
        <h2 class="header-title animate-fade">Quick Actions</h2>
        <div class="title-accent"></div>
        <p class="header-subtitle animate-fade-delay">
          Manage your workspace · ISD desk bookings
        </p>
      </div>
      <div class="head-right">
        <div class="brand-wrap">
          <img
            src="../../assets/isd-logo.webp"
            alt="ISD"
            class="brand-img pulse"
          />
        </div>
        <v-btn
          v-if="isAdmin"
          icon
          variant="text"
          size="small"
          @click="$emit('toggle')"
          class="toggle-btn"
        >
          <v-icon>mdi-chevron-left</v-icon>
        </v-btn>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";

defineEmits(["toggle"]);

const userEmail = ref("");
const isAdmin = ref(false);
onMounted(() => {
  const role = localStorage.getItem("role");
  isAdmin.value = String(role).toUpperCase() === "ADMIN";
  userEmail.value = localStorage.getItem("email") || "";
});
</script>

<style scoped>
.panel-header {
  background: var(--surface);
  border-bottom: 1px solid var(--panel-sep);
  font-family: "Inter", -apple-system, BlinkMacSystemFont, sans-serif;
}

.header-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.head-left {
  min-width: 0;
}

/* --- EMAIL CHIP DESIGN --- */
.user-identity-pill {
  display: inline-flex;
  align-items: center;
  height: 28px;
  padding: 0 10px 0 4px;
  background: #f3f4f6;
  border: 1px solid #e5e7eb;
  border-radius: 99px;
  max-width: 100%;
  transition: all 0.2s ease;
  cursor: default;
  margin-bottom: 6px;
}

.user-identity-pill:hover {
  background: #ffffff;
  border-color: #d1d5db;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.03);
}

.id-icon {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  overflow: hidden;
  background: #fff;
  margin-right: 8px;
  border: 1px solid rgba(0, 0, 0, 0.05);
  flex-shrink: 0;
}

.id-icon img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.id-text {
  font-size: 0.8rem;
  font-weight: 600;
  color: #4b5563;
  font-family: "Menlo", "Monaco", monospace;
  margin-right: 12px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 280px;
}

.id-status {
  display: flex;
  align-items: center;
  gap: 4px;
  padding-left: 10px;
  border-left: 1px solid #e5e7eb;
  height: 14px;
  flex-shrink: 0;
}

.status-text {
  font-size: 0.65rem;
  text-transform: uppercase;
  font-weight: 700;
  color: #10b981;
  letter-spacing: 0.5px;
}

.pulse-dot {
  width: 6px;
  height: 6px;
  background: #10b981;
  border-radius: 50%;
  box-shadow: 0 0 0 0 rgba(16, 185, 129, 0.7);
  animation: pulse-green 2s infinite;
  flex-shrink: 0;
}

.header-title {
  font-size: clamp(1.05rem, 0.9rem + 0.4vw, 1.22rem);
  font-weight: 900;
  color: var(--text-1);
  margin: 0;
  letter-spacing: -0.3px;
}

.title-accent {
  width: 54px;
  height: 3px;
  margin-top: 6px;
  border-radius: 2px;
  background: linear-gradient(90deg, #ff8a00, #ffb347);
  animation: grow 1.2s ease forwards;
}

.header-subtitle {
  font-size: clamp(0.9rem, 0.85rem + 0.2vw, 1rem);
  color: var(--text-2);
  font-weight: 650;
  margin: 8px 0 0;
  opacity: 0.85;
}

.head-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.brand-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.3s ease;
}

.brand-wrap:hover {
  transform: scale(1.05);
}

.toggle-btn {
  color: var(--accent) !important;
  transition: all 0.3s ease;
  opacity: 0.8;
}

.toggle-btn:hover {
  background: rgba(255, 138, 0, 0.1) !important;
  transform: scale(1.1);
  opacity: 1;
}

.brand-img {
  width: clamp(64px, 5.6vw, 84px);
  height: clamp(64px, 5.6vw, 84px);
  object-fit: contain;
  opacity: 0.9;
}

@keyframes grow {
  from {
    width: 0;
  }
  to {
    width: 54px;
  }
}

@keyframes pulse-green {
  0% {
    box-shadow: 0 0 0 0 rgba(16, 185, 129, 0.4);
  }
  70% {
    box-shadow: 0 0 0 5px rgba(16, 185, 129, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(16, 185, 129, 0);
  }
}

.animate-fade {
  opacity: 0;
  animation: fadeIn 0.9s ease forwards;
}

.animate-fade-delay {
  opacity: 0;
  animation: fadeIn 1s ease forwards;
  animation-delay: 0.25s;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(6px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 768px) {
  .id-text {
    max-width: 180px;
  }

  .status-text {
    display: none;
  }
}
</style>
