<script setup>
import { ref } from "vue";
import SidePanel from "./SidePanel.vue";
import OfficeMapOverlay from "../components/VisualFloorMap/OfficeMapOverlay.vue";
import DatePicker from "../components/DatePicker.vue";
import {
  selectedDate,
  loadAllColors,
  loadDesksFromBackend,
  resetLayout,
  DeskColors,
  layout,
} from "../components/VisualFloorMap/floorLayout.ts";

const sidePanelRef = ref(null);

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

function isSameDate(date1, date2) {
  const d1 = new Date(date1);
  const d2 = new Date(date2);

  console.log(`${date1} - ${date2}`);
  return (
    d1.getFullYear() === d2.getFullYear() &&
    d1.getMonth() === d2.getMonth() &&
    d1.getDate() === d2.getDate()
  );
}

function handleCancelItem(data) {
  if (!isSameDate(data.date, selectedDate.value)) {
    console.log(
      "Booking date does not match selected date, skipping color update"
    );
    return;
  }

  const desk = layout.find((d) => Number(d.i) === data.deskId);
  if (desk) {
    desk.color = "GREEN";
  }
}

function handleBookingCreated(data) {
  console.log("[Dashboard] Booking created, notifying SidePanel", data);
  if (sidePanelRef.value && sidePanelRef.value.refreshUpcoming) {
    sidePanelRef.value.refreshUpcoming();
  }
}

const legends = [
  { color: "#50c878", label: "Available", icon: "mdi-check-circle" },
  { color: "#ee4b2b", label: "Fully booked", icon: "mdi-close-circle" },
  { color: "#ffbf00", label: "Partially booked", icon: "mdi-clock-outline" },
  { color: "#7393b3", label: "Assigned", icon: "mdi-account" },
  { color: "#818589", label: "Unavailable", icon: "mdi-minus-circle" },
  { color: "#E1BEE7", label: "Your booking", icon: "mdi-minus-circle" },
];
</script>

<template>
  <div class="layout">
    <SidePanel ref="sidePanelRef" @cancel-item="handleCancelItem" />
    <div class="map-holder">
      <div class="map-section">
        <div class="map-controls">
          <DatePicker
            @update:date="
              selectedDate = $event;
              reloadColors();
            "
          />
        </div>
        <div class="embedded-map">
          <OfficeMapOverlay
            :selectedDateISO="selectedDate"
            @booking-created="handleBookingCreated"
          />
        </div>
        <div class="legend-bar">
          <div
            v-for="(item, index) in legends"
            :key="index"
            class="legend-badge"
          >
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
  align-items: stretch;
  justify-content: flex-start;
  gap: 0;
  height: 100vh;
  overflow: hidden;
  padding: 0;
  box-sizing: border-box;
  width: 100%;
  margin: 0;
}

.layout > :first-child {
  flex: 0 0 auto;
}
.map-holder {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
  overflow: auto;
  min-width: 0;
  min-height: 0;
  width: 100%;
  box-sizing: border-box;
}

.map-section {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: 10px;
  width: 100%;
  max-width: 1100px;
  flex-shrink: 1;
  background: transparent;
  border-radius: 16px;
  padding: 12px;
  box-shadow: none;
}

.map-controls {
  display: flex;
  justify-content: center;
  flex-shrink: 0;
}

.embedded-map {
  width: min(1100px, 100%);
  aspect-ratio: 987 / 643;
  min-height: 420px;
  max-height: 75vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex-shrink: 1;
}

.embedded-map :deep(.floorplan-container) {
  flex: 1 1 auto;
  min-height: 0;
  width: 100%;
  max-width: 100%;
  display: flex;
  justify-content: center;
}

.embedded-map :deep(.floorplan-inner) {
  margin: 0 auto;
}

.legend-bar {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 12px;
  padding: 14px 18px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(12px);
  border-radius: 16px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.08);
  width: 100%;
  max-width: 720px;
  margin: 0 auto;
  flex-shrink: 0;
}

.legend-badge {
  position: relative;
  display: flex;
  align-items: center;
  gap: 9px;
  padding: 10px 16px;
  justify-content: flex-start;
  text-align: left;
  background: white;
  border-radius: 14px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  cursor: default;
  overflow: hidden;
  flex: 1 1 210px;
  max-width: 220px;
  min-width: 180px;
  min-height: 44px;
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

.badge-text {
  font-size: 12px;
  font-weight: 700;
  color: #1f2937;
  white-space: nowrap;
  position: relative;
  z-index: 1;
}

.badge-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}

/* Tablet and desktop optimizations */
@media (min-width: 1190px) {
  .map-holder {
    padding: 24px;
  }

  .embedded-map {
    min-height: 500px;
  }
}

@media (min-width: 1200px) {
  .map-holder {
    padding: 32px;
  }

  .embedded-map {
    min-height: 600px;
  }
}

/* iPad mini and smaller tablets */
@media (max-width: 768px) and (min-width: 481px) {
  .map-holder {
    padding: 16px;
    gap: 12px;
  }

  .legend-badge {
    padding: 8px 12px;
    gap: 7px;
    min-height: 40px;
    flex: 1 1 45%;
    max-width: none;
    text-align: center;
    justify-content: center;
  }

  .badge-text {
    font-size: 11px;
  }

  .badge-dot {
    width: 9px;
    height: 9px;
  }
}

/* Mobile and iPad Pro - vertical layout */
@media (max-width: 1189px) {
  .layout {
    flex-direction: column;
    height: auto;
    gap: 0;
    padding: 0;
  }

  .layout > :first-child {
    width: 100%;
    border-right: none;
    border-bottom: 1px solid #ddd;
    min-height: 80vh;
    max-height: none;
    overflow-y: auto;
    flex: 1 1 auto;
  }

  .map-section {
    gap: 10px;
    max-width: 100%;
    width: 100%;
    align-items: stretch;
  }

  .embedded-map {
    width: 100%;
    min-height: 220px;
    max-height: 320px;
    border-radius: 10px;
    overflow: hidden;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    display: block;
  }

  .legend-bar {
    justify-content: center;
    gap: 10px;
    padding: 12px;
    border-radius: 14px;
    max-width: 520px;
  }

  .legend-badge {
    padding: 8px 12px;
    gap: 7px;
    min-height: 38px;
    flex: 1 1 48%;
    max-width: none;
    text-align: center;
    justify-content: center;
  }

  .badge-text {
    font-size: 11px;
  }

  .badge-dot {
    width: 9px;
    height: 9px;
  }
  .map-holder {
    display: none !important;
  }
}

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
