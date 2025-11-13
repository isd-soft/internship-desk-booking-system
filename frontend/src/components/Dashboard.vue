<script setup>
import SidePanel from "../components/SidePanel.vue";
import OfficeMapOverlay from "../components/VisualFloorMap/OfficeMapOverlay.vue";
import DatePicker from "../components/DatePicker.vue";
import { ref } from "vue";
import {
  selectedDate,
  loadAllColors,
  loadDesksFromBackend,
  resetLayout,
  DeskColors,
} from "../components/VisualFloorMap/floorLayout.ts";

const reloadColors = async () => {
  resetLayout();
  DeskColors.value = [];
  await loadAllColors();
  await loadDesksFromBackend();
};

import { onMounted } from "vue";
onMounted(() => {
  reloadColors();
});

const legends = [
  { color: "#50c878", label: "Available", icon: "mdi-check-circle" },
  { color: "#ee4b2b", label: "Fully booked", icon: "mdi-close-circle" },
  { color: "#ffbf00", label: "Partially booked", icon: "mdi-clock-outline" },
  { color: "#7393b3", label: "Assigned", icon: "mdi-account" },
  { color: "#818589", label: "Unavailable", icon: "mdi-minus-circle" },
];
</script>

<template>
  <div class="layout">
    <SidePanel />
    <div class="map-holder">
      <DatePicker
        @update:date="
          selectedDate = $event;
          reloadColors();
        "
      />
      <OfficeMapOverlay :selected-date-i-s-o="selectedDate" />

      <div class="legend-bar">
        <div v-for="(item, index) in legends" :key="index" class="legend-badge">
          <div
            class="badge-glow"
            :style="{ background: `${item.color}20` }"
          ></div>
          <div class="badge-dot" :style="{ background: item.color }"></div>
          <span class="badge-text">{{ item.label }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap");

* {
  font-family: "Inter", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto,
    sans-serif;
}

.layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

.layout > :first-child {
  width: 340px;
  border-right: 1px solid #ddd;
  flex-shrink: 0;
}

.map-holder {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  padding: 32px 20px 20px;
  gap: 20px;
  overflow: auto;
  min-width: 0;
}

.legend-bar {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 8px;
  padding: 8px;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(12px);
  border-radius: 20px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  max-width: 100%;
}

.legend-badge {
  position: relative;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: white;
  border-radius: 14px;
  border: 1px solid rgba(0, 0, 0, 0.04);
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
  cursor: default;
  overflow: hidden;
  min-width: 0;
}

.legend-badge:hover {
  transform: translateY(-3px) scale(1.02);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
  border-color: rgba(0, 0, 0, 0.08);
}

.badge-glow {
  position: absolute;
  inset: 0;
  opacity: 0;
  transition: opacity 0.4s ease;
  border-radius: 14px;
}

.legend-badge:hover .badge-glow {
  opacity: 1;
}

.badge-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.legend-badge:hover .badge-dot {
  transform: scale(1.3);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.25);
}

.badge-text {
  font-size: 13px;
  font-weight: 600;
  color: #1f2937;
  white-space: nowrap;
  position: relative;
  z-index: 1;
}

/* Tablet */
@media (max-width: 1024px) {
  .layout > :first-child {
    width: 280px;
  }

  .map-holder {
    padding: 24px 16px 16px;
    gap: 16px;
  }

  .legend-bar {
    gap: 6px;
    padding: 6px;
  }

  .legend-badge {
    padding: 8px 12px;
    gap: 6px;
  }

  .badge-text {
    font-size: 12px;
  }

  .badge-dot {
    width: 8px;
    height: 8px;
  }
}

/* Mobile */
@media (max-width: 768px) {
  .layout {
    flex-direction: column;
  }

  .layout > :first-child {
    width: 100%;
    border-right: none;
    border-bottom: 1px solid #ddd;
    max-height: 40vh;
    overflow-y: auto;
  }

  .map-holder {
    padding: 16px 12px;
    gap: 12px;
  }

  .legend-bar {
    flex-direction: column;
    width: 100%;
    max-width: 300px;
    gap: 4px;
    padding: 6px;
  }

  .legend-badge {
    width: 100%;
    padding: 10px 14px;
    justify-content: flex-start;
  }

  .badge-text {
    font-size: 13px;
  }

  .badge-dot {
    width: 10px;
    height: 10px;
  }
}

/* Small Mobile */
@media (max-width: 480px) {
  .map-holder {
    padding: 12px 8px;
    gap: 10px;
  }

  .legend-badge {
    padding: 8px 12px;
    gap: 8px;
  }

  .badge-text {
    font-size: 12px;
  }

  .badge-dot {
    width: 8px;
    height: 8px;
  }
}
</style>