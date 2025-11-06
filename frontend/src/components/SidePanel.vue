<template>
  <v-sheet
    class="user-panel pa-6 d-flex flex-column justify-space-between"
    width="340"
  >
    <div>
      <div class="section-header mb-6">
        <h3 class="text-h6 font-weight-bold">Quick Actions</h3>
        <div class="header-accent"></div>
      </div>

      <v-btn
        block
        color="primary"
        variant="elevated"
        prepend-icon="mdi-star"
        class="mb-3 action-btn"
        @click="loadData('favourites')"
        elevation="0"
      >
        <span class="btn-text">Favorites</span>
      </v-btn>

      <v-btn
        block
        color="teal"
        variant="elevated"
        prepend-icon="mdi-seat"
        class="mb-3 action-btn"
        @click="loadData('bookings')"
        elevation="0"
      >
        <span class="btn-text">My Bookings</span>
      </v-btn>

      <v-btn
        block
        color="success"
        variant="elevated"
        prepend-icon="mdi-calendar-clock"
        class="mb-3 action-btn"
        @click="loadData('upcoming')"
        elevation="0"
      >
        <span class="btn-text">Upcoming</span>
      </v-btn>

      <v-btn
        block
        variant="tonal"
        prepend-icon="mdi-refresh"
        class="action-btn refresh-btn"
        @click="refreshData"
        elevation="0"
      >
        <span class="btn-text">Refresh</span>
      </v-btn>
    </div>

    <v-slide-y-transition>
      <div v-if="items.length > 0" class="results-container mt-6">
        <div class="results-header">
          <v-icon size="20" class="mr-2">mdi-view-list</v-icon>
          <span class="results-title">{{ currentTitle }}</span>
          <v-chip size="small" class="ml-auto" color="primary" variant="tonal">
            {{ items.length }}
          </v-chip>
        </div>

        <v-slide-y-transition group>
          <div v-for="(item, i) in items" :key="i" class="data-item">
            <div class="item-icon">
              <v-icon size="20" color="primary">mdi-calendar-check</v-icon>
            </div>
            <div class="item-content">
              <div class="item-title">{{ item.title }}</div>
              <div class="item-desc">{{ item.desc }}</div>
            </div>
          </div>
        </v-slide-y-transition>
      </div>
    </v-slide-y-transition>

    <v-snackbar
      v-model="snackbar.show"
      :color="snackbar.color"
      timeout="2500"
      rounded="pill"
      elevation="8"
      location="top"
    >
      <div class="d-flex align-center">
        <v-icon class="mr-2">
          {{
            snackbar.color === "success"
              ? "mdi-check-circle"
              : "mdi-alert-circle"
          }}
        </v-icon>
        {{ snackbar.message }}
      </div>
    </v-snackbar>
  </v-sheet>
</template>

<script setup>
import { ref } from "vue";
import api from "../plugins/axios";

const items = ref([]);
const currentTitle = ref("Data");
const snackbar = ref({
  show: false,
  message: "",
  color: "error",
});

async function loadData(type) {
  try {
    if (type === "bookings") {
      currentTitle.value = "My Bookings";

      console.log("Fetching bookings from /api/v1/bookings/my ...");
      const response = await api.get("/booking/my");
      console.log("Bookings response:", response.data);

      items.value = response.data.map((b) => ({
        title: `${b.desk?.deskName || "Desk"} — ${
          b.desk?.zone || "Unknown zone"
        }`,
        desc: `${formatDate(b.startTime)} • ${formatTime(
          b.startTime
        )} - ${formatTime(b.endTime)} • ${b.status}`,
      }));

      snackbar.value = {
        show: true,
        message: "Bookings loaded successfully",
        color: "success",
      };
    } else if (type === "favourites") {
      currentTitle.value = "Favorites";
      items.value = [
        { title: "Desk A12", desc: "Near window, IT Dept" },
        { title: "Desk B05", desc: "By the wall, Design Dept" },
      ];
    } else if (type === "upcoming") {
      currentTitle.value = "Upcoming";
      items.value = [
        { title: "Desk D21", desc: "Monday 08:00 - 17:00" },
        { title: "Team Room", desc: "Tuesday 10:00 - 12:00" },
      ];
    }
  } catch (error) {
    console.error("Error fetching data:", error.response?.data || error);
    snackbar.value = {
      show: true,
      message: "Failed to load data",
      color: "error",
    };
  }
}

function refreshData() {
  items.value = [];
  currentTitle.value = "Data";
}

function formatDate(dateStr) {
  const date = new Date(dateStr);
  return date.toLocaleDateString("en-US", { month: "short", day: "numeric" }); // Nov 7
}

function formatTime(dateStr) {
  const date = new Date(dateStr);
  return date.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" }); // 00:51
}
</script>

<style scoped>
.user-panel {
  background: linear-gradient(to bottom, #fafbfc 0%, #f5f7fa 100%);
  border-left: 1px solid #e5e7eb;
  height: 100vh;
  overflow-y: auto;
}

.section-header {
  position: relative;
  padding-bottom: 8px;
}

.section-header h3 {
  color: #1f2937;
  font-size: 1.1rem;
  letter-spacing: -0.02em;
}

.header-accent {
  height: 3px;
  width: 40px;
  background: linear-gradient(90deg, #1976d2 0%, #42a5f5 100%);
  border-radius: 2px;
  margin-top: 4px;
}

.action-btn {
  height: 52px !important;
  border-radius: 14px !important;
  text-transform: none;
  font-weight: 600;
  font-size: 0.95rem;
  letter-spacing: 0.01em;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08) !important;
}

.action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.12) !important;
}

.action-btn:active {
  transform: translateY(0px);
}

.btn-text {
  font-weight: 600;
}

.refresh-btn {
  background: white !important;
  border: 1.5px solid #e5e7eb;
}

.results-container {
  background: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  max-height: 45%;
  overflow-y: auto;
  border: 1px solid #e5e7eb;
}

.results-header {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 2px solid #f3f4f6;
}

.results-title {
  font-size: 0.9rem;
  font-weight: 700;
  color: #374151;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.data-item {
  display: flex;
  gap: 12px;
  padding: 14px;
  margin-bottom: 10px;
  background: linear-gradient(135deg, #fafbfc 0%, #f9fafb 100%);
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.data-item::before {
  content: "";
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  width: 4px;
  background: linear-gradient(180deg, #1976d2 0%, #42a5f5 100%);
  border-radius: 0 2px 2px 0;
  transform: scaleY(0);
  transition: transform 0.3s ease;
}

.data-item:hover {
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border-color: #cbd5e1;
}

.data-item:hover::before {
  transform: scaleY(1);
}

.item-icon {
  flex-shrink: 0;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
  border-radius: 10px;
  border: 1px solid #90caf9;
}

.item-content {
  flex: 1;
  min-width: 0;
}

.item-title {
  font-weight: 700;
  font-size: 1rem;
  color: #111827;
  margin-bottom: 6px;
  line-height: 1.3;
}

.item-desc {
  font-size: 0.875rem;
  color: #4b5563;
  line-height: 1.5;
  font-weight: 600;
}

.results-container::-webkit-scrollbar {
  width: 6px;
}

.results-container::-webkit-scrollbar-track {
  background: #f3f4f6;
  border-radius: 10px;
}

.results-container::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 10px;
}

.results-container::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}
</style>
