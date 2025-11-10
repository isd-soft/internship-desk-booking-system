<script setup lang="ts">
import { reactive, watch } from "vue";

interface Props {
  modelValue: boolean;
  desk: any;
  isBooked: boolean;
  existingBooking?: {
    duration: number;
  };
}

interface Emits {
  (e: "update:modelValue", value: boolean): void;
  (e: "confirm", data: { duration: number }): void;
  (e: "cancel"): void;
}

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const bookingForm = reactive({
  duration: 30,
  customMinutes: 30,
});

// Синхронизация с существующим бронированием
watch(
  () => props.existingBooking,
  (booking) => {
    if (booking) {
      bookingForm.duration = booking.duration;
      bookingForm.customMinutes = booking.duration;
    } else {
      bookingForm.duration = 30;
      bookingForm.customMinutes = 30;
    }
  },
  { immediate: true }
);

function setDuration(minutes: number) {
  bookingForm.duration = minutes;
  bookingForm.customMinutes = minutes;
}

function handleConfirm() {
  emit("confirm", { duration: bookingForm.duration });
  closeModal();
}

function handleCancel() {
  emit("cancel");
}

function closeModal() {
  emit("update:modelValue", false);
}

function formatDuration(minutes: number): string {
  const hours = Math.floor(minutes / 60);
  const mins = minutes % 60;
  if (hours === 0) return `${mins}min`;
  if (mins === 0) return `${hours}h`;
  return `${hours}h ${mins}min`;
}
</script>

<template>
  <v-dialog
    :model-value="modelValue"
    @update:model-value="emit('update:modelValue', $event)"
    max-width="480"
    transition="dialog-bottom-transition"
    persistent
  >
    <v-card class="booking-card" elevation="0">
      <v-card-title class="card-header">
        <div class="header-content">
          <div class="header-info">
            <div class="workspace-label">WORKSPACE</div>
            <div class="desk-title">Desk {{ desk?.i }}</div>
          </div>
          <v-btn
            icon
            variant="text"
            size="small"
            @click="closeModal"
            class="close-button"
          >
            <v-icon size="20">mdi-close</v-icon>
          </v-btn>
        </div>
      </v-card-title>

      <v-card-text class="card-body">
        <v-alert
          v-if="isBooked"
          class="status-alert"
          variant="tonal"
          density="compact"
        >
          <div class="alert-content">
            <v-icon size="18">mdi-check-circle</v-icon>
            <span>Currently Reserved</span>
          </div>
        </v-alert>

        <div class="section">
          <div class="section-title">Duration</div>
          <div class="duration-grid">
            <button
              v-for="duration in [30, 60, 120]"
              :key="duration"
              @click.stop="setDuration(duration)"
              :class="[
                'duration-btn',
                { active: bookingForm.duration === duration },
              ]"
            >
              <div class="duration-value">{{ duration }}</div>
              <div class="duration-unit">min</div>
            </button>
          </div>
        </div>

        <div class="section">
          <div class="section-title">Custom Duration</div>
          <div class="custom-input-wrapper">
            <input
              v-model.number="bookingForm.customMinutes"
              @input="bookingForm.duration = bookingForm.customMinutes"
              type="number"
              class="custom-input"
              placeholder="Enter minutes"
            />
            <span class="input-suffix">minutes</span>
          </div>
        </div>

        <div class="summary-box">
          <div class="summary-icon">
            <v-icon size="24" color="white">mdi-clock-outline</v-icon>
          </div>
          <div class="summary-info">
            <div class="summary-label">Total Booking Time</div>
            <div class="summary-duration">
              {{ formatDuration(bookingForm.duration) }}
            </div>
          </div>
        </div>
      </v-card-text>

      <v-card-actions class="card-actions">
        <v-btn
          v-if="isBooked"
          variant="text"
          class="cancel-button"
          @click.stop="handleCancel"
        >
          Cancel Booking
        </v-btn>
        <v-spacer v-if="isBooked" />
        <v-btn class="confirm-button" size="x-large" @click.stop="handleConfirm">
          {{ isBooked ? "Update Booking" : "Confirm Booking" }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap");

* {
  font-family: "Inter", -apple-system, BlinkMacSystemFont, "Segoe UI",
    sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

.booking-card {
  border-radius: 20px !important;
  background: #ffffff;
  border: 1px solid #e5e5e5;
  box-shadow: 0 24px 48px rgba(0, 0, 0, 0.12) !important;
  overflow: hidden;
}

.card-header {
  padding: 28px 28px 24px 28px;
}

.header-content {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  width: 100%;
}

.header-info {
  flex: 1;
}

.workspace-label {
  font-size: 11px;
  font-weight: 700;
  color: #737373;
  letter-spacing: 1.5px;
  margin-bottom: 8px;
}

.desk-title {
  font-size: 28px;
  font-weight: 800;
  color: #171717;
  letter-spacing: -0.5px;
  line-height: 1;
}

.close-button {
  opacity: 0.6;
  transition: all 0.2s ease;
  margin-top: -4px;
}

.close-button:hover {
  opacity: 1;
  background-color: #f5f5f5 !important;
  transform: rotate(90deg);
}

.card-body {
  padding: 0 28px 28px 28px;
}

.status-alert {
  background: #fafafa !important;
  border: 1px solid #e5e5e5 !important;
  border-radius: 12px !important;
  margin-bottom: 24px;
  padding: 12px 16px !important;
}

.alert-content {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
  font-weight: 600;
  color: #525252;
}

.section {
  margin-bottom: 24px;
}

.section-title {
  font-size: 13px;
  font-weight: 700;
  color: #171717;
  margin-bottom: 12px;
  letter-spacing: 0.3px;
}

.duration-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.duration-btn {
  background: #ffffff;
  border: 2px solid #e5e5e5;
  border-radius: 12px;
  padding: 20px 16px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  outline: none;
}

.duration-btn:hover {
  border-color: #a3a3a3;
  background: #fafafa;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.duration-btn.active {
  background: #171717;
  border-color: #171717;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.16);
}

.duration-btn.active .duration-value,
.duration-btn.active .duration-unit {
  color: #ffffff;
}

.duration-value {
  font-size: 24px;
  font-weight: 800;
  color: #171717;
  letter-spacing: -0.5px;
  line-height: 1;
}

.duration-unit {
  font-size: 12px;
  font-weight: 600;
  color: #737373;
  text-transform: lowercase;
  letter-spacing: 0.2px;
}

.custom-input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.custom-input {
  width: 100%;
  padding: 16px 90px 16px 16px;
  border: 2px solid #e5e5e5;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 600;
  color: #171717;
  background: #ffffff;
  transition: all 0.3s ease;
  outline: none;
}

.custom-input:hover {
  border-color: #a3a3a3;
  background: #fafafa;
}

.custom-input:focus {
  border-color: #171717;
  background: #ffffff;
  box-shadow: 0 0 0 3px rgba(0, 0, 0, 0.05);
}

.custom-input::placeholder {
  color: #a3a3a3;
  font-weight: 500;
}

.input-suffix {
  position: absolute;
  right: 16px;
  font-size: 13px;
  font-weight: 600;
  color: #737373;
  pointer-events: none;
}

.summary-box {
  background: #171717;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  margin-top: 28px;
}

.summary-icon {
  width: 48px;
  height: 48px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.summary-info {
  flex: 1;
}

.summary-label {
  font-size: 12px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 4px;
  letter-spacing: 0.3px;
}

.summary-duration {
  font-size: 24px;
  font-weight: 800;
  color: #ffffff;
  letter-spacing: -0.5px;
  line-height: 1;
}

.card-actions {
  padding: 20px 28px 28px 28px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
}

.cancel-button {
  color: #dc2626 !important;
  font-weight: 600 !important;
  text-transform: none !important;
  letter-spacing: 0.2px;
  transition: all 0.2s ease;
}

.cancel-button:hover {
  background: #fef2f2 !important;
}

.confirm-button {
  background: #171717 !important;
  color: #ffffff !important;
  font-weight: 700 !important;
  text-transform: none !important;
  letter-spacing: 0.3px;
  padding: 0 40px !important;
  height: 52px !important;
  border-radius: 12px !important;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.16) !important;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.confirm-button:hover {
  background: #262626 !important;
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.24) !important;
  transform: translateY(-2px);
}

:deep(.v-overlay__scrim) {
  backdrop-filter: blur(8px);
  background: rgba(0, 0, 0, 0.4);
}

:deep(.dialog-bottom-transition-enter-active),
:deep(.dialog-bottom-transition-leave-active) {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

:deep(.dialog-bottom-transition-enter-from) {
  opacity: 0;
  transform: translateY(30px) scale(0.96);
}

:deep(.dialog-bottom-transition-leave-to) {
  opacity: 0;
  transform: translateY(30px) scale(0.96);
}
</style>