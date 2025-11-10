<template>
  <div class="panel-header pa-6 pb-4">
    <div class="d-flex align-items-center justify-space-between mb-3">
      <div class="head-left">
        <h2 class="header-title">Quick Actions</h2>
        <p class="header-subtitle">Manage your workspace · ISD desk bookings</p>
      </div>
      <div class="brand-wrap">
        <img src="../../assets/isd-logo.webp" alt="ISD" class="brand-img" />
      </div>
    </div>
  </div>
</template>

<script setup></script>

<style scoped>
.panel-header {
  background: var(--surface);
  border-bottom: 1px solid var(--panel-sep);
}
.head-left {
  min-width: 0;
}
.header-title {
  font-size: clamp(1.05rem, 0.9rem + 0.4vw, 1.22rem);
  font-weight: 900;
  color: var(--text-1);
  margin: 0;
}
.header-subtitle {
  font-size: clamp(0.9rem, 0.85rem + 0.2vw, 1rem);
  color: var(--text-2);
  font-weight: 650;
  margin: 4px 0 0;
}
.brand-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  width: clamp(76px, 6.4vw, 96px);
  height: clamp(76px, 6.4vw, 96px);
  border-radius: 16px;
  border: 1px solid #ffd6a3;
  background: transparent;
  box-shadow: 0 6px 18px rgba(255, 138, 0, 0.1);
}
.brand-img {
  width: clamp(64px, 5.6vw, 84px);
  height: clamp(64px, 5.6vw, 84px);
  object-fit: contain;
}
</style>
