<script setup lang="ts">
import { onMounted, ref, onBeforeUnmount, computed, watch } from "vue";
import {
  layout,
  rowHeight,
  loadDesksFromBackend,
  resetLayout,
  horizontalDesks,
  loadAllColors,
  selectedDate as sharedSelectedDate,
  imageUrl,
  getImageFromBackend,
  imageDimensions,
} from "../VisualFloorMap/floorLayout";
import BookingModal from "../VisualFloorMap/BookingModal.vue";
import { useFavouritesStore } from "@/stores/favourites";

const props = defineProps({
  selectedDateISO: {
    type: String,
    required: true,
  },
});

const emit = defineEmits([
  "booking-created",
  "favourite-toggled",
  "booking-cancelled",
]);

const favStore = useFavouritesStore();

const container = ref<HTMLElement | null>(null);
const containerWidth = ref<number>(imageDimensions.value.width);
const containerHeightAvailable = ref<number | null>(null);
const scale = ref<number>(1);
const isMobileView = ref(window.innerWidth <= 768);
const showLegend = ref(!isMobileView.value);

// dont remove please !!
const scaledContainerHeight = computed(() =>
  Math.round(imageDimensions.value.height * scale.value)
);
// dont remove please !!
const scaledHeightCompensation = computed(() =>
  scale.value >= 1 ? 0 : Math.round(imageDimensions.value.height * (1 - scale.value))
);

onMounted(async () => {
  resetLayout();
  loadDesksFromBackend();
  getImageFromBackend(1);
  console.log(imageUrl);
  await favStore.ensureLoaded();

  const updateScale = () => {
    if (!container.value) return;

    const availW = container.value.clientWidth;
    const availH = container.value.clientHeight;

    if (!availW || !availH) {
      return;
    }

    const scaleByWidth = availW / imageDimensions.value.width;
    const scaleByHeight = availH / imageDimensions.value.height;
    const newScale = Math.max(0.1, Math.min(scaleByWidth, scaleByHeight));
    scale.value = newScale;
  };

  const handleResize = () => {
    isMobileView.value = window.innerWidth <= 768;
    if (isMobileView.value && !showLegend.value) {
      showLegend.value = true;
    }
    requestAnimationFrame(() => {
      if (container.value) {
        containerWidth.value = container.value.clientWidth;
        containerHeightAvailable.value = container.value.clientHeight;
        updateScale();
      }
    });
  };

  if (container.value) {
    containerWidth.value = container.value.clientWidth || imageDimensions.value.width;
    containerHeightAvailable.value = container.value.clientHeight || null;
    updateScale();
  }

  const ro = new ResizeObserver((entries) => {
    for (const entry of entries) {
      const { width, height } = entry.contentRect;
      if (width) {
        containerWidth.value = width;
      }
      if (height) {
        containerHeightAvailable.value = height;
      }
    }
    requestAnimationFrame(() => {
      updateScale();
    });
  });

  if (container.value) ro.observe(container.value);
  window.addEventListener("resize", handleResize);

  onBeforeUnmount(() => {
    try {
      ro.disconnect();
    } catch (e) {}
    window.removeEventListener("resize", handleResize);
  });
});

const showBookingModal = ref(false);
const selectedDesk = ref<any>(null);
const bookedDesks = ref<Set<string>>(new Set());

function isDeskFavourite(id: string | number) {
  return favStore.isFav(Number(id));
}

function handleDeskClick(item: any) {
  if (item.isNonInteractive) return;
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
      return "#818589	";
    case "PURPLE":
      return "#E1BEE7";
    default:
      return "";
  }
}

function isDeskBooked(id: string) {
  return bookedDesks.value.has(id);
}

function handeCancelItem(data: { deskId: number; color: string }) {
  const desk = layout.find((d: any) => Number(d.i) === data.deskId);

  if (desk) {
    desk.color = data.color;
  }
}

function handleCreated(data: { deskId: number; success: boolean }) {
  const desk = layout.find((d: any) => Number(d.i) === data.deskId);

  if (desk) {
    desk.color = "PURPLE";
  }
  console.log("[Map] booking created → refresh data");
  emit("booking-created", data);
  showBookingModal.value = false;
}

function handleFavouriteToggled(payload: any) {
  console.log("[Map] favourite toggled → notifying parent");
  emit("favourite-toggled", payload);
}

async function handleCancelBooking(payload?: { deskId?: number }) {
  showBookingModal.value = false;
  const targetDeskId = payload?.deskId ?? Number(selectedDesk.value?.i);

  if (targetDeskId) {
    const desk = layout.find((d: any) => Number(d.i) === targetDeskId);
    if (desk) {
      desk.color = "GREEN";
    }
  }

  try {
    await loadAllColors();
  } catch (error) {
    console.warn("[Map] Failed to refresh colors after cancellation", error);
  }

  console.log("[Map] booking cancelled → notifying parent");
  emit("booking-cancelled", payload);
}

watch(
  () => props.selectedDateISO,
  async (newDate) => {
    if (!newDate) return;
    console.log("[Map] Date changed to:", newDate);
    try {
      sharedSelectedDate.value = newDate;
      await loadAllColors();
      console.log("[Map] Colors reloaded successfully");
    } catch (e) {
      console.warn("[Map] Could not reload colors on date change", e);
    }
  }
);
</script>

<template>
  <div ref="container" class="floorplan-container no-anim">
    <div
      class="floorplan-inner"
      :style="{
        width: imageDimensions.width + 'px',
        height: imageDimensions.height + 'px',
        transform: 'scale(' + scale + ')',
        transformOrigin: 'center center',
      }"
    >
      <img
        :src="imageUrl"
        alt="Office floor plan"
        class="floorplan-bg"
        draggable="false"
      />
      <GridLayout
        v-model:layout="layout"
        :col-num="imageDimensions.width"
        :row-height="rowHeight"
        :width="imageDimensions.width"
        :max-rows="imageDimensions.height"
        :margin="[0, 0]"
        :responsive="false"
        :vertical-compact="false"
        prevent-collision
        :use-css-transforms="true"
        :is-draggable="false"
        :is-resizable="false"
        style="position: relative; width: 100%; height: 100%"
      >
        <template #item="{ item }">
          <div
            class="desk"
            :class="{
              static: item.isNonInteractive,
              favourite: isDeskFavourite(item.i),
              vertical: !(item.w >= item.h),
              'non-interactive': item.isNonInteractive,
            }"
            @click="handleDeskClick(item)"
            :style="{
              backgroundColor: getDeskColor(item.color),
              cursor: item.isNonInteractive ? 'default' : 'pointer',
            }"
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
    </div>

    <BookingModal
      v-model="showBookingModal"
      :desk="selectedDesk"
      :is-booked="selectedDesk ? isDeskBooked(selectedDesk.i) : false"
      :selectedDateISO="props.selectedDateISO"
      :office-start-hour="8"
      :office-end-hour="18"
      @created="handleCreated"
      @cancel="handleCancelBooking"
      @favourite-toggled="handleFavouriteToggled"
    />
  </div>
</template>

<style scoped>
.desk.non-interactive {
  pointer-events: none !important;
  cursor: default !important;
}

:deep(.vgl-item:has(.desk.non-interactive)) {
  pointer-events: none !important;
  cursor: default !important;
}

:deep(.vgl-item:has(.desk.non-interactive):hover) {
  transform: none !important;
  border-color: #d1d5db !important;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1), 0 1px 3px rgba(0, 0, 0, 0.06) !important;
  background: linear-gradient(135deg, #ffffff 0%, #f9fafb 100%) !important;
}

.floorplan-container {
  width: 100%;
  height: 100%;
  position: relative;
  overflow: visible;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0;
  padding: 0;
}

.floorplan-inner {
  position: relative;
  background: #f4f6fb;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
  transform-origin: center center;
  flex-shrink: 0;
  margin: 0;
  padding: 0;
}

.floorplan-bg {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  pointer-events: none;
  user-select: none;
  image-rendering: -webkit-optimize-contrast;
  image-rendering: crisp-edges;
  image-rendering: pixelated;
  filter: saturate(1.05) contrast(1.08);
  z-index: 0;
}

:deep(.vue-grid-layout) {
  position: relative;
  z-index: 1;
}

:deep(.vgl-item:has(.desk.favourite)) {
  z-index: 1000 !important;
  position: relative;
}

.no-anim :deep(.vgl-item) {
  transition: none !important;
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
  cursor: pointer;
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

.favourite-badge {
  position: absolute;
  top: -6px;
  right: -6px;
  width: 14px;
  height: 14px;
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 6px rgba(239, 68, 68, 0.4), 0 0 0 2px #ffffff;
  z-index: 99999;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  pointer-events: none;
}

.heart-icon {
  width: 8px;
  height: 8px;
  color: #ffffff;
  filter: drop-shadow(0 0.5px 1px rgba(0, 0, 0, 0.3));
}

.desk.favourite {
  position: relative;
  z-index: 1;
}

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

.desk.favourite:hover .favourite-badge {
  transform: scale(1.3) rotate(8deg);
  box-shadow: 0 3px 8px rgba(239, 68, 68, 0.5), 0 0 0 2px #ffffff;
}

.legend-container {
  position: absolute;
  bottom: 16px;
  right: 16px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  z-index: 50;
  max-width: 280px;
  transition: all 0.3s ease;
}

.legend-toggle {
  width: 100%;
  padding: 10px 12px;
  background: none;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #666;
  font-weight: 600;
  font-size: 12px;
  border-radius: 12px;
  transition: all 0.2s ease;
}

.legend-toggle:hover {
  background: rgba(0, 0, 0, 0.04);
}

.legend-items {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #4b5563;
  font-weight: 500;
}

.legend-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.legend-container.hidden .legend-items {
  display: none;
}

@media (max-width: 1024px) and (min-width: 600px) {
  .floorplan-container {
    max-width: 820px;
  }
}

@media (max-width: 1189px) {
  .legend-container {
    position: fixed;
    bottom: 16px;
    right: 16px;
    max-width: 85vw;
    min-width: 280px;
    display: flex;
    flex-direction: column;
    z-index: 100;
  }

  .legend-container.hidden {
    display: none !important;
  }

  .legend-toggle {
    display: none !important;
  }

  .legend-items {
    gap: 8px;
    padding: 12px;
    border-top: none;
    display: flex;
    flex-direction: column;
  }

  .legend-item {
    font-size: 12px;
    font-weight: 500;
  }

  .legend-dot {
    width: 8px;
    height: 8px;
  }
}

@media (max-width: 480px) {
  .legend-container {
    bottom: 12px;
    right: 12px;
    min-width: 260px;
  }

  .legend-items {
    gap: 6px;
    padding: 10px;
  }

  .legend-item {
    font-size: 11px;
  }

  .legend-dot {
    width: 7px;
    height: 7px;
  }
}

@media (max-width: 1189px) {
  .floorplan-container {
    margin: 0;
    max-width: 100%;
    flex-shrink: 0;
    padding: 0;
    align-items: flex-start;
    height: auto;
    min-height: 0;
  }

  .floorplan-inner {
    box-shadow: none;
    margin: 0;
  }
}
</style>
