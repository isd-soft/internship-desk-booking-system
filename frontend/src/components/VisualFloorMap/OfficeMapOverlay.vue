<script setup lang="ts">
import { reactive, onMounted } from "vue";
import {
  layout,
  colNum,
  rowHeight,
  totalRows,
  IMAGE_WIDTH_PX,
  makeBottomClusters,
  makeTopClusters,
  makeLeftClusters,
} from "../VisualFloorMap/floorLayout";

onMounted(() => {
  makeBottomClusters(888, 555, 4);
  makeBottomClusters(400, 555, 2);

  makeTopClusters(888, 30, 4);
  makeTopClusters(400, 30, 1);

  makeLeftClusters(178, 555, 2);

  makeLeftClusters(271, 173, 2);
});

function handleDeskClick(item: any) {
  console.log("Clicked desk:", item.i);
}
</script>

<template>
  <div class="floorplan-container">
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
      :is-draggable="false"
      :is-resizable="false"
      style="position: relative"
    >
      <template #item="{ item }">
        <div
          class="desk"
          :class="{ static: item.static }"
          @click="handleDeskClick(item)"
        >
          <span class="text">{{ item.i }}</span>
        </div>
      </template>
    </GridLayout>
  </div>
</template>

<style scoped>
.floorplan-container {
  width: 987px;
  height: 643px;
  margin: 20px auto;
  border: 5px solid #000;
  position: relative;
  overflow: hidden;
  background: url("/floorplan/Floor.png") center/contain no-repeat;
  background-color: #f0f0f0;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
}

:deep(.vgl-item--static) {
  border: none !important;
  background-color: #333 !important;
}

:deep(.vgl-item:not(.vgl-item--static)) {
  border: 2px solid #2c3e50;
  background: linear-gradient(145deg, #e6e6e6, #ffffff);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1), 0 1px 2px rgba(0, 0, 0, 0.06);
  border-radius: 4px;
  position: relative;
  overflow: hidden;
}

:deep(.vgl-item:not(.vgl-item--static)::before) {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    145deg,
    rgba(76, 175, 80, 0.1),
    rgba(76, 175, 80, 0.05)
  );
  opacity: 0;
  transition: opacity 0.3s ease;
}

:deep(.vgl-item:not(.vgl-item--static):hover) {
  background: linear-gradient(145deg, #4caf50, #45a049);
  border-color: #388e3c;
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(76, 175, 80, 0.3), 0 4px 8px rgba(0, 0, 0, 0.15);
}

:deep(.vgl-item:not(.vgl-item--static):hover::before) {
  opacity: 1;
}

:deep(.vgl-item:not(.vgl-item--static):hover .text) {
  color: white;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

:deep(.vgl-item:not(.vgl-item--static):active) {
  transform: translateY(0) scale(0.98);
  box-shadow: 0 2px 4px rgba(76, 175, 80, 0.2), 0 1px 2px rgba(0, 0, 0, 0.1);
  background: linear-gradient(145deg, #45a049, #3d8b40);
  transition: all 0.1s ease;
}

:deep(.vgl-item:not(.vgl-item--static):focus-visible) {
  outline: 3px solid #4caf50;
  outline-offset: 2px;
}

.desk {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.text {
  font-size: 18px;
  text-align: center;
  pointer-events: none;
  color: #2c3e50;
  font-weight: 700;
  user-select: none;
  transition: all 0.3s ease;
  text-shadow: 0 1px 2px rgba(255, 255, 255, 0.8);
  letter-spacing: 0.5px;
}
</style>
