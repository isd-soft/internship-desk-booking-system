<template>
  <div class="admin-desk">
    <div class="admin-card">
      <div class="admin-header">
        <div class="title-wrap">
          <div class="workspace-label">ADMIN PANEL</div>
          <h2 class="title">Deleted Desks</h2>
          <span class="sub">{{ filteredDesks.length }} deleted desks</span>
        </div>
        <div class="header-actions">
          <v-text-field
              v-model="searchQuery"
              density="compact"
              variant="outlined"
              hide-details
              clearable
              placeholder="Search deleted desks..."
              style="max-width: 250px;"
              :disabled="loading"
              class="search-field"
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
        <p class="loading-text mt-3">Loading deleted desks…</p>
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
              <div class="desk-name">{{ item.zoneName }}</div>
            </div>
          </template>

          <template #item.type="{ item }">
            <v-chip size="x-small" :color="getColor(item.type)" variant="flat" class="status-chip">
              {{ item.type }}
            </v-chip>
          </template>

          <template #item.deletedAt="{ item }">
            <div class="cell-main">
              <div class="deleted-time">{{ formatDate(item.deletedAt) }}</div>
            </div>
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
                    :loading="restoringId === item.id"
                    :disabled="restoringId === item.id"
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
                    @click="onRestore(item)"
                    prepend-icon="mdi-restore"
                    title="Restore desk"
                    :disabled="restoringId === item.id"
                    class="restore-item"
                ></v-list-item>
              </v-list>
            </v-menu>
          </template>
          <template #no-data>
            <div class="empty-state">
              <v-icon size="48" color="#10b981" class="mb-3">mdi-check-circle</v-icon>
              <div class="empty-title">No deleted desks</div>
              <div class="empty-sub">All desks are active. Deleted desks will appear here.</div>
            </div>
          </template>
        </v-data-table>

        <!-- View Modal -->
        <DeskViewModal
            :show="showViewModal"
            v-model="showViewModal"
            :desk="selectedDesk"
        />

        <!-- Restore Confirmation Dialog -->
        <v-dialog v-model="showRestoreDialog" max-width="500">
          <v-card class="restore-dialog">
            <v-card-title class="dialog-title">
              <v-icon color="success" class="mr-2">mdi-restore</v-icon>
              Restore Desk
            </v-card-title>
            <v-card-text class="dialog-text">
              Are you sure you want to restore desk <strong>{{ selectedDesk?.name }}</strong>?
              This will make the desk available for booking again.
            </v-card-text>
            <v-card-actions class="dialog-actions">
              <v-spacer></v-spacer>
              <v-btn
                  variant="text"
                  @click="showRestoreDialog = false"
                  :disabled="restoringId !== null"
              >
                Cancel
              </v-btn>
              <v-btn
                  color="success"
                  variant="flat"
                  @click="confirmRestore"
                  :loading="restoringId !== null"
              >
                Restore
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-dialog>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import api from '../plugins/axios';
import DeskViewModal from "../components/AdminDashboard/DeskViewModal.vue";

// State
const desks = ref([]);
const loading = ref(false);
const error = ref(null);
const successMessage = ref(null);
const searchQuery = ref('');

// Modals
const showViewModal = ref(false);
const showRestoreDialog = ref(false);
const selectedDesk = ref(null);
const restoringId = ref(null);

// Filter options
const TYPE_OPTIONS = [
  { title: 'All', value: 'ALL' },
  { title: 'Shared', value: 'SHARED' },
  { title: 'Assigned', value: 'ASSIGNED' },
  { title: 'Unavailable', value: 'UNAVAILABLE' },
];

const typeFilter = ref('ALL');

// Table headers
const headers = [
  { title: 'ID', key: 'id', width: 80, align: 'start' },
  { title: 'Desk', key: 'name', width: 150 },
  { title: 'Zone ID', key: 'zoneId', width: 100,align: 'center' },
  { title: 'Zone Name', key: 'zoneName', width: 140,align: 'center' },
  { title: 'Type', key: 'type', width: 120, align: 'center' },
  { title: '', key: 'actions', width: 56, align: 'end', sortable: false }
] as const;

// Computed
const filteredDesks = computed(() => {
  let filtered = desks.value || [];

  // Type filter
  if (typeFilter.value !== 'ALL') {
    filtered = filtered.filter((d) => d.type === typeFilter.value);
  }

  // Search filter
  if (searchQuery.value) {
    const search = searchQuery.value.toLowerCase();
    filtered = filtered.filter((d) =>
        d.displayName?.toLowerCase().includes(search) ||
        d.type?.toLowerCase().includes(search) ||
        d.zoneDto?.zoneName?.toLowerCase().includes(search)
    );
  }

  // Map to display format
  return filtered.map((d) => ({
    id: d.id ?? '—',
    name: d.displayName ?? 'N/A',
    zoneId: d.zoneDto?.id ?? '0',
    zoneName: d.zoneDto?.zoneName ?? 'N/A',
    type: d.type ?? 'N/A',
    deletedAt: d.deletedAt ?? 'N/A',
    rawData: d // Keep original data for viewing
  }));
});

// Methods
const fetchDeletedDesks = async () => {
  try {
    loading.value = true;
    error.value = null;
    const response = await api.get('/admin/desks/deleted');
    desks.value = response.data;
  } catch (err) {
    console.error('Error fetching deleted desks:', err);
    error.value = err.response?.data?.message || err.message || 'Failed to fetch deleted desks';
  } finally {
    loading.value = false;
  }
};

function resetFilters() {
  typeFilter.value = 'ALL';
  searchQuery.value = '';
}

function getColor(type: string): string {
  const typeMap: Record<string, string> = {
    SHARED: "#10b981",
    ASSIGNED: "#0b4df5",
    UNAVAILABLE: "#737373FF",
  };
  return typeMap[type?.toUpperCase()] || "#737373";
}

function formatDate(dateString: string): string {
  if (!dateString || dateString === 'N/A') return 'N/A';
  try {
    const date = new Date(dateString);
    return date.toLocaleString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  } catch {
    return dateString;
  }
}

function onView(item) {
  selectedDesk.value = item;
  showViewModal.value = true;
}

function onRestore(item) {
  selectedDesk.value = item;
  showRestoreDialog.value = true;
}

async function confirmRestore() {
  if (!selectedDesk.value?.id) return;

  try {
    restoringId.value = selectedDesk.value.id;
    error.value = null;
    successMessage.value = null;

    await api.patch(`/admin/restore/desk/${selectedDesk.value.id}`);

    successMessage.value = `Desk "${selectedDesk.value.name}" restored successfully`;
    showRestoreDialog.value = false;
    selectedDesk.value = null;

    // Refresh desk list
    await fetchDeletedDesks();
  } catch (err) {
    console.error('Error restoring desk:', err);
    error.value = err.response?.data?.message || err.message || 'Failed to restore desk';
    showRestoreDialog.value = false;
  } finally {
    restoringId.value = null;
  }
}

onMounted(() => {
  fetchDeletedDesks();
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
  padding: 14px 12px !important;
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

.zone-id {
  font-weight: 600;
  color: #171717;
  font-size: 14px;
  letter-spacing: -0.1px;
}

.deleted-time {
  font-weight: 600;
  color: #737373;
  font-size: 13px;
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

.action-menu :deep(.v-list-item__prepend .v-icon) {
  opacity: 0.7;
}

.restore-item :deep(.v-list-item__prepend .v-icon) {
  color: #10b981;
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

/* Restore Dialog */
.restore-dialog {
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