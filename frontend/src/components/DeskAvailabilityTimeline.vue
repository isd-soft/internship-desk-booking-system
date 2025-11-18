<template>
  <div class="timeline-container">
    <div class="timeline-wrapper">
      <div class="quick-presets" v-if="quickCasts.length > 0">
        <button
          v-for="cast in quickCasts"
          :key="cast.label"
          class="preset-btn"
          :class="{ active: isQuickCastActive(cast) }"
          @click="applyQuickCast(cast)"
        >
          {{ cast.label }}
        </button>
      </div>

      <div
        ref="trackRef"
        class="timeline-track"
        :class="{ dragging: dragState !== null }"
        @mousedown="onTrackMouseDown"
      >
        <div class="slots-container">
          <div
            v-for="hour in hours"
            :key="`slot-${hour}`"
            class="timeline-slot"
            :class="slotClasses(hour)"
            :style="slotStyle(hour)"
            @click.stop="handleSlotClick(hour)"
          ></div>
        </div>

        <div class="timeline-range-container">
          <div
            v-for="(segment, index) in selectedSegments"
            :key="index"
            class="timeline-range"
            :class="`range-${segment.status.toLowerCase()}`"
            :style="{ left: segment.left, width: segment.width }"
          />
        </div>

        <div
          class="timeline-handle"
          :style="{ left: `${startPosition}%` }"
          @mousedown.stop="startDrag('start', $event)"
        >
          <div class="handle-time">{{ formatTime(startHour) }}</div>
        </div>

        <div
          class="timeline-handle"
          :style="{ left: `${endPosition}%` }"
          @mousedown.stop="startDrag('end', $event)"
        >
          <div class="handle-time">{{ formatTime(endHour) }}</div>
        </div>
      </div>

      <div class="hour-markers">
        <div
          v-for="hour in hours"
          :key="`marker-${hour}`"
          class="hour-marker"
          :class="{ 'lunch-hour': hour === LUNCH_HOUR }"
          :style="{ left: `${getHourPosition(hour)}%` }"
        >
          <span class="marker-label">{{ hour }}:00</span>
        </div>
      </div>

      <div class="time-display">
        <div class="time-info">
          <span class="time-label">Selected Time</span>
          <span class="time-value"
            >{{ formatTime(startHour) }} - {{ formatTime(endHour) }}</span
          >
        </div>
        <div class="duration-badge">
          <span class="duration-value">{{ selectedDuration }}</span>
          <span class="duration-unit">{{
            selectedDuration === 1 ? "hour" : "hours"
          }}</span>
        </div>
      </div>

      <div class="timeline-labels">
        <span class="label-time">{{ MIN_HOUR }}:00</span>
        <span class="label-lunch" :class="{ 'has-overlap': lunchOverlap }">
          Lunch Break
          <v-tooltip
            v-if="lunchOverlap"
            activator="parent"
            location="top"
            content-class="lunch-tooltip"
          >
            <div class="tooltip-content">
              <div class="tooltip-text">
                <strong>Lunch Hours (13:00 - 14:00)</strong><br />
                Workspace may be less available
              </div>
            </div>
          </v-tooltip>
        </span>
        <span class="label-time">{{ MAX_HOUR }}:00</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from "vue";
import api from "@/plugins/axios";

type SlotStatus = "FREE" | "BUSY";

interface AvailabilityInterval {
  start: string;
  end: string;
}

interface DeskAvailabilityApiResponse {
  workdayStart: string;
  workdayEnd: string;
  busyIntervals: AvailabilityInterval[];
}

const props = defineProps<{
  deskId: number | null;
  dateIso: string | null;
  startHour: number;
  endHour: number;
  minHour?: number;
  maxHour?: number;
}>();

const emit = defineEmits<{
  (e: "update:startHour", value: number): void;
  (e: "update:endHour", value: number): void;
}>();

const MIN_HOUR = computed(() => props.minHour ?? 9);
const MAX_HOUR = computed(() => props.maxHour ?? 18);
const LUNCH_HOUR = 13;

const hours = computed(() =>
  Array.from(
    { length: MAX_HOUR.value - MIN_HOUR.value + 1 },
    (_, i) => MIN_HOUR.value + i
  )
);

interface QuickCast {
  label: string;
  start: number;
  end: number;
  available: boolean;
}

const availabilitySlots = ref<{ hour: number; status: SlotStatus }[]>([]);
const isLoading = ref(false);
const trackRef = ref<HTMLElement | null>(null);
const dragState = ref<null | "start" | "end">(null);
const rafId = ref<number | null>(null);
const pendingMouseEvent = ref<MouseEvent | null>(null);

const quickCasts = computed<QuickCast[]>(() => {
  const slots = availabilitySlots.value;
  const freeSlots = slots.filter(
    (s) => s.status === "FREE" && s.hour !== LUNCH_HOUR
  );

  if (freeSlots.length === 0) return [];

  const casts: QuickCast[] = [];

  const morningSlots = freeSlots.filter((s) => s.hour < LUNCH_HOUR);
  if (morningSlots.length > 0) {
    const start = morningSlots[0].hour;
    const end = morningSlots[morningSlots.length - 1].hour + 1;
    casts.push({
      label: "Morning",
      start,
      end,
      available: true,
    });
  }

  const afternoonSlots = freeSlots.filter((s) => s.hour > LUNCH_HOUR);
  if (afternoonSlots.length > 0) {
    const start = afternoonSlots[0].hour;
    const end = afternoonSlots[afternoonSlots.length - 1].hour + 1;
    casts.push({
      label: "Afternoon",
      start,
      end,
      available: true,
    });
  }

  if (freeSlots.length > 0) {
    const start = freeSlots[0].hour;
    const end = freeSlots[freeSlots.length - 1].hour + 1;
    casts.push({
      label: "Full Day",
      start,
      end,
      available: true,
    });
  }

  return casts;
});

function applyQuickCast(cast: QuickCast) {
  emit("update:startHour", cast.start);
  emit("update:endHour", cast.end);
}

function isQuickCastActive(cast: QuickCast): boolean {
  return props.startHour === cast.start && props.endHour === cast.end;
}

const selectedDuration = computed(() => {
  let duration = 0;
  for (let h = props.startHour; h < props.endHour; h++) {
    if (slotStatus(h) === "FREE") {
      duration++;
    }
  }
  return duration;
});

const lunchOverlap = computed(() => {
  const LUNCH_END = 14;
  return props.startHour < LUNCH_END && props.endHour > LUNCH_HOUR;
});

function formatTime(hour: number): string {
  const h = Math.floor(hour);
  const m = (hour % 1) * 60;
  return `${h}:${m.toString().padStart(2, "0")}`;
}

async function loadDeskAvailability() {
  if (!props.deskId || !props.dateIso) {
    availabilitySlots.value = [];
    return;
  }

  isLoading.value = true;

  try {
    const dateParam = props.dateIso.slice(0, 10);
    const { data } = await api.get<DeskAvailabilityApiResponse>(
      `/desk/${props.deskId}/availability`,
      { params: { date: dateParam } }
    );

    const workdayStart = new Date(data.workdayStart);
    const workdayEnd = new Date(data.workdayEnd);

    const startHour = workdayStart.getHours();
    const endHour = workdayEnd.getHours();

    const slots: { hour: number; status: SlotStatus }[] = [];

    for (let h = startHour; h < endHour; h++) {
      const slotStart = new Date(workdayStart);
      slotStart.setHours(h, 0, 0, 0);

      const slotEnd = new Date(workdayStart);
      slotEnd.setHours(h + 1, 0, 0, 0);

      const isBusy = data.busyIntervals.some((interval) => {
        const intStart = new Date(interval.start);
        const intEnd = new Date(interval.end);
        return slotStart < intEnd && slotEnd > intStart;
      });

      slots.push({ hour: h, status: isBusy ? "BUSY" : "FREE" });
    }

    availabilitySlots.value = slots;

    const freeSlots = slots.filter((s) => s.status === "FREE");
    if (freeSlots.length > 0) {
      const minFree = freeSlots[0].hour;
      const maxFree = freeSlots[freeSlots.length - 1].hour + 1;

      if (props.startHour < minFree || props.startHour >= maxFree) {
        emit("update:startHour", minFree);
      }

      if (props.endHour > maxFree || props.endHour <= props.startHour) {
        emit("update:endHour", Math.min(minFree + 1, maxFree));
      }
    }
  } catch (error) {
    console.error(
      "[DeskAvailabilityTimeline] Failed to load availability",
      error
    );
    availabilitySlots.value = [];
  } finally {
    isLoading.value = false;
  }
}

watch(
  () => [props.deskId, props.dateIso],
  () => {
    if (props.deskId && props.dateIso) {
      loadDeskAvailability();
    }
  },
  { immediate: true }
);

function slotStatus(hour: number): SlotStatus {
  if (hour === LUNCH_HOUR) return "BUSY";
  const slot = availabilitySlots.value.find((s) => s.hour === hour);
  return slot ? slot.status : "BUSY";
}

function slotClasses(hour: number) {
  const status = slotStatus(hour);
  return {
    "slot-free": status === "FREE",
    "slot-busy": status === "BUSY",
  };
}

function slotStyle(hour: number) {
  const totalSpan = MAX_HOUR.value - MIN_HOUR.value;
  const left = ((hour - MIN_HOUR.value) / totalSpan) * 100;
  const width = (1 / totalSpan) * 100;
  return {
    left: `${left}%`,
    width: `${width}%`,
  };
}

function getHourPosition(hour: number): number {
  const totalSpan = MAX_HOUR.value - MIN_HOUR.value;
  return ((hour - MIN_HOUR.value) / totalSpan) * 100;
}

const startPosition = computed(() => getHourPosition(props.startHour));
const endPosition = computed(() => getHourPosition(props.endHour));

const selectedSegments = computed(() => {
  const segments: { left: string; width: string; status: SlotStatus }[] = [];
  const start = props.startHour;
  const end = props.endHour;

  if (start >= end) return [];

  let currentSegmentStart = start;
  let currentStatus = slotStatus(Math.floor(start));

  for (let h = start; h < end; h += 0.5) {
    const hourFloor = Math.floor(h);
    const status = slotStatus(hourFloor);

    if (status !== currentStatus) {
      const segmentStartPos = getHourPosition(currentSegmentStart);
      const segmentEndPos = getHourPosition(h);
      segments.push({
        left: `${segmentStartPos}%`,
        width: `${segmentEndPos - segmentStartPos}%`,
        status: currentStatus,
      });
      currentSegmentStart = h;
      currentStatus = status;
    }
  }

  const lastSegmentStartPos = getHourPosition(currentSegmentStart);
  const lastSegmentEndPos = getHourPosition(end);
  segments.push({
    left: `${lastSegmentStartPos}%`,
    width: `${lastSegmentEndPos - lastSegmentStartPos}%`,
    status: currentStatus,
  });

  return segments;
});

function handleSlotClick(hour: number) {
  const status = slotStatus(hour);
  if (status !== "FREE" || hour === LUNCH_HOUR) return;

  const isAdjacent = hour === props.startHour - 1 || hour === props.endHour;

  if (isAdjacent) {
    if (hour === props.startHour - 1) {
      emit("update:startHour", hour);
    } else if (hour === props.endHour) {
      let nextHour = hour + 1;
      if (nextHour === LUNCH_HOUR) nextHour = LUNCH_HOUR + 1;
      nextHour = Math.min(nextHour, MAX_HOUR.value);
      emit("update:endHour", nextHour);
    }
  } else {
    let nextHour = hour + 1;
    if (nextHour === LUNCH_HOUR) nextHour = LUNCH_HOUR + 1;
    nextHour = Math.min(nextHour, MAX_HOUR.value);

    emit("update:startHour", hour);
    emit("update:endHour", nextHour);
  }
}

function startDrag(which: "start" | "end", event: MouseEvent) {
  event.preventDefault();
  dragState.value = which;
}

function onTrackMouseDown(event: MouseEvent) {
  const rect = trackRef.value?.getBoundingClientRect();
  if (!rect) return;

  const x = Math.max(0, Math.min(event.clientX - rect.left, rect.width));
  const percentage = x / rect.width;

  const exactHour =
    MIN_HOUR.value + percentage * (MAX_HOUR.value - MIN_HOUR.value);
  let hour = Math.round(exactHour);

  const status = slotStatus(hour);
  if (status !== "FREE" || hour === LUNCH_HOUR) return;

  const currentMid = (props.startHour + props.endHour) / 2;

  if (exactHour < currentMid) {
    emit("update:startHour", hour);
  } else {
    let nextHour = hour + 1;
    if (hour === LUNCH_HOUR - 1) nextHour = LUNCH_HOUR + 1;
    nextHour = Math.min(nextHour, MAX_HOUR.value);
    emit("update:endHour", nextHour);
  }
}

function handleMouseMove(event: MouseEvent) {
  if (!dragState.value || !trackRef.value) return;

  pendingMouseEvent.value = event;

  if (rafId.value !== null) return;

  rafId.value = requestAnimationFrame(() => {
    rafId.value = null;

    const evt = pendingMouseEvent.value;
    if (!evt || !trackRef.value) return;

    const rect = trackRef.value.getBoundingClientRect();
    const x = Math.max(0, Math.min(evt.clientX - rect.left, rect.width));
    const percentage = x / rect.width;
    const totalSpan = MAX_HOUR.value - MIN_HOUR.value;

    const exactHour = MIN_HOUR.value + percentage * totalSpan;
    let hour = Math.round(exactHour);

    if (hour < MIN_HOUR.value) hour = MIN_HOUR.value;
    if (hour > MAX_HOUR.value) hour = MAX_HOUR.value;

    if (dragState.value === "start") {
      const maxStart = props.endHour - 1;
      if (hour >= maxStart) hour = maxStart;

      if (props.startHour !== hour) {
        emit("update:startHour", hour);
      }
    } else {
      // 'end'
      const minEnd = props.startHour + 1;
      if (hour <= minEnd) hour = minEnd;

      if (props.endHour !== hour) {
        emit("update:endHour", hour);
      }
    }
  });
}

function handleMouseUp() {
  dragState.value = null;
  pendingMouseEvent.value = null;

  if (rafId.value !== null) {
    cancelAnimationFrame(rafId.value);
    rafId.value = null;
  }
}

onMounted(() => {
  window.addEventListener("mousemove", handleMouseMove);
  window.addEventListener("mouseup", handleMouseUp);
});

onBeforeUnmount(() => {
  window.removeEventListener("mousemove", handleMouseMove);
  window.removeEventListener("mouseup", handleMouseUp);

  if (rafId.value !== null) {
    cancelAnimationFrame(rafId.value);
    rafId.value = null;
  }
});
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap");

.timeline-container {
  width: 100%;
  font-family: "Inter", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto,
    sans-serif;
}

.timeline-wrapper {
  position: relative;
}

.quick-presets {
  display: flex;
  gap: 6px;
  margin-bottom: 14px;
  justify-content: center;
}

.preset-btn {
  padding: 7px 14px;
  background: #ffffff;
  border: 1.5px solid #e5e7eb;
  border-radius: 8px;
  color: #171717;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  white-space: nowrap;
  font-family: inherit;
}

.preset-btn:hover {
  background: #f9fafb;
  border-color: #d1d5db;
  transform: translateY(-1px);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
}

.preset-btn.active {
  background: linear-gradient(135deg, #171717 0%, #262626 100%);
  border-color: #171717;
  color: #ffffff;
  box-shadow: 0 3px 10px rgba(23, 23, 23, 0.18);
}

.timeline-track {
  position: relative;
  height: 14px;
  background: linear-gradient(180deg, #2a2a2a 0%, #1a1a1a 100%);
  border-radius: 12px;
  cursor: pointer;
  overflow: visible;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  will-change: transform;
  box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.4);
}

.timeline-track.dragging {
  cursor: grabbing;
  user-select: none;
}

.slots-container {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border-radius: 14px;
  overflow: hidden;
  display: flex;
  pointer-events: none;
}

.slots-container .timeline-slot {
  pointer-events: auto;
}

.timeline-slot {
  position: absolute;
  top: 0;
  bottom: 0;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  border-right: 1px solid rgba(0, 0, 0, 0.08);
  will-change: filter, transform;
}

.timeline-slot:last-child {
  border-right: none;
}

.timeline-slot:hover:not(.slot-busy) {
  filter: brightness(1.1);
  transform: scaleY(1.1);
  box-shadow: inset 0 0 0 2px rgba(255, 255, 255, 0.3);
  z-index: 1;
}

.timeline-slot.slot-free {
  background: linear-gradient(
    180deg,
    rgba(34, 197, 94, 0.45) 0%,
    rgba(22, 163, 74, 0.35) 100%
  );
}

.timeline-slot.slot-busy {
  background: linear-gradient(
    180deg,
    rgba(248, 113, 113, 0.45) 0%,
    rgba(239, 68, 68, 0.35) 100%
  );
  cursor: not-allowed;
}

.timeline-range-container {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  border-radius: 10px;
  overflow: hidden;
}

.timeline-range {
  position: absolute;
  height: 100%;
  transition: left 0.35s cubic-bezier(0.34, 1.56, 0.64, 1),
    width 0.35s cubic-bezier(0.34, 1.56, 0.64, 1), background 0.3s ease;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
}

.timeline-range.range-free {
  background: linear-gradient(135deg, #22c55e 0%, #16a34a 50%, #15803d 100%);
  box-shadow: 0 2px 12px rgba(34, 197, 94, 0.5),
    inset 0 1px 0 rgba(255, 255, 255, 0.3), 0 0 20px rgba(34, 197, 94, 0.2);
}

.timeline-range.range-busy {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 50%, #b91c1c 100%);
  box-shadow: 0 2px 12px rgba(239, 68, 68, 0.4),
    inset 0 1px 0 rgba(255, 255, 255, 0.2), 0 0 20px rgba(239, 68, 68, 0.2);
}

.timeline-handle {
  position: absolute;
  top: 50%;
  transform: translate(-50%, -50%);
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #ffffff 0%, #f9fafb 100%);
  border: 3px solid #2a2a2a;
  border-radius: 50%;
  cursor: grab;
  transition: all 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
  z-index: 10;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.35),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
}

.timeline-handle:hover {
  transform: translate(-50%, -50%) scale(1.2);
  box-shadow: 0 6px 20px rgba(239, 68, 68, 0.5),
    0 0 0 4px rgba(239, 68, 68, 0.1), inset 0 1px 0 rgba(255, 255, 255, 0.8);
  border-color: #ef4444;
}

.timeline-handle:active {
  cursor: grabbing;
  transform: translate(-50%, -50%) scale(1.1);
  border-color: #dc2626;
}

.handle-time {
  position: absolute;
  top: -32px;
  left: 50%;
  transform: translateX(-50%);
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
  color: #ffffff;
  padding: 6px 12px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 700;
  white-space: nowrap;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.25),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
  pointer-events: none;
  font-family: inherit;
  opacity: 0;
  transition: opacity 0.3s cubic-bezier(0.4, 0, 0.2, 1),
    top 0.3s cubic-bezier(0.4, 0, 0.2, 1),
    transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.timeline-handle:hover .handle-time,
.timeline-handle:active .handle-time {
  opacity: 1;
  top: -40px;
  transform: translateX(-50%) scale(1.05);
}

.time-display {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
  padding: 10px 14px;
  background: #f9fafb;
  border-radius: 10px;
  border: 1px solid #f3f4f6;
}

.time-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.time-label {
  font-size: 10px;
  font-weight: 700;
  color: #9ca3af;
  letter-spacing: 0.8px;
  text-transform: uppercase;
}

.time-value {
  font-size: 15px;
  font-weight: 700;
  color: #171717;
  letter-spacing: 0.2px;
}

.duration-badge {
  display: flex;
  align-items: baseline;
  gap: 4px;
  padding: 6px 12px;
  background: linear-gradient(135deg, #171717 0%, #262626 100%);
  border-radius: 8px;
  box-shadow: 0 2px 6px rgba(23, 23, 23, 0.15);
}

.duration-value {
  font-size: 18px;
  font-weight: 800;
  color: #ffffff;
  line-height: 1;
}

.duration-unit {
  font-size: 11px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.7);
}

.lunch-warning {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  padding: 12px 14px;
  margin-top: 12px;
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  border-radius: 10px;
  border: 1px solid #fbbf24;
}

.warning-icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(245, 158, 11, 0.15);
  border-radius: 8px;
  flex-shrink: 0;
  color: #d97706;
}

.warning-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.warning-title {
  font-size: 13px;
  font-weight: 700;
  color: #92400e;
  font-family: inherit;
}

.warning-message {
  font-size: 12px;
  font-weight: 500;
  color: #78350f;
  line-height: 1.4;
  font-family: inherit;
}

.slide-fade-enter-active,
.slide-fade-leave-active {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.slide-fade-enter-from {
  opacity: 0;
  transform: translateY(-12px);
}

.slide-fade-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

.hour-markers {
  position: relative;
  height: 20px;
  margin-top: 8px;
}

.hour-marker {
  position: absolute;
  transform: translateX(-50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.marker-label {
  font-size: 10px;
  font-weight: 600;
  color: #9ca3af;
  font-family: inherit;
  white-space: nowrap;
}

.hour-marker.lunch-hour .marker-label {
  color: #ffffff;
  font-weight: 700;
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
  padding: 4px 8px;
  border-radius: 6px;
  box-shadow: 0 2px 8px rgba(239, 68, 68, 0.4);
}

.timeline-labels {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
  padding: 0 6px;
  font-size: 10px;
  color: #9ca3af;
  font-weight: 600;
  font-family: inherit;
}
.lunch-tooltip :deep(.v-overlay__content) {
  background: linear-gradient(135deg, #ffffff 0%, #fffbeb 100%) !important;
  border: 2px solid #fbbf24 !important;
  border-radius: 16px !important;
  padding: 0 !important;
  box-shadow: 0 12px 40px rgba(251, 191, 36, 0.35),
    0 0 0 1px rgba(251, 191, 36, 0.1) !important;
  backdrop-filter: blur(8px);
}

.tooltip-content {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px 18px;
  position: relative;
  overflow: hidden;
}

.tooltip-content::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #fbbf24 0%, #f59e0b 50%, #fbbf24 100%);
  background-size: 200% 100%;
  animation: shimmer 3s ease-in-out infinite;
}

@keyframes shimmer {
  0%,
  100% {
    background-position: 0% 0%;
  }
  50% {
    background-position: 100% 0%;
  }
}

.tooltip-icon {
  font-size: 28px;
  flex-shrink: 0;
  line-height: 1;
  filter: drop-shadow(0 2px 4px rgba(245, 158, 11, 0.3));
  animation: pulse-warning 2s ease-in-out infinite;
}

@keyframes pulse-warning {
  0%,
  100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
}

.tooltip-text {
  font-size: 13px;
  font-weight: 600;
  color: #78350f;
  line-height: 1.6;
}

.tooltip-text strong {
  font-weight: 800;
  color: #92400e;
  display: block;
  margin-bottom: 4px;
  font-size: 14px;
  letter-spacing: 0.2px;
}
</style>
