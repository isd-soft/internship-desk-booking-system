<script setup>
import SidePanel from "../components/SidePanel.vue";
import OfficeMapOverlay from "../components/VisualFloorMap/OfficeMapOverlay.vue";
</script>

<template>
  <div class="layout">
    <SidePanel />
   <div class="d-none d-md-block" style="height: 100%; width: 100%;">
  <OfficeMapOverlay />
</div>

  </div>
</template>

<style scoped>
.layout {
  display: flex;
  height: 100vh;
}

.layout > :first-child {
  width: 340px;
  border-right: 1px solid #ddd;
}

.map-holder {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
}
</style>
