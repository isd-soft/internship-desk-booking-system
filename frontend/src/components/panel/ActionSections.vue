<template>
  <div class="actions-section px-6 pb-4">
    <v-btn
      block
      variant="text"
      class="neo-btn mb-3"
      elevation="0"
      size="large"
      @click="$emit('load', 'bookings')"
    >
      <v-icon class="mr-2" size="20">mdi-calendar-check</v-icon>
      <span class="btn-text">My Bookings</span>
      <v-badge
        v-if="currentType === 'bookings' && itemsCount > 0"
        :content="itemsCount"
        color="white"
        inline
        class="ml-auto badge-pill"
      />
    </v-btn>

    <v-btn
      block
      variant="text"
      class="neo-btn mb-3"
      elevation="0"
      size="large"
      @click="$emit('load', 'favourites')"
    >
      <v-icon class="mr-2" size="20">mdi-star</v-icon>
      <span class="btn-text">Favorites</span>
    </v-btn>

    <v-btn
      block
      variant="text"
      class="neo-btn mb-3"
      elevation="0"
      size="large"
      @click="$emit('load', 'upcoming')"
    >
      <v-icon class="mr-2" size="20">mdi-clock-outline</v-icon>
      <span class="btn-text">Upcoming</span>
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
defineProps<{
  currentType: string;
  itemsCount: number;
}>();

defineEmits<{
  (e: "openMap"): void;
  (e: "load", type: "bookings" | "favourites" | "upcoming"): void;
  (e: "logout"): void;
}>();
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
</style>
