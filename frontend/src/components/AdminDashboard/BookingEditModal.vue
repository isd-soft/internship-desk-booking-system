<script setup lang="ts">
import {reactive, ref, watch} from "vue";
import {fetchBookingStatus,statusBookingOptions} from "@/utils/useEnums"
import api from "../../plugins/axios";

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
fetchBookingStatus(false)
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

const deskName = ref<string>("");
const deskZone = ref<string>("");
const deskType = ref<string>("");
const loadingDesk = ref(false);
const deskError = ref<string>("");

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

// Watch desk ID changes to fetch desk details
watch(
    () => bookingForm.deskId,
    async (newDeskId) => {
      if (newDeskId && newDeskId > 0) {
        await fetchDeskDetails(newDeskId);
      } else {
        deskName.value = "";
        deskZone.value = "";
        deskType.value = "";
        deskError.value = "";
      }
    }
);

async function fetchDeskDetails(deskId: number) {
  try {
    loadingDesk.value = true;
    deskError.value = "";
    const response = await api.get(`/admin/desk/${deskId}`);
    const desk = response.data;
    deskName.value = desk.displayName || "Unknown Desk";
    deskZone.value = desk.zoneDto?.zoneName || "Unknown Zone";
    deskType.value = desk.type || "Unknown";
  } catch (err: any) {
    console.error("Error fetching desk details:", err);
    deskError.value = "Desk not found";
    deskName.value = "";
    deskZone.value = "";
    deskType.value = "";
  } finally {
    loadingDesk.value = false;
  }
}

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
        <v-alert
            v-if="error"
            type="error"
            variant="tonal"
            class="mb-4"
            density="compact"
            closable
        >
          {{error}}
        </v-alert>
        <div class="section">
          <div class="section-title">Desk ID</div>
          <input
              v-model.number="bookingForm.deskId"
              type="number"
              class="custom-input"
              placeholder="Enter desk ID"
          />
          <div v-if="loadingDesk" class="desk-info loading">
            <v-progress-circular
                indeterminate
                size="16"
                width="2"
                color="#171717"
            />
            <span>Loading desk details...</span>
          </div>
          <div v-else-if="deskError" class="desk-info error">
            <v-icon size="16" color="#ef4444">mdi-alert-circle</v-icon>
            <span>{{ deskError }}</span>
          </div>
          <div v-else-if="deskName" class="desk-info success">
            <v-icon size="16" color="#10b981">mdi-check-circle</v-icon>
            <div class="desk-details">
              <span class="desk-name">{{ deskName }}</span>
              <span class="desk-meta">{{ deskZone }} • {{ deskType }}</span>
            </div>
          </div>
        </div>

        <div class="section">
          <div class="section-title">Start Date & Time</div>
          <input
              v-model="bookingForm.startTime"
              type="datetime-local"
              class="custom-input"
          />
        </div>

        <div class="section">
          <div class="section-title">End Date & Time</div>
          <input
              v-model="bookingForm.endTime"
              type="datetime-local"
              class="custom-input"
          />
        </div>

        <div class="section">
          <div class="section-title">Status</div>
          <div class="status-grid">
            <button
                v-for="option in statusBookingOptions"
                :key="option.value"
                @click.stop="bookingForm.status = option.value"
                :class="[
                'status-btn',
                { active: bookingForm.status === option.value },
              ]"
            >
              {{ option.value }}
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
                <span class="summary-value">{{ deskName || bookingForm.deskId || "—" }}</span>
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

.desk-info {
  margin-top: 12px;
  padding: 12px 16px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
  transition: all 0.3s ease;
}

.desk-info.loading {
  background: #f5f5f5;
  border: 1px solid #e5e5e5;
  color: #737373;
}

.desk-info.error {
  background: #fef2f2;
  border: 1px solid #fecaca;
  color: #dc2626;
  font-weight: 500;
}

.desk-info.success {
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
  color: #16a34a;
}

.desk-details {
  display: flex;
  flex-direction: column;
  gap: 2px;
  flex: 1;
}

.desk-name {
  font-weight: 700;
  font-size: 14px;
  color: #171717;
  letter-spacing: -0.2px;
}

.desk-meta {
  font-size: 12px;
  font-weight: 500;
  color: #737373;
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