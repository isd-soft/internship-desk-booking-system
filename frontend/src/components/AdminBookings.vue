<template>
  <div class="admin-bookings">
    <div class="admin-card">
      <div class="admin-header">
        <div class="title-wrap">
          <div class="workspace-label">ADMIN PANEL</div>
          <h2 class="title">All Bookings</h2>
          <span class="sub">{{ filteredBookings.length }} total bookings</span>
        </div>
        <div class="header-actions">
          <v-text-field
            v-model="searchQuery"
            density="compact"
            variant="outlined"
            hide-details
            clearable
            placeholder="Search bookings..."
            style="max-width: 250px"
            :disabled="loading"
            class="search-field"
          />

          <v-select
            v-model="statusFilter"
            :items="STATUS_OPTIONS"
            item-title="title"
            item-value="value"
            density="compact"
            variant="outlined"
            style="max-width: 180px"
            :disabled="loading"
            :clearable="false"
            hide-details
            label="Filter by status"
            class="filter-select"
          />

          <v-select
            v-model="typeFilter"
            :items="TYPE_OPTIONS"
            item-title="title"
            item-value="value"
            density="compact"
            variant="outlined"
            style="max-width: 180px"
            :disabled="loading"
            :clearable="false"
            hide-details
            label="Filter by desk type"
            class="filter-select"
          />

          <v-btn
            color="#171717"
            variant="flat"
            @click="resetFilters"
            class="reset-btn"
          >
            Reset Filters
          </v-btn>

          <v-chip
            size="small"
            color="#171717"
            variant="flat"
            class="count-chip"
          >
            {{ filteredBookings.length }}
          </v-chip>
        </div>
      </div>

      <v-alert
        v-if="error"
        type="error"
        variant="tonal"
        class="mb-4"
        density="compact"
      >
        {{ error }}
      </v-alert>

      <div v-if="loading" class="loading-wrap">
        <v-progress-circular
          indeterminate
          size="48"
          width="4"
          color="#171717"
        />
        <p class="loading-text mt-3">Loading bookings…</p>
      </div>

      <template v-else>
        <v-data-table
          :headers="headers"
          :items="filteredBookings"
          item-key="id"
          density="compact"
          class="elevated-table"
          :items-per-page="15"
          fixed-header
          height="70vh"
        >
          <template #item.userId="{ item }">
            <span class="cell-strong">{{ item.userId ?? "—" }}</span>
          </template>

          <template #item.deskName="{ item }">
            <div class="cell-main">
              <div class="desk-name">{{ item.deskName }}</div>
              <div class="desk-meta">
                {{ item.zoneName }} • {{ item.zoneId }}
              </div>
            </div>
          </template>

          <template #item.deskType="{ item }">
            <v-chip
              size="x-small"
              :color="getColor(item.deskType)"
              variant="flat"
              class="status-chip"
            >
              {{ item.deskType }}
            </v-chip>
          </template>

          <template #item.startTime="{ item }">
            <div class="nowrap">{{ formatDate(item.startTime) }}</div>
            <div class="muted nowrap">{{ formatTime(item.startTime) }}</div>
          </template>

          <template #item.endTime="{ item }">
            <div class="nowrap">{{ formatDate(item.endTime) }}</div>
            <div class="muted nowrap">{{ formatTime(item.endTime) }}</div>
          </template>

          <template #item.duration="{ item }">
            <span class="cell-strong">{{ item.duration }}</span>
          </template>

          <template #item.status="{ item }">
            <v-chip
              size="x-small"
              :color="getColor(item.status)"
              variant="flat"
              class="status-chip"
            >
              {{ item.status }}
            </v-chip>
          </template>

          <template #item.actions="{ item }">
            <v-menu :close-on-content-click="true" location="bottom end">
              <template #activator="{ props }">
                <v-btn
                  v-bind="props"
                  icon
                  variant="text"
                  size="small"
                  color="#171717"
                  :loading="cancellingId === item.id"
                  :disabled="cancellingId === item.id"
                  class="action-btn"
                >
                  <v-icon>mdi-dots-vertical</v-icon>
                </v-btn>
              </template>
              <v-list density="compact" class="action-menu">
                <v-list-item
                  @click="viewBooking(item)"
                  prepend-icon="mdi-eye"
                  title="View"
                >
                </v-list-item>
                <v-list-item
                  @click="editBooking(item)"
                  prepend-icon="mdi-pencil"
                  title="Edit"
                >
                </v-list-item>
                <v-list-item
                  @click="cancelBooking(item)"
                  prepend-icon="mdi-cancel"
                  title="Cancel booking"
                  :disabled="
                    cancellingId === item.id || item.status === 'CANCELLED'
                  "
                ></v-list-item>
              </v-list>
            </v-menu>
          </template>

          <template #no-data>
            <div class="empty-state">
              <v-icon size="48" color="#a3a3a3" class="mb-3"
                >mdi-calendar-blank</v-icon
              >
              <div class="empty-title">No bookings found</div>
              <div class="empty-sub">
                Try adjusting your filters or check back later.
              </div>
            </div>
          </template>
        </v-data-table>

        <BookingViewModal
          :show="showViewModal"
          v-model="showViewModal"
          :booking="selectedBooking"
        />

        <BookingEditModal
          :show="showEditModal"
          :booking="selectedBooking"
          @close="showEditModal = false"
          @save="saveBooking"
        />
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from "vue";
import { useRouter, useRoute } from "vue-router";
import api from "../plugins/axios";
import BookingEditModal from "../components/AdminDashboard/BookingEditModal.vue";
import BookingViewModal from "../components/AdminDashboard/BookingViewModal.vue";

const router = useRouter();
const route = useRoute();

const bookings = ref([]);
const loading = ref(false);
const error = ref(null);
const cancellingId = ref(null);
const searchQuery = ref("");
const selectedBooking = ref(null);
const showEditModal = ref(false);
const showViewModal = ref(false);

const STATUS_OPTIONS = [
  { title: "All", value: "ALL" },
  { title: "Active", value: "ACTIVE" },
  { title: "Cancelled", value: "CANCELLED" },
  { title: "Confirmed", value: "CONFIRMED" },
];

const TYPE_OPTIONS = [
  { title: "All", value: "ALL" },
  { title: "Shared", value: "SHARED" },
  { title: "Assigned", value: "ASSIGNED" },
  { title: "Unavailable", value: "UNAVAILABLE" },
];

const statusFilter = ref(String(route.query?.status || "ALL").toUpperCase());
const typeFilter = ref(String(route.query?.type || "ALL").toUpperCase());

const headers = [
  { title: "Booking ID", key: "id", width: 100, align: "start" },
  { title: "Desk ID", key: "deskId", width: 100 },
  { title: "User ID", key: "userId", width: 100 },
  { title: "Desk", key: "deskName", width: 220 },
  { title: "Desk Type", key: "deskType", width: 100 },
  { title: "Start", key: "startTime", width: 150 },
  { title: "End", key: "endTime", width: 150 },
  { title: "Duration", key: "duration", width: 110 },
  { title: "Status", key: "status", width: 120 },
  { title: "", key: "actions", width: 56, align: "end", sortable: false },
];

const filteredBookings = computed(() => {
  let filtered = bookings.value;

  if (statusFilter.value !== "ALL") {
    filtered = filtered.filter((b) => b.status === statusFilter.value);
  }

  if (typeFilter.value !== "ALL") {
    filtered = filtered.filter((b) => b.desk?.type === typeFilter.value);
  }

  if (searchQuery.value) {
    const search = searchQuery.value.toLowerCase();
    filtered = filtered.filter(
      (b) =>
        b.desk?.displayName?.toLowerCase().includes(search) ||
        b.user_id?.toString().includes(search)
    );
  }

  return filtered.map((b) => ({
    id: b.bookingId ?? "—",
    deskId: b.desk?.id ?? null,
    userId: b.user_id ?? null,
    deskName: b.desk?.displayName ?? "N/A",
    zoneId: b.desk?.zoneDto?.id ?? "0",
    zoneName: b.desk?.zoneDto?.zoneName ?? "N/A",
    deskType: b.desk?.type ?? "N/A",
    startTime: b.startTime,
    endTime: b.endTime,
    duration: formatDuration(b.startTime, b.endTime),
    status: b.status ?? "—",
  }));
});

function resetFilters() {
  statusFilter.value = "ALL";
  typeFilter.value = "ALL";
  searchQuery.value = "";
}

const fetchBookings = async () => {
  try {
    loading.value = true;
    error.value = null;
    const params =
      statusFilter.value !== "ALL" ? { status: statusFilter.value } : {};
    const response = await api.get("/booking/all", { params });
    bookings.value = response.data;
  } catch (err) {
    console.error("Error fetching bookings:", err);
    error.value =
      err.response?.data?.message || err.message || "Failed to fetch bookings";
  } finally {
    loading.value = false;
  }
};

function getColor(status: string): string {
  const statusMap: Record<string, string> = {
    ACTIVE: "#10b981",
    SHARED: "#10b981",
    COMPLETED: "#0b4df5",
    CONFIRMED: "#0b4df5",
    ASSIGNED: "#0b4df5",
    CANCELLED: "#ef4444",
    DEACTIVATED: "#ef4444",
    UNAVAILABLE: "#737373FF",
  };
  return statusMap[status?.toUpperCase()] || "#737373";
}

function toDatetimeLocalValue(dateStr) {
  const date = new Date(dateStr);
  const pad = (n) => n.toString().padStart(2, "0");
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(
    date.getDate()
  )}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
}

function formatDate(dateStr) {
  return dateStr
    ? new Date(dateStr).toLocaleDateString("en-US", {
        month: "short",
        day: "numeric",
      })
    : "—";
}

function formatTime(dateStr) {
  return dateStr
    ? new Date(dateStr).toLocaleTimeString([], {
        hour: "2-digit",
        minute: "2-digit",
      })
    : "—";
}

function formatDuration(startStr, endStr) {
  if (!startStr || !endStr) return "—";
  const diff = new Date(endStr) - new Date(startStr);
  const min = Math.max(0, Math.round(diff / 60000));
  const h = Math.floor(min / 60);
  const m = min % 60;
  return h > 0 ? `${h}h ${m.toString().padStart(2, "0")}m` : `${m}m`;
}

function viewBooking(item) {
  selectedBooking.value = item;
  showViewModal.value = true;
}

function editBooking(item) {
  selectedBooking.value = {
    id: item.id,
    userId: item.userId,
    deskId: item.deskId,
    startTime: toDatetimeLocalValue(item.startTime),
    endTime: toDatetimeLocalValue(item.endTime),
    status: item.status,
  };
  showEditModal.value = true;
}

async function saveBooking(updatedData) {
  try {
    const bookingUpdateCommand = {
      userId: selectedBooking.value.userId,
      deskId: updatedData.deskId,
      startTime: updatedData.startTime,
      endTime: updatedData.endTime,
      status: updatedData.status,
    };

    await api.patch(
      `/admin/edit/booking/${selectedBooking.value.id}`,
      bookingUpdateCommand
    );
    await fetchBookings();
    showEditModal.value = false;
  } catch (err) {
    console.error("Error updating booking:", err);
    error.value = err.response?.data?.message || "Failed to update booking";
  }
}

async function cancelBooking(item) {
  if (!confirm(`Cancel booking #${item.id}?`)) return;

  try {
    cancellingId.value = item.id;
    await api.patch(`/admin/cancel/booking/${item.id}`);
    await fetchBookings();
  } catch (err) {
    console.error("Cancel booking failed:", err);
    error.value =
      err.response?.data?.message || `Failed to cancel booking #${item.id}`;
  } finally {
    cancellingId.value = null;
  }
}

onMounted(() => {
  const initial = String(route.query?.status || "ALL").toUpperCase();
  if (!["ALL", "ACTIVE", "CANCELLED", "CONFIRMED"].includes(initial)) {
    router.replace({ query: { ...route.query, status: undefined } });
    statusFilter.value = "ALL";
  } else {
    statusFilter.value = initial;
  }
  fetchBookings();
});

watch(statusFilter, (val) => {
  const nextQuery = { ...route.query, status: val === "ALL" ? undefined : val };
  router.replace({ query: nextQuery });
  fetchBookings();
});
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap");

* {
  font-family: "Inter", -apple-system, BlinkMacSystemFont, "Segoe UI",
    sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

.admin-bookings {
  padding: 28px;
  background: #fafafa;
  min-height: 100vh;
}

.admin-card {
  background: #ffffff;
  border: 1px solid #e5e5e5;
  border-radius: 20px;
  padding: 28px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
}

.admin-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 2px solid #f5f5f5;
}

.title-wrap {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.workspace-label {
  font-size: 11px;
  font-weight: 700;
  color: #737373;
  letter-spacing: 1.5px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.title {
  font-size: 28px;
  font-weight: 800;
  color: #171717;
  margin: 0;
  letter-spacing: -0.5px;
  line-height: 1;
}

.sub {
  font-size: 13px;
  font-weight: 600;
  color: #737373;
  letter-spacing: 0.3px;
}

.count-chip {
  font-weight: 700;
  font-size: 13px;
  color: #fff !important;
  letter-spacing: 0.3px;
  padding: 0 12px !important;
}

.reset-btn {
  font-weight: 600 !important;
  text-transform: none !important;
  letter-spacing: 0.3px;
  border-radius: 12px !important;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08) !important;
  transition: all 0.3s ease;
}

.reset-btn:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12) !important;
  transform: translateY(-1px);
}

.search-field :deep(.v-field),
.filter-select :deep(.v-field) {
  border-radius: 12px !important;
  border: 2px solid #e5e5e5 !important;
  font-weight: 600;
}

.search-field :deep(.v-field:hover),
.filter-select :deep(.v-field:hover) {
  border-color: #a3a3a3 !important;
}

.search-field :deep(.v-field--focused),
.filter-select :deep(.v-field--focused) {
  border-color: #171717 !important;
  box-shadow: 0 0 0 3px rgba(0, 0, 0, 0.05);
}

.loading-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
}

.loading-text {
  font-size: 15px;
  font-weight: 600;
  color: #737373;
  letter-spacing: 0.3px;
}

.elevated-table {
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid #f5f5f5;
}

.elevated-table :deep(table) {
  border-collapse: separate !important;
  border-spacing: 0;
}

.elevated-table :deep(thead th) {
  position: sticky;
  top: 0;
  background: #fafafa !important;
  z-index: 2;
  font-weight: 700 !important;
  color: #171717 !important;
  font-size: 13px !important;
  letter-spacing: 0.3px;
  border-bottom: 2px solid #f5f5f5 !important;
}

.elevated-table :deep(tbody tr) {
  transition: all 0.2s ease;
}

.elevated-table :deep(tbody tr:hover) {
  background: #fafafa !important;
  transform: scale(1.001);
}

.elevated-table :deep(tbody td) {
  font-size: 14px;
  border-bottom: 1px solid #f5f5f5 !important;
}

.cell-main {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.desk-name {
  font-weight: 700;
  color: #171717;
  font-size: 14px;
  letter-spacing: -0.2px;
}

.desk-meta {
  font-weight: 600;
  color: #737373;
  font-size: 12px;
  letter-spacing: 0.3px;
}

.cell-strong {
  font-weight: 700;
  color: #171717;
  letter-spacing: -0.2px;
}

.muted {
  color: #737373;
  font-weight: 600;
  font-size: 13px;
}

.nowrap {
  white-space: nowrap;
  font-weight: 600;
  color: #171717;
  font-size: 13px;
}

.status-chip {
  font-weight: 700 !important;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  font-size: 11px !important;
}

.action-btn {
  border-radius: 8px !important;
  transition: all 0.2s ease;
}

.action-btn:hover {
  background-color: #f5f5f5 !important;
  transform: rotate(90deg);
}

.action-menu {
  border-radius: 12px !important;
  border: 1px solid #e5e5e5 !important;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12) !important;
}

.action-menu :deep(.v-list-item) {
  font-weight: 600;
  letter-spacing: 0.3px;
}

.empty-state {
  text-align: center;
  padding: 60px 0;
  color: #737373;
}

.empty-title {
  font-weight: 700;
  color: #171717;
  font-size: 18px;
  margin-bottom: 8px;
  letter-spacing: -0.3px;
}

.empty-sub {
  font-weight: 600;
  color: #737373;
  font-size: 14px;
  letter-spacing: 0.3px;
}
</style>
