<template>
  <div class="admin-desks">
    <div class="admin-card">
      <div class="admin-header">
        <div class="title-wrap">
          <h2 class="title">All Desks</h2>
          <span class="sub">{{ desks.length }} items</span>
        </div>
        <v-chip size="small" color="orange-darken-2" variant="flat" class="count-chip">{{ desks.length }}</v-chip>
      </div>

      <v-alert v-if="error" type="error" variant="tonal" class="mb-4" density="compact">{{ error }}</v-alert>

      <div v-if="loading" class="loading-wrap">
        <v-progress-circular indeterminate size="48" width="4" color="orange-darken-2" />
        <p class="loading-text mt-3">Loading desks…</p>
      </div>

      <template v-else>
        <v-data-table
            :headers="headers"
            :items="mappedDesks"
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
              <div class="desk-meta">{{ item.zone }} • {{ item.type }}</div>
            </div>
          </template>

          <template #item.status="{ item }">
            <v-chip size="x-small" :color="statusToColor(item.status)" variant="flat" class="status-chip">
              {{ item.status ?? '—' }}
            </v-chip>
          </template>

          <template #no-data>
            <div class="empty-state">
              <v-icon size="40" color="grey-lighten-1" class="mb-2">mdi-folder-open</v-icon>
              <div class="empty-title">No desks found</div>
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

const desks = ref([]);
const loading = ref(false);
const error = ref(null);

// Adjust to match your backend response keys
const headers = [
  { title: 'ID', key: 'id', width: 80, align: 'start' },
  { title: 'Desk', key: 'name', minWidth: 220 },
  { title: 'Zone', key: 'zone', width: 140 },
  { title: 'Type', key: 'type', width: 120 },
  { title: 'Status', key: 'status', width: 120 },
];

const mappedDesks = computed(() => {
  return (desks.value || []).map((d) => ({
    id: d.id ?? '—',
    name: d.displayName ?? 'N/A',
    zone: d.zoneId ?? 'N/A',
    type: d.type ?? 'N/A',
    status: d.deskStatus ?? 'N/A',
    isTemporarilyAvailable: d.isTemporarilyAvailable ?? true,
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

function statusToColor(s) {
  if (!s) return 'primary';
  const val = String(s).toUpperCase();
  if (val === 'UNAVAILABLE') return 'error';
  if (val === 'AVAILABLE') return 'success';

  return 'primary';
}

onMounted(() => {
  fetchDesks();
});
</script>

<style scoped>
.admin-desks { padding: 18px 20px 20px 20px; background: #fff; }
.admin-card { background: #ffffff; border: 1px solid rgba(255, 138, 0, 0.16); border-radius: 16px; padding: 14px 14px 6px 14px; }
.admin-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px; padding-bottom: 8px; border-bottom: 1px solid rgba(255, 170, 64, 0.16); }
.title-wrap { display: flex; align-items: baseline; gap: 10px; }
.title { font-size: clamp(1.08rem, .98rem + .3vw, 1.2rem); font-weight: 900; color: #111827; margin: 0; }
.sub { font-size: clamp(.9rem,.86rem + .15vw,1rem); font-weight: 750; color: #6b7280; }
.count-chip { font-weight: 800; font-size: .78rem; color: #fff !important; }
.loading-wrap { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 24px 0; }
.loading-text { font-size: .95rem; font-weight: 750; color: #6b7280; }
.elevated-table :deep(table) { border-collapse: separate !important; border-spacing: 0; }
.elevated-table :deep(thead th) { position: sticky; top: 0; background: #fffaf4 !important; z-index: 2; font-weight: 800; }
.elevated-table :deep(tbody tr:hover) { background: #fffdfa !important; }
.cell-main { display: flex; flex-direction: column; }
.desk-name { font-weight: 850; color: #0f172a; }
.desk-meta { font-weight: 700; color: #6b7280; font-size: .85rem; }
.status-chip { font-weight: 820; text-transform: uppercase; letter-spacing: .3px; }
.empty-state { text-align: center; padding: 28px 0; color: #6b7280; }
.empty-title { font-weight: 900; color: #0f172a; }
.empty-sub { font-weight: 700; }
</style>