<script setup lang="ts">
import { onMounted, ref } from "vue";
import {
  layout,
  colNum,
  rowHeight,
  totalRows,
  IMAGE_WIDTH_PX,
  loadDesksFromBackend,
  resetLayout,
  horizontalDesks,
} from "../VisualFloorMap/floorLayout";
import BookingModal from "../VisualFloorMap/BookingModal.vue";
import { useFavouritesStore } from "@/stores/favourites";

const props = defineProps({
  selectedDateISO: {
    type: String,
    required: true,
  },
});

const favStore = useFavouritesStore();

onMounted(async () => {
  resetLayout();
  loadDesksFromBackend();
  await favStore.ensureLoaded();
});

const showBookingModal = ref(false);
const selectedDesk = ref<any>(null);
const bookedDesks = ref<Set<string>>(new Set());

// Дата из календаря (ISO): по умолчанию сегодня
// const selectedDateISO = ref<string>(new Date().toISOString().slice(0, 10));

// Если у тебя есть отдельный DatePicker, просто привяжи его так:
// <DatePicker @update:date="(v:string)=> selectedDateISO.value = v" />

function isDeskFavourite(id: string | number) {
  return favStore.isFav(Number(id));
}

function handleDeskClick(item: any) {
  if (item.static) return;
  console.log("Clicked desk:", item.i);
  selectedDesk.value = item;
  showBookingModal.value = true;
}

function getDeskColor(color: string) {
  switch (color) {
    case "GREEN":
      return "#50C878";
    case "RED":
      return "#EE4B2B";
    case "AMBER":
      return "#FFBF00";
    case "BLUE":
      return "#7393B3";
    case "GRAY":
      return "#818589";
    default:
      return "";
  }
}

function isDeskBooked(id: string) {
  return bookedDesks.value.has(id);
}

function handleCreated() {
  // тут можно перезагрузить данные/цвета/лист букингов
  console.log("[Map] booking created → refresh data");
  showBookingModal.value = false;
}

function handleCancelBooking() {
  showBookingModal.value = false;
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
          :class="{
            static: item.static,
            favourite: isDeskFavourite(item.i),
            vertical: !horizontalDesks.includes(Number(item.i)),
          }"
          @click="handleDeskClick(item)"
          :style="{ backgroundColor: getDeskColor(item.color) }"
        >
          <span class="text">{{ item.deskName || item.i }}</span>
          <div v-if="isDeskFavourite(item.i)" class="favourite-badge">
            <svg
              xmlns="http://www.w3.org/2000/svg"
              viewBox="0 0 24 24"
              fill="currentColor"
              class="heart-icon"
            >
              <path
                d="M11.645 20.91l-.007-.003-.022-.012a15.247 15.247 0 01-.383-.218 25.18 25.18 0 01-4.244-3.17C4.688 15.36 2.25 12.174 2.25 8.25 2.25 5.322 4.714 3 7.688 3A5.5 5.5 0 0112 5.052 5.5 5.5 0 0116.313 3c2.973 0 5.437 2.322 5.437 5.25 0 3.925-2.438 7.111-4.739 9.256a25.175 25.175 0 01-4.244 3.17 15.247 15.247 0 01-.383.219l-.022.012-.007.004-.003.001a.752.752 0 01-.704 0l-.003-.001z"
              />
            </svg>
          </div>
        </div>
      </template>
    </GridLayout>

    <BookingModal
      v-model="showBookingModal"
      :desk="selectedDesk"
      :is-booked="selectedDesk ? isDeskBooked(selectedDesk.i) : false"
      :selected-date-i-s-o="props.selectedDateISO"
      :office-start-hour="8"
      :office-end-hour="18"
      @created="handleCreated"
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
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  border-radius: 10px;
  border: 2px solid #d1d5db;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1), 0 1px 3px rgba(0, 0, 0, 0.06);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.desk.vertical .text {
  writing-mode: vertical-rl;
  text-orientation: mixed;
  transform: rotate(180deg);
  font-size: 8px;
  text-shadow: 0 1px 2px rgba(255, 255, 255, 0.5);
  letter-spacing: 0.3px;
  font-weight: 800;
  white-space: nowrap;
}

/* Улучшенный hover эффект */
:deep(.vgl-item:not(.vgl-item--static):hover) {
  border-color: #3b82f6;
  background: linear-gradient(135deg, #eff6ff 0%, #dbeafe 100%);
  box-shadow: 0 8px 20px rgba(59, 130, 246, 0.25),
    0 4px 10px rgba(59, 130, 246, 0.15);
  transform: translateY(-3px) scale(1.02);
}

.text {
  font-size: 8px;
  pointer-events: none;
  color: #1e293b;
  font-weight: 800;
  user-select: none;
  transition: color 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  letter-spacing: 0.3px;
  position: relative;
  z-index: 1;
  text-shadow: 0 1px 2px rgba(255, 255, 255, 0.5);
}

:deep(.vgl-item:not(.vgl-item--static):hover) .text {
  color: #1e40af;
  text-shadow: 0 2px 3px rgba(59, 130, 246, 0.1);
}

:deep(.vgl-item:not(.vgl-item--static):active) {
  transform: translateY(-1px) scale(0.98);
  transition: transform 0.1s ease;
}

/* Улучшенная иконка избранного - над десками */
.favourite-badge {
  position: absolute;
  top: -10px;
  right: -10px;
  width: 20px;
  height: 20px;
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(239, 68, 68, 0.5), 0 0 0 3px #ffffff,
    0 1px 3px rgba(0, 0, 0, 0.1);
  z-index: 100;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  pointer-events: none;
}

.heart-icon {
  width: 10px;
  height: 10px;
  color: #ffffff;
  filter: drop-shadow(0 1px 1px rgba(0, 0, 0, 0.2));
}

/* Мягкое свечение для избранных десков */
.desk.favourite::before {
  content: "";
  position: absolute;
  inset: -2px;
  border-radius: 10px;
  background: linear-gradient(135deg, #ef4444, #f97316, #ef4444);
  background-size: 200% 200%;
  opacity: 0;
  z-index: -1;
  animation: gradientShift 3s ease infinite;
  transition: opacity 0.3s ease;
}

.desk.favourite:hover::before {
  opacity: 0.15;
}

@keyframes gradientShift {
  0%,
  100% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
}

/* Улучшенный hover для избранных десков */
.desk.favourite:hover .favourite-badge {
  transform: scale(1.2) rotate(5deg);
  box-shadow: 0 3px 10px rgba(239, 68, 68, 0.5), 0 1px 4px rgba(0, 0, 0, 0.15);
}
</style>
