<template>
<v-dialog
    :model-value="visible"
    @update:model-value="val => !val && $emit('cancel')"
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
          <div class="workspace-label">EDITOR</div>
          <div class="desk-title">
            {{ desk?.deskName || `Desk ${desk?.i}` }}
          </div>
        </div>
      </div>
      <v-btn icon variant="text" density="comfortable" @click="close">
        <v-icon>mdi-close</v-icon>
      </v-btn>
    </div>
  </v-card-title>

  <v-card-text class="card-body">
    <v-container class="pa-0">
      <v-row dense>
        <v-col cols="12" sm="7">
          <div class="input-label">Desk Name</div>
          <v-text-field
              v-model="localDesk.deskName"
              variant="outlined"
              density="comfortable"
              hide-details
              class="modern-input"
              placeholder="e.g. D-104"
              prepend-inner-icon="mdi-rename-box"
          />
        </v-col>
        <v-col cols="12" sm="5">
          <div class="input-label">Zone Assignment</div>
          <v-select
              v-model="zoneDisplay"
              :items="zones.map(z => ({
                  title: `${z.zoneId} - ${z.zoneAbv}`,
                  subtitle: z.zoneName,
                  value: `${z.zoneId} - ${z.zoneAbv} - ${z.zoneName}`
                }))"
              item-title="title"
              item-value="value"
              variant="outlined"
              density="comfortable"
              hide-details
              class="modern-select"
              prepend-inner-icon="mdi-map-marker-radius"
          >
            <template v-slot:item="{ props, item }">
              <v-list-item v-bind="props" :subtitle="item.raw.subtitle"></v-list-item>
            </template>
          </v-select>
        </v-col>
      </v-row>

      <v-divider class="my-6 border-opacity-50"></v-divider>

      <div class="section mb-6">
        <div class="input-label mb-2">Availability Status</div>
        <div class="status-toggle-group">
          <v-btn
              class="status-btn"
              :class="{ 'active-success': !localDesk.isNonInteractive }"
              @click="activateDesk"
          >
            <v-icon start icon="mdi-check-circle-outline" />
            Activate Desk
          </v-btn>

          <v-btn
              class="status-btn"
              :class="{ 'active-error': localDesk.isNonInteractive }"
              @click="deactivateDesk"
          >
            <v-icon start icon="mdi-cancel" />
            Deactivate
          </v-btn>
        </div>
        <div class="status-hint mt-2">
          <v-icon size="small" color="grey" class="mr-1">mdi-information-outline</v-icon>
          <span v-if="localDesk.isNonInteractive" class="text-caption text-grey">
                This desk is disabled and cannot be booked by users.
              </span>
          <span v-else class="text-caption text-grey">
                This desk is live and available for booking.
              </span>
        </div>
      </div>

      <div class="input-label mb-2">Layout Configuration</div>
      <v-sheet border rounded="lg" class="pa-4 bg-grey-lighten-5">
        <v-row dense>
          <v-col cols="6" sm="3">
            <span class="sub-label">Width</span>
            <v-text-field
                v-model.number="localDesk.w"
                type="number"
                variant="outlined"
                density="compact"
                hide-details
                class="bg-white"
                prepend-inner-icon="mdi-arrow-expand-horizontal"
            />
          </v-col>
          <v-col cols="6" sm="3">
            <span class="sub-label">Height</span>
            <v-text-field
                v-model.number="localDesk.h"
                type="number"
                variant="outlined"
                density="compact"
                hide-details
                class="bg-white"
                prepend-inner-icon="mdi-arrow-expand-vertical"
            />
          </v-col>

          <v-col cols="6" sm="3">
            <span class="sub-label">Pos X</span>
            <v-text-field
                v-model.number="localDesk.x"
                type="number"
                variant="outlined"
                density="compact"
                hide-details
                class="bg-white"
                prepend-inner-icon="mdi-arrow-up-down"
            />
          </v-col>
          <v-col cols="6" sm="3">
            <span class="sub-label">Pos Y</span>
            <v-text-field
                v-model.number="localDesk.y"
                type="number"
                variant="outlined"
                density="compact"
                hide-details
                class="bg-white"
                prepend-inner-icon="mdi-arrow-left-right"
            />
          </v-col>
        </v-row>
      </v-sheet>
    </v-container>
  </v-card-text>

  <v-card-actions class="card-actions d-flex flex-column gap-2">
    <div class="main-actions w-100 d-flex gap-3">
      <v-btn
          v-if="localDesk.newDesk"
          class="action-btn confirm-btn"
          size="large"
          block
          @click="create"
      >
        <v-icon start>mdi-plus-box</v-icon>
        Create Desk
      </v-btn>

      <v-btn
          v-else
          class="action-btn confirm-btn"
          size="large"
          block
          @click="confirm"
      >
        <v-icon start>mdi-content-save</v-icon>
        Save Changes
      </v-btn>
    </div>

    <div class="secondary-actions w-100 d-flex justify-space-between mt-2">
      <v-btn
          variant="text"
          color="grey-darken-1"
          class="px-2"
          @click="$emit('restore', desk.i)"
      >
        <v-icon start size="small">mdi-backup-restore</v-icon>
        Restore Defaults
      </v-btn>

      <v-btn
          variant="text"
          color="error"
          class="px-2"
          @click="$emit('delete', desk.i)"
      >
        <v-icon start size="small">mdi-trash-can-outline</v-icon>
        Delete Desk
      </v-btn>
    </div>
  </v-card-actions>
</v-card>
</v-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from "vue";
import { zones } from "./adminFloorLayout";
import api from '@/plugins/axios';

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

async function activateDesk() {
  try {
    const deskId = localDesk.value.id ?? localDesk.value.deskId ?? localDesk.value.i;
    await api.patch(`/admin/activateDesk/${deskId}`);
    localDesk.value.isNonInteractive = false;
  } catch (error) {
    console.error("Failed to activate desk:", error);
  }
}

async function deactivateDesk() {
  try {
    const deskId = localDesk.value.id ?? localDesk.value.deskId ?? localDesk.value.i;
    await api.patch(`/admin/deactivateDesk/${deskId}`);
    localDesk.value.isNonInteractive = true;
  } catch (error) {
    console.error("Failed to deactivate desk:", error);
  }
}



function close(){
  emit("close", localDesk.value );
}

function create(){
  console.log(localDesk.value);
  emit("create", localDesk.value);
}

function confirm() {
  console.log(localDesk.value);
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
}

.sub-label {
  display: block;
  font-size: 11px;
  font-weight: 600;
  color: #6b7280;
  margin-bottom: 4px;
  text-transform: uppercase;
}

.modern-input :deep(.v-field),
.modern-select :deep(.v-field) {
  border-radius: 12px;
  background-color: #ffffff;
  border-color: #e5e7eb;
  transition: all 0.2s ease;
}

.modern-input :deep(.v-field.v-field--focused) {
  border-color: #171717;
  box-shadow: 0 0 0 1px #171717;
}

.status-toggle-group {
  display: flex;
  gap: 12px;
}

.status-btn {
  flex: 1;
  border-radius: 12px !important;
  border-color: #e5e7eb !important;
  color: #6b7280 !important;
  text-transform: none !important;
  font-weight: 600 !important;
  letter-spacing: 0.3px;
  background: white;
  transition: all 0.2s ease;
}

.status-btn.active-success {
  background: #ecfdf5 !important;
  border-color: #10b981 !important;
  color: #047857 !important;
}

.status-btn.active-error {
  background: #fef2f2 !important;
  border-color: #ef4444 !important;
  color: #b91c1c !important;
}

.card-actions {
  padding: 0 28px 28px;
  background: #ffffff;
}

.action-btn {
  border-radius: 12px !important;
  text-transform: none !important;
  font-weight: 700 !important;
  letter-spacing: 0.3px;
  height: 52px !important;
}

.confirm-btn {
  background: #171717 !important;
  color: #ffffff !important;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06) !important;
}

.confirm-btn:hover {
  background: #262626 !important;
  transform: translateY(-1px);
}

.gap-2 { gap: 8px; }
.gap-3 { gap: 12px; }
</style>
