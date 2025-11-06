<template>
  <v-sheet
    class="user-panel d-flex flex-column"
    style="width: clamp(380px, 34vw, 560px);"
  >
    <!-- Header -->
    <div class="panel-header pa-6 pb-4">
      <div class="d-flex align-items-center justify-space-between mb-3">
        <div>
          <h2 class="header-title">Quick Actions</h2>
          <p class="header-subtitle">Manage your workspace · ISD desk bookings</p>
        </div>
        <div class="header-icon">
          <img src="../assets/isd-logo.webp" alt="ISD" class="brand-img" />
        </div>
      </div>
    </div>

    <!-- Actions -->
    <div class="actions-section px-6 pb-4">
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

      <!-- Logout (интерсептор подхватит отсутствие токена) -->
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

    <!-- Results -->
    <v-slide-y-transition>
      <div v-if="items.length > 0" class="results-section flex-grow-1 px-6 pb-6">
        <div class="results-container">
          <div class="results-header">
            <div class="results-title-wrap">
              <div class="results-title">{{ currentTitle }}</div>
              <div class="results-sub">{{ items.length }} items</div>
            </div>
            <v-chip size="small" :color="getTypeChipColor()" variant="flat" class="results-badge">
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
                      >
                        {{ item.status }}
                      </v-chip>
                      <v-btn
                        size="small"
                        variant="text"
                        class="more-btn"
                        @click="openDetails(item)"
                      >
                        Подробнее
                        <v-icon size="16" class="ml-1">mdi-chevron-right</v-icon>
                      </v-btn>
                    </div>
                  </div>

                  <!-- одна жирная строка меты -->
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

          <!-- Pagination -->
          <div v-if="totalPages > 1" class="pagination-section">
            <div class="d-flex align-items-center justify-space-between">
              <v-btn
                icon
                variant="text"
                size="small"
                :disabled="currentPage === 1"
                @click="currentPage--"
              >
                <v-icon>mdi-chevron-left</v-icon>
              </v-btn>

              <div class="pagination-info">
                <span class="page-text">{{ startItem }}-{{ endItem }} of {{ items.length }}</span>
              </div>

              <v-btn
                icon
                variant="text"
                size="small"
                :disabled="currentPage === totalPages"
                @click="currentPage++"
              >
                <v-icon>mdi-chevron-right</v-icon>
              </v-btn>
            </div>
          </div>
        </div>
      </div>
    </v-slide-y-transition>

    <!-- Empty -->
    <div
      v-if="items.length === 0 && !loading"
      class="empty-panel flex-grow-1 d-flex align-items-center justify-center pa-6"
    >
      <div class="text-center">
        <div class="empty-icon-wrapper mb-4">
          <v-icon size="48" color="grey-lighten-1">mdi-gesture-tap</v-icon>
        </div>
        <h3 class="empty-title">Get Started</h3>
        <p class="empty-subtitle">Select an action above to view your data</p>
      </div>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="loading-panel flex-grow-1 d-flex align-items-center justify-center">
      <div class="text-center">
        <v-progress-circular indeterminate size="48" width="4"></v-progress-circular>
        <p class="loading-text mt-4">Loading {{ currentTitle }}...</p>
      </div>
    </div>

    <!-- Snackbar -->
    <v-snackbar
      v-model="snackbar.show"
      :color="snackbar.color"
      timeout="2200"
      rounded="lg"
      elevation="8"
      location="top"
      class="snackbar"
    >
      <div class="d-flex align-center">
        <v-icon class="mr-3" size="20">
          {{ snackbar.color === 'success' ? 'mdi-check-circle' : 'mdi-alert-circle' }}
        </v-icon>
        <span class="snackbar-text">{{ snackbar.message }}</span>
      </div>
    </v-snackbar>

    <!-- Details Modal -->
    <v-dialog v-model="details.open" max-width="520" scrollable>
      <v-card class="details-card">
        <v-card-title class="d-flex align-center justify-space-between">
          <div class="d-flex align-center gap-2">
            <v-icon class="mr-2">mdi-desk</v-icon>
            <strong>{{ details.item?.desk }}</strong>
          </div>
          <v-chip size="x-small" :color="details.item?.statusColor" variant="flat">
            {{ details.item?.status }}
          </v-chip>
        </v-card-title>

        <v-card-text>
          <div class="details-grid">
            <div class="d-row"><span class="d-label">Zone</span><span class="d-val">{{ details.item?.zone }}</span></div>
            <div class="d-row"><span class="d-label">Type</span><span class="d-val">{{ details.item?.type }}</span></div>
            <div class="d-row"><span class="d-label">Date</span><span class="d-val">{{ details.item?.date }}</span></div>
            <div class="d-row"><span class="d-label">Time</span><span class="d-val">{{ details.item?.time }}</span></div>
            <div class="d-row"><span class="d-label">Duration</span><span class="d-val">{{ details.item?.duration }}</span></div>
            <div class="d-row" v-if="details.item?.raw?.desk?.isTemporarilyAvailable">
              <span class="d-label">Temp. available until</span>
              <span class="d-val">{{ formatTime(details.item.raw.desk.temporaryAvailableUntil) }}</span>
            </div>
          </div>
        </v-card-text>

        <v-card-actions class="px-4 pb-3">
          <v-spacer></v-spacer>
          <v-btn variant="text" @click="details.open = false">Close</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-sheet>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import api from '../plugins/axios';

const router = useRouter();

const items = ref([]);
const currentTitle = ref('Data');
const currentType = ref('');
const loading = ref(false);
const currentPage = ref(1);
const itemsPerPage = 3; // меньше карточек -> больше воздуха/читаемость

const snackbar = ref({ show: false, message: '', color: 'success' });

const details = ref({ open: false, item: null });

// Pagination
const totalPages = computed(() => Math.ceil(items.value.length / itemsPerPage));
const paginatedItems = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage;
  const end = start + itemsPerPage;
  return items.value.slice(start, end);
});
const startItem = computed(() => (currentPage.value - 1) * itemsPerPage + 1);
const endItem = computed(() => Math.min(currentPage.value * itemsPerPage, items.value.length));

// UI helpers
const getTypeChipColor = () => 'grey-darken-1';

const statusToColor = (s) => (s === 'CONFIRMED' ? 'success' : s === 'CANCELLED' ? 'error' : 'primary');

// Data
async function loadData(type) {
  try {
    loading.value = true;
    currentType.value = type;
    currentPage.value = 1;

    if (type === 'bookings') {
      currentTitle.value = 'My Bookings';
      const response = await api.get('/booking/my');
      const data = (response.data || [])
        .slice()
        .sort((a, b) => new Date(a.startTime) - new Date(b.startTime));

      items.value = data.map((b, idx) => ({
        id: b.id ?? idx,
        desk: b.desk?.deskName || 'Desk',
        zone: b.desk?.zone || 'Unknown zone',
        type: b.desk?.deskType || '—',
        date: formatDate(b.startTime),
        time: `${formatTime(b.startTime)} - ${formatTime(b.endTime)}`,
        duration: formatDuration(b.startTime, b.endTime),
        status: b.status,
        statusColor: statusToColor(b.status),
        raw: b
      }));

      snackbar.value = { show: true, message: `Loaded ${items.value.length} bookings`, color: 'success' };
    } else if (type === 'favourites') {
      currentTitle.value = 'Favorites';
      await wait(300);
      items.value = [
        { id: 1, desk: 'Desk A05', zone: 'IT Department', type: 'SHARED', date: '—', time: '—', duration: '—', status: '—', statusColor: 'grey', raw: {} },
        { id: 2, desk: 'Desk A12', zone: 'IT Department', type: 'SHARED', date: '—', time: '—', duration: '—', status: '—', statusColor: 'grey', raw: {} },
        { id: 3, desk: 'Desk B05', zone: 'Design', type: 'ASSIGNED', date: '—', time: '—', duration: '—', status: '—', statusColor: 'grey', raw: {} },
      ];
      snackbar.value = { show: true, message: `Loaded ${items.value.length} favorites`, color: 'success' };
    } else if (type === 'upcoming') {
      currentTitle.value = 'Upcoming';
      await wait(300);
      items.value = [
        { id: 1, desk: 'Desk D21', zone: 'Ops', type: 'SHARED', date: 'Mon', time: '08:00 - 17:00', duration: '9h 00m', status: 'CONFIRMED', statusColor: 'success', raw: {} },
        { id: 2, desk: 'Team Room', zone: '—', type: '—', date: 'Tue', time: '10:00 - 12:00', duration: '2h 00m', status: 'CONFIRMED', statusColor: 'success', raw: {} },
      ];
      snackbar.value = { show: true, message: `Loaded ${items.value.length} upcoming`, color: 'success' };
    }
  } catch (error) {
    console.error('Error fetching data:', error?.response?.data || error);
    snackbar.value = { show: true, message: 'Failed to load data', color: 'error' };
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
  // Чистим токен и заголовок, чтобы интерсептор не слал старый Authorization
  localStorage.removeItem('token');
  try {
    if (api?.defaults?.headers?.common?.Authorization) {
      delete api.defaults.headers.common['Authorization'];
    }
  } catch (_) {}
  snackbar.value = { show: true, message: 'Logged out', color: 'success' };
  router.push('/login');
}

// Utils
const wait = (ms) => new Promise((r) => setTimeout(r, ms));
function formatDate(dateStr) {
  const date = new Date(dateStr);
  return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
}
function formatTime(dateStr) {
  const date = new Date(dateStr);
  return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
}
function formatDuration(startStr, endStr) {
  const start = new Date(startStr);
  const end = new Date(endStr);
  const diffMs = Math.max(0, end - start);
  const totalMin = Math.round(diffMs / 60000);
  const h = Math.floor(totalMin / 60);
  const m = totalMin % 60;
  return h > 0 ? `${h}h ${m.toString().padStart(2, '0')}m` : `${m}m`;
}
</script>

<style scoped>
/* Палитра */
:root{
  --card-bg:#fffefb;
  --card-border:rgba(255,138,0,.18);
  --panel-sep:rgba(255,170,64,.18);
  --soft:#fff7f0;
  --surface:#fffaf4;
  --text-1:#0f172a;
  --text-2:#5f5b53;
  --accent:#ff8a00;
  --danger-soft:#ffe9ea;
  --danger-line:rgba(210,80,80,.22);
}

*{font-family:"Inter",-apple-system,BlinkMacSystemFont,"Segoe UI",Roboto,sans-serif}

/* Контейнер */
.user-panel{background:var(--surface);border-left:1px solid var(--panel-sep);height:100vh;overflow:hidden}

/* Header */
.panel-header{background:#fff;border-bottom:1px solid var(--panel-sep)}
.header-title{font-size:1.14rem;font-weight:900;color:var(--text-1);margin:0}
.header-subtitle{font-size:.92rem;color:var(--text-2);font-weight:650;margin:4px 0 0}
.header-icon{width:86px;height:86px;border-radius:16px;display:flex;align-items:center;justify-content:center;background:#fff;border:1px solid #ffd6a3;box-shadow:0 8px 22px rgba(255,138,0,.12);transition:transform .22s ease}
.header-icon:hover{transform:translateY(-2px)}
.brand-img{width:72px;height:72px;object-fit:contain}

/* Кнопки */
.neo-btn{height:48px!important;border-radius:12px!important;text-transform:none;font-weight:750;font-size:.96rem;justify-content:flex-start;letter-spacing:.2px;padding:0 14px;transition:transform .16s ease,box-shadow .16s ease,background .16s ease,border-color .16s ease;background:#fff;border:1px solid #e5e7eb;box-shadow:0 1px 2px rgba(0,0,0,.04);color:#1f2937}
.neo-btn:hover{transform:translateY(-1px);background:#fff7eb;border-color:var(--accent);box-shadow:0 6px 12px rgba(255,138,0,.12)}
.neo-btn.danger{background:#fff;border:1px solid #fca5a5;color:#b91c1c}
.btn-text{font-weight:800}

/* Badge */
.badge-pill :deep(.v-badge__badge){border-radius:999px;padding:0 8px;font-weight:800;box-shadow:0 4px 10px rgba(0,0,0,.08);color:#2b2f3a}

/* Results */
.results-section{overflow-y:auto;background:var(--soft)}
.results-container{background:var(--card-bg);border-radius:16px;padding:18px;border:1px solid var(--card-border);height:100%;display:flex;flex-direction:column}

.results-header{display:flex;align-items:center;justify-content:space-between;margin-bottom:12px;padding-bottom:10px;border-bottom:1px solid var(--panel-sep)}
.results-title-wrap{display:flex;align-items:baseline;gap:10px}
.results-title{font-size:1.12rem;font-weight:900;color:#111827;letter-spacing:.2px}
.results-sub{font-size:.92rem;font-weight:750;color:#6b7280}
.results-badge{font-weight:800;font-size:.78rem;color:#fff!important}

/* List */
.items-list{flex:1;overflow-y:auto;margin-bottom:10px}
.items-list::-webkit-scrollbar{width:6px}
.items-list::-webkit-scrollbar-thumb{background:rgba(0,0,0,.18);border-radius:10px}
.items-list::-webkit-scrollbar-thumb:hover{background:rgba(0,0,0,.28)}

.data-item{position:relative;padding:16px 16px 16px 64px;margin-bottom:14px;background:#fff;border-radius:12px;border:1px solid #e5e7eb;transition:transform .16s ease,box-shadow .16s ease,border-color .16s ease,background .16s ease}
.data-item:hover{transform:translateY(-1px);background:#fffdfa;border-color:#fbbf24;box-shadow:0 8px 18px rgba(255,138,0,.12)}

.index-badge{position:absolute;left:14px;top:14px;min-width:34px;height:34px;padding:0 8px;border-radius:999px;display:flex;align-items:center;justify-content:center;font-weight:900;font-size:.9rem;color:#fff;background:var(--accent);box-shadow:0 4px 10px rgba(255,138,0,.25);border:2px solid #fff}

.item-header{display:flex;align-items:center;justify-content:space-between;gap:10px}
.item-title{font-weight:900;font-size:1.08rem;color:#0f172a}
.item-actions{display:flex;align-items:center}
.status-chip{font-weight:800;text-transform:uppercase;letter-spacing:.4px}
.more-btn{font-weight:800}

/* Одна жирная строка меты */
.item-meta{margin-top:6px;font-size:.98rem;font-weight:850;color:#374151;display:flex;align-items:center;gap:8px}
.item-meta .dot{opacity:.45}
.meta-ic{opacity:.7;margin-right:2px}
.nowrap{white-space:nowrap;overflow:hidden;text-overflow:ellipsis}

/* Pagination */
.pagination-section{padding-top:10px;border-top:1px solid var(--panel-sep)}
.pagination-info{text-align:center}
.page-text{font-size:.9rem;font-weight:800;color:#4b5563}

/* Empty */
.empty-panel{background:var(--soft)}
.empty-icon-wrapper{width:100px;height:100px;margin:0 auto;background:#fff;border-radius:50%;display:flex;align-items:center;justify-content:center;border:2px solid var(--panel-sep)}
.empty-title{font-size:1.08rem;font-weight:900;color:var(--text-1);margin-bottom:8px}
.empty-subtitle{font-size:.92rem;color:var(--text-2);font-weight:650}

/* Loading */
.loading-panel{background:var(--soft)}
.loading-text{font-size:.9rem;font-weight:750;color:#6b7280}

/* Snackbar */
.snackbar-text{font-size:.9rem;font-weight:750}

/* Dialog details */
.details-card{border-radius:14px}
.details-grid{display:grid;grid-template-columns:1fr;gap:10px}
.d-row{display:flex;align-items:center;justify-content:space-between;border-bottom:1px dashed #e5e7eb;padding:8px 0}
.d-label{font-weight:800;color:#4b5563}
.d-val{font-weight:800;color:#111827}

/* Animations */
.list-fade-enter-active,.list-fade-leave-active{transition:all .28s cubic-bezier(.22,1,.36,1)}
.list-fade-enter-from{opacity:0;transform:translateY(10px) scale(.985)}
.list-fade-leave-to{opacity:0;transform:translateY(-8px) scale(.985)}

/* Responsive */
@media (max-width:600px){
  .user-panel{width:100%!important}
  .brand-img{width:64px;height:64px}
  .neo-btn{height:52px!important;font-size:1rem}
  .results-container{padding:16px}
  .data-item{padding:14px 14px 14px 64px}
  .item-title{font-size:1.02rem}
  .item-meta{font-size:.96rem}
}
</style>
