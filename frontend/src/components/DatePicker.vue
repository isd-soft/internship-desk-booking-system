<template>
  <VRow justify="center" class="date-picker-row">
    <VCol cols="12" class="d-flex flex-column align-center">
      <!-- Week Navigation -->
      <div class="week-navigation">
        <v-btn
          icon
          variant="text"
          size="small"
          @click="previousWeek"
          class="week-nav-btn"
        >
          <v-icon>mdi-chevron-left</v-icon>
        </v-btn>

        <div class="week-range">
          {{ weekRangeDisplay }}
        </div>

        <v-btn
          icon
          variant="text"
          size="small"
          @click="nextWeek"
          :disabled="!canGoNextWeek"
          class="week-nav-btn"
        >
          <v-icon>mdi-chevron-right</v-icon>
        </v-btn>
      </div>

      <!-- Weekday Buttons -->
      <div class="weekday-buttons">
        <button
          v-for="day in weekDays"
          :key="day.iso"
          @click="selectDay(day)"
          :class="['weekday-btn', { active: isSelectedDay(day), today: isToday(day), disabled: isPastDay(day) }]"
          :disabled="isPastDay(day)"
        >
          <span class="day-name">{{ day.name }}</span>
          <span class="day-date">{{ day.dateNum }}</span>
        </button>
      </div>
    </VCol>
  </VRow>
</template>

<script setup>
import { ref, computed, onActivated, watch } from "vue";

const emit = defineEmits(["update:date"]);

const selectedDate = ref(null);
const currentWeekStart = ref(null);

const getTodayDate = () => {
  const d = new Date();
  d.setHours(0, 0, 0, 0);
  return d;
};

const getMonday = (date) => {
  const d = new Date(date);
  const day = d.getDay();
  const diff = d.getDate() - day + (day === 0 ? -6 : 1);
  d.setDate(diff);
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

const weekDays = computed(() => {
  const days = [];
  const dayNames = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri'];
  
  for (let i = 0; i < 5; i++) {
    const date = new Date(currentWeekStart.value);
    date.setDate(date.getDate() + i);
    days.push({
      name: dayNames[i],
      dateNum: date.getDate(),
      iso: toISO(date),
      date: date
    });
  }
  
  return days;
});

const weekRangeDisplay = computed(() => {
  if (!currentWeekStart.value) return '';
  
  const start = new Date(currentWeekStart.value);
  const end = new Date(currentWeekStart.value);
  end.setDate(end.getDate() + 4);
  
  const formatDate = (d) => {
    const day = String(d.getDate()).padStart(2, '0');
    const month = String(d.getMonth() + 1).padStart(2, '0');
    return `${day}.${month}`;
  };
  
  return `${formatDate(start)} - ${formatDate(end)}`;
});

const canGoNextWeek = computed(() => {
  const nextMonday = new Date(currentWeekStart.value);
  nextMonday.setDate(nextMonday.getDate() + 7);
  const maxDate = new Date(maxDateISO.value);
  return nextMonday <= maxDate;
});

const isSelectedDay = (day) => {
  if (!selectedDate.value) return false;
  return toISO(selectedDate.value) === day.iso;
};

const isToday = (day) => {
  return toISO(getTodayDate()) === day.iso;
};

const isPastDay = (day) => {
  const today = getTodayDate();
  return day.date < today;
};

const selectDay = (day) => {
  if (isPastDay(day)) return;
  selectedDate.value = new Date(day.date);
  emitISO();
};

const previousWeek = () => {
  const newMonday = new Date(currentWeekStart.value);
  newMonday.setDate(newMonday.getDate() - 7);
  
  const today = getTodayDate();
  const mondayOfToday = getMonday(today);
  
  if (newMonday >= mondayOfToday) {
    currentWeekStart.value = newMonday;
  }
};

const nextWeek = () => {
  if (!canGoNextWeek.value) return;
  
  const newMonday = new Date(currentWeekStart.value);
  newMonday.setDate(newMonday.getDate() + 7);
  currentWeekStart.value = newMonday;
};

const resetToToday = () => {
  const today = getTodayDate();
  selectedDate.value = today;
  currentWeekStart.value = getMonday(today);
};

// Watch for calendar date changes to update week
watch(selectedDate, (newDate) => {
  if (newDate) {
    const monday = getMonday(newDate);
    currentWeekStart.value = monday;
  }
});

onActivated(() => {
  resetToToday();
  emitISO();
});

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
  margin-top: 0;
  margin-bottom: 6px;
}

@media (max-width: 1440px) {
  .date-picker-row {
    margin-top: 6px;
    margin-bottom: 8px;
  }
}

@media (max-width: 1366px) {
  .date-picker-row {
    margin-top: 4px;
    margin-bottom: 6px;
  }
}

@media (max-width: 1280px) {
  .date-picker-row {
    margin-top: 3px;
    margin-bottom: 5px;
  }
}

@media (max-width: 1024px) {
  .date-picker-row {
    margin-top: 2px;
    margin-bottom: 4px;
  }
}

/* Week Navigation */
.week-navigation {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
  padding: 6px 12px;
  background: linear-gradient(135deg, #f9fafb 0%, #ffffff 100%);
  border-radius: 10px;
  border: 1.5px solid #e5e7eb;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.06);
}

@media (min-width: 1441px) {
  .week-navigation {
    padding: 6px 12px;
    gap: 10px;
    margin-bottom: 8px;
  }
  
  .weekday-buttons {
    gap: 5px;
  }
  
  .weekday-btn {
    padding: 6px 10px;
    min-width: 50px;
    gap: 2px;
  }
  
  .day-name {
    font-size: 8.5px;
  }
  
  .day-date {
    font-size: 14px;
  }
}

@media (max-width: 1440px) and (min-width: 1190px) {
  .week-navigation {
    padding: 6px 12px;
    gap: 8px;
    margin-bottom: 8px;
    border-radius: 10px;
  }
}

@media (max-width: 1366px) and (min-width: 1190px) {
  .week-navigation {
    padding: 5px 10px;
    gap: 7px;
    margin-bottom: 7px;
    border-radius: 9px;
  }
}

@media (max-width: 1280px) and (min-width: 1190px) {
  .week-navigation {
    padding: 4px 8px;
    gap: 6px;
    margin-bottom: 6px;
    border-radius: 8px;
  }
}

@media (max-width: 1024px) and (min-width: 1190px) {
  .week-navigation {
    padding: 4px 7px;
    gap: 5px;
    margin-bottom: 5px;
    border-radius: 7px;
  }
}.week-nav-btn {
  background: #ffffff !important;
  border: 1px solid #e5e7eb !important;
  border-radius: 8px !important;
  transition: all 0.2s ease !important;
  width: 28px !important;
  height: 28px !important;
}

.week-nav-btn :deep(.v-icon) {
  font-size: 16px !important;
}

@media (max-width: 1440px) and (min-width: 1190px) {
  .week-nav-btn {
    width: 28px !important;
    height: 28px !important;
  }
  .week-nav-btn :deep(.v-icon) {
    font-size: 16px !important;
  }
}

@media (max-width: 1366px) and (min-width: 1190px) {
  .week-nav-btn {
    width: 26px !important;
    height: 26px !important;
  }
  .week-nav-btn :deep(.v-icon) {
    font-size: 15px !important;
  }
}

@media (max-width: 1280px) and (min-width: 1190px) {
  .week-nav-btn {
    width: 24px !important;
    height: 24px !important;
  }
  .week-nav-btn :deep(.v-icon) {
    font-size: 14px !important;
  }
}

@media (max-width: 1024px) and (min-width: 1190px) {
  .week-nav-btn {
    width: 22px !important;
    height: 22px !important;
  }
  .week-nav-btn :deep(.v-icon) {
    font-size: 13px !important;
  }
}

.week-nav-btn:hover:not(:disabled) {
  background: #f3f4f6 !important;
  border-color: #d1d5db !important;
  transform: scale(1.05);
}

.week-nav-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.week-range {
  font-size: 13px;
  font-weight: 700;
  color: #171717;
  letter-spacing: 0.2px;
  min-width: 100px;
  text-align: center;
  font-variant-numeric: tabular-nums;
}

@media (max-width: 1440px) and (min-width: 1190px) {
  .week-range {
    font-size: 13px;
    min-width: 100px;
    letter-spacing: 0.2px;
  }
}

@media (max-width: 1366px) and (min-width: 1190px) {
  .week-range {
    font-size: 12px;
    min-width: 95px;
    letter-spacing: 0.15px;
  }
}

@media (max-width: 1280px) and (min-width: 1190px) {
  .week-range {
    font-size: 11px;
    min-width: 90px;
  }
}

@media (max-width: 1024px) and (min-width: 1190px) {
  .week-range {
    font-size: 10px;
    min-width: 85px;
  }
}

/* Weekday Buttons */
.weekday-buttons {
  display: flex;
  gap: 4px;
  margin-bottom: 0;
  width: 100%;
  justify-content: center;
  flex-wrap: wrap;
}

@media (max-width: 1440px) and (min-width: 1190px) {
  .weekday-buttons {
    gap: 4px;
  }
}

@media (max-width: 1366px) and (min-width: 1190px) {
  .weekday-buttons {
    gap: 4px;
  }
}

@media (max-width: 1280px) and (min-width: 1190px) {
  .weekday-buttons {
    gap: 3px;
  }
}

@media (max-width: 1024px) and (min-width: 1190px) {
  .weekday-buttons {
    gap: 2px;
  }
}

.weekday-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  padding: 5px 8px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  min-width: 46px;
  position: relative;
  overflow: hidden;
}

@media (max-width: 1440px) and (min-width: 1190px) {
  .weekday-btn {
    padding: 5px 9px;
    min-width: 48px;
    gap: 2px;
    border-radius: 8px;
  }
}

@media (max-width: 1366px) and (min-width: 1190px) {
  .weekday-btn {
    padding: 5px 8px;
    min-width: 46px;
    gap: 2px;
    border-radius: 8px;
  }
}

@media (max-width: 1280px) and (min-width: 1190px) {
  .weekday-btn {
    padding: 4px 7px;
    min-width: 44px;
    gap: 2px;
    border-radius: 7px;
  }
}

@media (max-width: 1024px) and (min-width: 1190px) {
  .weekday-btn {
    padding: 4px 6px;
    min-width: 42px;
    gap: 1px;
    border-radius: 7px;
  }
}

.weekday-btn::before {
  content: "";
  position: absolute;
  inset: 0;
  background: radial-gradient(
    circle at center,
    rgba(0, 0, 0, 0.02) 0%,
    transparent 70%
  );
  opacity: 0;
  transition: opacity 0.3s ease;
}

.weekday-btn:hover:not(.disabled)::before {
  opacity: 1;
}

.weekday-btn:hover:not(.disabled) {
  border-color: #3b82f6;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.15);
}

@media (max-width: 1280px) and (min-width: 1190px) {
  .weekday-btn:hover:not(.disabled) {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(59, 130, 246, 0.15);
  }
}

.weekday-btn.active {
  background: linear-gradient(135deg, #171717 0%, #262626 100%);
  border-color: #171717;
  box-shadow: 0 4px 16px rgba(23, 23, 23, 0.2);
}

@media (max-width: 1280px) and (min-width: 1190px) {
  .weekday-btn.active {
    box-shadow: 0 4px 16px rgba(23, 23, 23, 0.2);
  }
}

.weekday-btn.today:not(.active) {
  border-color: #3b82f6;
  background: linear-gradient(135deg, #eff6ff 0%, #dbeafe 100%);
}

.weekday-btn.disabled {
  opacity: 0.4;
  cursor: not-allowed;
  border-color: #f3f4f6;
}

.day-name {
  font-size: 8px;
  font-weight: 700;
  color: #6b7280;
  letter-spacing: 0.2px;
  text-transform: uppercase;
}

@media (max-width: 1440px) and (min-width: 1190px) {
  .day-name {
    font-size: 8px;
    letter-spacing: 0.2px;
  }
}

@media (max-width: 1366px) and (min-width: 1190px) {
  .day-name {
    font-size: 7.5px;
    letter-spacing: 0.2px;
  }
}

@media (max-width: 1280px) and (min-width: 1190px) {
  .day-name {
    font-size: 7px;
    letter-spacing: 0.15px;
  }
}

@media (max-width: 1024px) and (min-width: 1190px) {
  .day-name {
    font-size: 6.5px;
    letter-spacing: 0.15px;
  }
}

.weekday-btn.active .day-name {
  color: rgba(255, 255, 255, 0.7);
}

.weekday-btn.today:not(.active) .day-name {
  color: #3b82f6;
}

.day-date {
  font-size: 13px;
  font-weight: 800;
  color: #171717;
  letter-spacing: -0.15px;
}

@media (max-width: 1440px) and (min-width: 1190px) {
  .day-date {
    font-size: 13px;
    letter-spacing: -0.15px;
  }
}

@media (max-width: 1366px) and (min-width: 1190px) {
  .day-date {
    font-size: 12px;
    letter-spacing: -0.15px;
  }
}

@media (max-width: 1280px) and (min-width: 1190px) {
  .day-date {
    font-size: 11px;
  }
}

@media (max-width: 1024px) and (min-width: 1190px) {
  .day-date {
    font-size: 10px;
  }
}

.weekday-btn.active .day-date {
  color: #ffffff;
}

.weekday-btn.today:not(.active) .day-date {
  color: #2563eb;
}
</style>

