<script setup lang="ts">
import { onMounted, ref, onBeforeUnmount, computed } from "vue";
import {
  layout,
  colNum,
  rowHeight,
  totalRows,
  IMAGE_WIDTH_PX,
  IMAGE_HEIGHT_PX,
  loadDesksFromBackend,
  resetLayout,
  horizontalDesks,
} from "../VisualFloorMap/floorLayout";
import api from "../../plugins/axios.js";
import DeskEditModal from "./DeskEditModal.vue";


const isDraggingNew = ref(false);
const newDeskPreview = ref<{ x: number; y: number } | null>(null);

 const showEditModal = ref(false);
  const selectedDesk = ref<any>(null);

  function openDeskEditor(item: any) {
    selectedDesk.value = { ...item }; 
    showEditModal.value = true;
  }

  async function applyDeskChanges(updated: any) {
    await api.patch(`/admin/edit/desk/${Number(updated.i)}`,{
      displayName: updated.deskName,
      currentX: updated.x,
      currentY: updated.y
    });

    const item = layout.find(item => item.i === String(updated.i));
    if (item){
      item.deskName = updated.deskName;
    }
    showEditModal.value = false;
  }

  async function restoreDeskDefaults(deskId: number) {
    try{
      console.log(deskId);
      const response = await api.get(`/admin/desk/${deskId}`);
      console.log(response);

      const desk = response.data;

      await api.patch(`/admin/edit/desk/${desk.id}`,
      {currentX: desk.baseX,
      currentY: desk.baseY});

      const item = layout.find(item => item.i === String(deskId));
      if(item){
        item.x = desk.baseX;
        item.y = desk.baseY;
      }

      showEditModal.value = false;
    } catch(err){
      console.error("Failed to restore desk defaults: ", err)
    }
  }

onMounted(async () => {
  resetLayout();
  await loadDesksFromBackend();
});

function handleFloorplanMouseMove(event: MouseEvent) {
  if (!isDraggingNew.value) return;

  const target = event.currentTarget as HTMLElement;
  const rect = target.getBoundingClientRect();
  const x = event.clientX - rect.left;
  const y = event.clientY - rect.top;

  const gridX = Math.floor(x / (IMAGE_WIDTH_PX / colNum));
  const gridY = Math.floor(y / rowHeight);

  newDeskPreview.value = { 
    x: gridX * (IMAGE_WIDTH_PX / colNum), 
    y: gridY * rowHeight 
  };
}

function startAddingDesk() {
  isDraggingNew.value = true;
}

function handleFloorplanClick(event: MouseEvent) {
  if (!isDraggingNew.value) return;

  const target = event.currentTarget as HTMLElement;
  const rect = target.getBoundingClientRect();
  const x = (event.clientX - rect.left) / scale.value;
  const y = (event.clientY - rect.top) / scale.value;

  const gridX = Math.floor(x / (IMAGE_WIDTH_PX / colNum));
  const gridY = Math.floor(y / rowHeight);

  const newId = Math.max(...layout.map((item: any) => Number(item.i)), 0) + 1;
  layout.push({
    x: gridX,
    y: gridY,
    w: 1,
    h: 1,
    i: String(newId),
    deskName: `Desk ${newId}`,
    color: "",
    isNonInteractive: false,
  });

  isDraggingNew.value = false;
  newDeskPreview.value = null;
}

function saveDesk() {
  showEditModal.value = false;
  selectedDesk.value = null;
}

function deleteDesk() {
  showEditModal.value = false;
  selectedDesk.value = null;
}

function saveAllChanges() {
    //to be used to save at the back
}

function cancelAddingDesk() {
  isDraggingNew.value = false;
  newDeskPreview.value = null;
}
</script>

<template>
  <div class="admin-container">
    <div class="toolbar">
      <div class="toolbar-actions">
        <button
          v-if="!isDraggingNew"
          @click="startAddingDesk"
          class="btn btn-primary"
        >
          Add a desk
        </button>
        <button
          v-else
          @click="cancelAddingDesk"
          class="btn btn-secondary"
        >
          Cancel
        </button>
        <button @click="saveAllChanges" class="btn btn-success">
          Save all
        </button>

        <button @click="restoreAllDesks" class="btn btn-defaul">
          Restore all to defaults 
        </button>
      </div>
    </div>

    <div class="floorplan-container">
      <div
        class="floorplan-inner"
        :class="{ 'adding-mode': isDraggingNew }"
        @click="handleFloorplanClick"
        @mousemove="handleFloorplanMouseMove"
      >
        <img
          src="/floorplan/Floor.png"
          alt="Office floor plan"
          class="floorplan-bg"
          draggable="false"
        />

        <div
          v-if="isDraggingNew && newDeskPreview"
          class="new-desk-preview"
          :style="{
            left: newDeskPreview.x + 'px',
            top: newDeskPreview.y + 'px',
            width: (IMAGE_WIDTH_PX / colNum) + 'px',
            height: rowHeight + 'px',
          }"
        ></div>

        <GridLayout
          v-model:layout="layout"
          :col-num="colNum"
          :row-height="rowHeight"
          :width="IMAGE_WIDTH_PX"
          :max-rows="totalRows"
          :margin="[0, 0]"
          :responsive="false"
          :vertical-compact="false"
          prevent-collision
          :use-css-transforms="true"
          :is-draggable="true"
          :is-resizable="false"
        >
        <template #item="{ item }">
        <div
            class="desk-admin"
            @click.stop="openDeskEditor(item)"
            :class="{ vertical: !horizontalDesks.includes(Number(item.i)) }"
        >
            <span class="desk-label">{{ item.deskName || item.i }}</span>
        </div>
        </template>
        </GridLayout>
      </div>
    </div>    
  </div>
  <DeskEditModal
  :visible="showEditModal"
  :desk="selectedDesk"
  @confirm="applyDeskChanges"
  @restore="restoreDeskDefaults"
  @cancel="showEditModal = false"
/>
</template>

<style scoped>

.toolbar {
  background: white;
  padding: 16px 24px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  display: flex;
  justify-content: flex-start;
  align-items: center;
  z-index: 10;
}

.toolbar-actions {
  display: flex;
  gap: 12px;
}

.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-primary {
  background: #3b82f6;
  color: white;
}

.btn-primary:hover {
  background: #2563eb;
}

.btn-secondary {
  background: #6b7280;
  color: white;
}

.btn-secondary:hover {
  background: #4b5563;
}

.btn-success {
  background: #10b981;
  color: white;
}

.btn-success:hover {
  background: #059669;
}

.floorplan-container {
  flex: 1;
  display: flex;
  justify-content: flex-start;
  align-items: flex-start;
}

.floorplan-inner {
  position: relative;
  background: #f4f6fb;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  border: 2px solid #e5e7eb;
  width: 987px;
  height: 643px;
  flex-shrink: 0;
}

.floorplan-inner.adding-mode {
  cursor: crosshair;
}

.floorplan-bg {
  position: absolute;
}

.new-desk-preview {
  position: absolute;
  background: rgba(59, 130, 246, 0.4);
  border: 2px solid #3b82f6;
  border-radius: 0;
  pointer-events: none;
  z-index: 1000;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.3);
}

:deep(.vue-grid-layout) {
  position: absolute;
  top: 0;
  left: 0;
  width: 987px !important;
  height: 643px !important;
  z-index: 1;
}

:deep(.vgl-item) {
  border: 2px solid #d1d5db;
  background: white;
  cursor: move;
}

:deep(.vgl-item:hover) {
  border-color: #3b82f6;
}

:deep(.vgl-item.vue-grid-placeholder) {
  background: rgba(59, 130, 246, 0.2);
  border: 2px dashed #3b82f6;
}

.desk-admin {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  pointer-events: auto;
  cursor: pointer;
}

.desk-admin.vertical .desk-label {
  writing-mode: vertical-rl;
  text-orientation: mixed;
  transform: rotate(180deg);
}

.desk-label {
  font-size: 9px;
  font-weight: 700;
  color: #1f2937;
  pointer-events: none;
  user-select: none;
}
</style>