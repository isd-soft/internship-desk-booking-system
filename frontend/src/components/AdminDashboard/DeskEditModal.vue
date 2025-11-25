<template>
  <v-dialog
      :model-value="modelValue"
      @update:model-value="$event ? null : closeModal()"
      max-width="600"
      transition="dialog-bottom-transition"
      persistent
  >
    <v-card class="booking-card">
      <v-card-title class="card-header">
        <div class="header-content">
          <div class="d-flex align-center">
            <v-avatar color="grey-lighten-4" class="mr-4" rounded="lg">
              <v-icon color="#171717">mdi-desk</v-icon>
            </v-avatar>
            <div class="header-info">
              <div class="workspace-label">DESK MANAGER</div>
              <div class="desk-title">Edit Desk</div>
            </div>
          </div>
          <v-btn icon variant="text" density="comfortable" @click="closeModal">
            <v-icon>mdi-close</v-icon>
          </v-btn>
        </div>
      </v-card-title>

      <v-card-text class="card-body">
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
          <v-row dense class="mb-4">
            <v-col cols="12" sm="6">
              <div class="input-label">
                Desk Name <span class="text-caption text-grey">(ID: {{ desk?.id }})</span>
              </div>
              <v-text-field
                  v-model="deskForm.displayName"
                  variant="outlined"
                  density="comfortable"
                  hide-details
                  class="modern-input"
                  placeholder="Enter desk name"
                  prepend-inner-icon="mdi-rename-box"
              />
            </v-col>
            <v-col cols="12" sm="6">
              <div class="input-label">
                Zone <span class="text-caption text-grey">(ID: {{ desk?.zoneId }})</span>
              </div>
              <v-text-field
                  v-model="deskForm.zoneName"
                  variant="outlined"
                  density="comfortable"
                  hide-details
                  class="modern-input"
                  prepend-inner-icon="mdi-map-marker-radius"
                  disabled
                  bg-color="grey-lighten-5"
              />
              <div class="text-caption text-grey mt-1 ml-1">Zone cannot be changed</div>
            </v-col>
          </v-row>

          <v-divider class="mb-6 border-opacity-50"></v-divider>

          <v-row dense class="mb-6">
            <v-col cols="12" class="mb-4">
              <div class="input-label">Desk Type</div>
              <div class="button-grid">
                <v-btn
                    v-for="option in typeDeskOptions"
                    :key="option.value"
                    @click="deskForm.type = option.value"
                    variant="outlined"
                    height="46"
                    class="option-btn"
                    :class="{ 'active-dark': deskForm.type === option.value }"
                >
                  {{ option.value }}
                </v-btn>
              </div>
            </v-col>

            <v-col cols="12">
              <div class="input-label">Desk Status</div>
              <div class="button-grid">
                <v-btn
                    v-for="option in statusDeskOptions"
                    :key="option.value"
                    @click="handleStatusClick(option)"
                    variant="outlined"
                    height="46"
                    class="option-btn"
                    :class="{
                    'active-dark': deskForm.status === option.value && option.value === 'ACTIVE',
                    'active-error': deskForm.status === option.value && option.value === 'DEACTIVATED'
                  }"
                >
                  <v-icon
                      start
                      size="small"
                      :icon="option.value === 'ACTIVE' ? 'mdi-check-circle-outline' : 'mdi-cancel'"
                  />
                  {{ option.value }}
                </v-btn>
              </div>
            </v-col>
          </v-row>

          <div class="input-label mb-2">Coordinates</div>
          <v-sheet border rounded="lg" class="pa-4 bg-grey-lighten-5 mb-6">
            <v-row dense>
              <v-col cols="6">
                  <span class="sub-label">Base X <span class="text-grey">(Saved: {{ desk?.currentX }})</span></span>
                <v-text-field
                    v-model.number="deskForm.baseX"
                    type="number"
                    variant="outlined"
                    density="compact"
                    hide-details
                    class="bg-white"
                    prepend-inner-icon="mdi-axis-x-arrow"
                />
              </v-col>
              <v-col cols="6">
                <span class="sub-label">Base Y <span class="text-grey">(Saved: {{ desk?.currentY }})</span></span>
                <v-text-field
                    v-model.number="deskForm.baseY"
                    type="number"
                    variant="outlined"
                    density="compact"
                    hide-details
                    class="bg-white"
                    prepend-inner-icon="mdi-axis-y-arrow"
                />
              </v-col>
            </v-row>
          </v-sheet>

          <div class="section">
            <div class="d-flex justify-space-between align-center mb-2">
              <div class="input-label mb-0">Temporary Availability</div>
              <v-switch
                  v-model="deskForm.isTemporarilyAvailable"
                  :disabled="!canEnableTemporaryAvailability"
                  color="#171717"
                  hide-details
                  inset
                  density="compact"
                  class="modern-switch"
              />
            </div>

            <div
                class="text-caption mb-3 d-flex align-center"
                :class="!canEnableTemporaryAvailability ? 'text-warning' : 'text-grey'"
            >
              <v-icon size="small" class="mr-1" :color="!canEnableTemporaryAvailability ? 'warning' : 'grey'">
                mdi-information-outline
              </v-icon>
              {{ tempAvailabilityHint }}
            </div>

            <v-expand-transition>
              <v-sheet
                  v-if="deskForm.isTemporarilyAvailable"
                  border
                  rounded="lg"
                  class="pa-4 bg-grey-lighten-5"
              >
                <v-row dense>
                  <v-col cols="12" sm="6">
                    <span class="sub-label">Available From</span>
                    <v-text-field
                        v-model="deskForm.temporaryAvailableFrom"
                        type="datetime-local"
                        variant="outlined"
                        density="compact"
                        hide-details
                        class="bg-white"
                        prepend-inner-icon="mdi-calendar-start"
                    />
                  </v-col>
                  <v-col cols="12" sm="6">
                    <span class="sub-label">Available Until</span>
                    <v-text-field
                        v-model="deskForm.temporaryAvailableUntil"
                        type="datetime-local"
                        variant="outlined"
                        density="compact"
                        hide-details
                        class="bg-white"
                        prepend-inner-icon="mdi-calendar-end"
                    />
                  </v-col>
                </v-row>
              </v-sheet>
            </v-expand-transition>
          </div>

        </v-container>
      </v-card-text>

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
import { reactive, watch, computed, onMounted, ref } from "vue";
import api from '@/plugins/axios';
import {
  fetchDeskStatusEnum,
  fetchDeskTypeEnum,
  statusDeskOptions,
  typeDeskOptions
} from "@/utils/useEnums"

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
    currentX: number | null;
    currentY: number | null ;
    baseX: number | null ;
    baseY: number | null ;
    rawData?: any;
  };
  error: String;
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
    currentX: number | null ;
    currentY: number | null ;
    baseX: number | null ;
    baseY: number | null ;
  }): void;
}

fetchDeskTypeEnum(false);
fetchDeskStatusEnum(false);

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const zones = ref<Array<{ id: number; zoneName: string; zoneAbv: string }>>([]);

onMounted(async () => {
  try {
    const response = await api.get('/admin/zones');
    zones.value = response.data;
  } catch (error) {
    console.error('Error fetching zones:', error);
  }
});

const deskForm = reactive({
  id: 0,
  displayName: "",
  zoneId: 0,
  zoneName: "",
  zoneAbv: "",
  type: "",
  status: "ACTIVE",
  isTemporarilyAvailable: false,
  temporaryAvailableFrom: null as string | null,
  temporaryAvailableUntil: null as string | null,
  currentX: 0,
  currentY: 0,
  baseX: 0,
  baseY: 0
});

watch(() => deskForm.zoneId, (newZoneId) => {
  const selectedZone = zones.value.find(z => z.id === newZoneId);
  if (selectedZone) {
    deskForm.zoneName = selectedZone.zoneName;
    deskForm.zoneAbv = selectedZone.zoneAbv;
  }
});

const canEnableTemporaryAvailability = computed(() => {
  return deskForm.type === "ASSIGNED" && deskForm.status === "ACTIVE";
});

const tempAvailabilityHint = computed(() => {
  if (!deskForm.type) {
    return "Select a desk type first";
  }
  if (deskForm.type !== "ASSIGNED") {
    return "Only available for Assigned desks";
  }
  if (deskForm.status === "DEACTIVATED") {
    return "Cannot set temporary availability on deactivated desks";
  }
  return "Make this desk temporarily available for booking";
});

watch(() => deskForm.type, (newType) => {
  if (newType !== "ASSIGNED" && deskForm.isTemporarilyAvailable) {
    deskForm.isTemporarilyAvailable = false;
    deskForm.temporaryAvailableFrom = null;
    deskForm.temporaryAvailableUntil = null;
  }
});

watch(() => deskForm.status, (newStatus) => {
  if (newStatus === "DEACTIVATED" && deskForm.isTemporarilyAvailable) {
    deskForm.isTemporarilyAvailable = false;
    deskForm.temporaryAvailableFrom = null;
    deskForm.temporaryAvailableUntil = null;
  }
});

async function deactivateDesk() {
  try {
    await api.patch(`/admin/deactivateDesk/${deskForm.id}`);
  } catch (error) {
    console.error("Failed to deactivate desk:", error);
  }
}

async function activateDesk() {
  try {
    await api.patch(`/admin/activateDesk/${deskForm.id}`);
  } catch (error) {
    console.error("Failed to activate desk:", error);
  }
}

async function handleStatusClick(option: { value: string }) {
  deskForm.status = option.value;
  if (option.value === "DEACTIVATED") {
    await deactivateDesk();
  } else if (option.value === "ACTIVE") {
    await activateDesk();
  }
}

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
        deskForm.currentX = desk.currentX;
        deskForm.currentY = desk.currentY;
        deskForm.baseX = desk.baseX;
        deskForm.baseY = desk.baseY;
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
  deskForm.zoneAbv = "";
  deskForm.type = "";
  deskForm.status = "ACTIVE";
  deskForm.isTemporarilyAvailable = false;
  deskForm.temporaryAvailableFrom = null;
  deskForm.temporaryAvailableUntil = null;
  deskForm.currentX = 0;
  deskForm.currentY = 0;
  deskForm.baseX = 0;
  deskForm.baseY = 0
}

function handleSave() {
  emit("save", {
    displayName: deskForm.displayName,
    type: deskForm.type,
    deskStatus: deskForm.status,
    isTemporarilyAvailable: deskForm.isTemporarilyAvailable,
    temporaryAvailableFrom: deskForm.isTemporarilyAvailable ? deskForm.temporaryAvailableFrom : null,
    temporaryAvailableUntil: deskForm.isTemporarilyAvailable ? deskForm.temporaryAvailableUntil : null,
    currentX: deskForm.currentX,
    currentY: deskForm.currentY,
    baseX: deskForm.baseX,
    baseY: deskForm.baseY
  });
}

function closeModal() {
  emit("update:modelValue", false);
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

.modern-input :deep(.v-field.v-field--disabled) {
  background-color: #f9fafb;
  opacity: 1;
  color: #6b7280;
}

/* Button Grid for Type/Status */
.button-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
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

/* Active State: Default (Dark) */
.option-btn.active-dark {
  background: #171717 !important;
  border-color: #171717 !important;
  color: #ffffff !important;
}

/* Active State: Error (Deactivated) */
.option-btn.active-error {
  background: #fef2f2 !important;
  border-color: #ef4444 !important;
  color: #b91c1c !important;
}

/* Switch Styling */
.modern-switch :deep(.v-switch__track) {
  background: #e5e7eb;
  opacity: 1;
}

.modern-switch :deep(.v-switch__thumb) {
  background: white;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

/* Footer Actions */
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
</style>