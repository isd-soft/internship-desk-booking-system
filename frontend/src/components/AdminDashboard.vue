<script setup>
import AdminSidePanel from "../components/AdminSidePanel.vue";
import { ref } from "vue";
const isPanelOpen = ref(true);
</script>

<template>
  <div class="layout">
    <!-- Close button - shows when panel is open -->
    <v-btn
        v-if="isPanelOpen"
        icon
        class="panel-toggle-btn close"
        size="small"
        variant="text"
        @click="isPanelOpen = false"
    >
      <v-icon>mdi-close</v-icon>
    </v-btn>

    <!-- Open button - shows when panel is closed -->
    <v-btn
        v-else
        icon
        class="panel-toggle-btn open"
        color="primary"
        elevation="3"
        size="large"
        @click="isPanelOpen = true"
    >
      <v-icon>mdi-menu</v-icon>
    </v-btn>

    <AdminSidePanel
        v-model="isPanelOpen"
        @update:modelValue="isPanelOpen = $event"
    />

    <div class="content-area" :class="{ 'panel-closed': !isPanelOpen }">
      <router-view />
    </div>
  </div>
</template>

<style scoped>
.layout {
  display: flex;
  height: 100vh;
  position: relative;
}

.layout > :first-child {
  flex-shrink: 0;
}

.content-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24px;
  overflow-y: auto;
  background: #fff;
  transition: all 0.3s ease;
}

.content-area > * {
  width: 100%;
  max-width: 100%;
}

.panel-toggle-btn {
  position: fixed !important;
  z-index: 999;
}

.panel-toggle-btn.close {
  top: 12px;
  left: 650px;
  background: rgba(255, 255, 255, 0.9) !important;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.panel-toggle-btn.close:hover {
  background: rgba(255, 138, 0, 0.1) !important;
}

.panel-toggle-btn.open {
  top: 20px;
  left: 20px;
  width: 48px !important;
  height: 48px !important;
  min-width: 48px !important;
  border-radius: 50% !important;
}

@media (max-width: 900px) {
  .layout {
    flex-direction: column;
  }

  .content-area {
    padding: 16px;
  }
}
</style>