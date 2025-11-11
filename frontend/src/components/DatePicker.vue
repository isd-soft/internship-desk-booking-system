<template>
  <VRow justify="center">
    <VCol cols="12" class="d-flex justify-center">
      <VMenu
        v-model="menu"
        :close-on-content-click="false"
        transition="scale-transition"
        offset="10"
        max-width="340"
      >
        <template #activator="{ props }">
          <VBtn
            v-bind="props"
            color="primary"
            variant="outlined"
            class="text-none date-btn"
            prepend-icon="mdi-calendar"
          >
            <span class="date-text">
              {{ displayDate || date }}
            </span>
          </VBtn>
        </template>

        <VDatePicker
          v-model.date="date"
          :min="todayISO"
          :max="maxDateISO"
          show-adjacent-months
          @update:model-value="menu = false"
          class="clean-picker"
        />
      </VMenu>
    </VCol>
  </VRow>
</template>

<script setup>
import { ref, computed } from 'vue'

const today = new Date()
today.setHours(0, 0, 0, 0)
const todayISO = today.toISOString().split('T')[0]

const maxDateObj = new Date(today)
maxDateObj.setDate(today.getDate() + 30)
const maxDateISO = maxDateObj.toISOString().split('T')[0]

const menu = ref(false)
const date = ref(today)

const displayDate = computed(() => {
  if (!date.value) return ''
  const d = date.value
  const day = String(d.getDate()).padStart(2, '0')
  const month = String(d.getMonth() + 1).padStart(2, '0')
  return `${day}.${month}.${d.getFullYear()}`
})
</script>

<style scoped>
.date-btn {
  min-width: 180px;          
  max-width: 260px;        
  width: fit-content;        
  padding: 0 16px;           
  white-space: nowrap;        
  flex-shrink: 0;          
  height: 48px;
  font-size: 16px;
}

.date-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 200px;         
  display: inline-block;
}

:deep(.v-col) {
  padding: 0;
}

.clean-picker :deep(.v-date-picker-controls__month-btn),
.clean-picker :deep(.v-date-picker-controls__mode-btn) {
  display: none !important;
}

.clean-picker :deep(.v-date-picker-controls__month) {
  display: flex !important;
  justify-content: space-between;
  flex: 1;
}

.clean-picker :deep(.v-picker-title) {
  display: block !important;
  visibility: hidden !important;
  height: 0 !important;
  min-height: 0 !important;
  margin: 0 !important;
  padding: 0 !important;
  overflow: hidden !important;
  line-height: 0 !important;
  font-size: 0 !important;
}

.clean-picker :deep(.v-date-picker-header__content){
  font-weight: 800;
  font-size: 20px;
  letter-spacing: 1px;
}

.clean-picker :deep(.v-date-picker-month__day),
.clean-picker :deep(.v-date-picker-month__weekday) {
  font-weight: 800;
  font-size: 15px;
}

.clean-picker :deep(.v-btn__content){
  font-size: 18px;
}

.clean-picker :deep(.v-date-picker-controls) {
  display: flex;
  justify-content: space-between;
  padding: 8px 12px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.12);
}

* {
  font-family: "Inter", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
}
</style>