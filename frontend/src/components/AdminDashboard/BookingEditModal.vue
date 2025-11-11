<script setup lang="ts">
import { reactive, watch } from "vue";

interface Props {
  show: boolean;
  booking?: {
    deskId: number;
    startDate: string;
    endDate: string;
    status: string;
  };
}

interface Emits {
  (e: "close"): void;
  (e: "save", data: {
    deskId: number;
    startDate: string;
    endDate: string;
    status: string;
  }): void;
}

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const bookingForm = reactive({
  deskId: 0,
  startDate: "",
  endDate: "",
  status: "ACTIVE",
});

const statusOptions = [
  { value: "ACTIVE", label: "Active" },
  { value: "COMPLETED", label: "Completed" },
  { value: "CANCELLED", label: "Cancelled" },
  { value: "PENDING", label: "Pending" },
];

// Sync with existing booking
watch(
    () => props.booking,
    (booking) => {
      if (booking) {
        bookingForm.deskId = booking.deskId;
        bookingForm.startDate = booking.startDate;
        bookingForm.endDate = booking.endDate;
        bookingForm.status = booking.status;
      } else {
        bookingForm.deskId = 0;
        bookingForm.startDate = "";
        bookingForm.endDate = "";
        bookingForm.status = "ACTIVE";
      }
    },
    { immediate: true }
);

function handleSave() {
  emit("save", {
    deskId: bookingForm.deskId,
    startDate: bookingForm.startDate,
    endDate: bookingForm.endDate,
    status: bookingForm.status,
  });
}

function closeModal() {
  emit("close");
}

function formatDateTime(dateStr: string): string {
  if (!dateStr) return "Not set";
  const date = new Date(dateStr);
  return date.toLocaleString("en-US", {
    month: "short",
    day: "numeric",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}
</script>

<template>
  <v-dialog
      :model-value="show"
      @update:model-value="$event ? null : closeModal()"
      max-width="480"
      transition="dialog-bottom-transition"
      persistent
  >
    <v-card class="booking-card" elevation="0">
      <v-card-title class="card-header">
        <div class="header-content">
          <div class="header-info">
            <div class="workspace-label">BOOKING</div>
            <div class="desk-title">Edit Booking</div>
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
        <div class="section">
          <div class="section-title">Desk ID</div>
          <input
              v-model.number="bookingForm.deskId"
              type="number"
              class="custom-input"
              placeholder="Enter desk ID"
          />
        </div>

        <div class="section">
          <div class="section-title">Start Date & Time</div>
          <input
              v-model="bookingForm.startDate"
              type="datetime-local"
              class="custom-input"
          />
        </div>

        <div class="section">
          <div class="section-title">End Date & Time</div>
          <input
              v-model="bookingForm.endDate"
              type="datetime-local"
              class="custom-input"
          />
        </div>

        <div class="section">
          <div class="section-title">Status</div>
          <div class="status-grid">
            <button
                v-for="option in statusOptions"
                :key="option.value"
                @click.stop="bookingForm.status = option.value"
                :class="[
                'status-btn',
                { active: bookingForm.status === option.value },
              ]"
            >
              {{ option.label }}
            </button>
          </div>
        </div>

        <div class="summary-box">
          <div class="summary-icon">
            <v-icon size="24" color="white">mdi-calendar-check</v-icon>
          </div>
          <div class="summary-info">
            <div class="summary-label">Booking Summary</div>
            <div class="summary-details">
              <div class="summary-row">
                <span class="summary-key">Desk:</span>
                <span class="summary-value">{{ bookingForm.deskId || "—" }}</span>
              </div>
              <div class="summary-row">
                <span class="summary-key">Status:</span>
                <span class="summary-value">{{ bookingForm.status }}</span>
              </div>
            </div>
          </div>
        </div>
      </v-card-text>

      <v-card-actions class="card-actions">
        <v-btn class="confirm-button" size="x-large" @click.stop="handleSave">
          Save Changes
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

.custom-input {
  width: 100%;
  padding: 16px;
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

.status-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.status-btn {
  background: #ffffff;
  border: 2px solid #e5e5e5;
  border-radius: 12px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  font-size: 14px;
  font-weight: 600;
  color: #171717;
  outline: none;
}

.status-btn:hover {
  border-color: #a3a3a3;
  background: #fafafa;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.status-btn.active {
  background: #171717;
  border-color: #171717;
  color: #ffffff;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.16);
}

.summary-box {
  background: #171717;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: flex-start;
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
  margin-bottom: 8px;
  letter-spacing: 0.3px;
}

.summary-details {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.summary-key {
  font-size: 13px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.7);
}

.summary-value {
  font-size: 15px;
  font-weight: 700;
  color: #ffffff;
  letter-spacing: -0.2px;
}

.card-actions {
  padding: 20px 28px 28px 28px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
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