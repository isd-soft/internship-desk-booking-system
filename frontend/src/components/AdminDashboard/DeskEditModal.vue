<script setup lang="ts" xmlns="http://www.w3.org/1999/html">
import { reactive, watch, computed } from "vue";
import api from '../../plugins/axios';
import {tr} from "vuetify/locale";


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

interface Emits {
  (e: "update:modelValue", value: boolean): void;
  (e: "save", data: {
    displayName: string;
    type: string;
    deskStatus: string;
    isTemporarilyAvailable: boolean;
    temporaryAvailableFrom: string | null;
    temporaryAvailableUntil: string | null;
  }): void;
}

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const deskForm = reactive({
  id: 0,
  displayName: "",
  zoneId: 0,
  zoneName: "",
  type: "",
  status: "ACTIVE",
  isTemporarilyAvailable: false,
  temporaryAvailableFrom: null as string | null,
  temporaryAvailableUntil: null as string | null,
});

const statusOptions = [
  { value: "ACTIVE", label: "Active" },
  { value: "DEACTIVATED", label: "Deactivated" },
];

const typeOptions = [
  { value: "SHARED", label: "Shared" },
  { value: "ASSIGNED", label: "Assigned" },
  { value: "UNAVAILABLE", label: "Unavailable" }
];

// Computed: Can temporary availability be enabled?
const canEnableTemporaryAvailability = computed(() => {
  // Only allow for SHARED desks that are ACTIVE
  return deskForm.type === "SHARED" && deskForm.status === "ACTIVE";
});

// Computed: Validation message
const tempAvailabilityHint = computed(() => {
  if (!deskForm.type) {
    return "Select a desk type first";
  }
  if (deskForm.type !== "SHARED") {
    return "Only available for Shared desks";
  }
  if (deskForm.status === "DEACTIVATED") {
    return "Cannot set temporary availability on deactivated desks";
  }
  return "Make this desk temporarily available for booking";
});

// Computed: Date validation
const dateRangeValid = computed(() => {
  if (!deskForm.isTemporarilyAvailable) return true;
  if (!deskForm.temporaryAvailableFrom || !deskForm.temporaryAvailableUntil) return false;

  const from = new Date(deskForm.temporaryAvailableFrom);
  const until = new Date(deskForm.temporaryAvailableUntil);

  return until > from;
});

const dateValidationMessage = computed(() => {
  if (!deskForm.isTemporarilyAvailable) return "";
  if (!deskForm.temporaryAvailableFrom || !deskForm.temporaryAvailableUntil) {
    return "Both dates are required";
  }
  if (!dateRangeValid.value) {
    return "End date must be after start date";
  }
  return "";
});

// Watch for type changes - disable temp availability if type becomes incompatible
watch(() => deskForm.type, (newType) => {
  if (newType !== "SHARED" && deskForm.isTemporarilyAvailable) {
    deskForm.isTemporarilyAvailable = false;
    deskForm.temporaryAvailableFrom = null;
    deskForm.temporaryAvailableUntil = null;
  }
});

// Watch for status changes - disable temp availability if deactivated
watch(() => deskForm.status, (newStatus) => {
  if (newStatus === "DEACTIVATED" && deskForm.isTemporarilyAvailable) {
    deskForm.isTemporarilyAvailable = false;
    deskForm.temporaryAvailableFrom = null;
    deskForm.temporaryAvailableUntil = null;
  }
});

async function deactivateDesk() {
  try{
    await api.patch(`/admin/deactivateDesk/${deskForm.id}`);
  }catch (error) {
    console.error("Failed to deactivate desk:", error);
  }
}
async function activateDesk() {
  try {
    await api.patch(`/admin/activateDesk/${deskForm.id}`);
  }catch (error) {
    console.error("Failed to activate desk:", error);
  }
}

async function handleStatusClick(option) {
  console.log("deskForm is", deskForm); // debug
  deskForm.status = option.value;
  if (option.value === "DEACTIVATED") {
    await deactivateDesk();
  } else if (option.value === "ACTIVE") {
    await activateDesk();
  }
}



// Watch for temporary availability toggle - clear dates when disabled
watch(() => deskForm.isTemporarilyAvailable, (enabled) => {
  if (!enabled) {
    deskForm.temporaryAvailableFrom = null;
    deskForm.temporaryAvailableUntil = null;
  }
});

// Sync with existing desk
watch(
    () => props.desk,
    (desk) => {
      if (desk) {
        deskForm.id = desk.id;
        deskForm.displayName = desk.name;
        deskForm.zoneId = desk.zoneId;
        deskForm.zoneName = desk.zoneName;
        deskForm.type = desk.type;
        deskForm.status = desk.status;
        deskForm.isTemporarilyAvailable = desk.isTemporarilyAvailable;
        deskForm.temporaryAvailableFrom = desk.tempFrom;
        deskForm.temporaryAvailableUntil = desk.tempUntil;
      } else {
        resetForm();
      }
    },
    { immediate: true }
);

function resetForm() {
  deskForm.id = 0;
  deskForm.displayName = "";
  deskForm.zoneId = 0;
  deskForm.zoneName = "";
  deskForm.type = "";
  deskForm.status = "ACTIVE";
  deskForm.isTemporarilyAvailable = false;
  deskForm.temporaryAvailableFrom = null;
  deskForm.temporaryAvailableUntil = null;
}

function handleSave() {
  // Validate before saving
  if (deskForm.isTemporarilyAvailable && !dateRangeValid.value) {
    return;
  }

  emit("save", {
    displayName: deskForm.displayName,
    type: deskForm.type,
    deskStatus: deskForm.status,
    isTemporarilyAvailable: deskForm.isTemporarilyAvailable,
    temporaryAvailableFrom: deskForm.isTemporarilyAvailable ? deskForm.temporaryAvailableFrom : null,
    temporaryAvailableUntil: deskForm.isTemporarilyAvailable ? deskForm.temporaryAvailableUntil : null,
  });
}

function closeModal() {
  emit("update:modelValue", false);
}
</script>

<template>
  <v-dialog
      :model-value="modelValue"
      @update:model-value="$event ? null : closeModal()"
      max-width="480"
      transition="dialog-bottom-transition"
      persistent
  >
    <v-card class="booking-card" elevation="0">
      <v-card-title class="card-header">
        <div class="header-content">
          <div class="header-info">
            <div class="workspace-label">DESK</div>
            <div class="desk-title">Edit Desk</div>
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
          <div class="section-title">Desk Name</div>
          <input
              v-model="deskForm.displayName"
              type="text"
              class="custom-input"
              placeholder="Enter desk name"
          />
        </div>

        <div class="section">
          <div class="section-title">Zone</div>
          <input
              v-model="deskForm.zoneName"
              type="text"
              class="custom-input"
              placeholder="Enter zone name"
              disabled
              style="opacity: 0.6; cursor: not-allowed;"
          />
          <div class="input-hint">Zone cannot be changed</div>
        </div>

        <div class="section">
          <div class="section-title">Desk Type</div>
          <div class="status-grid">
            <button
                v-for="option in typeOptions"
                :key="option.value"
                @click.stop="deskForm.type = option.value"
                :class="['status-btn', { active: deskForm.type === option.value }]"
            >
              {{ option.label }}
            </button>
          </div>
        </div>

        <div class="section">
          <div class="section-title">Desk Status</div>
          <div class="status-grid">
            <button
                v-for="option in statusOptions"
                :key="option.value"
                @click.stop="handleStatusClick(option)"
                :class="['status-btn', { active: deskForm.status === option.value }]"
            >
              {{ option.label }}
            </button>
          </div>
        </div>

        <div class="section">
          <div class="section-title">Temporary Availability</div>
          <label
              :class="['checkbox-container', { disabled: !canEnableTemporaryAvailability }]"
              @click.prevent="canEnableTemporaryAvailability && (deskForm.isTemporarilyAvailable = !deskForm.isTemporarilyAvailable)"
          >
            <input
                type="checkbox"
                v-model="deskForm.isTemporarilyAvailable"
                class="custom-checkbox"
                :disabled="!canEnableTemporaryAvailability"
            />
            <span class="checkbox-label">Enable temporary availability</span>
          </label>
          <div class="input-hint" :class="{ 'hint-warning': !canEnableTemporaryAvailability }">
            {{ tempAvailabilityHint }}
          </div>

          <!-- Show date/time pickers when checkbox is checked -->
          <transition name="slide-fade">
            <div v-if="deskForm.isTemporarilyAvailable" class="date-range-section">
              <div class="date-input-group">
                <label class="date-label">Available From</label>
                <input
                    v-model="deskForm.temporaryAvailableFrom"
                    type="datetime-local"
                    class="custom-input date-input"
                    placeholder="Select start date & time"
                />
              </div>

              <div class="date-input-group">
                <label class="date-label">Available Until</label>
                <input
                    v-model="deskForm.temporaryAvailableUntil"
                    type="datetime-local"
                    class="custom-input date-input"
                    placeholder="Select end date & time"
                />
              </div>

              <div v-if="dateValidationMessage" class="validation-message">
                {{ dateValidationMessage }}
              </div>
            </div>
          </transition>
        </div>
      </v-card-text>

      <v-card-actions class="card-actions">
        <v-btn
            variant="text"
            @click="closeModal"
            class="cancel-button"
        >
          Cancel
        </v-btn>
        <v-btn
            class="confirm-button"
            size="x-large"
            @click.stop="handleSave"
            :disabled="deskForm.isTemporarilyAvailable && !dateRangeValid"
        >
          Save Changes
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

.section:last-child {
  margin-bottom: 0;
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

.custom-input:hover:not(:disabled) {
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

.input-hint {
  font-size: 12px;
  color: #737373;
  margin-top: 8px;
  font-weight: 500;
}

.hint-warning {
  color: #d97706;
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

.checkbox-container {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 16px;
  border: 2px solid #e5e5e5;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.checkbox-container:hover:not(.disabled) {
  border-color: #a3a3a3;
  background: #fafafa;
}

.checkbox-container.disabled {
  opacity: 0.5;
  cursor: not-allowed;
  background: #f5f5f5;
}

.custom-checkbox {
  width: 20px;
  height: 20px;
  cursor: pointer;
  accent-color: #171717;
}

.custom-checkbox:disabled {
  cursor: not-allowed;
}

.checkbox-label {
  font-size: 14px;
  font-weight: 600;
  color: #171717;
  cursor: pointer;
}

.disabled .checkbox-label {
  cursor: not-allowed;
}

.card-actions {
  padding: 20px 28px 28px 28px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
}

.cancel-button {
  font-weight: 600 !important;
  text-transform: none !important;
  letter-spacing: 0.3px;
  color: #737373 !important;
}

.cancel-button:hover {
  background-color: #f5f5f5 !important;
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

.confirm-button:hover:not(:disabled) {
  background: #262626 !important;
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.24) !important;
  transform: translateY(-2px);
}

.confirm-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.date-range-section {
  margin-top: 16px;
  padding: 20px;
  background: #fafafa;
  border-radius: 12px;
  border: 2px solid #e5e5e5;
}

.date-input-group {
  margin-bottom: 16px;
}

.date-input-group:last-child {
  margin-bottom: 0;
}

.date-label {
  display: block;
  font-size: 12px;
  font-weight: 700;
  color: #171717;
  margin-bottom: 8px;
  letter-spacing: 0.3px;
  text-transform: uppercase;
}

.date-input {
  cursor: pointer;
}

.date-input::-webkit-calendar-picker-indicator {
  cursor: pointer;
  filter: invert(0);
  opacity: 0.6;
  transition: opacity 0.2s ease;
}

.date-input::-webkit-calendar-picker-indicator:hover {
  opacity: 1;
}

.validation-message {
  margin-top: 12px;
  padding: 12px;
  background: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  color: #dc2626;
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