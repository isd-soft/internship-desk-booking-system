<template>
  <v-dialog v-model="model" max-width="560" scrollable>
    <v-card class="details-card">
      <v-card-title class="d-flex align-center justify-space-between">
        <div class="d-flex align-center gap-2">
          <v-icon class="mr-2">mdi-desk</v-icon>
          <strong>{{ item?.desk }}</strong>
        </div>
        <v-chip size="x-small" :color="item?.statusColor" variant="flat">{{
          item?.status
        }}</v-chip>
      </v-card-title>

      <v-card-text>
        <div class="details-grid">
          <div class="d-row">
            <span class="d-label">Zone</span
            ><span class="d-val">{{ item?.zone }}</span>
          </div>
          <div class="d-row">
            <span class="d-label">Type</span
            ><span class="d-val">{{ item?.type }}</span>
          </div>
          <div class="d-row">
            <span class="d-label">Date</span
            ><span class="d-val">{{ item?.date }}</span>
          </div>
          <div class="d-row">
            <span class="d-label">Time</span
            ><span class="d-val">{{ item?.time }}</span>
          </div>
          <div class="d-row">
            <span class="d-label">Duration</span
            ><span class="d-val">{{ item?.duration }}</span>
          </div>
        </div>
      </v-card-text>

      <v-card-actions class="px-4 pb-3">
        <v-spacer></v-spacer>
        <v-btn variant="text" @click="model = false">Close</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { computed } from "vue";

const props = defineProps<{ modelValue: boolean; item: any | null }>();
const emit = defineEmits<{ (e: "update:modelValue", v: boolean): void }>();
const model = computed({
  get: () => props.modelValue,
  set: (v) => emit("update:modelValue", v),
});
const item = computed(() => props.item ?? null);
</script>

<style scoped>
.details-card {
  border-radius: 14px;
}
.details-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 10px;
}
.d-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px dashed #e5e7eb;
  padding: 8px 0;
}
.d-label {
  font-weight: 800;
  color: #4b5563;
}
.d-val {
  font-weight: 800;
  color: #111827;
}
</style>
