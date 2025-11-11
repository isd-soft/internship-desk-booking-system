<script setup>
import AdminSidePanel from "../components/AdminSidePanel.vue";
</script>

<template>
  <div class="layout">
    <AdminSidePanel />

    <div class="content-area">
      <router-view />
    </div>
  </div>
</template>

<style scoped>
.layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

/* Left sidebar */
.layout > :first-child {
  width: 320px;
  border-right: 1px solid #ddd;
  background-color: #fffaf4;
}

/* Main content area for tables */
.content-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 24px;
  overflow-y: auto;
  background: #fff;
}

/* Optional: make it responsive */
@media (max-width: 900px) {
  .layout {
    flex-direction: column;
  }
  .layout > :first-child {
    width: 100%;
    border-right: none;
    border-bottom: 1px solid #ddd;
  }
  .content-area {
    padding: 16px;
  }
}
</style>
