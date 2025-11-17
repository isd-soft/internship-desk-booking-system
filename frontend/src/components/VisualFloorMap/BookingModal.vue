<template>
  <v-dialog
    :model-value="modelValue"
    @update:model-value="emit('update:modelValue', $event)"
    max-width="520"
    transition="dialog-bottom-transition"
    persistent
  >
    <v-card class="booking-card">
      <v-card-title class="card-header">
        <div class="header-content">
          <div class="header-info">
            <div class="workspace-label">WORKSPACE</div>
            <div class="desk-title">
              {{ desk?.deskName || `Desk ${desk?.i}` }}
            </div>

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

      <v-card-text class="card-body">
        <div class="section dark-slider">
          <div class="section-title">Select Time Range</div>

          <div class="timeline-container">
            <div
              ref="timelineRef"
              class="timeline-track"
              @mousedown="handleTrackClick"
            >
              <div
                class="timeline-range"
                :style="{
                  left: `${startPosition}%`,
                  width: `${endPosition - startPosition}%`,
                }"
              />

              <div
                class="timeline-handle"
                :style="{ left: `${startPosition}%` }"
                @mousedown.stop="startDrag('start', $event)"
              >
                <div class="handle-time">{{ bookingForm.startHour }}:00</div>
              </div>

              <div
                class="timeline-handle"
                :style="{ left: `${endPosition}%` }"
                @mousedown.stop="startDrag('end', $event)"
              >
                <div class="handle-time">{{ bookingForm.endHour }}:00</div>
              </div>
            </div>

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

          <div class="time-range-display">
            <div class="time-range-label">Selected</div>
            <div class="time-range-value">{{ formattedTimeRange }}</div>
            <div class="time-duration">
              {{ duration }} {{ duration === 1 ? "hour" : "hours" }}
            </div>
          </div>

          <transition name="slide-fade">
            <div v-if="lunchOverlap" class="modern-alert lunch-alert">
              <div class="alert-icon lunch-icon">
                <v-icon size="20">mdi-information-outline</v-icon>
              </div>
              <div class="alert-content">
                <div class="alert-title">Lunch Hours</div>
                <div class="alert-message">
                  This time includes lunch hours (13:00 - 14:00). Workspace may
                  be less available.
                </div>
              </div>
            </div>
          </transition>
        </div>

        <button
          type="button"
          @click.stop.prevent="toggleFavourite"
          :disabled="isFavouriteProcessing || !deskId"
          :class="[
            'favourite-button',
            { active: isFavourite, loading: isFavouriteProcessing },
          ]"
        >
          <v-icon size="20">
            {{ isFavourite ? "mdi-heart" : "mdi-heart-outline" }}
          </v-icon>
          <span>
            {{ isFavourite ? "Remove from Favourites" : "Add to Favourites" }}
          </span>
          <v-progress-circular
            v-if="isFavouriteProcessing"
            indeterminate
            size="16"
            width="2"
            class="fav-spinner"
          />
        </button>
      </v-card-text>

      <v-card-actions class="card-actions">
        <div style="width: 100%">
          <div
            v-if="bookingErrors.length"
            class="error-list"
            style="margin-bottom: 12px"
          >
            <div
              ref="errorContainer"
              class="modern-alert error-alert"
              role="alert"
              aria-live="assertive"
              tabindex="-1"
            >
              <div class="alert-icon error-icon">
                <v-icon size="20">mdi-close-circle-outline</v-icon>
              </div>
              <div class="alert-content">
                <div class="alert-title">Unable to Book</div>
                <div
                  v-for="(m, idx) in bookingErrors"
                  :key="idx"
                  class="alert-message"
                >
                  <div v-if="m.length <= 200 || expandedMessages.has(idx)">
                    {{ m }}
                  </div>
                  <div v-else>
                    {{ m.slice(0, 200) }}…
                    <a href="#" @click.prevent="toggleExpand(idx)">Show more</a>
                  </div>
                </div>

                <div class="error-actions"></div>
              </div>
            </div>
          </div>

          <v-btn
            :disabled="isProcessing"
            class="confirm-button"
            size="x-large"
            @click.stop="confirmBooking"
          >
            {{ isBooked ? "Update Booking" : "Confirm Booking" }}
            <template v-if="isProcessing">
              <v-progress-circular
                indeterminate
                size="18"
                width="3"
                class="ml-3"
              />
            </template>
          </v-btn>
        </div>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import {
  ref,
  reactive,
  computed,
  watch,
  onMounted,
  onUnmounted,
  nextTick,
} from "vue";
import api from "@/plugins/axios";
import { useFavouritesStore } from "@/stores/favourites";

interface Props {
  modelValue: boolean;
  desk: any;
  isBooked: boolean;
  selectedDateISO: string;
}

const props = defineProps<Props>();
const emit = defineEmits(["update:modelValue", "created", "cancel", "favourite-toggled"]);

const favStore = useFavouritesStore();
const isProcessing = ref(false);
const isFavouriteProcessing = ref(false);
const bookingErrors = ref<string[]>([]);
const errorContainer = ref<HTMLElement | null>(null);
const expandedMessages = ref<Set<number>>(new Set());

function mapBackendCode(code: string) {
  const map: Record<string, string> = {
    TIME_CONFLICT: "Selected time conflicts with an existing booking.",
    INVALID_HOURS: "Selected hours are outside office hours.",
    DESK_UNAVAILABLE: "The desk is currently unavailable.",
    TEMPORARY_WINDOW:
      "This desk has temporary availability windows — please choose another time.",
    WEEKEND_RESTRICTION: "Bookings are not allowed on weekends.",
    MIN_DURATION: "Booking duration is too short.",
    MAX_DURATION: "Booking duration is too long.",
  };

  return map[code] || null;
}

function parseBookingError(err: any): string[] {
  try {
    const res = err?.response?.data;
    const out: string[] = [];
    if (!res) {
      out.push("Network error. Please check your connection and try again.");
      return out;
    }

    if (res.message && typeof res.message === "string") {
      out.push(res.message);
      return Array.from(new Set(out));
    }

    if (typeof res === "string") {
      out.push(res);
      return Array.from(new Set(out));
    }

    if (res.code) {
      const friendly = mapBackendCode(String(res.code));
      out.push(friendly || String(res.code));
      return Array.from(new Set(out));
    }

    if (Array.isArray(res.errors)) {
      res.errors.forEach((e: any) =>
        out.push(typeof e === "string" ? e : JSON.stringify(e))
      );
      if (out.length > 0) return Array.from(new Set(out));
    }

    if (res.validation && typeof res.validation === "object") {
      Object.values(res.validation).forEach((arr: any) => {
        if (Array.isArray(arr)) out.push(...arr.map((s) => String(s)));
      });
      if (out.length > 0) return Array.from(new Set(out));
    }

    if (
      res.errors &&
      typeof res.errors === "object" &&
      !Array.isArray(res.errors)
    ) {
      Object.values(res.errors).forEach((arr: any) => {
        if (Array.isArray(arr)) out.push(...arr.map((s) => String(s)));
      });
      if (out.length > 0) return Array.from(new Set(out));
    }

    if (out.length === 0)
      out.push("Booking failed. Please try again or contact support.");
    return Array.from(new Set(out));
  } catch (e) {
    return ["Booking failed. Please try again."];
  }
}
const bookingForm = reactive({ startHour: 9, endHour: 18 });

const LUNCH_START = 13;
const LUNCH_END = 14;
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

const lunchOverlap = computed(() => {
  return bookingForm.startHour < LUNCH_END && bookingForm.endHour > LUNCH_START;
});

watch(
  () => props.modelValue,
  async (open) => {
    if (open) {
      bookingErrors.value = [];
      await favStore.ensureLoaded();
    }
  }
);

watch(
  () => [bookingForm.startHour, bookingForm.endHour],
  () => {
    if (bookingErrors.value.length) bookingErrors.value = [];
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
  if (!deskId.value || isFavouriteProcessing.value) return;
  isFavouriteProcessing.value = true;
  try {
    console.log('[BookingModal] Toggling favourite for desk:', deskId.value);
    await favStore.toggle(deskId.value);
    console.log('[BookingModal] Toggle complete, emitting event');
    // Emit event to notify parent to refresh lists
    emit('favourite-toggled', { deskId: deskId.value, isFavourite: favStore.isFav(deskId.value) });
  } catch (e) {
    console.error('[BookingModal] Failed to toggle favourite:', e);
  } finally {
    isFavouriteProcessing.value = false;
  }
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
    isProcessing.value = true;
    bookingErrors.value = [];

    await api.post("/booking", {
      deskId: deskId.value,
      startTime,
      endTime,
    });

    emit("created", { deskId: deskId.value, success: true });
    closeModal();
  } catch (err) {
    console.error("Booking error:", err);
    bookingErrors.value = parseBookingError(err);
    // focus the error container for accessibility
    await nextTick();
    if (errorContainer.value) {
      try {
        errorContainer.value.focus();
      } catch (e) {
        /* ignore */
      }
    }
  } finally {
    isProcessing.value = false;
  }
}

function toggleExpand(idx: number) {
  const s = new Set(expandedMessages.value);
  if (s.has(idx)) s.delete(idx);
  else s.add(idx);
  expandedMessages.value = s;
}

function retryBooking() {
  if (isProcessing.value) return;
  confirmBooking();
}

function contactSupport() {
  const deskLabel = props.desk?.deskName || `Desk ${props.desk?.i || "?"}`;
  const subject = encodeURIComponent(
    `Booking support: ${deskLabel} on ${props.selectedDateISO}`
  );
  const body = encodeURIComponent(
    `Attempted booking:\nDesk: ${deskLabel}\nDate: ${
      props.selectedDateISO
    }\nTime: ${bookingForm.startHour}:00 - ${
      bookingForm.endHour
    }:00\n\nErrors:\n${bookingErrors.value.join("\n")}`
  );
  window.location.href = `mailto:support@yourcompany.com?subject=${subject}&body=${body}`;
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
  transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
  font-weight: 600;
  color: #171717;
  cursor: pointer;
  outline: none;
  -webkit-tap-highlight-color: rgba(0, 0, 0, 0);
  -webkit-touch-callout: none;
  -webkit-user-select: none;
  -moz-user-select: none;
  -ms-user-select: none;
  user-select: none;
  touch-action: manipulation;
  position: relative;
  overflow: hidden;
}

.favourite-button::before {
  content: "";
  position: absolute;
  inset: 0;
  background: radial-gradient(
    circle at center,
    rgba(0, 0, 0, 0.02) 0%,
    transparent 70%
  );
  opacity: 0;
  transition: opacity 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
  pointer-events: none;
}

.favourite-button:focus {
  outline: none;
  box-shadow: none;
}

.favourite-button:focus-visible {
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.2);
}

.favourite-button.loading {
  cursor: wait;
  opacity: 0.85;
}

.fav-spinner {
  margin-left: 6px;
}

.favourite-button:hover:not(:disabled) {
  border-color: #a3a3a3;
  background: #fafafa;
  transform: translateY(-1.5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.favourite-button:hover:not(:disabled)::before {
  opacity: 1;
}

.favourite-button:active:not(:disabled) {
  transform: translateY(0px) scale(0.98);
  transition: all 0.2s cubic-bezier(0.25, 0.8, 0.25, 1);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.06);
}

.favourite-button:active:not(:disabled)::before {
  opacity: 1;
  background: radial-gradient(
    circle at center,
    rgba(0, 0, 0, 0.04) 0%,
    transparent 70%
  );
}

.favourite-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.favourite-button.active {
  background: linear-gradient(135deg, #fef2f2 0%, #fee2e2 100%);
  border-color: #fca5a5;
  color: #dc2626;
  transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.favourite-button.active::before {
  background: radial-gradient(
    circle at center,
    rgba(220, 38, 38, 0.03) 0%,
    transparent 70%
  );
}

.favourite-button.active:hover:not(:disabled) {
  background: linear-gradient(135deg, #fee2e2 0%, #fecaca 100%);
  border-color: #f87171;
  box-shadow: 0 4px 12px rgba(220, 38, 38, 0.15);
  transform: translateY(-1.5px);
}

.favourite-button.active:active:not(:disabled) {
  background: linear-gradient(135deg, #fecaca 0%, #fca5a5 100%);
  transform: translateY(0px) scale(0.98);
  transition: all 0.2s cubic-bezier(0.25, 0.8, 0.25, 1);
  box-shadow: 0 2px 6px rgba(220, 38, 38, 0.1);
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

.lunch-warning-alert {
  margin-top: 20px !important;
  background: rgba(249, 188, 30, 0.08) !important;
  border-color: rgba(249, 188, 30, 0.3) !important;
}

.lunch-warning-alert :deep(.v-alert__content) {
  color: #92400e;
}

.slide-fade-enter-active,
.slide-fade-leave-active {
  transition: all 0.3s ease;
}

.slide-fade-enter-from {
  opacity: 0;
  transform: translateY(-10px);
}

.slide-fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

.modern-alert {
  display: flex;
  gap: 14px;
  align-items: flex-start;
  padding: 14px 16px;
  border-radius: 12px;
  border: 1px solid rgba(0, 0, 0, 0.08);
  background: #fafafa;
}

.alert-icon {
  width: 40px;
  height: 40px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  flex-shrink: 0;
}

.error-icon {
  background: #f3e8e8;
  color: #7f1d1d;
}

.lunch-alert {
  background: #f8f9fb;
  border-color: rgba(30, 58, 138, 0.08);
}

.lunch-alert .alert-icon {
  background: #e8f0fd;
  color: #1e3a8a;
}

.alert-content {
  display: flex;
  flex-direction: column;
  gap: 6px;
  flex: 1;
}

.alert-title {
  font-weight: 800;
  color: #1f2937;
  font-size: 14px;
  letter-spacing: 0.2px;
}

.alert-message {
  color: #4b5563;
  font-size: 13px;
  font-weight: 600;
  line-height: 1.5;
}

.error-actions {
  display: flex;
  gap: 10px;
  margin-top: 10px;
  flex-wrap: wrap;
}

.error-actions :deep(.v-btn) {
  font-size: 12px !important;
  font-weight: 600 !important;
  text-transform: none !important;
  padding: 6px 12px !important;
  height: auto !important;
  color: #7f1d1d !important;
}

.error-actions :deep(.v-btn:hover:not(:disabled)) {
  background: #ede9e9 !important;
}

.error-alert {
  background: linear-gradient(135deg, #faf9f8 0%, #f5f3f2 100%);
  border-color: rgba(127, 29, 29, 0.1);
}
</style>
