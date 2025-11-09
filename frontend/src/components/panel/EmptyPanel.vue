<template>
  <div
    class="empty-panel flex-grow-1 d-flex align-items-center justify-center pa-6"
  >
    <div class="text-center">
      <div class="empty-icon-wrapper mb-4">
        <v-icon size="48" color="grey-lighten-1">mdi-folder-open</v-icon>
      </div>
      <h3 class="empty-title">{{ title }}</h3>
      <p class="empty-subtitle">{{ subtitle }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{ title: string; subtitle: string }>();
</script>

<style scoped>
.empty-panel {
  background: var(--soft);
}
.empty-icon-wrapper {
  width: 100px;
  height: 100px;
  margin: 0 auto;
  background: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid var(--panel-sep);
}
.empty-title {
  font-size: 1.06rem;
  font-weight: 900;
  color: var(--text-1);
  margin-bottom: 8px;
}
.empty-subtitle {
  font-size: 0.92rem;
  color: var(--text-2);
  font-weight: 650;
}
</style>
