<script setup>
import { ref, computed } from "vue";
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
const activeLegendFilter = ref(null);
const hoveredLegendFilter = ref(null);

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

function handleBookingCancelled(data) {
  console.log("[Dashboard] Booking cancelled, notifying SidePanel", data);
  if (sidePanelRef.value && sidePanelRef.value.refreshUpcoming) {
    sidePanelRef.value.refreshUpcoming();
  }
}

function handleFavouriteToggled(payload) {
  console.log("[Dashboard] Favourite toggled, notifying SidePanel", payload);
  if (sidePanelRef.value && sidePanelRef.value.refreshFavourites) {
    sidePanelRef.value.refreshFavourites();
  }
}

const legends = [
  { color: "#10b981", label: "Available", icon: "mdi-check-circle", deskColor: "GREEN" },
  { color: "#ef4444", label: "Fully Booked", icon: "mdi-close-circle", deskColor: "RED" },
  { color: "#f59e0b", label: "Partially Booked", icon: "mdi-clock-outline", deskColor: "AMBER" },
  { color: "#3b82f6", label: "Assigned", icon: "mdi-account", deskColor: "BLUE" },
  { color: "#6b7280", label: "Unavailable", icon: "mdi-minus-circle", deskColor: "GRAY" },
  { color: "#8b5cf6", label: "Your Booking", icon: "mdi-account-check", deskColor: "PURPLE" },
];

function handleLegendClick(deskColor) {
  // Toggle: если уже активен этот фильтр - убираем, иначе устанавливаем
  if (activeLegendFilter.value === deskColor) {
    activeLegendFilter.value = null;
  } else {
    activeLegendFilter.value = deskColor;
  }
}

function handleLegendHover(deskColor) {
  // Показываем hover только если нет активного клика
  if (activeLegendFilter.value === null) {
    hoveredLegendFilter.value = deskColor;
  }
}

function handleLegendLeave() {
  // Убираем hover
  hoveredLegendFilter.value = null;
}

// Computed для определения текущего активного фильтра
const currentActiveFilter = computed(() => {
  return activeLegendFilter.value || hoveredLegendFilter.value;
});
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
            :activeColorFilter="currentActiveFilter"
            @booking-created="handleBookingCreated"
            @booking-cancelled="handleBookingCancelled"
            @favourite-toggled="handleFavouriteToggled"
          />
        </div>
        <div class="legend-bar">
          <div
            v-for="(item, index) in legends"
            :key="index"
            class="legend-badge"
            :class="{ 'legend-active': activeLegendFilter === item.deskColor || hoveredLegendFilter === item.deskColor }"
            @click="handleLegendClick(item.deskColor)"
            @mouseenter="handleLegendHover(item.deskColor)"
            @mouseleave="handleLegendLeave"
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
  padding: 8px;
  overflow: hidden;
  min-width: 0;
  min-height: 0;
  width: 100%;
  box-sizing: border-box;
}

.map-section {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: 6px;
  width: 100%;
  height: 100%;
  max-width: 1100px;
  flex-shrink: 1;
  background: transparent;
  border-radius: 16px;
  padding: 6px;
  box-shadow: none;
  overflow: hidden;
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
  max-height: 78vh;
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
  image-rendering: -webkit-optimize-contrast;
  image-rendering: crisp-edges;
}

.embedded-map :deep(.floorplan-container img) {
  image-rendering: -webkit-optimize-contrast;
  image-rendering: crisp-edges;
  transform: translateZ(0);
  backface-visibility: hidden;
}

.embedded-map :deep(.floorplan-inner) {
  margin: 0 auto;
}

.legend-bar {
  display: flex;
  flex-wrap: nowrap;
  justify-content: space-between;
  gap: 3px;
  padding: 3px 4px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(12px);
  border-radius: 10px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  box-shadow: 0 2px 8px rgba(15, 23, 42, 0.05);
  width: 100%;
  max-width: 100%;
  margin: 0 auto;
  flex-shrink: 0;
}

.legend-badge {
  position: relative;
  display: flex;
  align-items: center;
  gap: 3px;
  padding: 3px 5px;
  justify-content: center;
  text-align: center;
  background: white;
  border-radius: 8px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  cursor: pointer;
  overflow: hidden;
  flex: 1 1 0;
  min-height: 24px;
  white-space: nowrap;
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

.legend-badge.legend-active {
  transform: translateY(-3px) scale(1.05);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.15);
  border-color: rgba(0, 0, 0, 0.12);
  z-index: 10;
}

.legend-badge.legend-active .badge-glow {
  opacity: 1;
}

.badge-text {
  font-size: 13px;
  font-weight: 800;
  color: #111827;
  white-space: nowrap;
  position: relative;
  z-index: 1;
  line-height: 1;
  text-shadow: 0 0 1px rgba(0, 0, 0, 0.1);
}

.badge-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  flex-shrink: 0;
}

/* Laptop and desktop responsive sizing */
@media (max-width: 1440px) and (min-width: 1190px) {
  .map-section {
    max-width: 100%;
    padding: 8px;
    gap: 6px;
  }
  
  .map-holder {
    padding: 12px;
  }
  
  .embedded-map {
    max-height: 68vh;
    min-height: 420px;
  }
  
  .legend-bar {
    gap: 3px;
    padding: 3px 4px;
  }
  
  .legend-badge {
    padding: 3px 5px;
    gap: 3px;
    min-height: 24px;
  }
  
  .badge-text {
    font-size: 11px;
  }
  
  .badge-dot {
    width: 7px;
    height: 7px;
  }
}

@media (max-width: 1366px) and (min-width: 1190px) {
  .map-section {
    max-width: 100%;
    padding: 6px;
    gap: 5px;
  }
  
  .map-holder {
    padding: 10px;
  }
  
  .embedded-map {
    max-height: 66vh;
    min-height: 400px;
  }
  
  .legend-bar {
    padding: 3px 4px;
    gap: 2px;
  }
  
  .legend-badge {
    padding: 3px 4px;
    gap: 2px;
    min-height: 22px;
  }
  
  .badge-text {
    font-size: 12px;
  }
  
  .badge-dot {
    width: 6.5px;
    height: 6.5px;
  }
}

@media (max-width: 1280px) and (min-width: 1190px) {
  .map-section {
    max-width: 100%;
    padding: 5px;
    gap: 4px;
  }
  
  .map-holder {
    padding: 8px;
  }
  
  .embedded-map {
    max-height: 64vh;
    min-height: 380px;
  }
  
  .legend-bar {
    padding: 2px 3px;
    gap: 2px;
  }
  
  .legend-badge {
    padding: 2px 4px;
    gap: 2px;
    min-height: 20px;
  }
  
  .badge-text {
    font-size: 11.5px;
  }
  
  .badge-dot {
    width: 6px;
    height: 6px;
  }
}

@media (max-width: 1024px) and (min-width: 1190px) {
  .map-section {
    max-width: 100%;
    padding: 4px;
    gap: 3px;
  }
  
  .map-holder {
    padding: 6px;
  }
  
  .embedded-map {
    max-height: 62vh;
    min-height: 360px;
  }
  
  .legend-bar {
    padding: 2px 3px;
    gap: 2px;
  }
  
  .legend-badge {
    padding: 2px 3px;
    gap: 2px;
    min-height: 18px;
  }
  
  .badge-text {
    font-size: 11px;
  }
  
  .badge-dot {
    width: 5.5px;
    height: 5.5px;
  }
}

@media (min-width: 1441px) {
  .map-holder {
    padding: 10px;
  }

  .embedded-map {
    min-height: 500px;
    max-height: 80vh;
  }
  
  .map-section {
    gap: 8px;
    padding: 8px;
  }
  
  .legend-bar {
    gap: 3px;
    padding: 3px 5px;
  }
  
  .legend-badge {
    min-height: 26px;
    padding: 4px 6px;
    gap: 3px;
  }
  
  .badge-text {
    font-size: 13.5px;
  }
  
  .badge-dot {
    width: 7.5px;
    height: 7.5px;
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
