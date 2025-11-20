<template>
  <div class="actions-section px-4 pb-3 pt-2">
    <v-btn
        v-if="isAdmin"
        block
        variant="flat"
        class="neo-btn mb-3 admin-gradient-btn"
        elevation="3"
        size="large"
        @click="$router.push('/dashboard')"
    >
      <v-icon class="mr-2" size="20">mdi-account-convert</v-icon>
      <span class="btn-text">Switch to User View</span>
    </v-btn>

    <v-btn
      v-if="isAdmin"
      block
      variant="text"
      class="neo-btn mb-3"
      elevation="0"
      size="large"
      @click="$emit('openAdmin', 'map')"
    >
      <v-icon class="mr-2" size="20">mdi-map-clock-outline</v-icon>
      <span class="btn-text">Map Settings</span>
    </v-btn>

    <v-btn
      v-if="isAdmin"
      block
      variant="text"
      class="neo-btn mb-3"
      elevation="0"
      size="large"
      @click="$emit('openAdmin', 'bookings')"
    >
      <v-icon class="mr-2" size="20">mdi-clipboard-text-clock</v-icon>
      <span class="btn-text">All Bookings</span>
    </v-btn>

    <v-btn
      v-if="isAdmin"
      block
      variant="text"
      class="neo-btn mb-3"
      elevation="0"
      size="large"
      @click="$emit('openAdmin', 'desks')"
    >
      <v-icon class="mr-2" size="20">mdi-desk</v-icon>
      <span class="btn-text">All Desks</span>
    </v-btn>

    <v-btn
      v-if="isAdmin"
      block
      variant="text"
      class="neo-btn mb-3"
      elevation="0"
      size="large"
      @click="$emit('openAdmin', 'users')"
    >
      <v-icon class="mr-2" size="20">mdi-account-group</v-icon>
      <span class="btn-text">All Users</span>
    </v-btn>

    <v-btn
      v-if="isAdmin"
      block
      variant="text"
      class="neo-btn mb-3"
      elevation="0"
      size="large"
      @click="$emit('openAdmin', 'statistics')"
    >
      <v-icon class="mr-2" size="20">mdi-chart-bar</v-icon>
      <span class="btn-text">Statistics</span>
    </v-btn>

    <v-btn
      v-if="isAdmin"
      block
      variant="text"
      class="neo-btn mb-3"
      elevation="0"
      size="large"
      @click="$emit('openAdmin', 'settings')"
    >
      <v-icon class="mr-2" size="20">mdi-cog</v-icon>
      <span class="btn-text">Settings</span>
    </v-btn>

    <v-btn
      v-if="isAdmin"
      block
      variant="text"
      class="neo-btn mb-3"
      elevation="0"
      size="large"
      @click="$emit('openAdmin', 'deleted-desks')"
    >
      <v-icon class="mr-2" size="20">mdi-delete-empty</v-icon>
      <span class="btn-text">Deleted Desks</span>
    </v-btn>

    <v-divider class="my-2"></v-divider>

    <v-btn
      block
      variant="text"
      class="neo-btn danger"
      elevation="0"
      size="large"
      @click="$emit('logout')"
    >
      <v-icon class="mr-2" size="20">mdi-logout</v-icon>
      <span class="btn-text">Logout</span>
    </v-btn>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";

defineProps<{
  currentType: string;
  itemsCount: number;
}>();

const emit = defineEmits<{
  (e: "openMap"): void;
  (
    e: "openAdmin",
    page:
      | "dashboard"
      | "bookings"
      | "desks"
      | "users"
      | "settings"
      | "statistics"
      | "map"
      | "deleted-desks"
  ): void;
  (e: "logout"): void;
}>();

const isAdmin = ref(false);
onMounted(() => {
  const role = localStorage.getItem("role");
  isAdmin.value = String(role).toUpperCase() === "ADMIN";
});
</script>

<style scoped>
.neo-btn {
  height: clamp(46px, 4.2vh, 52px) !important;
  border-radius: 12px !important;
  text-transform: none;
  font-weight: 780;
  font-size: clamp(0.95rem, 0.9rem + 0.2vw, 1.02rem);
  justify-content: flex-start;
  letter-spacing: 0.2px;
  padding: 0 14px;
  transition: transform 0.16s ease, box-shadow 0.16s ease, background 0.16s ease,
    border-color 0.16s ease;
  background: #fff;
  border: 1px solid #e5e7eb;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
  color: #1f2937;
}
.neo-btn:hover {
  transform: translateY(-1px);
  background: #fff7eb;
  border-color: var(--accent);
  box-shadow: 0 6px 12px rgba(255, 138, 0, 0.1);
}
.neo-btn.danger {
  background: #fff;
  border: 1px solid #fca5a5;
  color: #b91c1c;
}
.btn-text {
  font-weight: 820;
}
.badge-pill :deep(.v-badge__badge) {
  border-radius: 999px;
  padding: 0 8px;
  font-weight: 820;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.08);
  color: #2b2f3a;
}
.admin-gradient-btn {
  background: linear-gradient(135deg, #eadf66 0%, #e16531 100%) !important;
  color: white !important;
}
</style>
