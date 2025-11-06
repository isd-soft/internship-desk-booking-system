<
<template>
  <v-sheet class="user-panel d-flex flex-column" :style="panelStyle">
    <div class="panel-header pa-6 pb-4">
      <div class="d-flex align-items-center justify-space-between mb-3">
        <div class="head-left">
          <h2 class="header-title">Quick Actions</h2>
          <p class="header-subtitle">
            Manage your workspace · ISD desk bookings
          </p>
        </div>
        <div class="brand-wrap">
          <img src="../assets/isd-logo.webp" alt="ISD" class="brand-img" />
        </div>
      </div>
    </div>

    <div class="actions-section px-6 pb-4">
      <v-btn
        block
        variant="text"
        class="neo-btn mb-3"
        elevation="0"
        size="large"
        @click="loadData('bookings')"
      >
        <v-icon class="mr-2" size="20">mdi-calendar-check</v-icon>
        <span class="btn-text">My Bookings</span>
        <v-badge
          v-if="currentType === 'bookings' && items.length > 0"
          :content="items.length"
          color="white"
          inline
          class="ml-auto badge-pill"
        />
      </v-btn>

      <v-btn
        block
        variant="text"
        class="neo-btn mb-3"
        elevation="0"
        size="large"
        @click="loadData('favourites')"
      >
        <v-icon class="mr-2" size="20">mdi-star</v-icon>
        <span class="btn-text">Favorites</span>
        <v-badge
          v-if="currentType === 'favourites' && items.length > 0"
          :content="items.length"
          color="white"
          inline
          class="ml-auto badge-pill"
        />
      </v-btn>

      <v-btn
        block
        variant="text"
        class="neo-btn mb-3"
        elevation="0"
        size="large"
        @click="loadData('upcoming')"
      >
        <v-icon class="mr-2" size="20">mdi-clock-outline</v-icon>
        <span class="btn-text">Upcoming</span>
        <v-badge
          v-if="currentType === 'upcoming' && items.length > 0"
          :content="items.length"
          color="white"
          inline
          class="ml-auto badge-pill"
        />
      </v-btn>

      <v-divider class="my-2"></v-divider>

      <v-btn
        block
        variant="text"
        class="neo-btn danger"
        elevation="0"
        size="large"
        @click="logout"
      >
        <v-icon class="mr-2" size="20">mdi-logout</v-icon>
        <span class="btn-text">Logout</span>
      </v-btn>
    </div>

    <div
      v-if="items.length === 0 && !loading"
      class="empty-panel flex-grow-1 d-flex align-items-center justify-center pa-6"
    >
      <div class="text-center">
        <v-icon size="54" color="grey-lighten-1" class="mb-3"
          >mdi-alert-circle-outline</v-icon
        >
        <h3 class="empty-title">No {{ currentTitle.toLowerCase() }} found</h3>
        <p class="empty-subtitle">
          There are no {{ currentTitle.toLowerCase() }} available.
        </p>
      </div>
    </div>

    <v-slide-y-transition>
      <div
        v-if="items.length > 0"
        class="results-section flex-grow-1 px-6 pb-6"
      >
        <div class="results-container">
          <div class="results-header">
            <div class="results-title-wrap">
              <div class="results-title">{{ currentTitle }}</div>
              <div class="results-sub">{{ items.length }} items</div>
            </div>
            <v-chip
              size="small"
              :color="getTypeChipColor()"
              variant="flat"
              class="results-badge"
            >
              {{ items.length }}
            </v-chip>
          </div>

          <div class="items-list">
            <transition-group name="list-fade" tag="div">
              <div
                v-for="(item, i) in paginatedItems"
                :key="item.id ?? i"
                class="data-item"
              >
                <div class="index-badge">{{ startItem + i }}</div>

                <div class="item-content">
                  <div class="item-header">
                    <div class="item-title">{{ item.desk }}</div>
                    <div class="item-actions">
                      <v-chip
                        class="status-chip mr-1"
                        size="x-small"
                        :color="item.statusColor"
                        variant="flat"
                        >{{ item.status }}</v-chip
                      >
                      <v-btn
                        size="small"
                        variant="text"
                        class="more-btn"
                        @click="openDetails(item)"
                      >
                        Details
                        <v-icon size="16" class="ml-1"
                          >mdi-chevron-right</v-icon
                        >
                      </v-btn>
                    </div>
                  </div>

                  <div class="item-meta nowrap">
                    <v-icon size="16" class="meta-ic">mdi-calendar</v-icon>
                    <span class="meta">{{ item.date }}</span>
                    <span class="dot">•</span>
                    <v-icon size="16" class="meta-ic">mdi-clock-outline</v-icon>
                    <span class="meta">{{ item.time }}</span>
                    <span class="dot">•</span>
                    <v-icon size="16" class="meta-ic">mdi-timer-outline</v-icon>
                    <span class="meta">{{ item.duration }}</span>
                  </div>
                </div>
              </div>
            </transition-group>
          </div>
        </div>
      </div>
    </v-slide-y-transition>

    <v-dialog
      v-model="details.open"
      max-width="440"
      transition="fade-transition"
    >
      <v-card class="details-sheet pa-5">
        <h2 class="ds-title mb-5">{{ details.item?.desk }}</h2>

        <div class="ds-table">
          <div class="ds-row">
            <span class="ds-label">Zone</span>
            <span class="ds-value">{{ details.item?.zone }}</span>
          </div>

          <div class="ds-row">
            <span class="ds-label">Date</span>
            <span class="ds-value">{{ details.item?.date }}</span>
          </div>

          <div class="ds-row">
            <span class="ds-label">Time</span>
            <span class="ds-value">{{ details.item?.time }}</span>
          </div>

          <div class="ds-row">
            <span class="ds-label">Duration</span>
            <span class="ds-value">{{ details.item?.duration }}</span>
          </div>

          <div class="ds-row">
            <span class="ds-label">Status</span>
            <span class="ds-value">{{ details.item?.status }}</span>
          </div>
        </div>

        <v-btn variant="text" class="ds-close" @click="details.open = false">
          Close
        </v-btn>
      </v-card>
    </v-dialog>
    <div
      v-if="loading"
      class="loading-panel flex-grow-1 d-flex align-items-center justify-center"
    >
      <div class="text-center">
        <v-progress-circular
          indeterminate
          size="48"
          width="4"
        ></v-progress-circular>
        <p class="loading-text mt-4">Loading {{ currentTitle }}...</p>
      </div>
    </div>
  </v-sheet>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from "vue";
import { useRouter } from "vue-router";
import api from "../plugins/axios";

const router = useRouter();

const items = ref([]);
const currentTitle = ref("Data");
const currentType = ref("");
const loading = ref(false);
const currentPage = ref(1);

const winW = ref(window.innerWidth);
const itemsPerPage = ref(3);

const updateLayout = () => {
  winW.value = window.innerWidth;
  if (winW.value < 900) itemsPerPage.value = 2;
  else if (winW.value < 1280) itemsPerPage.value = 3;
  else itemsPerPage.value = 3;
};
onMounted(() => {
  updateLayout();
  window.addEventListener("resize", updateLayout);
});
onBeforeUnmount(() => window.removeEventListener("resize", updateLayout));

const panelStyle = computed(() => {
  const vwWidth = Math.round(window.innerWidth * 0.34);
  const w = Math.min(Math.max(vwWidth, 360), 640);
  return `width:${w}px;max-width:100vw;`;
});

const snackbar = ref({ show: false, message: "", color: "success" });
const details = ref({ open: false, item: null });

const totalPages = computed(() =>
  Math.ceil(items.value.length / itemsPerPage.value)
);
const paginatedItems = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage.value;
  const end = start + itemsPerPage.value;
  return items.value.slice(start, end);
});
const startItem = computed(
  () => (currentPage.value - 1) * itemsPerPage.value + 1
);
const endItem = computed(() =>
  Math.min(currentPage.value * itemsPerPage.value, items.value.length)
);

const getTypeChipColor = () => "grey-darken-1";
const statusToColor = (s) =>
  s === "CONFIRMED" ? "success" : s === "CANCELLED" ? "error" : "primary";

async function loadData(type) {
  try {
    loading.value = true;
    currentType.value = type;
    currentPage.value = 1;

    if (type === "bookings") {
      currentTitle.value = "My Bookings";
      const response = await api.get("/booking/my");
      const data = (response.data || [])
        .slice()
        .sort((a, b) => new Date(a.startTime) - new Date(b.startTime));

      items.value = data.map((b, idx) => ({
        id: b.id ?? idx,
        desk: b.desk?.deskName || "Desk",
        zone: b.desk?.zone || "Unknown zone",
        type: b.desk?.deskType || "—",
        date: formatDate(b.startTime),
        time: `${formatTime(b.startTime)} - ${formatTime(b.endTime)}`,
        duration: formatDuration(b.startTime, b.endTime),
        status: b.status,
        statusColor: statusToColor(b.status),
        raw: b,
      }));

      snackbar.value = {
        show: true,
        message: `Loaded ${items.value.length} bookings`,
        color: "success",
      };
    } else if (type === "favourites") {
      currentTitle.value = "Favorites";
      const response = await api.get("/booking/my");
      const data = (response.data || [])
        .slice()
        .sort((a, b) => new Date(a.startTime) - new Date(b.startTime));

      items.value = data.map((b, idx) => ({
        id: b.id ?? idx,
        desk: b.desk?.deskName || "Desk",
        zone: b.desk?.zone || "Unknown zone",
        type: b.desk?.deskType || "—",
        date: formatDate(b.startTime),
        time: `${formatTime(b.startTime)} - ${formatTime(b.endTime)}`,
        duration: formatDuration(b.startTime, b.endTime),
        status: b.status,
        statusColor: statusToColor(b.status),
        raw: b,
      }));

      snackbar.value = {
        show: true,
        message: `Loaded ${items.value.length} favorites`,
        color: "success",
      };
    } else if (type === "upcoming") {
      currentTitle.value = "Upcoming";

      const response = await api.get("/booking/my");
      const data = (response.data || [])
        .slice()
        .sort((a, b) => new Date(a.startTime) - new Date(b.startTime));

      items.value = data.map((b, idx) => ({
        id: b.id ?? idx,
        desk: b.desk?.deskName || "Desk",
        zone: b.desk?.zone || "Unknown zone",
        type: b.desk?.deskType || "—",
        date: formatDate(b.startTime),
        time: `${formatTime(b.startTime)} - ${formatTime(b.endTime)}`,
        duration: formatDuration(b.startTime, b.endTime),
        status: b.status,
        statusColor: statusToColor(b.status),
        raw: b,
      }));

      snackbar.value = {
        show: true,
        message: `Loaded ${items.value.length} upcoming`,
        color: "success",
      };
    }
  } catch (error) {
    console.error("Error fetching data:", error?.response?.data || error);
    snackbar.value = {
      show: true,
      message: "Failed to load data",
      color: "error",
    };
    items.value = [];
  } finally {
    loading.value = false;
  }
}

function openDetails(item) {
  details.value.item = item;
  details.value.open = true;
}

function logout() {
  localStorage.removeItem("token");
  try {
    if (api?.defaults?.headers?.common?.Authorization) {
      delete api.defaults.headers.common["Authorization"];
    }
  } catch (_) {}
  snackbar.value = { show: true, message: "Logged out", color: "success" };
  router.push("/login");
}

function formatDate(dateStr) {
  const date = new Date(dateStr);
  return date.toLocaleDateString("en-US", { month: "short", day: "numeric" });
}
function formatTime(dateStr) {
  const date = new Date(dateStr);
  return date.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
}
function formatDuration(startStr, endStr) {
  const start = new Date(startStr);
  const end = new Date(endStr);
  const diffMs = Math.max(0, end - start);
  const totalMin = Math.round(diffMs / 60000);
  const h = Math.floor(totalMin / 60);
  const m = totalMin % 60;
  return h > 0 ? `${h}h ${m.toString().padStart(2, "0")}m` : `${m}m`;
}
</script>

<style scoped>
:root {
  --card-bg: #ffffff;
  --card-border: rgba(255, 138, 0, 0.16);
  --panel-sep: rgba(255, 170, 64, 0.16);
  --soft: #fff7f0;
  --surface: #fffaf4;
  --text-1: #0f172a;
  --text-2: #5f5b53;
  --accent: #ff8a00;
  --danger-soft: #ffe9ea;
  --danger-line: rgba(210, 80, 80, 0.22);
}

* {
  font-family: "Inter", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto,
    sans-serif;
}

.user-panel {
  background: #f3f4f6;
  border-left: 1px solid var(--panel-sep);
  height: 100vh;
  overflow: hidden;
}

.panel-header {
  background: #f9fafb;
  border-bottom: 1px solid var(--panel-sep);
}
.head-left {
  min-width: 0;
}
.header-title {
  font-size: clamp(1.05rem, 0.9rem + 0.4vw, 1.22rem);
  font-weight: 900;
  color: var(--text-1);
  margin: 0;
}
.header-subtitle {
  font-size: clamp(0.9rem, 0.85rem + 0.2vw, 1rem);
  color: var(--text-2);
  font-weight: 650;
  margin: 4px 0 0;
}
.brand-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  width: clamp(76px, 6.4vw, 96px);
  height: clamp(76px, 6.4vw, 96px);
  border-radius: 16px;
  border: 1px solid #ffd6a3;
  background: transparent;
  box-shadow: 0 6px 18px rgba(255, 138, 0, 0.1);
}
.brand-img {
  width: clamp(64px, 5.6vw, 84px);
  height: clamp(64px, 5.6vw, 84px);
  object-fit: contain;
}

.neo-btn {
  height: clamp(46px, 4.2vh, 52px) !important;
  border-radius: 12px !important;
  text-transform: none;
  font-weight: 780;
  font-size: clamp(0.95rem, 0.9rem + 0.2vw, 1.02rem);
  justify-content: flex-start;
  letter-spacing: 0.2px;
  padding: 0 14px;
  transition: transform 0.16s ease, box-shadow 0.16s ease, background 0.16s ease,
    border-color 0.16s ease;
  background: #fff;
  border: 1px solid #e5e7eb;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
  color: #1f2937;
}
.neo-btn:hover {
  transform: translateY(-1px);
  background: #fff7eb;
  border-color: var(--accent);
  box-shadow: 0 6px 12px rgba(255, 138, 0, 0.1);
}
.neo-btn.danger {
  background: #fff;
  border: 1px solid #fca5a5;
  color: #b91c1c;
}
.btn-text {
  font-weight: 820;
}

.badge-pill :deep(.v-badge__badge) {
  border-radius: 999px;
  padding: 0 8px;
  font-weight: 820;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.08);
  color: #1f2937 !important;
  background-color: #e5e7eb !important;
}

.results-section {
  overflow-y: auto;
  background: var(--soft);
}
.results-container {
  background: var(--card-bg);
  border-radius: 16px;
  padding: clamp(14px, 1.2vw, 20px);
  border: 1px solid var(--card-border);
  height: 100%;
  display: flex;
  flex-direction: column;
}
.results-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--panel-sep);
}
.results-title-wrap {
  display: flex;
  align-items: baseline;
  gap: 10px;
}
.results-title {
  font-size: clamp(1.08rem, 0.98rem + 0.3vw, 1.2rem);
  font-weight: 900;
  color: #111827;
  letter-spacing: 0.2px;
}
.results-sub {
  font-size: clamp(0.9rem, 0.86rem + 0.15vw, 1rem);
  font-weight: 750;
  color: #6b7280;
}
.results-badge {
  font-weight: 800;
  font-size: 0.78rem;
  color: #fff !important;
}

.items-list {
  flex: 1;
  overflow-y: auto;
  margin-bottom: 10px;
}
.items-list::-webkit-scrollbar {
  width: 6px;
}
.items-list::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.18);
  border-radius: 10px;
}
.items-list::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.28);
}

.data-item {
  position: relative;
  padding: 16px 16px 16px 68px;
  margin-bottom: 14px;
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  transition: transform 0.16s ease, box-shadow 0.16s ease,
    border-color 0.16s ease, background 0.16s ease;
}
.data-item:hover {
  transform: translateY(-1px);
  background: #fffdfa;
  border-color: #fbbf24;
  box-shadow: 0 8px 18px rgba(255, 138, 0, 0.1);
}

.index-badge {
  position: absolute;
  left: 14px;
  top: 14px;
  min-width: clamp(30px, 2.6vw, 36px);
  height: clamp(30px, 2.6vw, 36px);
  padding: 0 8px;
  border-radius: 999px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 900;
  font-size: clamp(0.82rem, 0.76rem + 0.2vw, 0.95rem);
  color: #1f2937;
  background: #fbbf24;
  box-shadow: 0 4px 10px rgba(251, 191, 36, 0.3);
  border: 2px solid #fff;
}

.item-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}
.item-title {
  font-weight: 900;
  font-size: clamp(1.02rem, 0.98rem + 0.25vw, 1.12rem);
  color: #0f172a;
}
.item-actions {
  display: flex;
  align-items: center;
}
.status-chip {
  font-weight: 820;
  text-transform: uppercase;
  letter-spacing: 0.35px;
}
.more-btn {
  font-weight: 820;
}

.item-meta {
  margin-top: 6px;
  font-size: clamp(0.95rem, 0.9rem + 0.2vw, 1.02rem);
  font-weight: 850;
  color: #374151;
  display: flex;
  align-items: center;
  gap: 8px;
}
.item-meta .dot {
  opacity: 0.45;
}
.meta-ic {
  opacity: 0.7;
  margin-right: 2px;
}
.nowrap {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.pagination-section {
  padding-top: 10px;
  border-top: 1px solid var(--panel-sep);
}
.pagination-info {
  text-align: center;
}
.page-text {
  font-size: 0.9rem;
  font-weight: 800;
  color: #4b5563;
}

.empty-panel {
  background: var(--soft);
}
.empty-icon-wrapper {
  width: 100px;
  height: 100px;
  margin: 0 auto;
  background: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid var(--panel-sep);
}
.empty-title {
  font-size: 1.06rem;
  font-weight: 900;
  color: var(--text-1);
  margin-bottom: 8px;
}
.empty-subtitle {
  font-size: 0.92rem;
  color: var(--text-2);
  font-weight: 650;
}

.loading-panel {
  background: var(--soft);
}
.loading-text {
  font-size: 0.9rem;
  font-weight: 750;
  color: #6b7280;
}

.snackbar-text {
  font-size: 0.9rem;
  font-weight: 750;
}

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

.list-fade-enter-active,
.list-fade-leave-active {
  transition: all 0.28s cubic-bezier(0.22, 1, 0.36, 1);
}
.list-fade-enter-from {
  opacity: 0;
  transform: translateY(10px) scale(0.985);
}
.list-fade-leave-to {
  opacity: 0;
  transform: translateY(-8px) scale(0.985);
}

@media (max-width: 900px) {
  .user-panel {
    width: 100% !important;
  }
  .results-container {
    padding: 16px;
  }
}
.details-sheet {
  border-radius: 18px;
  background: #fff;
  box-shadow: 0 10px 32px rgba(0, 0, 0, 0.14);
}

.ds-title {
  font-weight: 900;
  font-size: 1.45rem;
  color: #0f172a;
}

.ds-table {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.ds-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  padding-bottom: 6px;
}

.ds-label {
  flex: 0 0 120px;
  font-size: 0.92rem;
  font-weight: 700;
  color: #6b7280;
}

.ds-value {
  text-align: right;
  flex: 1;
  font-size: 1.04rem;
  font-weight: 820;
  color: #111827;
}

.ds-close {
  margin-top: 28px;
  text-transform: none;
  font-weight: 800 !important;
  font-size: 1rem;
  color: #374151 !important;
  align-self: flex-end;
}
</style>
