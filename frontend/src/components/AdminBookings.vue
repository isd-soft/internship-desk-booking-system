<template>
  <div class="bookings-container">
    <div class="bookings-card">
      <div class="bookings-header">
        <div class="header-title-section">
          <div class="header-label">ADMIN PANEL</div>
          <h2 class="header-title">All Bookings</h2>
          <span class="header-subtitle"
            >{{ filteredBookings.length }} total bookings</span
          >
        </div>
        <div class="header-controls">
          <v-text-field
            v-model="searchQuery"
            density="compact"
            variant="outlined"
            hide-details
            clearable
            placeholder="Search bookings..."
            style="max-width: 250px"
            :disabled="loading"
            class="control-input"
          />

          <v-select
            v-model="statusFilter"
            :items="statusBookingOptions"
            item-title="title"
            item-value="value"
            density="compact"
            variant="outlined"
            style="max-width: 180px"
            :disabled="loading"
            :clearable="false"
            hide-details
            label="Filter by status"
            class="control-select"
          />

          <v-select
            v-model="typeFilter"
            :items="typeDeskOptions"
            item-title="title"
            item-value="value"
            density="compact"
            variant="outlined"
            style="max-width: 180px"
            :disabled="loading"
            :clearable="false"
            hide-details
            label="Filter by desk type"
            class="control-select"
          />

          <v-btn
            color="#171717"
            variant="flat"
            @click="resetFilters"
            class="control-button"
          >
            Reset Filters
          </v-btn>
        </div>
      </div>

      <v-alert
        v-if="error"
        type="error"
        variant="tonal"
        class="mb-4"
        density="compact"
        closable
      >
        {{ error }}
      </v-alert>

      <div v-if="loading" class="loading-container">
        <v-progress-circular
          indeterminate
          size="48"
          width="4"
          color="#171717"
        />
        <p class="loading-message mt-3">Loading bookings…</p>
      </div>

      <template v-else>
        <v-data-table
          :headers="headers"
          :items="filteredBookings"
          item-key="id"
          density="compact"
          class="bookings-table"
          :items-per-page="15"
          fixed-header
          height="70vh"
        >
          <template #item.userId="{ item }">
            <span class="table-text-bold">{{ item.userId ?? "—" }}</span>
          </template>

          <template #item.deskName="{ item }">
            <div class="table-cell-stacked">
              <div class="table-text-bold">{{ item.deskName }}</div>
              <div class="table-text-meta">
                {{ item.zoneAbv }} • {{ item.zoneId }}
              </div>
            </div>
          </template>

          <template #item.deskType="{ item }">
            <v-chip
              size="x-small"
              :color="getColor(item.deskType)"
              variant="flat"
              class="table-chip"
            >
              {{ item.deskType }}
            </v-chip>
          </template>

          <template #item.startTime="{ item }">
            <div class="table-text-primary">
              {{ formatDate(item.startTime) }}
            </div>
            <div class="table-text-secondary">
              {{ formatTime(item.startTime) }}
            </div>
          </template>

          <template #item.endTime="{ item }">
            <div class="table-text-primary">{{ formatDate(item.endTime) }}</div>
            <div class="table-text-secondary">
              {{ formatTime(item.endTime) }}
            </div>
          </template>

          <template #item.duration="{ item }">
            <span class="table-text-bold">{{ item.duration }}</span>
          </template>

          <template #item.status="{ item }">
            <v-chip
              size="x-small"
              :color="getColor(item.status)"
              variant="flat"
              class="table-chip"
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
                  class="table-action-button"
                >
                  <v-icon>mdi-dots-vertical</v-icon>
                </v-btn>
              </template>
              <v-list density="compact" class="table-action-menu">
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
                  @click="CancelBooking(item)"
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
            <div class="table-empty-state">
              <v-icon size="48" color="#a3a3a3" class="mb-3"
                >mdi-calendar-blank</v-icon
              >
              <div class="table-empty-title">No bookings found</div>
              <div class="table-empty-subtitle">
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
          :error="error"
          @close="showEditModal = false"
          @save="handleSaveBooking"
        />
        <v-dialog v-model="showCancelDialog" max-width="500">
          <v-card class="delete-dialog">
            <v-card-title class="dialog-title">
              <v-icon color="error" class="mr-2">mdi-alert-circle</v-icon>
              Cancel Booking
            </v-card-title>
            <v-card-text class="dialog-text">
              <p class="mb-4">
                Are you sure you want to cancel booking <strong>#{{ selectedBooking?.id }}</strong> for
                <strong>{{ selectedBooking?.deskName }}</strong>?
              </p>
              <v-textarea
                  v-model="cancelReason"
                  label="Reason for cancellation"
                  placeholder="Please provide a reason for canceling this booking..."
                  variant="outlined"
                  rows="3"
                  :error-messages="cancelReasonError"
                  hide-details="auto"
                  class="cancel-reason-input"
              />
            </v-card-text>
            <v-card-actions class="dialog-actions">
              <v-spacer></v-spacer>
              <v-btn
                  variant="text"
                  @click="closeCancelDialog"
                  :disabled="cancellingId !== null"
              >
                Cancel
              </v-btn>
              <v-btn
                  color="error"
                  variant="flat"
                  @click="confirmCancelBooking"
                  :loading="cancellingId !== null"
                  :disabled="!cancelReason || cancelReason.trim().length === 0"
              >
                Yes, Cancel Booking
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-dialog>
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
import {fetchDeskTypeEnum,fetchBookingStatus, statusBookingOptions, typeDeskOptions,fetchColors,getColor} from "@/utils/useEnums";
import { formatDateTimeLocal, formatTime, formatDate, calculateDuration} from "@/utils/useFormatDate";


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
const showCancelDialog = ref(false);
const successMessage = ref(null);
const cancelReason = ref('');
const cancelReasonError = ref('');
const statusColorMap = ref<Record<string, string>>({});
const statusFilter = ref(String(route.query?.status || "ALL").toUpperCase());
const typeFilter = ref(String(route.query?.type || "ALL").toUpperCase());

const headers = [
  { title: "ID", key: "id", width: 80, align: "start", sortable: true },
  {
    title: "Desk Name",
    key: "deskName",
    width: 150,
    align: "start",
    sortable: true,
  },
  {
    title: "Zone",
    key: "zoneName",
    width: 150,
    align: "start",
    sortable: true,
  },
  {
    title: "Type",
    key: "deskType",
    width: 100,
    align: "center",
    sortable: false,
  },
  {
    title: "Start Time",
    key: "startTime",
    width: 160,
    align: "center",
    sortable: false,
  },
  {
    title: "End Time",
    key: "endTime",
    width: 160,
    align: "center",
    sortable: false,
  },
  {
    title: "Duration",
    key: "duration",
    width: 120,
    align: "center",
    sortable: false,
  },
  {
    title: "Status",
    key: "status",
    width: 120,
    align: "center",
    sortable: false,
  },
  {
    title: "Actions",
    key: "actions",
    width: 120,
    align: "center",
    sortable: false,
  },
] as const;

const filteredBookings = computed(() => {
  return applyFilters(bookings.value).map(transformBookingData);
});

function applyFilters(bookingsList: any[]) {
  let filtered = bookingsList;

  if (statusFilter.value !== "ALL") {
    filtered = filtered.filter((b) => b.status === statusFilter.value);
  }

  if (typeFilter.value !== "ALL") {
    filtered = filtered.filter((b) => b.desk?.type === typeFilter.value);
  }

  if (searchQuery.value) {
    filtered = applySearchFilter(filtered, searchQuery.value);
  }

  return filtered;
}

function applySearchFilter(bookingsList: any[], query: string) {
  const search = query.toLowerCase();
  return bookingsList.filter(
    (b) =>
      b.desk?.displayName?.toLowerCase().includes(search) ||
      b.user_id?.toString().includes(search)
  );
}

function transformBookingData(booking: any) {
  return {
    id: booking.bookingId ?? "—",
    deskId: booking.desk?.id ?? null,
    userId: booking.userId ?? null,
    deskName: booking.desk ? booking.desk.displayName : "[Deleted Desk]",
    zoneId: booking.desk?.zoneDto?.id ?? "0",
    zoneName: booking.desk?.zoneDto?.zoneName ?? "N/A",
    zoneAbv: booking.desk?.zoneDto?.zoneAbv ?? "N/A",
    deskType: booking.desk?.type ?? "N/A",
    startTime: booking.startTime,
    endTime: booking.endTime,
    duration: calculateDuration(booking.startTime, booking.endTime),
    status: booking.status ?? "—",
  };
}

async function fetchBookings() {
  try {
    loading.value = true;
    error.value = null;
    const params =
      statusFilter.value !== "ALL" ? { status: statusFilter.value } : {};
    const response = await api.get("/booking/all", { params });
    bookings.value = response.data;
  } catch (err: any) {
    console.error("Error fetching bookings:", err);
    error.value =
      err.response?.data?.message || err.message || "Failed to fetch bookings";
  } finally {
    loading.value = false;
  }
}



async function updateBooking(bookingId: number, updateData: any) {
  await api.patch(`/admin/edit/booking/${bookingId}`, updateData);
}

function resetFilters() {
  statusFilter.value = "ALL";
  typeFilter.value = "ALL";
  searchQuery.value = "";
}

function viewBooking(item: any) {
  selectedBooking.value = item;
  showViewModal.value = true;
}

function editBooking(item: any) {
  selectedBooking.value = {
    id: item.id,
    userId: item.userId,
    deskId: item.deskId,
    startTime: formatDateTimeLocal(item.startTime),
    endTime: formatDateTimeLocal(item.endTime),
    status: item.status,
  };
  showEditModal.value = true;
}

async function handleSaveBooking(updatedData: any) {
  try {
    const bookingUpdateCommand = {
      userId: selectedBooking.value.userId,
      deskId: updatedData.deskId,
      startTime: updatedData.startTime,
      endTime: updatedData.endTime,
      status: updatedData.status,
    };

    await updateBooking(selectedBooking.value.id, bookingUpdateCommand);
    await fetchBookings();
    showEditModal.value = false;
  } catch (err: any) {
    console.error("Error updating booking:", err);
    error.value = err.response?.data?.message || "Failed to update booking";
  }
}
function CancelBooking(item) {
  selectedBooking.value = item;
  cancelReason.value = '';
  cancelReasonError.value = '';
  showCancelDialog.value = true;
}

function closeCancelDialog() {
  showCancelDialog.value = false;
  cancelReason.value = '';
  cancelReasonError.value = '';
}

async function confirmCancelBooking() {
  if (!selectedBooking.value?.id) return;

  if (!cancelReason.value || cancelReason.value.trim().length === 0) {
    cancelReasonError.value = 'Please provide a reason for cancellation';
    return;
  }

  try {
    cancellingId.value = selectedBooking.value.id;
    error.value = null;
    successMessage.value = null;

    await api.patch(`/admin/cancel/booking/${selectedBooking.value.id}`, {
      reason: cancelReason.value.trim()
    });

    successMessage.value = `Booking #${selectedBooking.value.id} cancelled successfully`;
    closeCancelDialog();
    selectedBooking.value = null;

    await fetchBookings();
  } catch (err) {
    console.error('Error cancelling booking:', err);
    error.value = err.response?.data?.message || err.message || 'Failed to cancel booking';
    showCancelDialog.value = false;
  } finally {
    cancellingId.value = null;
  }
}

function updateRouteQuery(status: string) {
  const nextQuery = {
    ...route.query,
    status: status === "ALL" ? undefined : status,
  };
  router.replace({ query: nextQuery });
}

onMounted(async () => {
  await Promise.all([
    fetchDeskTypeEnum(),
    fetchColors(),
    fetchBookings(),
    fetchBookingStatus()
  ]);
});

watch(statusFilter, (val) => {
  updateRouteQuery(val);
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

.bookings-container {
  padding: 28px;
  background: #fafafa;
  min-height: 100vh;
}

.bookings-card {
  background: #ffffff;
  border: 1px solid #e5e5e5;
  border-radius: 20px;
  padding: 28px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
}

.bookings-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 2px solid #f5f5f5;
}

.header-title-section {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.header-label {
  font-size: 11px;
  font-weight: 700;
  color: #737373;
  letter-spacing: 1.5px;
}

.header-title {
  font-size: 28px;
  font-weight: 800;
  color: #171717;
  margin: 0;
  letter-spacing: -0.5px;
  line-height: 1;
}

.header-subtitle {
  font-size: 13px;
  font-weight: 600;
  color: #737373;
  letter-spacing: 0.3px;
}

.header-controls {
  display: flex;
  align-items: center;
  gap: 12px;
}

.control-input :deep(.v-field),
.control-select :deep(.v-field) {
  border-radius: 12px !important;
  border: 2px solid #e5e5e5 !important;
  font-weight: 600;
}

.control-input :deep(.v-field:hover),
.control-select :deep(.v-field:hover) {
  border-color: #a3a3a3 !important;
}

.control-input :deep(.v-field--focused),
.control-select :deep(.v-field--focused) {
  border-color: #171717 !important;
  box-shadow: 0 0 0 3px rgba(0, 0, 0, 0.05);
}

.control-button {
  font-weight: 600 !important;
  text-transform: none !important;
  letter-spacing: 0.3px;
  border-radius: 12px !important;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08) !important;
  transition: all 0.3s ease;
}

.control-button:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12) !important;
  transform: translateY(-1px);
}

.control-chip {
  font-weight: 700;
  font-size: 13px;
  color: #fff !important;
  letter-spacing: 0.3px;
  padding: 0 12px !important;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
}

.loading-message {
  font-size: 15px;
  font-weight: 600;
  color: #737373;
  letter-spacing: 0.3px;
}
/* Delete Dialog */
.delete-dialog {
  border-radius: 16px !important;
}

.dialog-title {
  font-weight: 700;
  font-size: 20px;
  color: #171717;
  padding: 24px 24px 16px;
}

.dialog-text {
  font-size: 15px;
  color: #525252;
  padding: 0 24px 16px;
  line-height: 1.6;
}

.dialog-actions {
  padding: 16px 24px 24px;
}

.bookings-table {
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid #f5f5f5;
}

.bookings-table :deep(table) {
  border-collapse: separate !important;
  border-spacing: 0;
}

.bookings-table :deep(thead th) {
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

.bookings-table :deep(tbody tr) {
  transition: all 0.2s ease;
}

.bookings-table :deep(tbody tr:hover) {
  background: #fafafa !important;
}

.bookings-table :deep(tbody td) {
  font-size: 14px;
  border-bottom: 1px solid #f5f5f5 !important;
}

.table-cell-stacked {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.table-text-bold {
  font-weight: 700;
  color: #171717;
  letter-spacing: -0.2px;
}

.table-text-meta {
  font-weight: 600;
  color: #737373;
  font-size: 12px;
  letter-spacing: 0.3px;
}

.table-text-primary {
  white-space: nowrap;
  font-weight: 600;
  color: #171717;
  font-size: 13px;
}

.table-text-secondary {
  color: #737373;
  font-weight: 600;
  font-size: 13px;
}

.table-chip {
  font-weight: 700 !important;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  font-size: 11px !important;
}

.table-action-button {
  border-radius: 8px !important;
  transition: all 0.2s ease;
}

.table-action-button:hover {
  background-color: #f5f5f5 !important;
  transform: rotate(90deg);
}

.table-action-menu {
  border-radius: 12px !important;
  border: 1px solid #e5e5e5 !important;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12) !important;
}

.table-action-menu :deep(.v-list-item) {
  font-weight: 600;
  letter-spacing: 0.3px;
}

.table-empty-state {
  text-align: center;
  padding: 60px 0;
  color: #737373;
}

.table-empty-title {
  font-weight: 700;
  color: #171717;
  font-size: 18px;
  margin-bottom: 8px;
  letter-spacing: -0.3px;
}

.table-empty-subtitle {
  font-weight: 600;
  color: #737373;
  font-size: 14px;
  letter-spacing: 0.3px;
}
</style>
