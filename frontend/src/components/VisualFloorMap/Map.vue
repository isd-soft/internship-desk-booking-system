<script setup lang="ts">
import { reactive, onMounted } from 'vue';
import{
  layout,
  colNum,
  rowHeight,
  totalRows,
  IMAGE_WIDTH_PX,
  makeBottomClusters,
  makeTopClusters,
  makeLeftClusters,
} from './floorLayout.ts';

onMounted(() => {
  makeBottomClusters(888, 555, 4);
  makeBottomClusters(400, 555, 2);

  makeTopClusters(888, 30, 4);
  makeTopClusters(400, 30, 1);

  makeLeftClusters(178, 555, 2)

  makeLeftClusters(271, 173, 2)
});
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
      style="position: relative;"
    >
      <template #item="{ item }">
        <div class="desk" :class="{ static: item.static }">
          <span v-if="!item.static" class="text">{{ item.i }}</span>
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
  background: url('/floorplan/Floor.png') center/contain no-repeat;
  background-color: #f0f0f0;
  box-shadow: 0 10px 30px rgba(0,0,0,0.3);
}

:deep(.vgl-item--static) {
  border: none !important;
  background-color: #333 !important;
}

:deep(.vgl-item:not(.vgl-item--static)) {
  border: 1px solid black;
  background-color: #ccc;
}

.text {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  margin: auto;
  font-size: 18px;
  text-align: center;
  pointer-events: none;
  color: black;
  font-weight: bold;
}
</style>