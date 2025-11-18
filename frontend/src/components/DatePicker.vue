<template>
  <VRow justify="center" class="date-picker-row">
    <VCol cols="12" class="d-flex justify-center">
      <VMenu
        v-model="menu"
        :close-on-content-click="false"
        transition="scale-transition"
        offset="10"
        max-width="360"
        location="bottom"
      >
        <template #activator="{ props }">
          <div class="date-selector" v-bind="props">
            <v-icon size="20" class="calendar-icon"
              >mdi-calendar-outline</v-icon
            >
            <span class="date-value">{{ displayDate }}</span>
            <v-icon size="18" class="chevron-icon">mdi-chevron-down</v-icon>
          </div>
        </template>
        <div class="calendar-card">
          <VDatePicker
            v-model.date="selectedDate"
            :min="todayISO"
            :max="maxDateISO"
            show-adjacent-months
            @update:model-value="onDateSelected"
            class="elegant-picker"
            color="black"
          />
        </div>
      </VMenu>
    </VCol>
  </VRow>
</template>

<script setup>
import { ref, computed, onActivated } from "vue";

const emit = defineEmits(["update:date"]);

const menu = ref(false);
const selectedDate = ref(null);

const getTodayDate = () => {
  const d = new Date();
  d.setHours(0, 0, 0, 0);
  return d;
};

const toISO = (date) => {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
};

const todayISO = computed(() => toISO(getTodayDate()));

const maxDateISO = computed(() => {
  const d = new Date();
  d.setDate(d.getDate() + 30);
  d.setHours(0, 0, 0, 0);
  return toISO(d);
});

const displayDate = computed(() => {
  if (!selectedDate.value) return "Select a date";
  const d = selectedDate.value;
  return d.toLocaleDateString("en-GB", {
    weekday: "short",
    day: "numeric",
    month: "short",
    year: "numeric",
  });
});

const resetToToday = () => {
  selectedDate.value = getTodayDate();
};

onActivated(() => {
  resetToToday();
  emitISO();
});

const onDateSelected = () => {
  menu.value = false;
  emitISO();
};

const emitISO = () => {
  if (selectedDate.value) {
    emit("update:date", toISO(selectedDate.value));
  }
};

resetToToday();
emitISO();
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap");

* {
  font-family: "Inter", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto,
    sans-serif;
}

:deep(.v-col) {
  padding: 0;
}

.date-picker-row {
  margin-top: 8px;
  margin-bottom: 12px;
}

/* Адаптив для ноутбуков */
@media (max-width: 1440px) {
  .date-picker-row {
    margin-top: 6px;
    margin-bottom: 10px;
  }
}

@media (max-width: 1280px) {
  .date-picker-row {
    margin-top: 5px;
    margin-bottom: 8px;
  }
}

@media (max-width: 1024px) {
  .date-picker-row {
    margin-top: 4px;
    margin-bottom: 6px;
  }
}

.date-selector {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  min-width: 0;
  width: min(100%, 320px);
}

/* Адаптив селектора для ноутбуков */
@media (max-width: 1440px) {
  .date-selector {
    padding: 9px 14px;
    gap: 9px;
    width: min(100%, 300px);
  }
}

@media (max-width: 1280px) {
  .date-selector {
    padding: 8px 12px;
    gap: 8px;
    width: min(100%, 280px);
  }
}

@media (max-width: 1024px) {
  .date-selector {
    padding: 7px 10px;
    gap: 7px;
    width: min(100%, 260px);
  }
}

.date-selector:hover {
  border-color: #d1d5db;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.date-icon-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, #171717 0%, #404040 100%);
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  flex-shrink: 0;
}

.date-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.date-label {
  font-size: 11px;
  font-weight: 700;
  color: #6b7280;
  letter-spacing: 0.8px;
  text-transform: uppercase;
}

.date-value {
  font-size: 16px;
  font-weight: 600;
  color: #171717;
  letter-spacing: -0.2px;
}

/* Адаптив текста для ноутбуков */
@media (max-width: 1440px) {
  .date-value {
    font-size: 15px;
  }
}

@media (max-width: 1280px) {
  .date-value {
    font-size: 14px;
  }
}

@media (max-width: 1024px) {
  .date-value {
    font-size: 13px;
  }
}

.calendar-icon {
  color: #171717;
}

.chevron-icon {
  color: #171717;
  transition: transform 0.3s ease;
  flex-shrink: 0;
}

.date-selector:hover .chevron-icon {
  transform: translateY(2px);
}

.calendar-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
  overflow: hidden;
  border: 1px solid #e5e7eb;
  max-height: 85vh;
  overflow-y: auto;
}

/* Адаптив календаря для ноутбуков */
@media (max-width: 1440px) {
  .calendar-card {
    max-height: 80vh;
    transform: scale(0.92);
    transform-origin: top center;
  }
}

@media (max-width: 1280px) {
  .calendar-card {
    max-height: 75vh;
    transform: scale(0.85);
    transform-origin: top center;
  }
}

@media (max-width: 1024px) {
  .calendar-card {
    max-height: 70vh;
    transform: scale(0.78);
    transform-origin: top center;
  }
}

/* Стилизация календаря */
.elegant-picker {
  background: white !important;
}

.elegant-picker :deep(.v-picker-title) {
  display: none !important;
}

.elegant-picker :deep(.v-date-picker-header) {
  padding: 12px 16px 10px !important;
  background: #171717;
  border-bottom: 1px solid #262626;
}

@media (max-width: 1440px) {
  .elegant-picker :deep(.v-date-picker-header) {
    padding: 10px 14px 8px !important;
  }
}

@media (max-width: 1280px) {
  .elegant-picker :deep(.v-date-picker-header) {
    padding: 9px 12px 7px !important;
  }
}

@media (max-width: 1024px) {
  .elegant-picker :deep(.v-date-picker-header) {
    padding: 8px 10px 6px !important;
  }
}

.elegant-picker :deep(.v-date-picker-header__content) {
  font-weight: 800 !important;
  font-size: 18px !important;
  letter-spacing: -0.3px !important;
  color: #ffffff !important;
}

@media (max-width: 1440px) {
  .elegant-picker :deep(.v-date-picker-header__content) {
    font-size: 16px !important;
  }
}

@media (max-width: 1280px) {
  .elegant-picker :deep(.v-date-picker-header__content) {
    font-size: 15px !important;
  }
}

@media (max-width: 1024px) {
  .elegant-picker :deep(.v-date-picker-header__content) {
    font-size: 14px !important;
  }
}

.elegant-picker :deep(.v-date-picker-controls) {
  padding: 10px 16px !important;
}

.elegant-picker :deep(.v-btn--icon) {
  width: 36px !important;
  height: 36px !important;
  border-radius: 10px !important;
  transition: all 0.25s ease !important;
}

@media (max-width: 1440px) {
  .elegant-picker :deep(.v-btn--icon) {
    width: 32px !important;
    height: 32px !important;
  }
}

@media (max-width: 1280px) {
  .elegant-picker :deep(.v-btn--icon) {
    width: 28px !important;
    height: 28px !important;
  }
}

@media (max-width: 1024px) {
  .elegant-picker :deep(.v-btn--icon) {
    width: 26px !important;
    height: 26px !important;
  }
}

.elegant-picker :deep(.v-btn--icon:hover) {
  background: #f3f4f6 !important;
  transform: scale(1.05);
}

.elegant-picker :deep(.v-date-picker-month) {
  padding: 8px 16px 16px !important;
}

@media (max-width: 1440px) {
  .elegant-picker :deep(.v-date-picker-month) {
    padding: 7px 14px 14px !important;
  }
}

@media (max-width: 1280px) {
  .elegant-picker :deep(.v-date-picker-month) {
    padding: 6px 12px 12px !important;
  }
}

@media (max-width: 1024px) {
  .elegant-picker :deep(.v-date-picker-month) {
    padding: 5px 10px 10px !important;
  }
}

.elegant-picker :deep(.v-date-picker-month__weekday) {
  font-weight: 700 !important;
  font-size: 12px !important;
  color: #6b7280 !important;
  padding: 8px 0 !important;
}

@media (max-width: 1440px) {
  .elegant-picker :deep(.v-date-picker-month__weekday) {
    font-size: 11px !important;
    padding: 6px 0 !important;
  }
}

@media (max-width: 1280px) {
  .elegant-picker :deep(.v-date-picker-month__weekday) {
    font-size: 10px !important;
    padding: 5px 0 !important;
  }
}

@media (max-width: 1024px) {
  .elegant-picker :deep(.v-date-picker-month__weekday) {
    font-size: 9px !important;
    padding: 4px 0 !important;
  }
}

.elegant-picker :deep(.v-date-picker-month__day) {
  margin: 2px !important;
  border-radius: 10px !important;
  transition: all 0.2s ease !important;
}

.elegant-picker :deep(.v-date-picker-month__day .v-btn) {
  font-weight: 600 !important;
  font-size: 14px !important;
  border-radius: 10px !important;
  transition: all 0.2s ease !important;
}

@media (max-width: 1440px) {
  .elegant-picker :deep(.v-date-picker-month__day .v-btn) {
    font-size: 13px !important;
  }
}

@media (max-width: 1280px) {
  .elegant-picker :deep(.v-date-picker-month__day .v-btn) {
    font-size: 12px !important;
  }
}

@media (max-width: 1024px) {
  .elegant-picker :deep(.v-date-picker-month__day .v-btn) {
    font-size: 11px !important;
  }
}

.elegant-picker :deep(.v-date-picker-month__day:hover .v-btn) {
  background: #f3f4f6 !important;
  transform: scale(1.08);
}

.elegant-picker :deep(.v-date-picker-month__day--selected .v-btn) {
  background: #171717 !important;
  color: white !important;
  font-weight: 700 !important;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2) !important;
}

.elegant-picker :deep(.v-date-picker-month__day--adjacent) {
  opacity: 0.3 !important;
}

.elegant-picker :deep(.v-date-picker-month__day--disabled) {
  opacity: 0.2 !important;
}

.elegant-picker :deep(.v-btn__content) {
  font-size: 16px !important;
  font-weight: 700 !important;
}

.elegant-picker :deep(*) {
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}
</style>
