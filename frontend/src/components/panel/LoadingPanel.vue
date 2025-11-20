<template>
  <div
    class="loading-panel flex-grow-1 d-flex align-items-center justify-center"
  >
    <div class="text-center">
      <v-progress-circular
        indeterminate
        size="48"
        width="4"
      ></v-progress-circular>
      <p class="loading-text mt-4">Loading {{ title }}...</p>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{ title: string }>();
</script>

<style scoped>
.loading-panel {
  background: var(--soft);
}
.loading-text {
  font-size: 0.9rem;
  font-weight: 750;
  color: #6b7280;
}
</style>
