<script setup lang="ts">
import { onMounted, ref } from "vue";
import {
  layout,
  rowHeight,
  loadDesksFromBackend,
  resetLayout,
  DEFAULT_HEIGHT,
  DEFAULT_WIDTH,
  HORIZONTAL_DESK_HEIGHT,
  HORIZONTAL_DESK_WIDTH,
  imageDimensions,
  getBackgroundFromBackend,
  imageUrl,
} from "../VisualFloorMap/floorLayout";
import {
  getAllDesksFromBackend
} from "../VisualFloorMap/adminFloorLayout";
import api from "../../plugins/axios.js";
import DeskEditModal from "./DeskEditModal.vue";
import { zones } from "./adminFloorLayout";
import { useToast } from "vue-toastification";

const isDraggingNew = ref(false);
const newDeskPreview = ref<{ x: number; y: number } | null>(null);

const showEditModal = ref(false);
const selectedDesk = ref<any>(null);

const isDraggingTemplate = ref(false);
const dragTemplateData = ref<{ w: number; h: number; isHorizontal: boolean } | null>(null);
const previewPosition = ref<{ x: number; y: number } | null>(null);
const isDraggingDesk = ref(false);
const toast = useToast();

async function createNewDesk(newDesk: any){
  try{
    console.log(newDesk.h);
    await api.post(
      "/admin/addDesk",
      {
        displayName: newDesk.deskName,
        currentX: newDesk.x,
        currentY: newDesk.y,
        baseX: newDesk.x,
        baseY: newDesk.y,
        height: newDesk.h,
        width: newDesk.w,
        zoneDto: {
          id: newDesk.zone.zoneId,
          zoneName: newDesk.zone.zoneName,
          zoneAbv: newDesk.zone.zoneAbv
          }
    });

    const item = layout.find(item => item.i === String(newDesk.i));
    console.log(item);
    if (item) {
      item.deskName = newDesk.deskName;
      item.w = newDesk.w;
      item.h = newDesk.h;
      item.newDesk = false;
    }
    console.log(`New desk ${newDesk.deskName} saved`);
    showEditModal.value = false;
  }catch(err){
    console.error("Failed to create new desk: ", err);
  }
}

function onDragStart(event: DragEvent, data: { w: number; h: number; isHorizontal: boolean }) {
  dragTemplateData.value = data;
  isDraggingTemplate.value = true;

  const ghost = document.createElement('div');
  ghost.style.width = `${data.w}px`;
  ghost.style.height = `${data.h * rowHeight}px`;
  ghost.style.background = 'rgba(59, 130, 246, 0.4)';
  ghost.style.border = '2px dashed #3b82f6';
  ghost.style.position = 'absolute';
  ghost.style.top = '-1000px';
  document.body.appendChild(ghost);
  event.dataTransfer?.setDragImage(ghost, 0, 0);
}

function handleFloorplanDragOver(event: DragEvent) {
  if (!isDraggingTemplate.value || !dragTemplateData.value) return;

  event.preventDefault();

  const target = event.currentTarget as HTMLElement;
  const rect = target.getBoundingClientRect();
  const x = event.clientX - rect.left;
  const y = event.clientY - rect.top;

  const gridX = Math.floor(x);
  const gridY = Math.floor(y / rowHeight);

  previewPosition.value = {
    x: gridX,
    y: gridY * rowHeight
  };
}

function handleFloorplanDrop(event: DragEvent) {
  if (!isDraggingTemplate.value || !dragTemplateData.value || !previewPosition.value) return;

  event.preventDefault();

  const { w, h, isHorizontal } = dragTemplateData.value;
  const gridX = Math.floor((event.clientX - (event.currentTarget as HTMLElement).getBoundingClientRect().left));
  const gridY = Math.floor((event.clientY - (event.currentTarget as HTMLElement).getBoundingClientRect().top) / rowHeight);

  const newId = layout.length === 0 
    ? 1 
    : Math.max(...layout.map(item => Number(item.i) || 0)) + 1;

  layout.push({
    x: gridX,
    y: gridY,
    w,
    h,
    i: String(newId),
    deskName: `Desk ${newId}`,
    newDesk: true,
    color: "",
    isNonInteractive: false,
    zone: zones.value[0]
  });

  isDraggingTemplate.value = false;
  dragTemplateData.value = null;
  previewPosition.value = null;
}

function handleFloorplanDragLeave() {
  if (isDraggingTemplate.value) {
    previewPosition.value = null;
  }
}

function openDeskEditor(item: any) {
  if (isDraggingDesk.value) {
    isDraggingDesk.value = false;
    return;
  }
  selectedDesk.value = { ...item }; 
  showEditModal.value = true;
}

function updateDesk(updated){
  const matchingZone = zones.value.find((z: any) => z.zoneId === updated.zone.zoneId);

  const item = layout.find(item => item.i === String(updated.i));
  if (item) {
    item.deskName = updated.deskName;
    item.w = updated.w;
    item.h = updated.h;
    item.zone = matchingZone;
    item.x = updated.x;
    item.y = updated.y;
  }
}

async function applyDeskChanges(updated: any) {
  if(checkLowerThanZeroOrMoreThan(updated.x, imageDimensions.value.width)){
    toast.error("{x} is invalid", {
      timeout: 2000
    });
    return;
  } else if (checkLowerThanZeroOrMoreThan(updated.y, imageDimensions.value.height)){
    toast.error("{y} is invalid", {
      timeout: 2000
    });
    return;
  }
  await api.patch(`/admin/edit/desk/${Number(updated.i)}`, {
    displayName: updated.deskName,
    currentX: updated.x,
    currentY: updated.y,
    height: updated.h,
    width: updated.w,
    zoneId: updated.zone.zoneId
  });

  toast.success("Success", {
    timeout: 2000
  });

  updateDesk(updated);

  showEditModal.value = false;
}

async function restoreDeskDefaults(deskId: number) {
  try {
    console.log(deskId);
    const response = await api.get(`/admin/desk/${deskId}`);
    console.log(response);

    const desk = response.data;

    await api.patch(`/admin/edit/desk/${desk.id}`, {
      currentX: desk.baseX,
      currentY: desk.baseY
    });

    const item = layout.find(item => item.i === String(deskId));
    if (item) {
      item.x = desk.baseX;
      item.y = desk.baseY;
    }

    showEditModal.value = false;
  } catch (err) {
    console.error("Failed to restore desk defaults: ", err);
  }
}

function deleteDeskFromLayout(deskId: string){
    const index = layout.findIndex(item => item.i === deskId);

    if(index !== -1 ){
      layout.splice(index, 1);
      showEditModal.value = false;
    }
}

function checkLowerThanZeroOrMoreThan(toCheck: number, max: number){
  return toCheck < 0 || toCheck > max;
}

function closeModal(updated: any){
  if(checkLowerThanZeroOrMoreThan(updated.x, imageDimensions.value.width)){
    showEditModal.value = false;
    toast.warn("{x} is invalid", {
      timeout: 2000
    });
    return;
  }
  if(checkLowerThanZeroOrMoreThan(updated.y, imageDimensions.value.height)){
    showEditModal.value = false;
    toast.warn("{y} is invalid", {
      timeout: 2000
    });
    return;
  }

  updateDesk(updated);
  showEditModal.value = false;
}

async function deleteDesk(deskId: number){
  try{
    const response = await api.delete(`/admin/delete/desk/${deskId}`);
    deleteDeskFromLayout(String(deskId));
  }catch(err){
    console.error("Failed to delete desk from backend: ", err);
    deleteDeskFromLayout(String(deskId));
  }
}

async function restoreAllDesks(){
  try{
    await api.patch("/admin/desks/restoreCoordinates");
    toast.success("Restored succesfuly", {
      timeout: 2000
    });
    resetLayout();
    await getAllDesksFromBackend();
  }catch(err){
    toast.error("Erorr in restoring", {
      timeout: 2000
    });
    console.error("Failed to restore all desks to default: ", err)
  }
}

async function saveAllChanges(){
  try{
    const allDesks = layout.map(item =>({
      id: Number(item.i),
      displayName: item.deskName,
      currentX: item.x,
      currentY: item.y,
      zoneDto:{
        id: item.zone.zoneId,
        zoneName: item.zone.zoneName,
        zoneAbv: item.zone.zoneAbv
      },
      height: item.h,
      width: item.w
    }));
  const response = await api.patch(
    "/admin/desks/saveAll",
    allDesks
  );
  toast.success("Saved!", {
    timeout: 2000
  });
    console.log(`Saved ${response.data} desks`);
  }catch(err){
    console.error("Failed to save all the desks: ", err)
  }
}

onMounted(async () => {
  resetLayout();
  getBackgroundFromBackend();
  await getAllDesksFromBackend();
});
</script>

<template>
  <div class="admin-container">
    <div class="toolbar">
      <div class="center-group">
        <span class="drag-hint"> 🛈 Drag and drop</span>

        <div class="desk-templates">
          <div
              class="desk-template vertical"
              draggable="true"
              @dragstart="onDragStart($event, { w: DEFAULT_WIDTH, h: DEFAULT_HEIGHT, isHorizontal: false })"
          >
            <span class="label">Vertical Desk</span>
          </div>

          <div
              class="desk-template horizontal"
              draggable="true"
              @dragstart="onDragStart($event, { w: HORIZONTAL_DESK_WIDTH, h: HORIZONTAL_DESK_HEIGHT, isHorizontal: true })"
          >
            <span class="label">Horizontal Desk</span>
          </div>
        </div>
      </div>
      <div class="toolbar-actions">
        <button @click="saveAllChanges" class="btn btn-success mdi mdi-content-save">
          <span>Save</span>
        </button>
        <button @click="restoreAllDesks" class="btn btn-default mdi mdi-backup-restore">
          <span>Restore</span>
        </button>
      </div>

    </div>


    <div class="floorplan-container"
    :style="{
      'max-height': imageDimensions.height + 'px'
    }">
      <div
        class="floorplan-inner"
        :style="{
        width: imageDimensions.width + 'px',
        height: imageDimensions.height + 'px',}"
        :class="{ 'adding-mode': isDraggingTemplate }"
        @dragover="handleFloorplanDragOver"
        @drop="handleFloorplanDrop"
        @dragleave="handleFloorplanDragLeave"
        @dragenter.prevent
      >
        <img
          :src="imageUrl"
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
            width: (imageDimensions.value.width) + 'px',
            height: rowHeight + 'px',
          }"
        ></div>

        <GridLayout
          v-model:layout="layout"
          :col-num="imageDimensions.width"
          :row-height="rowHeight"
          :width="imageDimensions.width"
          :max-rows="imageDimensions.height"
          :margin="[0, 0]"
          :responsive="false"
          :vertical-compact="false"
          prevent-collision
          :use-css-transforms="true"
          :is-draggable="true"
          :is-resizable="false"
          @layout-updated="isDraggingDesk = true"
        >
          <template #item="{ item }">
            <div
              class="desk-admin"
              @click.stop="openDeskEditor(item)"
              :class="{ vertical: !(item.w >= item.h)}"
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
    @create="createNewDesk"
    @cancel="showEditModal = false"
    @delete="deleteDesk"
    @close="closeModal"
  />
</template>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  justify-content: center;
  padding-left: 24px;
  padding-right: 24px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.3);
  position: relative;
}
.center-group {
  display: flex;
  align-items: center;
  gap: 20px;
}
.desk-templates {
  display: flex;
  gap: 12px;
}
.toolbar-actions {
  position: absolute;
  right: 24px;
  display: flex;
  gap: 8px;
}
.drag-hint {
  font-size: 16px;
  color: #555;
  white-space: nowrap;
}



.btn {
  padding: 8px 16px;
  margin: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  border: none;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}

.btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 3px 8px rgba(0, 0, 0, 0.12);
}

.btn:active {
  transform: translateY(0);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.btn-success {
  background: linear-gradient(135deg, #10b981, #059669);
  color: white;
}

.btn-success:hover {
  background: linear-gradient(135deg, #059669, #047857);
}

.btn-default {
  background: white;
  color: #6b7280;
  border: 1px solid #e5e7eb;
}

.btn-default:hover {
  background: #f9fafb;
  border-color: #d1d5db;
  color: #4b5563;
}

.desk-templates {
  display: flex;
  gap: 20px;
  align-items: center;
  justify-content: center;
}

.desk-template {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 14px 24px;
  background: rgba(255, 255, 255, 0.9);
  border: 2px dashed rgba(102, 126, 234, 0.3);
  border-radius: 10px;
  cursor: grab;
  transition: all 0.25s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.desk-template:hover {
  border-color: #667eea;
  background: rgba(255, 255, 255, 1);
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.2);
}

.desk-template:active {
  cursor: grabbing;
  transform: translateY(-1px) scale(0.98);
}

.desk-template .label {
  font-size: 13px;
  font-weight: 700;
  color: #4b5563;
  white-space: nowrap;
  letter-spacing: 0.3px;
}

.desk-template.vertical::before {
  content: '';
  width: 28px;
  height: 42px;
  background:#27313b;
  border-radius: 6px;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.desk-template.horizontal::before {
  content: '';
  width: 42px;
  height: 28px;
  background:#27313b;
  border-radius: 6px;
  box-shadow: 0 4px 12px rgba(245, 87, 108, 0.3);
}
.floorplan-container {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
}

.floorplan-inner {
  position: relative;
  background: #f4f6fb;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  border: 2px solid #e5e7eb;
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