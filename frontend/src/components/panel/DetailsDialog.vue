<template>
  <v-dialog
    v-model="model"
    max-width="520"
    transition="dialog-bottom-transition"
    persistent
  >
    <v-card class="details-card" elevation="0">
      <v-card-title class="card-header">
        <div class="header-content">
          <div class="header-info">
            <div class="workspace-label">DESK DETAILS</div>
            <div class="desk-title">{{ item?.desk }}</div>
          </div>
          <v-btn
            icon
            variant="text"
            size="small"
            @click="model = false"
            class="close-button"
          >
            <v-icon size="20">mdi-close</v-icon>
          </v-btn>
        </div>
      </v-card-title>

      <v-card-text class="card-body">
        <v-alert
          v-if="item?.status"
          class="status-alert"
          variant="tonal"
          density="compact"
        >
          <div class="alert-content">
            <v-icon size="18">mdi-information</v-icon>
            <span>{{ item.status }}</span>
          </div>
        </v-alert>

        <div class="details-section">
          <div class="detail-row">
            <div class="detail-label">
              <v-icon size="18" class="detail-icon">mdi-map-marker</v-icon>
              Zone
            </div>
            <div class="detail-value">{{ item?.zone }}</div>
          </div>

          <div class="detail-row">
            <div class="detail-label">
              <v-icon size="18" class="detail-icon">mdi-shape</v-icon>
              Type
            </div>
            <div class="detail-value">{{ item?.type }}</div>
          </div>

          <div class="detail-row">
            <div class="detail-label">
              <v-icon size="18" class="detail-icon">mdi-calendar</v-icon>
              Date
            </div>
            <div class="detail-value">{{ item?.date }}</div>
          </div>

          <div class="detail-row">
            <div class="detail-label">
              <v-icon size="18" class="detail-icon">mdi-clock-outline</v-icon>
              Time
            </div>
            <div class="detail-value">{{ item?.time }}</div>
          </div>

          <div class="detail-row highlight">
            <div class="detail-label">
              <v-icon size="18" class="detail-icon">mdi-timer-outline</v-icon>
              Duration
            </div>
            <div class="detail-value">{{ item?.duration }}</div>
          </div>
        </div>
      </v-card-text>

      <v-card-actions class="card-actions">
        <v-btn
          class="close-action-button"
          size="x-large"
          @click="model = false"
        >
          Close
        </v-btn>
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
@import url("https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap");

* {
  font-family: "Inter", -apple-system, BlinkMacSystemFont, "Segoe UI",
    sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

.details-card {
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

.status-alert {
  background: #fafafa !important;
  border: 1px solid #e5e5e5 !important;
  border-radius: 12px !important;
  margin-bottom: 24px;
  padding: 12px 16px !important;
}

.alert-content {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
  font-weight: 600;
  color: #525252;
}

.details-section {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 18px;
  background: #fafafa;
  border-radius: 12px;
  transition: all 0.2s ease;
}

.detail-row:hover {
  background: #f5f5f5;
  transform: translateX(2px);
}

.detail-row.highlight {
  background: #171717;
  margin-top: 8px;
}

.detail-row.highlight .detail-label,
.detail-row.highlight .detail-value {
  color: #ffffff;
}

.detail-row.highlight .detail-icon {
  color: rgba(255, 255, 255, 0.8) !important;
}

.detail-label {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
  font-weight: 700;
  color: #525252;
  letter-spacing: 0.3px;
}

.detail-icon {
  color: #737373 !important;
  opacity: 0.8;
}

.detail-value {
  font-size: 15px;
  font-weight: 800;
  color: #171717;
  letter-spacing: -0.2px;
}

.card-actions {
  padding: 20px 28px 28px 28px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-action-button {
  background: #f5f5f5 !important;
  color: #171717 !important;
  font-weight: 700 !important;
  text-transform: none !important;
  letter-spacing: 0.3px;
  width: 100%;
  height: 52px !important;
  border-radius: 12px !important;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.close-action-button:hover {
  background: #e5e5e5 !important;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
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
