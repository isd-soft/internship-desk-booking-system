<script setup>
import AdminSidePanel from "../components/AdminSidePanel.vue";
import { ref, computed } from "vue";

const isPanelOpen = ref(true);
const isPanelExpanded = ref(true);
const drawerWidth = ref(360); // Default width - will be updated by drawer

// Handle expansion state change from panel
const handleExpandedChange = (expanded) => {
  isPanelExpanded.value = expanded;
};

// Handle drawer width changes
const handleDrawerWidthChange = (width) => {
  drawerWidth.value = width;
};

// Compute content area margin
const contentMargin = computed(() => {
  return isPanelOpen.value ? drawerWidth.value : 0;
});
</script>

<template>
  <div class="layout">
    <AdminSidePanel
        v-model="isPanelOpen"
        @update:modelValue="isPanelOpen = $event"
        @update:expanded="handleExpandedChange"
        @update:drawerWidth="handleDrawerWidthChange"
    />

    <div 
      class="content-area" 
      :class="`route-${$route.name}`"
      :style="{ marginLeft: `${contentMargin}px` }"
    >
      <router-view />
    </div>
  </div>
</template>

<style scoped>
.layout {
  display: flex;
  height: 100vh;
  position: relative;
  overflow: hidden;
  width: 100vw;
}

.content-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  padding: 24px;
  overflow-y: auto;
  overflow-x: hidden;
  background: #fff;
  transition: margin-left 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  min-width: 0;
  flex: 1;
}

.content-area.route-Map {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  padding: 0;
  overflow-y: auto;
  overflow-x: hidden;
  background: #fff;
  transition: margin-left 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  min-width: 0;
  flex: 1;
}

.content-area > * {
  width: 100%;
  max-width: 100%;
}

@media (max-width: 900px) {
  .layout {
    flex-direction: column;
  }

  .content-area {
    padding: 16px;
    margin-left: 0 !important;
  }
}
</style>