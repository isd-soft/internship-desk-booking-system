<template>
  <div class="panel-header pa-6 pb-4">
    <div class="header-inner">
      <div class="head-left">
        <h2 class="header-title animate-fade">Quick Actions</h2>
        <div class="title-accent"></div>
        <p class="header-subtitle animate-fade-delay">
          Manage your workspace · ISD desk bookings
        </p>
      </div>
      <div class="brand-wrap">
        <img
          src="../../assets/isd-logo.webp"
          alt="ISD"
          class="brand-img pulse"
        />
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

.header-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.head-left {
  min-width: 0;
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

.brand-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.3s ease;
}

.brand-wrap:hover {
  transform: scale(1.05);
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
</style>
