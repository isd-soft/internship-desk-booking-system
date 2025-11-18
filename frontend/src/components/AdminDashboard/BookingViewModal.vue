<script setup lang="ts">
import {computed, reactive, ref, watch} from "vue";
import { calculateDuration, formatDateTime } from "@/utils/useFormatDate"
import {fetchColors, getColor} from "@/utils/useEnums"
interface Props {
  show: boolean;
  booking?: {
    id: number;
    userId: number;
    deskId: number;
    deskName: string;
    deskType: string;
    zoneId: number;
    zoneName: string;
    startTime: string;
    endTime: string;
    status: string;
  };
}

const props = defineProps<Props>();

const bookingForm = reactive({
  id: 0,
  userId: 0,
  deskId: 0,
  deskName: "",
  deskType: "SHARED",
  zoneId: 0,
  zoneName: "",
  startTime: "",
  endTime: "",
  status: "ACTIVE",
});
fetchColors();
watch(
    () => props.booking,
    (booking) => {
      if (booking) {
        bookingForm.id = booking.id;
        bookingForm.userId = booking.userId;
        bookingForm.deskId = booking.deskId;
        bookingForm.deskName = booking.deskName;
        bookingForm.deskType = booking.deskType;
        bookingForm.zoneId = booking.zoneId;
        bookingForm.zoneName = booking.zoneName;
        bookingForm.startTime = booking.startTime;
        bookingForm.endTime = booking.endTime;
        bookingForm.status = booking.status;
      } else {
        bookingForm.id = 0;
        bookingForm.userId = 0;
        bookingForm.deskId = 0;
        bookingForm.deskName = "";
        bookingForm.deskType = "SHARED";
        bookingForm.zoneId = 0;
        bookingForm.zoneName = "";
        bookingForm.startTime = "";
        bookingForm.endTime = "";
        bookingForm.status = "ACTIVE";
      }
    },
    { immediate: true }
);

  const isLunchIncluded = true;
  const LUNCH_START = 13;
  const LUNCH_END = 14;
  const MIN_HOUR = 9;
  const MAX_HOUR = 18;
  const hours = Array.from(
    { length: MAX_HOUR - MIN_HOUR + 1 },
    (_, i) => MIN_HOUR + i
);

const lunchOverlap = computed(() => {
  if (!bookingForm.startTime || !bookingForm.endTime) return false;

  // Extract hour from ISO datetime string
  const start = new Date(bookingForm.startTime).getHours();
  const end = new Date(bookingForm.endTime).getHours();

  console.log('Start hour:', start);
  console.log('End hour:', end);
  console.log('Overlap check:', start < LUNCH_END && end > LUNCH_START);

  return start < LUNCH_END && end > LUNCH_START;
});

function getDeskTypeIcon(type: string): string {
  const typeMap: Record<string, string> = {
    SHARED: "mdi-account-multiple",
    PRIVATE: "mdi-account",
    MEETING: "mdi-account-group",
  };
  return typeMap[type.toUpperCase()] || "mdi-desk";
}
</script>

<template>
  <v-dialog
      :model-value="show"
      @update:model-value="$event ? null : $emit('update:modelValue', false)"
      max-width="560"
      transition="dialog-bottom-transition"
      persistent
  >
    <v-card class="booking-card" elevation="0">
      <v-card-title class="card-header">
        <div class="header-content">
          <div class="header-info">
            <div class="workspace-label">BOOKING DETAILS</div>
            <div class="desk-title">{{ booking?.deskName || 'Booking Information' }}</div>
            <div class="desk-subtitle">{{ booking?.zoneName }} • Zone {{ booking?.zoneId }}</div>
          </div>
          <v-btn
              icon
              variant="text"
              size="small"
              @click="$emit('update:modelValue', false)"
              class="close-button"
          >
            <v-icon size="20">mdi-close</v-icon>
          </v-btn>
        </div>
      </v-card-title>

      <v-card-text class="card-body">
        <!-- Status Banner -->
        <div class="status-banner" :style="{ background: getColor(booking?.status || 'ACTIVE') }">
          <div class="status-icon">
            <v-icon size="20" color="white">mdi-calendar-check</v-icon>
          </div>
          <div class="status-info">
            <div class="status-label">Booking Status</div>
            <div class="status-value">{{ booking?.status || '—' }}</div>
          </div>
        </div>

        <!-- Main Info Grid -->
        <div class="info-grid">
          <div class="info-card">
            <div class="info-icon">
              <v-icon size="20" color="#171717">mdi-identifier</v-icon>
            </div>
            <div class="info-content">
              <div class="info-label">Booking ID</div>
              <div class="info-value">{{ booking?.id || '—' }}</div>
            </div>
          </div>

          <div class="info-card">
            <div class="info-icon">
              <v-icon size="20" color="#171717">mdi-account</v-icon>
            </div>
            <div class="info-content">
              <div class="info-label">User ID</div>
              <div class="info-value">{{ booking?.userId || '—' }}</div>
            </div>
          </div>

          <div class="info-card">
            <div class="info-icon">
              <v-icon size="20" color="#171717">mdi-desk</v-icon>
            </div>
            <div class="info-content">
              <div class="info-label">Desk ID</div>
              <div class="info-value">{{ booking?.deskId || '—' }}</div>
            </div>
          </div>

          <div class="info-card">
            <div class="info-icon">
              <v-icon size="20" color="#171717" :icon="getDeskTypeIcon(booking?.deskType || 'SHARED')"></v-icon>
            </div>
            <div class="info-content">
              <div class="info-label">Desk Type</div>
              <div class="info-value">{{ booking?.deskType || '—' }}</div>
            </div>
          </div>
        </div>

        <!-- Zone Information -->
        <div class="section">
          <div class="section-title">
            <v-icon size="18" color="#171717" class="mr-2">mdi-map-marker</v-icon>
            Location Information
          </div>
          <div class="location-box">
            <div class="location-row">
              <span class="location-key">Zone ID</span>
              <span class="location-value">{{ booking?.zoneId || '—' }}</span>
            </div>
            <div class="location-divider"></div>
            <div class="location-row">
              <span class="location-key">Zone Name</span>
              <span class="location-value">{{ booking?.zoneName || '—' }}</span>
            </div>
          </div>
        </div>

        <!-- Time Information -->
        <div class="section">
          <div class="section-title">
            <v-icon size="18" color="#171717" class="mr-2">mdi-clock-outline</v-icon>
            Schedule
          </div>
          <div class="time-grid">
            <div class="time-card start">
              <div class="time-label">
                <v-icon size="16" color="#10b981" class="mr-1">mdi-clock-start</v-icon>
                Start Time
              </div>
              <div class="time-value">{{ formatDateTime(booking?.startTime || '') }}</div>
            </div>
            <div class="time-card end">
              <div class="time-label">
                <v-icon size="16" color="#ef4444" class="mr-1">mdi-clock-end</v-icon>
                End Time
              </div>
              <div class="time-value">{{ formatDateTime(booking?.endTime || '') }}</div>
            </div>
            <div class="time-card start simple">
              <div class="time-label">
                <v-icon size="16" color="#37aede" class="mr-1">mdi-clock-start</v-icon>
                Lunch Time
              </div>
              <div class="time-value">{{ lunchOverlap ? 'YES' : 'NO' }}</div>
            </div>
            <div class="time-card end simple">
              <div class="time-label">
                <v-icon size="16" color="#37aede" class="mr-1">mdi-clock-outline</v-icon>
                Duration
              </div>
              <div class="time-value">
                {{ calculateDuration(booking?.startTime, booking?.endTime) || '—' }}
              </div>
            </div>
          </div>
        </div>
      </v-card-text>

      <v-card-actions class="card-actions">
        <v-btn
            class="close-action-button"
            size="large"
            block
            @click="$emit('update:modelValue', false)"
        >
          Close
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap");

* {
  font-family: "Inter", -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
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
  background: #fafafa;
  border-bottom: 2px solid #f0f0f0;
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
  font-size: 24px;
  font-weight: 800;
  color: #171717;
  letter-spacing: -0.5px;
  line-height: 1.2;
  margin-bottom: 6px;
}

.desk-subtitle {
  font-size: 14px;
  font-weight: 600;
  color: #737373;
  letter-spacing: 0.3px;
}

.close-button {
  opacity: 0.6;
  transition: all 0.2s ease;
  margin-top: -4px;
}

.close-button:hover {
  opacity: 1;
  background-color: #ffffff !important;
  transform: rotate(90deg);
}

.card-body {
  padding: 24px 28px;
}

.status-banner {
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.status-icon {
  width: 44px;
  height: 44px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.status-info {
  flex: 1;
}

.status-label {
  font-size: 12px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.85);
  margin-bottom: 4px;
  letter-spacing: 0.5px;
}

.status-value {
  font-size: 20px;
  font-weight: 800;
  color: #ffffff;
  letter-spacing: -0.3px;
  text-transform: uppercase;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-bottom: 24px;
}

.info-card {
  background: #fafafa;
  border: 2px solid #f0f0f0;
  border-radius: 14px;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  transition: all 0.3s ease;
}

.info-card:hover {
  background: #ffffff;
  border-color: #e5e5e5;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
}

.info-icon {
  width: 40px;
  height: 40px;
  background: #ffffff;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  border: 2px solid #f0f0f0;
}

.info-content {
  flex: 1;
  min-width: 0;
}

.info-label {
  font-size: 11px;
  font-weight: 700;
  color: #737373;
  margin-bottom: 4px;
  letter-spacing: 0.5px;
  text-transform: uppercase;
}

.info-value {
  font-size: 16px;
  font-weight: 800;
  color: #171717;
  letter-spacing: -0.3px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
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
  display: flex;
  align-items: center;
}

.location-box {
  background: #171717;
  border-radius: 14px;
  padding: 18px 20px;
}

.location-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 0;
}

.location-divider {
  height: 1px;
  background: rgba(255, 255, 255, 0.15);
  margin: 8px 0;
}

.location-key {
  font-size: 13px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.7);
  letter-spacing: 0.3px;
}

.location-value {
  font-size: 15px;
  font-weight: 700;
  color: #ffffff;
  letter-spacing: -0.2px;
}

.time-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.time-card {
  background: #fafafa;
  border: 2px solid #f0f0f0;
  border-radius: 14px;
  padding: 16px;
  transition: all 0.3s ease;
}

.time-card:hover {
  background: #ffffff;
  border-color: #e5e5e5;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
}

.time-card.start {
  border-left: 4px solid #10b981;
}

.time-card.end {
  border-left: 4px solid #ef4444;
}

.time-card.simple {
  border-left: 4px solid #37aede;
}
.time-label {
  font-size: 11px;
  font-weight: 700;
  color: #737373;
  margin-bottom: 8px;
  letter-spacing: 0.5px;
  text-transform: uppercase;
  display: flex;
  align-items: center;
}

.time-value {
  font-size: 13px;
  font-weight: 700;
  color: #171717;
  letter-spacing: -0.1px;
  line-height: 1.4;
}

.card-actions {
  padding: 20px 28px 28px 28px;
  border-top: 2px solid #f5f5f5;
}

.close-action-button {
  background: #171717 !important;
  color: #ffffff !important;
  font-weight: 700 !important;
  text-transform: none !important;
  letter-spacing: 0.3px;
  height: 48px !important;
  border-radius: 12px !important;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.16) !important;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.close-action-button:hover {
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