<template>
  <v-dialog
      :model-value="show"
      @update:model-value="$event ? null : closeModal()"
      max-width="550"
      transition="dialog-bottom-transition"
      persistent
  >
    <v-card class="booking-card">
      <v-card-title class="card-header">
        <div class="header-content">
          <div class="d-flex align-center">
            <v-avatar color="grey-lighten-4" class="mr-4" rounded="lg">
              <v-icon color="#171717">mdi-calendar-check</v-icon>
            </v-avatar>
            <div class="header-info">
              <div class="workspace-label">BOOKING MANAGER</div>
              <div class="desk-title">Edit Booking</div>
            </div>
          </div>
          <v-btn icon variant="text" density="comfortable" @click="closeModal">
            <v-icon>mdi-close</v-icon>
          </v-btn>
        </div>
      </v-card-title>

      <v-card-text class="card-body">
        <!-- Error Alert -->
        <v-expand-transition>
          <v-alert
              v-if="error"
              type="error"
              variant="tonal"
              class="mb-4 border-error"
              density="compact"
              closable
              icon="mdi-alert-circle-outline"
          >
            {{ error }}
          </v-alert>
        </v-expand-transition>

        <v-container class="pa-0">
          <!-- Section: Target Desk -->
          <div class="section mb-6">
            <div class="input-label">Target Desk</div>
            <v-text-field
                v-model.number="bookingForm.deskId"
                type="number"
                variant="outlined"
                density="comfortable"
                hide-details
                class="modern-input"
                placeholder="Enter Desk ID"
                prepend-inner-icon="mdi-desk"
            />
          </div>

          <v-divider class="mb-6 border-opacity-50"></v-divider>

          <!-- Section: Timeline -->
          <div class="input-label mb-2">Booking Timeline</div>
          <v-sheet border rounded="lg" class="pa-4 bg-grey-lighten-5 mb-6">
            <v-row dense>
              <v-col cols="12" sm="6">
                <span class="sub-label">Start Time</span>
                <v-text-field
                    v-model="bookingForm.startTime"
                    type="datetime-local"
                    variant="outlined"
                    density="compact"
                    hide-details
                    class="bg-white"
                    prepend-inner-icon="mdi-clock-start"
                />
              </v-col>
              <v-col cols="12" sm="6">
                <span class="sub-label">End Time</span>
                <v-text-field
                    v-model="bookingForm.endTime"
                    type="datetime-local"
                    variant="outlined"
                    density="compact"
                    hide-details
                    class="bg-white"
                    prepend-inner-icon="mdi-clock-end"
                />
              </v-col>
            </v-row>
          </v-sheet>

          <!-- Section: Status -->
          <div class="section mb-6">
            <div class="input-label">Booking Status</div>
            <div class="button-grid">
              <v-btn
                  v-for="option in statusBookingOptions"
                  :key="option.value"
                  @click="bookingForm.status = option.value"
                  variant="outlined"
                  height="46"
                  class="option-btn"
                  :class="{ 'active-dark': bookingForm.status === option.value }"
              >
                <v-icon
                    start
                    size="small"
                    :icon="bookingForm.status === option.value ? 'mdi-check-circle' : 'mdi-circle-outline'"
                />
                {{ option.value }}
              </v-btn>
            </div>
          </div>

          <!-- Summary Card (Dark) -->
          <v-sheet class="summary-box" elevation="0">
            <div class="d-flex align-center gap-3">
              <div class="summary-icon">
                <v-icon size="24" color="white">mdi-clipboard-text-outline</v-icon>
              </div>
              <div class="flex-grow-1">
                <div class="summary-label">Live Summary</div>
                <div class="d-flex justify-space-between align-center">
                  <div class="summary-item">
                    <span class="text-grey-lighten-1 text-caption">Desk ID</span>
                    <div class="text-white font-weight-bold">{{ bookingForm.deskId || "—" }}</div>
                  </div>
                  <div class="summary-divider"></div>
                  <div class="summary-item text-right">
                    <span class="text-grey-lighten-1 text-caption">Current Status</span>
                    <div class="text-white font-weight-bold">{{ bookingForm.status }}</div>
                  </div>
                </div>
              </div>
            </div>
          </v-sheet>
        </v-container>
      </v-card-text>

      <!-- Footer -->
      <v-card-actions class="card-actions">
        <v-btn
            variant="text"
            class="cancel-button"
            size="large"
            @click="closeModal"
        >
          Cancel
        </v-btn>
        <v-btn
            class="confirm-button"
            size="large"
            @click="handleSave"
        >
          <v-icon start>mdi-content-save</v-icon>
          Save Changes
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { reactive, watch } from "vue";
import { fetchBookingStatus, statusBookingOptions } from "@/utils/useEnums";

interface Props {
  show: boolean;
  booking?: {
    deskId: number;
    startTime: string;
    endTime: string;
    status: string;
  };
  error: String;
}

// Maintain original API call
fetchBookingStatus(false);

interface Emits {
  (e: "close"): void;
  (e: "save", data: {
    deskId: number;
    startTime: string;
    endTime: string;
    status: string;
  }): void;
}

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const bookingForm = reactive({
  deskId: 0,
  startTime: "",
  endTime: "",
  status: "ACTIVE",
});

// Sync with existing booking
watch(
    () => props.booking,
    (booking) => {
      if (booking) {
        bookingForm.deskId = booking.deskId;
        bookingForm.startTime = booking.startTime;
        bookingForm.endTime = booking.endTime;
        bookingForm.status = booking.status;
      } else {
        bookingForm.deskId = 0;
        bookingForm.startTime = "";
        bookingForm.endTime = "";
        bookingForm.status = "ACTIVE";
      }
    },
    { immediate: true }
);

function handleSave() {
  emit("save", {
    deskId: bookingForm.deskId,
    startTime: bookingForm.startTime,
    endTime: bookingForm.endTime,
    status: bookingForm.status,
  });
}

function closeModal() {
  emit("close");
}
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap");

.booking-card {
  border-radius: 20px;
  background: #ffffff;
  font-family: "Inter", sans-serif;
}

.card-header {
  padding: 24px 28px;
  border-bottom: 1px solid #f3f4f6;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.workspace-label {
  font-size: 10px;
  font-weight: 700;
  color: #9ca3af;
  letter-spacing: 1.5px;
  text-transform: uppercase;
}

.desk-title {
  font-size: 20px;
  font-weight: 700;
  color: #111827;
  line-height: 1.2;
}

.card-body {
  padding: 28px;
  max-height: 70vh;
  overflow-y: auto;
}

.input-label {
  font-weight: 600;
  font-size: 13px;
  margin-bottom: 8px;
  color: #374151;
  display: flex;
  align-items: center;
  gap: 6px;
}

.sub-label {
  display: block;
  font-size: 11px;
  font-weight: 600;
  color: #6b7280;
  margin-bottom: 4px;
  text-transform: uppercase;
}

/* Modern Input Styling */
.modern-input :deep(.v-field) {
  border-radius: 12px;
  background-color: #ffffff;
  border-color: #e5e7eb;
  transition: all 0.2s ease;
}

.modern-input :deep(.v-field.v-field--focused) {
  border-color: #171717;
  box-shadow: 0 0 0 1px #171717;
}

.button-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.option-btn {
  border-radius: 12px !important;
  border-color: #e5e7eb !important;
  color: #6b7280 !important;
  text-transform: none !important;
  font-weight: 600 !important;
  letter-spacing: 0.3px;
  background: white;
  transition: all 0.2s ease;
}

.option-btn:hover {
  border-color: #d1d5db !important;
  background: #f9fafb;
}

.option-btn.active-dark {
  background: #171717 !important;
  border-color: #171717 !important;
  color: #ffffff !important;
}

.summary-box {
  background: #171717;
  border-radius: 16px;
  padding: 20px;
  margin-top: 8px;
}

.summary-icon {
  width: 42px;
  height: 42px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.summary-label {
  font-size: 11px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.6);
  text-transform: uppercase;
  letter-spacing: 1px;
  margin-bottom: 6px;
}

.summary-divider {
  width: 1px;
  height: 24px;
  background: rgba(255,255,255,0.15);
  margin: 0 16px;
}

.card-actions {
  padding: 0 28px 28px;
  background: #ffffff;
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.cancel-button {
  font-weight: 600 !important;
  text-transform: none !important;
  letter-spacing: 0.3px;
  color: #6b7280 !important;
  border-radius: 12px !important;
}

.confirm-button {
  background: #171717 !important;
  color: #ffffff !important;
  font-weight: 700 !important;
  text-transform: none !important;
  letter-spacing: 0.3px;
  height: 52px !important;
  border-radius: 12px !important;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06) !important;
  flex: 1;
  max-width: 200px;
}

.confirm-button:hover {
  background: #262626 !important;
  transform: translateY(-1px);
}

.border-error {
  border: 1px solid #ef4444 !important;
}

.gap-3 {
  gap: 12px;
}
</style>