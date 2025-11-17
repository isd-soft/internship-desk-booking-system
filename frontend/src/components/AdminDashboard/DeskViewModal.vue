<script setup lang="ts">
import { computed } from "vue";
import { fetchColors, getColor} from "@/utils/useEnums";
import { formatDateTime} from "@/utils/useFormatDate";

interface Props {
  modelValue: boolean;
  desk?: {
    id: number;
    name: string;
    zoneId: number;
    zoneName: string;
    type: string;
    status: string;
    isTemporarilyAvailable: boolean;
    tempFrom: string | null;
    tempUntil: string | null;
    rawData?: any;
  };
}
fetchColors();
interface Emits {
  (e: "update:modelValue", value: boolean): void;
  (e: "edit"): void;
}

const props = defineProps<Props>();
const emit = defineEmits<Emits>();



// Check if temporary availability is active now
const isTempAvailabilityActive = computed(() => {
  if (!props.desk?.isTemporarilyAvailable || !props.desk?.tempFrom || !props.desk?.tempUntil) {
    return false;
  }

  const now = new Date();
  const from = new Date(props.desk.tempFrom);
  const until = new Date(props.desk.tempUntil);

  return now >= from && now <= until;
});

function closeModal() {
  emit("update:modelValue", false);
}

function handleEdit() {
  emit("edit");
}
</script>

<template>
  <v-dialog
      :model-value="modelValue"
      @update:model-value="$event ? null : closeModal()"
      max-width="520"
      transition="dialog-bottom-transition"
      persistent
  >
    <v-card class="view-card" elevation="0">
      <v-card-title class="card-header">
        <div class="header-content">
          <div class="header-info">
            <div class="workspace-label">DESK DETAILS</div>
            <div class="desk-title">{{ desk?.name || 'Desk' }}</div>
            <div class="desk-id">ID: {{ desk?.id }}</div>
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
        <!-- Status and Type Badges -->
        <div class="badges-row">
          <div class="badge" :style="{ backgroundColor: getColor(desk?.status) }">
            {{ desk?.status }}
          </div>
          <div class="badge" :style="{ backgroundColor: getColor(desk?.type) }">
            {{ desk?.type }}
          </div>
        </div>

        <!-- Information Grid -->
        <div class="info-section">
          <div class="info-row">
            <div class="info-label">
              <v-icon size="18" class="info-icon">mdi-map-marker</v-icon>
              Zone
            </div>
            <div class="info-value">{{ desk?.zoneName }}</div>
          </div>

          <div class="info-row">
            <div class="info-label">
              <v-icon size="18" class="info-icon">mdi-identifier</v-icon>
              Zone ID
            </div>
            <div class="info-value">{{ desk?.zoneId }}</div>
          </div>
        </div>

        <!-- Temporary Availability Section -->
        <div v-if="desk?.isTemporarilyAvailable" class="temp-availability-section">
          <div class="section-header">
            <v-icon size="20" class="section-icon">mdi-clock-outline</v-icon>
            <span class="section-title">Temporary Availability</span>
            <div v-if="isTempAvailabilityActive" class="active-indicator">
              <span class="pulse-dot"></span>
              Active Now
            </div>
          </div>

          <div class="temp-dates">
            <div class="date-block">
              <div class="date-label">From</div>
              <div class="date-value">
                <v-icon size="16" class="date-icon">mdi-calendar-start</v-icon>
                {{ formatDateTime(desk?.tempFrom) }}
              </div>
            </div>

            <div class="date-divider">
              <v-icon size="20">mdi-arrow-right</v-icon>
            </div>

            <div class="date-block">
              <div class="date-label">Until</div>
              <div class="date-value">
                <v-icon size="16" class="date-icon">mdi-calendar-end</v-icon>
                {{ formatDateTime(desk?.tempUntil) }}
              </div>
            </div>
          </div>
        </div>

        <!-- No Temp Availability Message -->
        <div v-else class="no-temp-availability">
          <v-icon size="24" class="no-temp-icon">mdi-information-outline</v-icon>
          <span>No temporary availability configured</span>
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

.view-card {
  border-radius: 20px !important;
  background: #ffffff;
  border: 1px solid #e5e5e5;
  box-shadow: 0 24px 48px rgba(0, 0, 0, 0.12) !important;
  overflow: hidden;
}

.card-header {
  padding: 28px 28px 24px 28px;
  background: linear-gradient(135deg, #fafafa 0%, #ffffff 100%);
  border-bottom: 1px solid #e5e5e5;
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
  text-transform: uppercase;
}

.desk-title {
  font-size: 28px;
  font-weight: 800;
  color: #171717;
  letter-spacing: -0.5px;
  line-height: 1.2;
  margin-bottom: 4px;
}

.desk-id {
  font-size: 13px;
  font-weight: 600;
  color: #a3a3a3;
  letter-spacing: 0.3px;
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
  padding: 28px;
}

.badges-row {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
}

.badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.5px;
  text-transform: uppercase;
}

.badge-icon {
  opacity: 0.9;
}

.badge-success {
  background: #dcfce7;
  color: #16a34a;
}

.badge-error {
  background: #fee2e2;
  color: #dc2626;
}

.badge-primary {
  background: #dbeafe;
  color: #2563eb;
}

.badge-info {
  background: #e0e7ff;
  color: #4f46e5;
}

.badge-warning {
  background: #fef3c7;
  color: #d97706;
}

.info-section {
  background: #fafafa;
  border-radius: 12px;
  padding: 20px;
  border: 2px solid #e5e5e5;
  margin-bottom: 24px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #e5e5e5;
}

.info-row:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.info-row:first-child {
  padding-top: 0;
}

.info-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  font-weight: 600;
  color: #737373;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.info-icon {
  color: #a3a3a3;
}

.info-value {
  font-size: 15px;
  font-weight: 700;
  color: #171717;
}

.temp-availability-section {
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border: 2px solid #bae6fd;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 24px;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

.section-icon {
  color: #0284c7;
}

.section-title {
  font-size: 14px;
  font-weight: 700;
  color: #0c4a6e;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.active-indicator {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  font-weight: 700;
  color: #16a34a;
  background: #dcfce7;
  padding: 4px 12px;
  border-radius: 6px;
}

.pulse-dot {
  width: 8px;
  height: 8px;
  background: #16a34a;
  border-radius: 50%;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.5;
    transform: scale(1.2);
  }
}

.temp-dates {
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  gap: 16px;
  align-items: center;
}

.date-block {
  background: #ffffff;
  border: 2px solid #bae6fd;
  border-radius: 10px;
  padding: 14px;
}

.date-label {
  font-size: 11px;
  font-weight: 700;
  color: #0c4a6e;
  text-transform: uppercase;
  letter-spacing: 1px;
  margin-bottom: 6px;
}

.date-value {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  color: #0369a1;
}

.date-icon {
  color: #0284c7;
}

.date-divider {
  color: #0284c7;
  display: flex;
  align-items: center;
  justify-content: center;
}

.no-temp-availability {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 32px;
  background: #fafafa;
  border: 2px dashed #e5e5e5;
  border-radius: 12px;
  color: #a3a3a3;
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 24px;
}

.no-temp-icon {
  opacity: 0.5;
}

.card-actions {
  padding: 20px 28px 28px 28px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
  border-top: 1px solid #e5e5e5;
  background: #fafafa;
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

.cancel-button {
  font-weight: 600 !important;
  text-transform: none !important;
  letter-spacing: 0.3px;
  color: #737373 !important;
}

.cancel-button:hover {
  background-color: #e5e5e5 !important;
}

.edit-button {
  background: #171717 !important;
  color: #ffffff !important;
  font-weight: 700 !important;
  text-transform: none !important;
  letter-spacing: 0.3px;
  padding: 0 32px !important;
  height: 52px !important;
  border-radius: 12px !important;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.16) !important;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  gap: 8px;
}

.edit-button:hover {
  background: #262626 !important;
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.24) !important;
  transform: translateY(-2px);
}

.button-icon {
  margin-right: 4px;
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