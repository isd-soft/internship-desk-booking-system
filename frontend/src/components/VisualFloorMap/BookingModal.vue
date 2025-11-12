<script setup lang="ts">
import { reactive, watch, ref, computed } from "vue";
import { useFavouritesStore } from "@/stores/favourites";

interface Props {
  modelValue: boolean;
  desk: any;
  isBooked: boolean;
  existingBooking?: { duration: number };
}
interface Emits {
  (e: "update:modelValue", value: boolean): void;
  (e: "confirm", data: { duration: number }): void;
  (e: "cancel"): void;
}

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const bookingForm = reactive({ duration: 480 });
const isProcessing = ref(false);

const favStore = useFavouritesStore();

const deskId = computed<number | null>(() => {
  const n = Number(props.desk?.i);
  return Number.isFinite(n) ? n : null;
});

const isFavourite = computed(() => favStore.isFav(deskId.value));

watch(
  () => props.modelValue,
  async (open) => {
    if (!open) return;

    await favStore.ensureLoaded();
  },
  { immediate: true }
);


function setDuration(minutes: number) {
  bookingForm.duration = minutes;
}

function closeModal() {
  emit("update:modelValue", false);
}

async function toggleFavourite() {
  if (!deskId.value) return;
  isProcessing.value = true;
  await favStore.toggle(deskId.value);
  isProcessing.value = false;
}

function handleConfirm() {
  emit("confirm", { duration: bookingForm.duration });
  closeModal();
}

function handleCancel() {
  emit("cancel");
  closeModal();
}

function formatDuration(minutes: number) {
  const h = Math.floor(minutes / 60);
  const m = minutes % 60;
  return h ? `${h}h ${m}m` : `${m}m`;
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
    <v-card class="booking-card">
      <v-card-title class="card-header">
        <div class="header-content">
          <div class="header-info">
            <div class="workspace-label">WORKSPACE</div>
            <div class="desk-title">{{ desk?.deskName || `Desk ${desk?.i}` }}</div>
          </div>
          <v-btn icon variant="text" size="small" @click="closeModal">
            <v-icon size="20">mdi-close</v-icon>
          </v-btn>
        </div>
      </v-card-title>

      <v-card-text class="card-body">



<div class="section dark-slider">
  <div class="section-title">Custom Duration</div>

  <div class="slider-wrap">
    <input
      type="range"
      min="60"
      max="480"
      step="1"
      v-model="bookingForm.duration"
      class="black-duration-slider"
    />
    <div class="slider-value-label">
      {{ formatDuration(bookingForm.duration) }}
    </div>
  </div>

<div class="slider-scale">
  <span>1h</span>
  <span>2h</span>
  <span>4h</span>
  <span>6h</span>
  <span>8h</span>
</div>
  

</div>

        <button
          @click.stop="toggleFavourite"
          :disabled="isProcessing || !deskId"
          :class="['favourite-button', { active: isFavourite }]"
        >
          <v-icon size="20">
            {{ isFavourite ? 'mdi-heart' : 'mdi-heart-outline' }}
          </v-icon>
          <span>{{ isFavourite ? 'Remove from Favourites' : 'Add to Favourites' }}</span>
        </button>

      </v-card-text>

      <v-card-actions class="card-actions">
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

.slider-container {
  position: relative;
}

.slider-labels {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.slider-label {
  font-size: 11px;
  font-weight: 600;
  color: #a3a3a3;
  letter-spacing: 0.3px;
}

.duration-slider {
  width: 100%;
  height: 6px;
  -webkit-appearance: none;
  appearance: none;
  background: #e5e5e5;
  border-radius: 10px;
  outline: none;
  transition: background 0.3s ease;
}

.duration-slider:hover {
  background: #d4d4d4;
}

.duration-slider::-webkit-slider-thumb {
  -webkit-appearance: none;
  appearance: none;
  width: 24px;
  height: 24px;
  background: #171717;
  border-radius: 50%;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.duration-slider::-webkit-slider-thumb:hover {
  transform: scale(1.15);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.duration-slider::-webkit-slider-thumb:active {
  transform: scale(1.05);
}

.duration-slider::-moz-range-thumb {
  width: 24px;
  height: 24px;
  background: #171717;
  border: none;
  border-radius: 50%;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.duration-slider::-moz-range-thumb:hover {
  transform: scale(1.15);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.duration-slider::-moz-range-thumb:active {
  transform: scale(1.05);
}

.slider-value {
  text-align: center;
  font-size: 16px;
  font-weight: 700;
  color: #171717;
  margin-top: 12px;
  letter-spacing: 0.3px;
}

.summary-box {
  background: #171717;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  margin-top: 28px;
  margin-bottom: 16px;
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

.favourite-button {
  width: 100%;
  padding: 16px 20px;
  background: #ffffff;
  border: 2px solid #e5e5e5;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  font-size: 14px;
  font-weight: 600;
  color: #171717;
  letter-spacing: 0.2px;
  outline: none;
}

.favourite-button:hover:not(:disabled) {
  border-color: #a3a3a3;
  background: #fafafa;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.favourite-button:active:not(:disabled) {
  transform: translateY(0);
}

.favourite-button.active {
  background: #fef2f2;
  border-color: #fca5a5;
  color: #dc2626;
}

.favourite-button.active:hover:not(:disabled) {
  background: #fee2e2;
  border-color: #f87171;
}

.favourite-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
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

/* 🌙 Smooth Custom Slider */
.smooth-slider {
  background: linear-gradient(180deg, #fafafa 0%, #ffffff 100%);
  border: 1px solid #ececec;
  border-radius: 14px;
  padding: 20px 18px 24px 18px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.smooth-slider:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transform: translateY(-1px);
}

.slider-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
}

.custom-duration-slider {
  width: 100%;
  height: 8px;
  border-radius: 6px;
  background: linear-gradient(90deg, #ff8a00 0%, #ffb74d 100%);
  appearance: none;
  outline: none;
  cursor: pointer;
  transition: all 0.3s ease;
}

.custom-duration-slider:hover {
  filter: brightness(1.1);
}

.custom-duration-slider::-webkit-slider-thumb {
  appearance: none;
  width: 26px;
  height: 26px;
  background: #ffffff;
  border: 3px solid #ff8a00;
  border-radius: 50%;
  box-shadow: 0 3px 8px rgba(255, 138, 0, 0.3);
  transition: all 0.3s ease;
}

.custom-duration-slider::-webkit-slider-thumb:hover {
  transform: scale(1.1);
  box-shadow: 0 5px 14px rgba(255, 138, 0, 0.45);
}

.custom-duration-slider::-webkit-slider-thumb:active {
  transform: scale(0.95);
  border-color: #ff9f33;
}

/* 🖤 Premium Dark Smooth Slider */
.dark-slider {
  background: #111;
  border-radius: 16px;
  padding: 22px 18px 26px;
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.06),
    0 4px 16px rgba(0, 0, 0, 0.35);
  transition: all 0.3s ease;
}

.dark-slider:hover {
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.08),
    0 6px 24px rgba(0, 0, 0, 0.45);
  transform: translateY(-1px);
}

.black-duration-slider {
  width: 100%;
  height: 8px;
  background: linear-gradient(90deg, #2c2c2c 0%, #000000 100%);
  border-radius: 10px;
  appearance: none;
  outline: none;
  cursor: pointer;
  transition: background 0.5s ease;
}

.black-duration-slider:hover {
  background: linear-gradient(90deg, #3a3a3a 0%, #0d0d0d 100%);
}

.black-duration-slider::-webkit-slider-thumb {
  appearance: none;
  width: 26px;
  height: 26px;
  border-radius: 50%;
  background: #ffffff;
  border: 2px solid #1f1f1f;
  box-shadow:
    0 3px 10px rgba(0, 0, 0, 0.4),
    0 0 12px rgba(255, 255, 255, 0.1);
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.black-duration-slider::-webkit-slider-thumb:hover {
  transform: scale(1.12);
  box-shadow:
    0 5px 14px rgba(0, 0, 0, 0.5),
    0 0 18px rgba(255, 255, 255, 0.18);
}

.black-duration-slider::-webkit-slider-thumb:active {
  transform: scale(0.96);
  background: #f0f0f0;
}

.slider-value-label {
  margin-top: 16px;
  font-size: 17px;
  font-weight: 700;
  color: #fff;
  letter-spacing: 0.3px;
  text-align: center;
  text-shadow: 0 0 10px rgba(255, 255, 255, 0.15);
  transition: all 0.3s ease;
}

.slider-scale {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  font-weight: 600;
  color: #a3a3a3;
  margin-top: 10px;
  letter-spacing: 0.4px;
}

.dark-slider .section-title {
  color: #fff;
}

.time-range-display {
  margin-top: 20px;
  padding-top: 18px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.time-range-label {
  font-size: 11px;
  font-weight: 600;
  color: #a3a3a3;
  letter-spacing: 0.5px;
  margin-bottom: 8px;
  text-transform: uppercase;
}

.time-range-value {
  font-size: 20px;
  font-weight: 700;
  color: #ffffff;
  letter-spacing: 0.2px;
  text-shadow: 0 0 12px rgba(255, 255, 255, 0.2);
}
</style>