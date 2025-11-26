<template>
  <div class="admin-desk">
    <div class="admin-card">
      <div class="admin-header">
        <div class="title-wrap">
          <div class="workspace-label">ADMIN PANEL</div>
          <h2 class="title">Desk Management</h2>
          <span class="sub">{{ filteredDesks.length }} desks</span>
        </div>
        <div class="header-actions">
          <v-text-field
              v-model="searchQuery"
              density="compact"
              variant="outlined"
              hide-details
              clearable
              placeholder="Search desks..."
              :disabled="loading"
              class="search-field"
          />

          <v-select
              v-model="statusFilter"
              :items="statusDeskOptions"
              item-title="title"
              item-value="value"
              density="compact"
              variant="outlined"
              :disabled="loading"
              :clearable="false"
              hide-details
              label="Filter by status"
              class="filter-select"
          />

          <v-select
              v-model="typeFilter"
              :items="typeDeskOptions"
              item-title="title"
              item-value="value"
              density="compact"
              variant="outlined"
              :disabled="loading"
              :clearable="false"
              hide-details
              label="Filter by type"
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

          <v-chip size="small" color="#171717" variant="flat" class="count-chip">{{ filteredDesks.length }}</v-chip>
        </div>
      </div>

      <v-alert
          v-if="error"
          type="error"
          variant="tonal"
          class="mb-4"
          density="compact"
          closable
          @click:close="error = null"
      >
        {{ error }}
      </v-alert>

      <v-alert
          v-if="successMessage"
          type="success"
          variant="tonal"
          class="mb-4"
          density="compact"
          closable
          @click:close="successMessage = null"
      >
        {{ successMessage }}
      </v-alert>

      <div v-if="loading" class="loading-wrap">
        <v-progress-circular indeterminate size="48" width="4" color="#171717" />
        <p class="loading-text mt-3">Loading desks…</p>
      </div>

      <template v-else>
        <v-data-table
            :headers="headers"
            :items="filteredDesks"
            item-key="id"
            density="compact"
            class="elevated-table"
            :items-per-page="15"
            fixed-header
            height="70vh"
        >
          <template #item.name="{ item }">
            <div class="cell-main">
              <div class="desk-name">{{ item.name }}</div>
            </div>
          </template>

          <template #item.zoneId="{ item }">
            <div class="cell-main">
              <div class="zone-id">{{ item.zoneId }}</div>
            </div>
          </template>

          <template #item.zoneName="{ item }">
            <div class="cell-main">
              <div class="zone-name">{{ item.zoneName }}</div>
            </div>
          </template>

          <template #item.type="{ item }">
            <v-chip size="x-small" :color="getColor(item.type)" variant="flat" class="status-chip">
              {{ item.type }}
            </v-chip>
          </template>

          <template #item.status="{ item }">
            <v-chip size="x-small" :color="getColor(item.status)" variant="flat" class="status-chip">
              {{ item.status }}
            </v-chip>
          </template>

          <template #item.isTemporarilyAvailable="{ item }">
            <v-chip size="x-small" :color="item.isTemporarilyAvailable ? '#10b981' : '#ef4444'" variant="flat" class="status-chip">
              {{ item.isTemporarilyAvailable ? 'Yes' : 'No' }}
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
                    aria-label="Row actions"
                    :loading="deletingId === item.id"
                    :disabled="deletingId === item.id"
                    class="action-btn"
                >
                  <v-icon>mdi-dots-vertical</v-icon>
                </v-btn>
              </template>
              <v-list density="compact" class="action-menu">
                <v-list-item
                    @click="onView(item)"
                    prepend-icon="mdi-eye"
                    title="View">
                </v-list-item>
                <v-list-item
                    @click="onEdit(item)"
                    prepend-icon="mdi-pencil"
                    title="Edit">
                </v-list-item>
                <v-list-item
                    @click="onDeleteClick(item)"
                    prepend-icon="mdi-delete"
                    title="Delete desk"
                    class="delete-item"
                    :disabled="deletingId === item.id"
                ></v-list-item>
              </v-list>
            </v-menu>
          </template>

          <template #no-data>
            <div class="empty-state">
              <v-icon size="48" color="#171717" class="mb-3">mdi-desk</v-icon>
              <div class="empty-title">No desks found</div>
              <div class="empty-sub">Create a new desk to get started.</div>
            </div>
          </template>
        </v-data-table>

        <DeskViewModal
            v-if="showViewModal"
            :show="showViewModal"
            v-model="showViewModal"
            :desk="selectedDesk"
        />

        <DeskEditModal
            v-if="showEditModal"
            v-model="showEditModal"
            :desk="selectedDesk"
            :error="error"
            @close="showEditModal = false"
            @save="handleSave"
        />
      </template>
    </div>

    <!-- Delete Desk Dialog -->
    <v-dialog v-model="showDeleteDialog" max-width="550" persistent>
      <v-card class="delete-dialog">
        <v-card-title class="dialog-title">
          <v-icon color="error" class="mr-2">mdi-delete-alert</v-icon>
          Delete Desk
        </v-card-title>

        <v-card-text class="dialog-text">
          <div class="warning-box">
            <v-icon color="warning" class="mr-2">mdi-alert</v-icon>
            <span>You are about to delete desk <strong>{{ selectedDesk?.name }}</strong></span>
          </div>

          <p class="mt-4 mb-2">Please provide a reason for deletion:</p>

          <v-textarea
              v-model="deletionReason"
              variant="outlined"
              placeholder="e.g., Desk damaged, Office renovation, Equipment malfunction..."
              rows="4"
              :error-messages="deleteErrorMessage"
              :disabled="isDeleting"
              counter="500"
              maxlength="500"
              class="reason-textarea"
              auto-grow
          />

          <v-alert
              v-if="selectedDesk?.hasActiveBookings"
              type="warning"
              variant="tonal"
              density="compact"
              class="mt-2"
          >
            <template #prepend>
              <v-icon>mdi-calendar-alert</v-icon>
            </template>
            This desk has active or scheduled bookings. All bookings will be automatically cancelled.
          </v-alert>
        </v-card-text>

        <v-card-actions class="dialog-actions">
          <v-spacer></v-spacer>
          <v-btn
              variant="text"
              @click="handleDeleteCancel"
              :disabled="isDeleting"
          >
            Cancel
          </v-btn>
          <v-btn
              color="error"
              variant="flat"
              @click="handleDeleteConfirm"
              :loading="isDeleting"
              :disabled="!deletionReason.trim()"
          >
            Delete Desk
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute } from "vue-router";
import api from '../plugins/axios';
import DeskEditModal from "../components/AdminDashboard/DeskEditModal.vue";
import DeskViewModal from "../components/AdminDashboard/DeskViewModal.vue";
import {
  fetchDeskTypeEnum,
  fetchDeskStatusEnum,
  fetchColors,
  getColor,
  statusDeskOptions,
  typeDeskOptions
} from "@/utils/useEnums";

const desks = ref([]);
const loading = ref(false);
const error = ref(null);
const successMessage = ref(null);
const searchQuery = ref('');
const route = useRoute();

const showViewModal = ref(false);
const showEditModal = ref(false);
const showDeleteDialog = ref(false);
const selectedDesk = ref(null);
const deletingId = ref(null);

const deletionReason = ref('');
const deleteErrorMessage = ref('');
const isDeleting = ref(false);

const statusFilter = ref(String(route.query?.deskStatus || 'ALL').toUpperCase());
const typeFilter = ref(String(route.query?.type || 'ALL').toUpperCase());

const headers = [
  { title: 'ID', key: 'id', width: 80, align: 'start' },
  { title: 'Desk', key: 'name', width: 150 },
  { title: 'Zone ID', key: 'zoneId', width: 100, align: 'center' },
  { title: 'Zone Name', key: 'zoneName', width: 140, align: 'center' },
  { title: 'Type', key: 'type', width: 120, align: 'center' },
  { title: 'Status', key: 'status', width: 120, align: 'center' },
  { title: 'Temp Available', key: 'isTemporarilyAvailable', width: 130, align: 'center' },
  { title: '', key: 'actions', width: 56, align: 'end', sortable: false }
] as const;

const filteredDesks = computed(() => {
  let filtered = desks.value || [];

  if (statusFilter.value && statusFilter.value !== 'ALL') {
    filtered = filtered.filter((d) => {
      const deskStatus = d.deskStatus || d.status;
      return deskStatus === statusFilter.value;
    });
  }

  if (typeFilter.value && typeFilter.value !== 'ALL') {
    filtered = filtered.filter((d) => d.type === typeFilter.value);
  }

  if (searchQuery.value) {
    const search = searchQuery.value.toLowerCase();
    filtered = filtered.filter((d) =>
        d.displayName?.toLowerCase().includes(search) ||
        d.id?.toString().includes(search) ||
        d.zoneDto?.zoneName?.toLowerCase().includes(search)
    );
  }

  return filtered.map((d) => ({
    id: d.id ?? '—',
    name: d.displayName ?? 'N/A',
    zoneId: d.zoneDto?.id ?? '0',
    zoneName: d.zoneDto?.zoneName ?? 'N/A',
    type: d.type ?? 'N/A',
    status: d.deskStatus ?? 'N/A',
    isTemporarilyAvailable: d.isTemporarilyAvailable ?? false,
    tempFrom: d.temporaryAvailableFrom,
    tempUntil: d.temporaryAvailableUntil,
    hasActiveBookings: d.hasActiveBookings || false,
    currentX: d.currentX,
    currentY: d.currentY,
    baseX: d.baseX,
    baseY: d.baseY,
    rawData: d
  }));
});

const fetchDesks = async () => {
  try {
    loading.value = true;
    error.value = null;
    const response = await api.get('/admin/desks');
    desks.value = response.data;
  } catch (err) {
    console.error('Error fetching desks:', err);
    error.value = err.response?.data?.message || err.message || 'Failed to fetch desks';
  } finally {
    loading.value = false;
  }
};

function resetFilters() {
  statusFilter.value = 'ALL';
  typeFilter.value = 'ALL';
  searchQuery.value = '';
}

function onView(item) {
  selectedDesk.value = item;
  showViewModal.value = true;
}

function onEdit(item) {
  selectedDesk.value = item;
  showEditModal.value = true;
}

async function handleSave(updatedDesk) {
  try {
    error.value = null;
    successMessage.value = null;

    await api.patch(`/admin/edit/desk/${selectedDesk.value.id}`, updatedDesk);

    successMessage.value = `Desk "${selectedDesk.value.name}" updated successfully`;
    showEditModal.value = false;
    selectedDesk.value = null;

    await fetchDesks();
  } catch (err) {
    console.error('Error updating desk:', err);
    error.value = err.response?.data?.message || err.message || 'Failed to update desk';
  }
}

function onDeleteClick(item) {
  selectedDesk.value = item;
  deletionReason.value = '';
  deleteErrorMessage.value = '';
  showDeleteDialog.value = true;
}

function handleDeleteCancel() {
  if (!isDeleting.value) {
    showDeleteDialog.value = false;
    selectedDesk.value = null;
    deletionReason.value = '';
    deleteErrorMessage.value = '';
  }
}

async function handleDeleteConfirm() {
  const reason = deletionReason.value.trim();

  if (!reason) {
    deleteErrorMessage.value = 'Please provide a reason for deletion';
    return;
  }

  if (reason.length < 5) {
    deleteErrorMessage.value = 'Reason must be at least 5 characters long';
    return;
  }

  if (!selectedDesk.value?.id) return;

  try {
    isDeleting.value = true;
    deletingId.value = selectedDesk.value.id;
    deleteErrorMessage.value = '';
    error.value = null;

    await api.delete(`/admin/delete/desk/${selectedDesk.value.id}`, {
      params: { reason }
    });

    successMessage.value = `Desk "${selectedDesk.value.name}" deleted successfully`;
    showDeleteDialog.value = false;
    selectedDesk.value = null;
    deletionReason.value = '';

    await fetchDesks();
  } catch (err) {
    console.error('Error deleting desk:', err);
    error.value = err.response?.data?.message || err.message || 'Failed to delete desk';
    showDeleteDialog.value = false;
  } finally {
    isDeleting.value = false;
    deletingId.value = null;
  }
}

onMounted(async () => {
  const initialPromises = [fetchDesks(), fetchColors()];
  if (!statusDeskOptions.value || statusDeskOptions.value.length <= 1) {
    initialPromises.push(fetchDeskStatusEnum());
  }

  if (!typeDeskOptions.value || typeDeskOptions.value.length <= 1) {
    initialPromises.push(fetchDeskTypeEnum());
  }

  await Promise.all(initialPromises);
});
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap");

* {
  font-family: "Inter", -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

.admin-desk {
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
  text-transform: uppercase;
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

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.search-field {
  min-width: 180px;
  width: 250px;
  max-width: 300px;
}

.search-field :deep(.v-field) {
  border-radius: 12px !important;
  border: 2px solid #e5e5e5 !important;
  font-weight: 600;
  transition: all 0.2s ease;
}

.search-field :deep(.v-field:hover) {
  border-color: #a3a3a3 !important;
}

.search-field :deep(.v-field--focused) {
  border-color: #171717 !important;
  box-shadow: 0 0 0 3px rgba(0, 0, 0, 0.05);
}

.search-field :deep(input::placeholder) {
  color: #a3a3a3;
  font-weight: 500;
}

.filter-select {
  min-width: 150px;
  width: 200px;
  max-width: 220px;
}

.filter-select :deep(.v-field) {
  border-radius: 12px !important;
  border: 2px solid #e5e5e5 !important;
  font-weight: 600;
  transition: all 0.2s ease;
}

.filter-select :deep(.v-field:hover) {
  border-color: #a3a3a3 !important;
}

.filter-select :deep(.v-field--focused) {
  border-color: #171717 !important;
  box-shadow: 0 0 0 3px rgba(0, 0, 0, 0.05);
}

.reset-btn {
  font-weight: 600 !important;
  text-transform: none !important;
  letter-spacing: 0.3px;
  border-radius: 12px !important;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08) !important;
  transition: all 0.3s ease;
  padding: 0 20px !important;
}

.reset-btn:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12) !important;
  transform: translateY(-1px);
}

.count-chip {
  font-weight: 700;
  font-size: 13px;
  color: #fff !important;
  letter-spacing: 0.3px;
  padding: 0 12px !important;
  min-width: 40px;
  justify-content: center;
}

.loading-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 0;
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
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02);
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
  letter-spacing: 0.5px;
  text-transform: uppercase;
  border-bottom: 2px solid #e5e5e5 !important;
  padding: 16px 12px !important;
}

.elevated-table :deep(tbody tr) {
  transition: all 0.2s ease;
  cursor: pointer;
}

.elevated-table :deep(tbody tr:hover) {
  background: #fafafa !important;
}

.elevated-table :deep(tbody td) {
  font-size: 14px;
  border-bottom: 1px solid #f5f5f5 !important;
  padding: 0px 12px !important;
}

.elevated-table :deep(tbody tr:last-child td) {
  border-bottom: none !important;
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

.zone-name {
  font-weight: 600;
  color: #171717;
  font-size: 14px;
  letter-spacing: -0.1px;
}

.zone-id {
  font-weight: 600;
  color: #171717;
  font-size: 14px;
  letter-spacing: -0.1px;
}

.status-chip {
  font-weight: 700 !important;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  font-size: 11px !important;
  border-radius: 8px !important;
  padding: 4px 10px !important;
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
  overflow: hidden;
}

.action-menu :deep(.v-list-item) {
  font-weight: 600;
  letter-spacing: 0.3px;
  font-size: 14px;
  transition: all 0.2s ease;
}

.action-menu :deep(.v-list-item:hover) {
  background-color: #fafafa !important;
}

.action-menu :deep(.v-list-item--disabled) {
  opacity: 0.4;
}

.delete-item :deep(.v-list-item__prepend .v-icon) {
  color: #dc2626;
}

.delete-item:hover {
  background-color: #fef2f2 !important;
}

.empty-state {
  text-align: center;
  padding: 80px 20px;
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

/* Delete Dialog Styles */
.delete-dialog {
  border-radius: 16px !important;
}

.dialog-title {
  font-weight: 700;
  font-size: 20px;
  color: #171717;
  padding: 24px 24px 16px;
  display: flex;
  align-items: center;
}

.dialog-text {
  font-size: 15px;
  color: #525252;
  padding: 0 24px 16px;
  line-height: 1.6;
}

.warning-box {
  background: #fff7ed;
  border: 2px solid #fed7aa;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  align-items: center;
  font-weight: 600;
  color: #9a3412;
}

.reason-textarea :deep(.v-field) {
  border-radius: 12px !important;
  border: 2px solid #e5e5e5 !important;
  font-weight: 500;
  transition: all 0.2s ease;
}

.reason-textarea :deep(.v-field:hover) {
  border-color: #a3a3a3 !important;
}

.reason-textarea :deep(.v-field--focused) {
  border-color: #171717 !important;
  box-shadow: 0 0 0 3px rgba(0, 0, 0, 0.05);
}

.reason-textarea :deep(.v-field--error) {
  border-color: #dc2626 !important;
}

.dialog-actions {
  padding: 16px 24px 24px;
}

/* Alert styles */
:deep(.v-alert) {
  border-radius: 12px !important;
  font-weight: 600;
  letter-spacing: 0.3px;
}

/* Responsive adjustments */
@media (max-width: 1200px) {
  .admin-header {
    flex-direction: column;
    gap: 20px;
  }

  .header-actions {
    width: 100%;
    justify-content: flex-start;
  }
}

@media (max-width: 768px) {
  .admin-desk {
    padding: 16px;
  }

  .admin-card {
    padding: 20px;
    border-radius: 16px;
  }

  .title {
    font-size: 24px;
  }

  .header-actions {
    flex-direction: column;
    align-items: stretch;
  }

  .search-field,
  .filter-select {
    max-width: 100% !important;
  }

  .elevated-table {
    font-size: 13px;
  }
}
</style>