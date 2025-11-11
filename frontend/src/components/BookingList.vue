<script setup lang="ts">
import { ref } from "vue";
import {
  layout,
  colNum,
  rowHeight,
  totalRows,
  IMAGE_WIDTH_PX,

} from "../components/VisualFloorMap/floorLayout";

const showBookingModal = ref(false);
const selectedDesk = ref<any>(null);
const bookedDesks = ref<Set<string>>(new Set());

function handleDeskClick(item: any) {
  if (item.static) return;
  console.log("Clicked desk:", item.i);
  selectedDesk.value = item;
  showBookingModal.value = true;
}

function handleConfirmBooking(data: { duration: number }) {
  console.log("Booking confirmed:", selectedDesk.value?.i, data.duration);
  if (selectedDesk.value) {
    bookedDesks.value.add(selectedDesk.value.i);
  }
}

function handleCancelBooking() {
  console.log("Booking cancelled:", selectedDesk.value?.i);
  if (selectedDesk.value) {
    bookedDesks.value.delete(selectedDesk.value.i);
  }
  showBookingModal.value = false;
}

function isDeskBooked(deskId: string): boolean {
  return bookedDesks.value.has(deskId);
}

function getExistingBooking(deskId: string) {
  if (isDeskBooked(deskId)) {
    return { duration: 60 };
  }
  return undefined;
}
</script>

<template>
  <div class="floorplan-container no-anim">
    <GridLayout
      v-model:layout="layout"
      :col-num="colNum"
      :row-height="rowHeight"
      :width="IMAGE_WIDTH_PX"
      :max-rows="totalRows"
      :margin="[0, 0]"
      :responsive="false"
      :vertical-compact="false"
      prevent-collision
      :use-css-transforms="true"
      :is-draggable="false"
      :is-resizable="false"
      style="position: relative"
    >
      <template #item="{ item }">
        <div
          class="desk"
          :class="{ static: item.static }"
          @click="handleDeskClick(item)"
        >
          <div class="desk-inner">
            <span class="text">{{ item.i }}</span>
          </div>
        </div>
      </template>
    </GridLayout>

    <BookingModal
      v-model="showBookingModal"
      :desk="selectedDesk"
      :is-booked="selectedDesk ? isDeskBooked(selectedDesk.i) : false"
      :existing-booking="selectedDesk ? getExistingBooking(selectedDesk.i) : undefined"
      @confirm="handleConfirmBooking"
      @cancel="handleCancelBooking"
    />
  </div>
</template>

<style scoped>
.floorplan-container {
  width: 987px;
  height: 643px;
  margin: 20px auto;
  position: relative;
  overflow: hidden;
  background: url("/floorplan/Floor.png") center/contain no-repeat;
  background-color: #f0f0f0;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
}

.no-anim :deep(.vgl-item) {
  transition: none !important;
}

:deep(.vgl-item--static) {
  border: none !important;
  background-color: #333 !important;
}

:deep(.vgl-item:not(.vgl-item--static)) {
  border: 2px solid #d1d5db;
  background: linear-gradient(135deg, #ffffff 0%, #f9fafb 100%);
  border-radius: 10px;
  position: relative;
  overflow: visible;
  cursor: pointer;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1), 0 1px 3px rgba(0, 0, 0, 0.06);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.desk {
  position: relative;
  width: 100%;
  height: 100%;
}

.desk-inner {
  position: relative;
  width: 100%;
  padding-top: 100%;        
  border-radius: 10px;
}

.desk-inner .text {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 19px;
  color: #1e293b;
  font-weight: 800;
  user-select: none;
  pointer-events: none;
  text-shadow: 0 1px 2px rgba(255, 255, 255, 0.5);
}

:deep(.vgl-item:not(.vgl-item--static):hover) {
  border-color: #3b82f6;
  background: linear-gradient(135deg, #eff6ff 0%, #dbeafe 100%);
  box-shadow: 0 6px 16px rgba(59, 130, 246, 0.2),
              0 3px 8px rgba(59, 130, 246, 0.15);
  transform: translateY(-2px);
}

:deep(.vgl-item:not(.vgl-item--static):hover) .text {
  color: #1e40af;
  text-shadow: 0 2px 3px rgba(59, 130, 246, 0.1);
}

:deep(.vgl-item:not(.vgl-item--static):active) {
  transform: translateY(0) scale(0.97);
  transition: transform 0.1s ease;
}
</style>
