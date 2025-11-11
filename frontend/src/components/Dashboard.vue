<script setup>
import SidePanel from "../components/SidePanel.vue";
import OfficeMapOverlay from "../components/VisualFloorMap/OfficeMapOverlay.vue";
import DatePicker from "../components/DatePicker.vue";
</script>

<template>
  <div class="layout">
    <SidePanel />
    <div class="map-holder">
      <DatePicker/>
      <OfficeMapOverlay />
      <div class="color-legend-container">
        <div class="legend-item">
          <span class="legend-circle" style="background-color: #50C878;"></span>
          <span class="legend-label">available</span>
        </div>
        <div class="legend-item">
          <span class="legend-circle" style="background-color: #EE4B2B;"></span>
          <span class="legend-label">fully booked</span>
        </div>
        <div class="legend-item">
          <span class="legend-circle" style="background-color: #FFBF00;"></span>
          <span class="legend-label">partially booked</span>
        </div>
        <div class="legend-item">
          <span class="legend-circle" style="background-color: #7393B3;"></span>
          <span class="legend-label">assigned (not a share desk)</span>
        </div>
        <div class="legend-item">
          <span class="legend-circle" style="background-color: #818589;"></span>
          <span class="legend-label">unavailable</span>
        </div>
      </div>
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
  flex-direction: column;
  align-items: center; 
  justify-content: flex-start;  
  padding: 32px 20px 20px;   
  gap: 20px;                 
  overflow: auto;
}
.color-legend-container {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin: 20px auto 0;
  padding: 14px 20px;
  background-color: white;
  border-radius: 16px;
  box-shadow: 
    0 4px 12px rgba(0, 0, 0, 0.08),
    0 2px 6px rgba(0, 0, 0, 0.05);
  max-width: 900px;
  flex-wrap: wrap;
  font-family: inherit;
  border: 1px solid #e5e7eb;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13.5px;
  color: #374151;
  font-weight: 500;
}

.legend-circle {
  width: 13px;
  height: 13px;
  border-radius: 50%;
  display: inline-block;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.15);
  flex-shrink: 0;
}

.legend-label {
  white-space: nowrap;
  letter-spacing: 0.2px;
}
</style>
