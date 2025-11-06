<template>
  <v-expand-transition>
    <div v-if="show" class="booking-list">
      <v-progress-circular
        v-if="loading"
        indeterminate
        color="primary"
        class="mx-auto my-4 d-block"
      />

      <div v-else-if="bookings.length === 0" class="no-bookings">
        No bookings yet
      </div>

      <div v-else class="scrollable-list">
        <div
          v-for="(booking, index) in bookings"
          :key="index"
          class="booking-card"
        >
          <div class="card-content">
            <div class="desk-info">
              <div class="desk-icon">
                <v-icon size="20" color="white">mdi-desk</v-icon>
              </div>
              <div class="desk-details">
                <div class="desk-name">{{ booking.desk.deskName }}</div>
                <div class="desk-meta">
                  {{ booking.desk.zone }} • {{ booking.desk.deskType }}
                </div>
              </div>
            </div>

            <v-chip
              :color="statusColor(booking.status)"
              variant="flat"
              size="small"
              class="status-chip"
            >
              {{ booking.status }}
            </v-chip>
          </div>

          <div class="time-info">
            <div class="time-row">
              <span class="time-label">Date</span>
              <span class="time-value">{{ formatDate(booking.startTime) }}</span>
            </div>
            <div class="time-row">
              <span class="time-label">Time</span>
              <span class="time-value">
                {{ formatTime(booking.startTime) }} — {{ formatTime(booking.endTime) }}
              </span>
            </div>
          </div>

          <div class="availability-info" v-if="booking.desk.isTemporarilyAvailable">
            <div class="availability-dot"></div>
            <span class="availability-text">
              Available until {{ formatTime(booking.desk.temporaryAvailableUntil) }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </v-expand-transition>
</template>

<script setup>
import { ref, watch } from "vue";
import api from "../plugins/axios";

const props = defineProps({
  show: Boolean,
});

const bookings = ref([]);
const loading = ref(false);

const fetchBookings = async () => {
  try {
    loading.value = true;
    const response = await api.get("/bookings/my");
    bookings.value = response.data;
  } catch (err) {
    console.error("Error fetching bookings:", err);
  } finally {
    loading.value = false;
  }
};

watch(
  () => props.show,
  (val) => {
    if (val) fetchBookings();
  }
);

const formatDate = (dateStr) => {
  const date = new Date(dateStr);
  return date.toLocaleDateString("en-US", { 
    day: "numeric", 
    month: "short",
    year: "numeric"
  });
};

const formatTime = (dateStr) => {
  const date = new Date(dateStr);
  return date.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
};

const statusColor = (status) => {
  switch (status) {
    case "CONFIRMED":
      return "success";
    case "ACTIVE":
      return "primary";
    case "CANCELLED":
      return "error";
    default:
      return "grey";
  }
};
</script>

<style scoped>
.booking-list {
  max-height: 350px;
  overflow-y: auto;
  background: #fafafa;
  border-radius: 12px;
  padding: 12px;
}

.scrollable-list::-webkit-scrollbar {
  width: 6px;
}

.scrollable-list::-webkit-scrollbar-thumb {
  background: #cbd5e0;
  border-radius: 3px;
}

.scrollable-list::-webkit-scrollbar-thumb:hover {
  background: #a0aec0;
}

.booking-card {
  border-radius: 8px;
  margin-bottom: 12px;
  padding: 16px;
  background: white;
  border: 1px solid #e2e8f0;
  transition: all 0.2s ease;
}

.booking-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border-color: #cbd5e0;
}

.card-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.desk-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.desk-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.desk-details {
  flex: 1;
  min-width: 0;
}

.desk-name {
  font-weight: 600;
  font-size: 15px;
  color: #2d3748;
  margin-bottom: 4px;
}

.desk-meta {
  font-size: 13px;
  color: #718096;
}

.status-chip {
  font-weight: 600;
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  height: 24px !important;
}

.time-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px;
  background: #f7fafc;
  border-radius: 6px;
  margin-bottom: 12px;
}

.time-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.time-label {
  font-size: 12px;
  color: #718096;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.time-value {
  font-size: 14px;
  color: #2d3748;
  font-weight: 600;
}

.availability-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: #f0fdf4;
  border-radius: 6px;
  border: 1px solid #bbf7d0;
}

.availability-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #22c55e;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

.availability-text {
  font-size: 13px;
  color: #166534;
  font-weight: 500;
}

.no-bookings {
  text-align: center;
  color: #a0aec0;
  font-weight: 500;
  padding: 24px 0;
  font-size: 14px;
}
</style>