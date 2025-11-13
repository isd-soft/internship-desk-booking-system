<template>
  <v-dialog
    :model-value="modelValue"
    @update:model-value="emit('update:modelValue', $event)"
    max-width="520"
    transition="dialog-bottom-transition"
    persistent
  >
    <v-card class="booking-card">
      <!-- 🔹 Header -->
      <v-card-title class="card-header">
        <div class="header-content">
          <div class="header-info">
            <div class="workspace-label">WORKSPACE</div>
            <div class="desk-title">
              {{ desk?.deskName || `Desk ${desk?.i}` }}
            </div>

            <!-- 📅 Вывод даты -->
            <div class="date-badge">
              <v-icon size="16" class="date-icon">mdi-calendar</v-icon>
              <div class="date-content">
                <div class="date-day">{{ formattedDay }}</div>
                <div class="date-full">{{ formattedDateShort }}</div>
              </div>
            </div>
          </div>
          <v-btn icon variant="text" size="small" @click="closeModal">
            <v-icon size="20">mdi-close</v-icon>
          </v-btn>
        </div>
      </v-card-title>

      <!-- 🔹 Time Selection -->
      <v-card-text class="card-body">
        <div class="section dark-slider">
          <div class="section-title">Select Time Range</div>

          <!-- Интерактивная временная шкала -->
          <div class="timeline-container">
            <div
              ref="timelineRef"
              class="timeline-track"
              @mousedown="handleTrackClick"
            >
              <!-- Выбранный диапазон -->
              <div
                class="timeline-range"
                :style="{
                  left: `${startPosition}%`,
                  width: `${endPosition - startPosition}%`,
                }"
              />

              <!-- Ручка начала -->
              <div
                class="timeline-handle"
                :style="{ left: `${startPosition}%` }"
                @mousedown.stop="startDrag('start', $event)"
              >
                <div class="handle-time">{{ bookingForm.startHour }}:00</div>
              </div>

              <!-- Ручка конца -->
              <div
                class="timeline-handle"
                :style="{ left: `${endPosition}%` }"
                @mousedown.stop="startDrag('end', $event)"
              >
                <div class="handle-time">{{ bookingForm.endHour }}:00</div>
              </div>
            </div>

            <!-- Часовые метки -->
            <div class="timeline-labels">
              <div
                v-for="hour in hours"
                :key="hour"
                class="timeline-label"
                :style="{ left: `${getHourPosition(hour)}%` }"
              >
                {{ hour }}:00
              </div>
            </div>
          </div>

          <!-- Отображение выбранного времени -->
          <div class="time-range-display">
            <div class="time-range-label">Selected</div>
            <div class="time-range-value">{{ formattedTimeRange }}</div>
            <div class="time-duration">
              {{ duration }} {{ duration === 1 ? "hour" : "hours" }}
            </div>
          </div>
        </div>

        <!-- ❤️ Favourite button -->
        <button
          @click.stop="toggleFavourite"
          :disabled="isProcessing || !deskId"
          :class="['favourite-button', { active: isFavourite }]"
        >
          <v-icon size="20">
            {{ isFavourite ? "mdi-heart" : "mdi-heart-outline" }}
          </v-icon>
          <span>
            {{ isFavourite ? "Remove from Favourites" : "Add to Favourites" }}
          </span>
        </button>
      </v-card-text>

      <!-- 🔹 Actions -->
      <v-card-actions class="card-actions">
        <v-btn
          class="confirm-button"
          size="x-large"
          @click.stop="confirmBooking"
        >
          {{ isBooked ? "Update Booking" : "Confirm Booking" }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted, onUnmounted } from "vue";
import api from "@/plugins/axios";
import { useFavouritesStore } from "@/stores/favourites";

interface Props {
  modelValue: boolean;
  desk: any;
  isBooked: boolean;
  selectedDateISO: string;
}

const props = defineProps<Props>();
const emit = defineEmits(["update:modelValue", "created", "cancel"]);

const favStore = useFavouritesStore();
const isProcessing = ref(false);
const bookingForm = reactive({ startHour: 9, endHour: 18 });

const MIN_HOUR = 9;
const MAX_HOUR = 18;
const hours = Array.from(
  { length: MAX_HOUR - MIN_HOUR + 1 },
  (_, i) => MIN_HOUR + i
);

const timelineRef = ref<HTMLElement | null>(null);
const isDragging = ref<"start" | "end" | null>(null);

const deskId = computed(() => Number(props.desk?.i ?? 0));
const isFavourite = computed(() => favStore.isFav(deskId.value));

const duration = computed(() => bookingForm.endHour - bookingForm.startHour);

const startPosition = computed(
  () => ((bookingForm.startHour - MIN_HOUR) / (MAX_HOUR - MIN_HOUR)) * 100
);

const endPosition = computed(
  () => ((bookingForm.endHour - MIN_HOUR) / (MAX_HOUR - MIN_HOUR)) * 100
);

watch(
  () => props.modelValue,
  async (open) => {
    if (open) await favStore.ensureLoaded();
  }
);

function getHourPosition(hour: number): number {
  return ((hour - MIN_HOUR) / (MAX_HOUR - MIN_HOUR)) * 100;
}

function startDrag(type: "start" | "end", event: MouseEvent) {
  event.preventDefault();
  isDragging.value = type;
}

function handleMouseMove(event: MouseEvent) {
  if (!isDragging.value || !timelineRef.value) return;

  const rect = timelineRef.value.getBoundingClientRect();
  const x = Math.max(0, Math.min(event.clientX - rect.left, rect.width));
  const percentage = x / rect.width;
  const hour = Math.round(MIN_HOUR + percentage * (MAX_HOUR - MIN_HOUR));

  if (isDragging.value === "start") {
    const newStart = Math.max(
      MIN_HOUR,
      Math.min(hour, bookingForm.endHour - 1)
    );
    bookingForm.startHour = newStart;
  } else if (isDragging.value === "end") {
    const newEnd = Math.max(
      bookingForm.startHour + 1,
      Math.min(hour, MAX_HOUR)
    );
    bookingForm.endHour = newEnd;
  }
}

function handleMouseUp() {
  isDragging.value = null;
}

function handleTrackClick(event: MouseEvent) {
  if (!timelineRef.value) return;

  const rect = timelineRef.value.getBoundingClientRect();
  const x = event.clientX - rect.left;
  const percentage = x / rect.width;
  const hour = Math.round(MIN_HOUR + percentage * (MAX_HOUR - MIN_HOUR));

  const midPoint = (bookingForm.startHour + bookingForm.endHour) / 2;

  if (hour < midPoint) {
    bookingForm.startHour = Math.max(
      MIN_HOUR,
      Math.min(hour, bookingForm.endHour - 1)
    );
  } else {
    bookingForm.endHour = Math.max(
      bookingForm.startHour + 1,
      Math.min(hour, MAX_HOUR)
    );
  }
}

onMounted(() => {
  document.addEventListener("mousemove", handleMouseMove);
  document.addEventListener("mouseup", handleMouseUp);
});

onUnmounted(() => {
  document.removeEventListener("mousemove", handleMouseMove);
  document.removeEventListener("mouseup", handleMouseUp);
});

function closeModal() {
  emit("update:modelValue", false);
}

async function toggleFavourite() {
  if (!deskId.value) return;
  isProcessing.value = true;
  await favStore.toggle(deskId.value);
  isProcessing.value = false;
}

const formattedDate = computed(() => {
  if (!props.selectedDateISO) return "";
  const d = new Date(props.selectedDateISO);
  return d.toLocaleDateString("en-GB", {
    weekday: "long",
    day: "numeric",
    month: "long",
    year: "numeric",
  });
});

const formattedDay = computed(() => {
  if (!props.selectedDateISO) return "";
  const d = new Date(props.selectedDateISO);
  return d.toLocaleDateString("en-GB", { weekday: "short" }).toUpperCase();
});

const formattedDateShort = computed(() => {
  if (!props.selectedDateISO) return "";
  const d = new Date(props.selectedDateISO);
  return d.toLocaleDateString("en-GB", {
    day: "numeric",
    month: "short",
    year: "numeric",
  });
});

const formattedTimeRange = computed(() => {
  const pad = (v: number) => String(v).padStart(2, "0");
  return `${pad(bookingForm.startHour)}:00 - ${pad(bookingForm.endHour)}:00`;
});

async function confirmBooking() {
  if (!deskId.value) return;

  const startTime = `${props.selectedDateISO}T${String(
    bookingForm.startHour
  ).padStart(2, "0")}:00:00`;
  const endTime = `${props.selectedDateISO}T${String(
    bookingForm.endHour
  ).padStart(2, "0")}:00:00`;

  try {
    await api.post("/booking", {
      deskId: deskId.value,
      startTime,
      endTime,
    });
    
    emit("created", { deskId: deskId.value, success: true });
    closeModal();
  } catch (err) {
    console.error("Booking error:", err);
  }
}
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap");

.booking-card {
  border-radius: 20px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.15);
  overflow: hidden;
}

.card-header {
  padding: 26px 28px 16px;
  border-bottom: 1px solid #f0f0f0;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.workspace-label {
  font-size: 11px;
  font-weight: 700;
  color: #737373;
  letter-spacing: 1.5px;
}

.desk-title {
  font-size: 26px;
  font-weight: 800;
  color: #171717;
  margin-bottom: 12px;
}

.date-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 14px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 10px;
  border: 1px solid #e0e0e0;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.04);
}

.date-icon {
  color: #666;
}

.date-content {
  display: flex;
  flex-direction: column;
  gap: 1px;
}

.date-day {
  font-size: 10px;
  font-weight: 800;
  color: #666;
  letter-spacing: 0.8px;
  line-height: 1;
}

.date-full {
  font-size: 13px;
  font-weight: 600;
  color: #171717;
  line-height: 1.2;
}

.card-body {
  padding: 20px 28px 28px;
}

.dark-slider {
  background: linear-gradient(135deg, #0f0f0f 0%, #1a1a1a 100%);
  border-radius: 16px;
  padding: 24px;
  color: #fff;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.06),
    0 4px 16px rgba(0, 0, 0, 0.35);
}

.section-title {
  font-weight: 600;
  font-size: 15px;
  margin-bottom: 24px;
  letter-spacing: 0.5px;
}

.timeline-container {
  margin-bottom: 24px;
}

.timeline-track {
  position: relative;
  height: 12px;
  background: #2a2a2a;
  border-radius: 10px;
  cursor: pointer;
  margin-bottom: 16px;
}

.timeline-range {
  position: absolute;
  height: 100%;
  background: linear-gradient(90deg, #ffffff 0%, #e0e0e0 100%);
  border-radius: 10px;
  transition: all 0.15s ease;
  pointer-events: none;
}

.timeline-handle {
  position: absolute;
  top: 50%;
  transform: translate(-50%, -50%);
  width: 28px;
  height: 28px;
  background: #ffffff;
  border: 3px solid #0f0f0f;
  border-radius: 50%;
  cursor: grab;
  transition: all 0.2s ease;
  z-index: 10;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

.timeline-handle:hover {
  transform: translate(-50%, -50%) scale(1.15);
  box-shadow: 0 4px 12px rgba(255, 255, 255, 0.2);
}

.timeline-handle:active {
  cursor: grabbing;
  transform: translate(-50%, -50%) scale(1.1);
}

.handle-time {
  position: absolute;
  top: -32px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(255, 255, 255, 0.95);
  color: #0f0f0f;
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 700;
  white-space: nowrap;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  pointer-events: none;
}

.timeline-labels {
  position: relative;
  height: 20px;
  margin-top: 8px;
}

.timeline-label {
  position: absolute;
  transform: translateX(-50%);
  font-size: 11px;
  color: #888;
  font-weight: 600;
  white-space: nowrap;
}

.time-range-display {
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  text-align: center;
}

.time-range-label {
  font-size: 11px;
  font-weight: 700;
  color: #999;
  letter-spacing: 1px;
  text-transform: uppercase;
  margin-bottom: 8px;
}

.time-range-value {
  font-size: 28px;
  font-weight: 800;
  color: #fff;
  letter-spacing: -0.5px;
  margin-bottom: 6px;
}

.time-duration {
  font-size: 13px;
  font-weight: 600;
  color: #888;
}

.favourite-button {
  width: 100%;
  padding: 14px 20px;
  border: 2px solid #e5e5e5;
  border-radius: 12px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-top: 20px;
  transition: all 0.3s ease;
  font-weight: 600;
  color: #171717;
  cursor: pointer;
}

.favourite-button:hover:not(:disabled) {
  border-color: #a3a3a3;
  background: #fafafa;
  transform: translateY(-1px);
}

.favourite-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.favourite-button.active {
  background: #fef2f2;
  border-color: #fca5a5;
  color: #dc2626;
}

.favourite-button.active:hover:not(:disabled) {
  background: #fee2e2;
}

.card-actions {
  padding: 0 28px 28px;
}

.confirm-button {
  width: 100%;
  background: #171717 !important;
  color: #ffffff !important;
  font-weight: 700 !important;
  text-transform: none !important;
  letter-spacing: 0.3px;
  padding: 0 40px !important;
  height: 52px !important;
  border-radius: 12px !important;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.16) !important;
  transition: all 0.3s ease;
}

.confirm-button:hover {
  background: #262626 !important;
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.2) !important;
}
</style>