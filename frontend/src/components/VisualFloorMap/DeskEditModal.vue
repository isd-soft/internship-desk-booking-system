<template>
  <v-dialog
    :model-value="visible"
    @update:model-value="$emit('cancel')"
    max-width="520"
    transition="dialog-bottom-transition"
    persistent
  >
    <v-card class="booking-card">
      <v-card-title class="card-header">
        <div class="header-content">
          <div class="header-info">
            <div class="workspace-label">EDIT DESK</div>
            <div class="desk-title">
              {{ desk?.deskName || `Desk ${desk?.i}` }}
            </div>
          </div>
          <v-btn icon variant="text" size="small" @click="close">
            <v-icon size="20">mdi-close</v-icon>
          </v-btn>
        </div>
      </v-card-title>

      <v-card-text class="card-body">
        <div class="section">
          <div class="section-title">Desk Name</div>
          <v-text-field
            v-model="localDesk.deskName"
            variant="outlined"
            density="comfortable"
            hide-details
            class="modern-input"
            placeholder="Enter desk name"
          />
        </div>
        <div class="section">
          <div class="section-title">X</div>
          <v-text-field
            v-model.number="localDesk.x"
            variant="outlined"
            density="comfortable"
            hide-details
            class="modern-input"
            placeholder="Enter x coordinate"
          />
        </div>
        
        <div class="section">
          <div class="section-title">Y</div>
          <v-text-field
            v-model.number="localDesk.y"
            variant="outlined"
            density="comfortable"
            hide-details
            class="modern-input"
            placeholder="Enter y coordinate"
          />
        </div>

        <div class="section">
          <div class="section-title">Height</div>
          <v-text-field
            v-model.number="localDesk.h"
            variant="outlined"
            density="comfortable"
            hide-details
            class="modern-input"
            placeholder="Enter desk height"
          />
        </div>

        <div class="section">
          <div class="section-title">Width</div>
          <v-text-field
            v-model.number="localDesk.w"
            variant="outlined"
            density="comfortable"
            hide-details
            class="modern-input"
            placeholder="Enter desk width"
          />
        </div>

        <v-select
          label="Select"
          v-model="zoneDisplay"
          :items="zones.map(z => ({
            title: `${z.zoneId} - ${z.zoneAbv} - ${z.zoneName}`,
            value: `${z.zoneId} - ${z.zoneAbv} - ${z.zoneName}`
          }))"
          item-title="title"
          item-value="value"
        />

        <div class="section">
          <div class="section-title">Disable desk</div>
          <v-switch
            v-model="localDesk.isNonInteractive"
            :label="localDesk.isNonInteractive ? 'Disabled (non-bookable)' : 'Enabled (bookable)'"
            color="#171717"
            hide-details
            inset
            class="modern-switch"
          />
        </div>
      </v-card-text>

      <v-card-actions class="card-actions">
        <div style="width: 100%; display: flex; gap: 12px">
          <v-btn
            variant="outlined"
            class="restore-button"
            size="x-large"
            @click="$emit('restore', desk.i)"
          >
            Restore Default coordinates
          </v-btn>

          <v-btn
            variant="outlined"
            class="delete-button"
            size="x-large"
            @click="$emit('delete', desk.i)"
          >
            delete desk
          </v-btn>

          <v-btn v-if=" localDesk.newDesk"
            class="confirm-button"
            size="x-large"
            @click="create"
          >
            Create desk
          </v-btn>

          <v-btn v-else
            class="confirm-button"
            size="x-large"
            @click="confirm"
          >
            Save Changes
          </v-btn>
        </div>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { ref, watch, computed } from "vue";
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
  console.log(localDesk);
  emit("create", localDesk.value);
}

function confirm() {
  console.log(localDesk);
  emit("confirm", localDesk.value);
}
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap");

.booking-card {
  border-radius: 20px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.15);
  overflow: hidden;
  font-family: "Inter", sans-serif;
}

.card-header {
  padding: 26px 28px 16px;
  border-bottom: 1px solid #f0f0f0;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.workspace-label {
  font-size: 11px;
  font-weight: 700;
  color: #737373;
  letter-spacing: 1.5px;
}

.desk-title {
  font-size: 26px;
  font-weight: 800;
  color: #171717;
  margin-bottom: 12px;
}

.card-body {
  padding: 20px 28px 28px;
}

.section {
  margin-bottom: 24px;
}

.section-title {
  font-weight: 600;
  font-size: 15px;
  margin-bottom: 12px;
  letter-spacing: 0.5px;
  color: #171717;
}

.modern-input :deep(.v-field) {
  border-radius: 12px;
}

.modern-select :deep(.v-field) {
  border-radius: 12px;
}

.status-preview {
  display: flex;
  align-items: center;
  gap: 10px;
}

.status-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  flex-shrink: 0;
}

.orientation-toggle {
  width: 100%;
  background: #f1f5f9;
  border-radius: 12px;
  overflow: hidden;
}

.orientation-toggle :deep(.v-btn) {
  flex: 1;
  height: 56px !important;
  border-radius: 0 !important;
  opacity: 0.7;
}

.orientation-toggle :deep(.v-btn.v-btn--selected) {
  background: #171717 !important;
  color: white;
  opacity: 1;
}

.modern-switch :deep(.v-switch__track) {
  background: #e5e7eb;
}

.modern-switch :deep(.v-switch__thumb) {
  background: white;
}

.card-actions {
  padding: 0 28px 28px;
  display: flex;
  gap: 12px;
}

.restore-button {
  flex: 1;
  background: transparent !important;
  color: #525252 !important;
  border: 2px solid #d4d4d8 !important;
  font-weight: 700 !important;
  text-transform: none !important;
  letter-spacing: 0.3px;
  height: 52px !important;
  border-radius: 12px !important;
}

.restore-button:hover {
  background: #f4f4f5 !important;
  border-color: #a1a1aa !important;
}

.confirm-button {
  flex: 2;
  background: #171717 !important;
  color: #ffffff !important;
  font-weight: 700 !important;
  text-transform: none !important;
  letter-spacing: 0.3px;
  height: 52px !important;
  border-radius: 12px !important;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.16) !important;
  transition: all 0.3s ease;
}

.confirm-button:hover {
  background: #262626 !important;
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.2) !important;
}
</style>