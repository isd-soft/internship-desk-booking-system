<template>
  <v-dialog
      :model-value="visible"
      @update:model-value="$emit('cancel')"
      max-width="560"
      transition="dialog-bottom-transition"
      persistent
  >
    <v-card class="booking-card">
      <v-card-title class="card-header">
        <div class="header-content">
          <div class="header-info">
            <div class="workspace-label">
              <v-icon icon="mdi-pencil-box-outline" size="14" class="mr-1" />
              EDIT DESK
            </div>
            <div class="desk-title">
              {{ desk?.deskName || `Desk ${desk?.i}` }}
            </div>
          </div>
          <v-btn icon variant="text" density="comfortable" @click="close">
            <v-icon>mdi-close</v-icon>
          </v-btn>
        </div>
      </v-card-title>

      <v-card-text class="card-body">

        <div class="section">
          <div class="section-label">Desk Status</div>
          <div class="status-toggle-group">
            <button
                class="status-btn"
                :class="{ 'active-state': !localDesk.isNonInteractive }"
                @click="localDesk.isNonInteractive = false"
            >
              <v-icon icon="mdi-check-circle-outline" size="18" class="mr-2" />
              Activate
            </button>
            <button
                class="status-btn"
                :class="{ 'inactive-state': localDesk.isNonInteractive }"
                @click="localDesk.isNonInteractive = true"
            >
              <v-icon icon="mdi-block-helper" size="18" class="mr-2" />
              Deactivate
            </button>
          </div>
          <div class="status-hint">
            {{ !localDesk.isNonInteractive ? 'This desk is visible and bookable.' : 'This desk is disabled and cannot be booked.' }}
          </div>
        </div>

        <v-divider class="my-5"></v-divider>

        <div class="section">
          <div class="section-header">
            <v-icon icon="mdi-card-account-details-outline" size="18" class="mr-2 text-grey-darken-1"/>
            General Information
          </div>

          <v-row dense>
            <v-col cols="12">
              <div class="field-label">Desk Name</div>
              <v-text-field
                  v-model="localDesk.deskName"
                  variant="outlined"
                  density="comfortable"
                  hide-details
                  class="modern-input"
                  prepend-inner-icon="mdi-tag-text-outline"
                  placeholder="e.g. D-104"
              />
            </v-col>

            <v-col cols="12" class="mt-3">
              <div class="field-label">Zone</div>
              <v-select
                  v-model="zoneDisplay"
                  :items="zones.map(z => ({
                  title: `${z.zoneId} - ${z.zoneAbv} - ${z.zoneName}`,
                  value: `${z.zoneId} - ${z.zoneAbv} - ${z.zoneName}`
                }))"
                  item-title="title"
                  item-value="value"
                  variant="outlined"
                  density="comfortable"
                  hide-details
                  class="modern-select"
                  prepend-inner-icon="mdi-map-marker-radius-outline"
                  placeholder="Select a zone"
              />
            </v-col>
          </v-row>
        </div>

        <div class="section mt-6">
          <div class="section-header">
            <v-icon icon="mdi-axis-arrow" size="18" class="mr-2 text-grey-darken-1"/>
            Position & Layout
          </div>

          <v-row dense>
            <v-col cols="6">
              <div class="field-label">X Coordinate</div>
              <v-text-field
                  v-model.number="localDesk.x"
                  type="number"
                  variant="outlined"
                  density="compact"
                  hide-details
                  class="modern-input"
                  prepend-inner-icon="mdi-arrow-expand-horizontal"
              />
            </v-col>
            <v-col cols="6">
              <div class="field-label">Y Coordinate</div>
              <v-text-field
                  v-model.number="localDesk.y"
                  type="number"
                  variant="outlined"
                  density="compact"
                  hide-details
                  class="modern-input"
                  prepend-inner-icon="mdi-arrow-expand-vertical"
              />
            </v-col>

            <v-col cols="6" class="mt-3">
              <div class="field-label">Width</div>
              <v-text-field
                  v-model.number="localDesk.w"
                  type="number"
                  variant="outlined"
                  density="compact"
                  hide-details
                  class="modern-input"
                  prepend-inner-icon="mdi-ruler"
              />
            </v-col>
            <v-col cols="6" class="mt-3">
              <div class="field-label">Height</div>
              <v-text-field
                  v-model.number="localDesk.h"
                  type="number"
                  variant="outlined"
                  density="compact"
                  hide-details
                  class="modern-input"
                  prepend-inner-icon="mdi-ruler-square"
              />
            </v-col>
          </v-row>
        </div>

      </v-card-text>

      <v-divider></v-divider>

      <v-card-actions class="card-actions">
        <v-btn
            variant="text"
            color="error"
            class="delete-button"
            prepend-icon="mdi-trash-can-outline"
            @click="$emit('delete', desk.i)"
        >
          Delete
        </v-btn>

        <v-spacer></v-spacer>

        <v-btn
            variant="outlined"
            class="restore-button"
            @click="$emit('restore', desk.i)"
        >
          Reset Defaults
        </v-btn>

        <v-btn
            v-if="localDesk.newDesk"
            class="confirm-button"
            flat
            @click="create"
        >
          Create Desk
        </v-btn>

        <v-btn
            v-else
            class="confirm-button"
            flat
            @click="confirm"
        >
          Save Changes
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from "vue";
import { zones } from "./adminFloorLayout";

const props = defineProps<{
  visible: boolean;
  desk: any;
}>();

const emit = defineEmits<{
  (e: "confirm", desk: any): void;
  (e: "restore", deskId: number): void;
  (e: "cancel"): void;
  (e: "create", desk: any): void;
  (e: "delete", deskId: number): void;
  (e: "close", desk: any): void;
}>();

const zoneDisplay = ref('');
const localDesk = ref({ ...props.desk });

watch(
    () => props.desk,
    (newDesk) => {
      if (newDesk) {
        localDesk.value = { ...newDesk };
        // Ensure boolean is set correctly if undefined
        if (localDesk.value.isNonInteractive === undefined) {
          localDesk.value.isNonInteractive = false;
        }

        if(newDesk.zone){
          zoneDisplay.value = `${newDesk.zone.zoneId} - ${newDesk.zone.zoneAbv} - ${newDesk.zone.zoneName}`;
        }
      }
    },
    { immediate: true }
);

watch(zoneDisplay, (newZoneDisplay) => {
  if (newZoneDisplay) {
    const zoneId = Number(newZoneDisplay.split(' - ')[0]);
    const matchingZone = zones.value.find((z: any) => z.zoneId === zoneId);

    if (matchingZone) {
      localDesk.value.zone = matchingZone;
    }
  }
});

function close(){
  emit("close", localDesk.value );
}

function create(){
  emit("create", localDesk.value);
}

function confirm() {
  emit("confirm", localDesk.value);
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
  padding: 24px 28px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  width: 100%;
}

.workspace-label {
  display: flex;
  align-items: center;
  font-size: 11px;
  font-weight: 700;
  color: #737373;
  letter-spacing: 1.2px;
  text-transform: uppercase;
  margin-bottom: 4px;
}

.desk-title {
  font-size: 24px;
  font-weight: 800;
  color: #171717;
  line-height: 1.2;
}

.card-body {
  padding: 24px 28px 10px;
  max-height: 70vh;
  overflow-y: auto;
}

/* Section Styling */
.section-header {
  font-weight: 700;
  font-size: 14px;
  color: #171717;
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.field-label {
  font-size: 12px;
  font-weight: 600;
  color: #525252;
  margin-bottom: 6px;
  margin-left: 2px;
}

.section-label {
  font-size: 13px;
  font-weight: 600;
  color: #171717;
  margin-bottom: 8px;
}

/* Custom Status Buttons */
.status-toggle-group {
  display: flex;
  gap: 12px;
  margin-bottom: 8px;
}

.status-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 12px;
  border-radius: 10px;
  border: 1px solid #e5e7eb;
  background: #f9fafb;
  color: #737373;
  font-weight: 600;
  font-size: 14px;
  transition: all 0.2s ease;
  cursor: pointer;
}

.status-btn:hover {
  background: #f3f4f6;
}

.status-btn.active-state {
  background: #dcfce7; /* light green */
  border-color: #22c55e;
  color: #15803d;
}

.status-btn.inactive-state {
  background: #fee2e2; /* light red */
  border-color: #ef4444;
  color: #b91c1c;
}

.status-hint {
  font-size: 12px;
  color: #a3a3a3;
  margin-left: 2px;
}

/* Inputs */
.modern-input :deep(.v-field),
.modern-select :deep(.v-field) {
  border-radius: 10px;
  border-color: #e5e7eb;
  font-size: 14px;
}

.modern-input :deep(.v-field--focused),
.modern-select :deep(.v-field--focused) {
  border-color: #171717;
}

/* Actions */
.card-actions {
  padding: 20px 28px 24px;
  background: #fafafa;
  border-top: 1px solid #f0f0f0;
}

.delete-button {
  font-weight: 600;
  letter-spacing: 0.3px;
  text-transform: none;
  border-radius: 8px;
}

.restore-button {
  border-color: #d4d4d8 !important;
  color: #525252 !important;
  font-weight: 600;
  text-transform: none;
  letter-spacing: 0.3px;
  height: 44px;
  border-radius: 10px;
  padding: 0 20px;
}

.confirm-button {
  background: #171717 !important;
  color: white !important;
  font-weight: 600;
  text-transform: none;
  letter-spacing: 0.3px;
  height: 44px;
  border-radius: 10px;
  padding: 0 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.confirm-button:hover {
  background: #262626 !important;
  transform: translateY(-1px);
}
</style>