<template>
  <v-expand-transition>
    <div v-if="show" class="booking-list">
      <v-progress-circular
        v-if="loading"
        indeterminate
        color="orange-darken-2"
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
          <div class="desk-header">
            <v-icon size="22" color="orange-darken-3" class="mr-1">
              mdi-desk
            </v-icon>
            <div>
              <div class="desk-name">{{ booking.desk.deskName }}</div>
              <div class="desk-zone">
                {{ booking.desk.zone }} • {{ booking.desk.deskType }}
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

          <!-- Время -->
          <div class="booking-time">
            <v-icon size="18" color="orange-darken-2" class="mr-1">
              mdi-clock-time-four-outline
            </v-icon>
            <span class="time-text">
              📅 {{ formatDate(booking.startTime) }} •
              {{ formatTime(booking.startTime) }} –
              {{ formatTime(booking.endTime) }}
            </span>
          </div>

          <div class="availability" v-if="booking.desk.isTemporarilyAvailable">
            <v-icon size="18" color="green" class="mr-1"
              >mdi-check-circle</v-icon
            >
            <span class="availability-text">
              Available until
              {{ formatTime(booking.desk.temporaryAvailableUntil) }}
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
  return date.toLocaleDateString("en-US", { day: "numeric", month: "short" });
};

const formatTime = (dateStr) => {
  const date = new Date(dateStr);
  return date.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
};

const statusColor = (status) => {
  switch (status) {
    case "CONFIRMED":
      return "green";
    case "ACTIVE":
      return "blue";
    case "CANCELLED":
      return "red";
    default:
      return "grey";
  }
};
</script>

<style scoped>
.booking-list {
  max-height: 350px;
  overflow-y: auto;
  background: #fffdf8;
  border-radius: 14px;
  padding: 8px;
}

.scrollable-list::-webkit-scrollbar {
  width: 6px;
}
.scrollable-list::-webkit-scrollbar-thumb {
  background: #ff9800;
  border-radius: 4px;
}
.scrollable-list::-webkit-scrollbar-thumb:hover {
  background: #f57c00;
}

.booking-card {
  border-radius: 12px;
  margin: 8px 10px;
  padding: 14px 16px;
  background: linear-gradient(145deg, #ffffff, #fff5ea);
  box-shadow: 0 2px 8px rgba(255, 152, 0, 0.12);
  border: 1px solid rgba(255, 152, 0, 0.2);
  transition: 0.3s ease;
}

.booking-card:hover {
  box-shadow: 0 6px 14px rgba(255, 152, 0, 0.25);
  transform: translateY(-3px);
}

.desk-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.desk-name {
  font-weight: 700;
  font-size: 16px;
  color: #2d2d2d;
}

.desk-zone {
  font-size: 13px;
  color: #777;
  margin-top: 2px;
}

.status-chip {
  font-weight: 700;
  color: white;
  text-transform: uppercase;
  letter-spacing: 0.3px;
}

.booking-time {
  display: flex;
  align-items: center;
  color: #444;
  font-size: 14px;
  margin-top: 8px;
}

.time-text {
  font-weight: 600;
  color: #333;
}

.availability {
  display: flex;
  align-items: center;
  margin-top: 6px;
  font-size: 13px;
  color: #2e7d32;
  font-weight: 500;
}

.no-bookings {
  text-align: center;
  color: #9e9e9e;
  font-weight: 600;
  padding: 16px 0;
}
</style>
