<template>
  <div class="admin-bookings">
    <div class="admin-card">
      <div class="admin-header">
        <div class="title-wrap">
          <h2 class="title">All Bookings</h2>
          <span class="sub">{{ bookings.length }} items</span>
        </div>
        <v-chip size="small" color="orange-darken-2" variant="flat" class="count-chip">{{ bookings.length }}</v-chip>
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
        <v-progress-circular indeterminate size="48" width="4" color="orange-darken-2" />
        <p class="loading-text mt-3">Loading bookings…</p>
      </div>

      <template v-else>
        <v-data-table
          :headers="headers"
          :items="mappedBookings"
          item-key="id"
          density="compact"
          class="elevated-table"
          :items-per-page="15"
          fixed-header
          height="70vh"
        >
          <template #item.userId="{ item }">
            <span class="cell-strong">{{ item.userId ?? '—' }}</span>
          </template>

          <template #item.deskName="{ item }">
            <div class="cell-main">
              <div class="desk-name">{{ item.deskName }}</div>
              <div class="desk-meta">{{ item.zone }} • {{ item.deskType }}</div>
            </div>
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
            <span class="cell-strong">{{ formatDuration(item.startTime, item.endTime) }}</span>
          </template>

          <template #item.status="{ item }">
            <v-chip size="x-small" :color="statusToColor(item.status)" variant="flat" class="status-chip">
              {{ item.status }}
            </v-chip>
          </template>

          <template #no-data>
            <div class="empty-state">
              <v-icon size="40" color="grey-lighten-1" class="mb-2">mdi-folder-open</v-icon>
              <div class="empty-title">No bookings found</div>
              <div class="empty-sub">Try again later.</div>
            </div>
          </template>
        </v-data-table>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import api from '../plugins/axios';

const bookings = ref([]);
const loading = ref(false);
const error = ref(null);

const headers = [
  { title: 'ID', key: 'id', width: 80, align: 'start' },
  { title: 'User ID', key: 'userId', width: 100 },
  { title: 'Desk', key: 'deskName', minWidth: 220 },
  { title: 'Start', key: 'startTime', width: 150 },
  { title: 'End', key: 'endTime', width: 150 },
  { title: 'Duration', key: 'duration', width: 110 },
  { title: 'Status', key: 'status', width: 120 },
];

const mappedBookings = computed(() => {
  return (bookings.value || []).map((b) => ({
    id: b.id ?? '—',
    userId: b.userId ?? b.user?.id ?? b.user?.userId ?? null,
    deskName: b.desk?.deskName || 'N/A',
    zone: b.desk?.zone || 'N/A',
    deskType: b.desk?.deskType || 'N/A',
    startTime: b.startTime,
    endTime: b.endTime,
    duration: null,
    status: b.status || '—',
  }));
});

const fetchBookings = async () => {
  try {
    loading.value = true;
    error.value = null;
    const response = await api.get('/booking/all');
    bookings.value = response.data;
  } catch (err) {
    console.error('Error fetching bookings:', err);
    error.value = err.response?.data?.message || err.message || 'Failed to fetch bookings';
  } finally {
    loading.value = false;
  }
};

function statusToColor(s) {
  if (!s) return 'primary';
  const val = String(s).toUpperCase();
  if (val.includes('CONFIRM')) return 'success';
  if (val.includes('CANCEL')) return 'error';
  if (val.includes('PEND')) return 'warning';
  return 'primary';
}

function formatDate(dateStr) {
  if (!dateStr) return '—';
  return new Date(dateStr).toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
}
function formatTime(dateStr) {
  if (!dateStr) return '—';
  return new Date(dateStr).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
}
function formatDuration(startStr, endStr) {
  if (!startStr || !endStr) return '—';
  const diff = new Date(endStr) - new Date(startStr);
  const min = Math.max(0, Math.round(diff / 60000));
  const h = Math.floor(min / 60);
  const m = min % 60;
  return h > 0 ? `${h}h ${m.toString().padStart(2, '0')}m` : `${m}m`;
}

onMounted(() => {
  fetchBookings();
});
</script>

<style scoped>
.admin-bookings {
  padding: 18px 20px 20px 20px;
  background: #fff;
}

.admin-card {
  background: #ffffff;
  border: 1px solid rgba(255, 138, 0, 0.16);
  border-radius: 16px;
  padding: 14px 14px 6px 14px;
}

.admin-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
  padding-bottom: 8px;
  border-bottom: 1px solid rgba(255, 170, 64, 0.16);
}
.title-wrap { display: flex; align-items: baseline; gap: 10px; }
.title { font-size: clamp(1.08rem, .98rem + .3vw, 1.2rem); font-weight: 900; color: #111827; margin: 0; }
.sub { font-size: clamp(.9rem,.86rem + .15vw,1rem); font-weight: 750; color: #6b7280; }
.count-chip { font-weight: 800; font-size: .78rem; color: #fff !important; }

.loading-wrap { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 24px 0; }
.loading-text { font-size: .95rem; font-weight: 750; color: #6b7280; }

.elevated-table :deep(table) {
  border-collapse: separate !important;
  border-spacing: 0;
}
.elevated-table :deep(thead th) {
  position: sticky;
  top: 0;
  background: #fffaf4 !important;
  z-index: 2;
  font-weight: 800;
}
.elevated-table :deep(tbody tr:hover) {
  background: #fffdfa !important;
}

.cell-main { display: flex; flex-direction: column; }
.desk-name { font-weight: 850; color: #0f172a; }
.desk-meta { font-weight: 700; color: #6b7280; font-size: .85rem; }
.cell-strong { font-weight: 850; color: #111827; }
.muted { color: #6b7280; font-weight: 700; font-size: .85rem; }
.nowrap { white-space: nowrap; }
.status-chip { font-weight: 820; text-transform: uppercase; letter-spacing: .3px; }

.empty-state { text-align: center; padding: 28px 0; color: #6b7280; }
.empty-title { font-weight: 900; color: #0f172a; }
.empty-sub { font-weight: 700; }
</style>