<template>
  <div class="dashboard-layout d-flex">
    <v-sheet class="user-panel d-flex flex-column" :style="panelStyle">
    <div class="panel-header pa-6 pb-4">
      <div class="d-flex align-items-center justify-space-between mb-3">
        <div class="head-left">
          <h2 class="header-title">Quick Actions</h2>
          <p class="header-subtitle">Manage your workspace · ISD desk bookings</p>
        </div>
        <div class="brand-wrap">
          <img src="../assets/isd-logo.webp" alt="ISD" class="brand-img" />
        </div>
      </div>
    </div>

    <div class="actions-section px-6 pb-4">
      <v-btn block variant="text" class="neo-btn mb-3" elevation="0" size="large" @click="loadData('bookings')">
        <v-icon class="mr-2" size="20">mdi-calendar-check</v-icon>
        <span class="btn-text">My Bookings</span>
        <v-badge v-if="currentType === 'bookings' && items.length > 0" :content="items.length" color="white" inline class="ml-auto badge-pill" />
      </v-btn>

      <v-btn block variant="text" class="neo-btn mb-3" elevation="0" size="large" @click="loadData('favourites')">
        <v-icon class="mr-2" size="20">mdi-star</v-icon>
        <span class="btn-text">Favorites</span>
      </v-btn>

      <v-btn block variant="text" class="neo-btn mb-3" elevation="0" size="large" @click="loadData('upcoming')">
        <v-icon class="mr-2" size="20">mdi-clock-outline</v-icon>
        <span class="btn-text">Upcoming</span>
      </v-btn>

      <v-btn v-if="isAdmin" block variant="text" class="neo-btn mb-3" elevation="0" size="large" @click="openAllBookings">
        <v-icon class="mr-2" size="20">mdi-table</v-icon>
        <span class="btn-text">All Bookings</span>
      </v-btn>

      <v-divider class="my-2"></v-divider>

      <v-btn block variant="text" class="neo-btn danger" elevation="0" size="large" @click="logout">
        <v-icon class="mr-2" size="20">mdi-logout</v-icon>
        <span class="btn-text">Logout</span>
      </v-btn>
    </div>

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
              <div v-for="(item, i) in paginatedItems" :key="item.id ?? i" class="data-item">
                <div class="index-badge">{{ startItem + i }}</div>

                <div class="item-content">
                  <div class="item-header">
                    <div class="item-title">{{ item.desk }}</div>
                    <div class="item-actions">
                      <v-chip class="status-chip mr-1" size="x-small" :color="item.statusColor" variant="flat">
                        {{ item.status }}
                      </v-chip>
                      <v-btn size="small" variant="text" class="more-btn" @click="openDetails(item)">
                        Details
                        <v-icon size="16" class="ml-1">mdi-chevron-right</v-icon>
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

          <div v-if="totalPages > 1" class="pagination-section">
            <div class="d-flex align-items-center justify-space-between">
              <v-btn icon variant="text" size="small" :disabled="currentPage === 1" @click="currentPage--">
                <v-icon>mdi-chevron-left</v-icon>
              </v-btn>

              <div class="pagination-info">
                <span class="page-text">{{ startItem }}-{{ endItem }} of {{ items.length }}</span>
              </div>

              <v-btn icon variant="text" size="small" :disabled="currentPage === totalPages" @click="currentPage++">
                <v-icon>mdi-chevron-right</v-icon>
              </v-btn>
            </div>
          </div>
        </div>
      </div>
    </v-slide-y-transition>

    <div v-if="items.length === 0 && !loading" class="empty-panel flex-grow-1 d-flex align-items-center justify-center pa-6">
      <div class="text-center">
        <div class="empty-icon-wrapper mb-4">
          <v-icon size="48" color="grey-lighten-1">mdi-folder-open</v-icon>
        </div>
        <h3 class="empty-title">{{ emptyTitle }}</h3>
        <p class="empty-subtitle">{{ emptySubtitle }}</p>
      </div>
    </div>

    <div v-if="loading" class="loading-panel flex-grow-1 d-flex align-items-center justify-center">
      <div class="text-center">
        <v-progress-circular indeterminate size="48" width="4"></v-progress-circular>
        <p class="loading-text mt-4">Loading {{ currentTitle }}...</p>
      </div>
    </div>

    <v-snackbar v-model="snackbar.show" :color="snackbar.color" timeout="2200" rounded="lg" elevation="8" location="top" class="snackbar">
      <div class="d-flex align-center">
        <v-icon class="mr-3" size="20">mdi-alert-circle</v-icon>
        <span class="snackbar-text">{{ snackbar.message }}</span>
      </div>
    </v-snackbar>

    <v-dialog v-model="details.open" max-width="560" scrollable>
      <v-card class="details-card">
        <v-card-title class="d-flex align-center justify-space-between">
          <div class="d-flex align-center gap-2">
            <v-icon class="mr-2">mdi-desk</v-icon>
            <strong>{{ details.item?.desk }}</strong>
          </div>
          <v-chip size="x-small" :color="details.item?.statusColor" variant="flat">{{ details.item?.status }}</v-chip>
        </v-card-title>

        <v-card-text>
          <div class="details-grid">
            <div class="d-row"><span class="d-label">Zone</span><span class="d-val">{{ details.item?.zone }}</span></div>
            <div class="d-row"><span class="d-label">Type</span><span class="d-val">{{ details.item?.type }}</span></div>
            <div class="d-row"><span class="d-label">Date</span><span class="d-val">{{ details.item?.date }}</span></div>
            <div class="d-row"><span class="d-label">Time</span><span class="d-val">{{ details.item?.time }}</span></div>
            <div class="d-row"><span class="d-label">Duration</span><span class="d-val">{{ details.item?.duration }}</span></div>
          </div>
        </v-card-text>

        <v-card-actions class="px-4 pb-3">
          <v-spacer></v-spacer>
          <v-btn variant="text" @click="details.open = false">Close</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-sheet>

  <div class="content-panel flex-grow-1">
    <div v-if="showAdminBookings" class="h-100 overflow-auto">
      <AdminBookings />
    </div>
    <div v-else class="placeholder h-100 d-flex align-center justify-center">
      <div class="text-center">
        <v-icon size="48" color="grey-lighten-1">mdi-view-dashboard</v-icon>
        <p class="mt-2">Select an option from the left panel.</p>
      </div>
    </div>
  </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue';
import { useRouter } from 'vue-router';
import api from '../plugins/axios';
import AdminBookings from './AdminBookings.vue';

const router = useRouter();

const showAdminBookings = ref(false);
const isAdmin = ref((localStorage.getItem('role') || '').toUpperCase() === 'ADMIN');

const items = ref([]);
const currentTitle = ref('Data');
const currentType = ref('');
const loading = ref(false);
const currentPage = ref(1);

const winW = ref(window.innerWidth);
const itemsPerPage = ref(3);

const updateLayout = () => {
  winW.value = window.innerWidth;
  if (winW.value < 900) itemsPerPage.value = 2;
  else itemsPerPage.value = 3;
};
onMounted(() => {
  updateLayout();
  window.addEventListener('resize', updateLayout);
});
onBeforeUnmount(() => window.removeEventListener('resize', updateLayout));

const panelStyle = computed(() => {
  const vwWidth = Math.round(window.innerWidth * 0.34);
  const w = Math.min(Math.max(vwWidth, 360), 640);
  return `width:${w}px;max-width:100vw;`;
});

const snackbar = ref({ show: false, message: '', color: 'error' });
const details = ref({ open: false, item: null });

const totalPages = computed(() => Math.ceil(items.value.length / itemsPerPage.value));
const paginatedItems = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage.value;
  return items.value.slice(start, start + itemsPerPage.value);
});
const startItem = computed(() => (currentPage.value - 1) * itemsPerPage.value + 1);
const endItem = computed(() => Math.min(currentPage.value * itemsPerPage.value, items.value.length));

const getTypeChipColor = () => 'grey-darken-1';
const statusToColor = (s) => (s === 'CONFIRMED' ? 'success' : s === 'CANCELLED' ? 'error' : 'primary');

const emptyTitle = computed(() => {
  if (currentType.value === 'bookings') return 'No Bookings Yet';
  if (currentType.value === 'favourites') return 'No Favorites Yet';
  if (currentType.value === 'upcoming') return 'No Upcoming Events';
  return 'Get Started';
});

const emptySubtitle = computed(() => {
  if (currentType.value === 'bookings') return 'You have not made any bookings yet.';
  if (currentType.value === 'favourites') return 'Save desks to your favorites to see them here.';
  if (currentType.value === 'upcoming') return 'Nothing scheduled soon.';
  return 'Select an action above to view your data.';
});

async function loadData(type) {
  try {
    showAdminBookings.value = false;
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
    }

   if (type === 'favourites') {
  currentTitle.value = 'Favorites';

  const response = await api.get('/favourite/favourites');
  const data = response.data || [];

  items.value = data.map((d, idx) => ({
    id: d.deskId ?? idx,
    desk: d.deskName || 'Desk',
    zone: d.zone || 'Unknown zone',
    favourite: d.isFavourite,
    raw: d
  }));
}


    if (type === 'upcoming') {
      currentTitle.value = 'Upcoming';
      items.value = []; // пока пусто
    }

  } catch (err) {
    console.error(err);
    items.value = [];
    snackbar.value = { show: true, message: 'Failed to load data', color: 'error' };
  } finally {
    loading.value = false;
  }
}

function openDetails(item) {
  details.value.item = item;
  details.value.open = true;
}
function bookDesk(item) {
  console.log('Booking desk:', item.id);
  snackbar.value = { 
    show: true, 
    message: `Booking ${item.desk}...`, 
    color: 'info' 
  };
}

function openAllBookings() {
  showAdminBookings.value = true;
  currentType.value = '';
  items.value = [];
  currentTitle.value = 'All Bookings';
}

function logout() {
  localStorage.removeItem('token');
  router.push('/login');
}

function formatDate(dateStr) {
  return new Date(dateStr).toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
}

function formatTime(dateStr) {
  return new Date(dateStr).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
}

function formatDuration(startStr, endStr) {
  const diff = new Date(endStr) - new Date(startStr);
  const min = Math.round(diff / 60000);
  const h = Math.floor(min / 60);
  const m = min % 60;
  return h > 0 ? `${h}h ${m.toString().padStart(2, '0')}m` : `${m}m`;
}
</script>

<style scoped>
:root{
  --card-bg:#ffffff;
  --card-border:rgba(255,138,0,.16);
  --panel-sep:rgba(255,170,64,.16);
  --soft:#fff7f0;
  --surface:#fffaf4;
  --text-1:#0f172a;
  --text-2:#5f5b53;
  --accent:#ff8a00;
  --danger-soft:#ffe9ea;
  --danger-line:rgba(210,80,80,.22);
}

*{font-family:"Inter",-apple-system,BlinkMacSystemFont,"Segoe UI",Roboto,sans-serif}

.user-panel{
  background:var(--surface);
  border-left:1px solid var(--panel-sep);
  height:100vh;
  overflow:hidden;
}

.panel-header{
  background:var(--surface);
  border-bottom:1px solid var(--panel-sep);
}
.head-left{min-width:0}
.header-title{
  font-size:clamp(1.05rem, 0.9rem + 0.4vw, 1.22rem);
  font-weight:900; color:var(--text-1); margin:0
}
.header-subtitle{
  font-size:clamp(.9rem, .85rem + 0.2vw, 1rem);
  color:var(--text-2); font-weight:650; margin:4px 0 0
}
.brand-wrap{
  display:flex; align-items:center; justify-content:center;
  width:clamp(76px, 6.4vw, 96px);
  height:clamp(76px, 6.4vw, 96px);
  border-radius:16px; border:1px solid #ffd6a3;
  background:transparent;
  box-shadow:0 6px 18px rgba(255,138,0,.10);
}
.brand-img{
  width:clamp(64px, 5.6vw, 84px);
  height:clamp(64px, 5.6vw, 84px);
  object-fit:contain;
}

/* New dashboard split layout */
.dashboard-layout{ height:100vh; width:100%; }
.content-panel{ height:100vh; overflow:auto; background:#ffffff; }
.placeholder{ color:#6b7280; }

.neo-btn{
  height:clamp(46px, 4.2vh, 52px)!important;
  border-radius:12px!important;
  text-transform:none;
  font-weight:780;
  font-size:clamp(.95rem, .9rem + .2vw, 1.02rem);
  justify-content:flex-start; letter-spacing:.2px; padding:0 14px;
  transition:transform .16s ease,box-shadow .16s ease,background .16s ease,border-color .16s ease;
  background:#fff; border:1px solid #e5e7eb; box-shadow:0 1px 2px rgba(0,0,0,.04); color:#1f2937
}
.neo-btn:hover{ transform:translateY(-1px); background:#fff7eb; border-color:var(--accent); box-shadow:0 6px 12px rgba(255,138,0,.10) }
.neo-btn.danger{ background:#fff; border:1px solid #fca5a5; color:#b91c1c }
.btn-text{ font-weight:820 }

.badge-pill :deep(.v-badge__badge){ border-radius:999px; padding:0 8px; font-weight:820; box-shadow:0 4px 10px rgba(0,0,0,.08); color:#2b2f3a }

.results-section{ overflow-y:auto; background:var(--soft) }
.results-container{
  background:var(--card-bg);
  border-radius:16px;
  padding:clamp(14px, 1.2vw, 20px);
  border:1px solid var(--card-border);
  height:100%;
  display:flex; flex-direction:column;
}
.results-header{
  display:flex; align-items:center; justify-content:space-between;
  margin-bottom:12px; padding-bottom:10px; border-bottom:1px solid var(--panel-sep)
}
.results-title-wrap{ display:flex; align-items:baseline; gap:10px }
.results-title{ font-size:clamp(1.08rem, .98rem + .3vw, 1.2rem); font-weight:900; color:#111827; letter-spacing:.2px }
.results-sub{ font-size:clamp(.9rem,.86rem + .15vw,1rem); font-weight:750; color:#6b7280 }
.results-badge{ font-weight:800; font-size:.78rem; color:#fff!important }

.items-list{ flex:1; overflow-y:auto; margin-bottom:10px }
.items-list::-webkit-scrollbar{ width:6px }
.items-list::-webkit-scrollbar-thumb{ background:rgba(0,0,0,.18); border-radius:10px }
.items-list::-webkit-scrollbar-thumb:hover{ background:rgba(0,0,0,.28) }

.data-item{
  position:relative;
  padding:16px 16px 16px 68px;
  margin-bottom:14px;
  background:#fff;
  border-radius:12px; border:1px solid #e5e7eb;
  transition:transform .16s ease,box-shadow .16s ease,border-color .16s ease,background .16s ease
}
.data-item:hover{ transform:translateY(-1px); background:#fffdfa; border-color:#fbbf24; box-shadow:0 8px 18px rgba(255,138,0,.10) }

.index-badge{
  position:absolute; left:14px; top:14px;
  min-width:clamp(30px, 2.6vw, 36px);
  height:clamp(30px, 2.6vw, 36px); padding:0 8px;
  border-radius:999px; display:flex; align-items:center; justify-content:center;
  font-weight:900; font-size:clamp(.82rem, .76rem + .2vw, .95rem); color:#000000;
  background:var(--accent); box-shadow:0 4px 10px rgba(255,138,0,.20); border:2px solid #fff
}

.item-header{ display:flex; align-items:center; justify-content:space-between; gap:10px }
.item-title{ font-weight:900; font-size:clamp(1.02rem, .98rem + .25vw, 1.12rem); color:#0f172a }
.item-actions{ display:flex; align-items:center }
.status-chip{ font-weight:820; text-transform:uppercase; letter-spacing:.35px }
.more-btn{ font-weight:820 }

.item-meta{
  margin-top:6px;
  font-size:clamp(.95rem, .9rem + .2vw, 1.02rem);
  font-weight:850; color:#374151; display:flex; align-items:center; gap:8px
}
.item-meta .dot{ opacity:.45 }
.meta-ic{ opacity:.7; margin-right:2px }
.nowrap{ white-space:nowrap; overflow:hidden; text-overflow:ellipsis }

.pagination-section{ padding-top:10px; border-top:1px solid var(--panel-sep) }
.pagination-info{text-align:center}
.page-text{ font-size:.9rem; font-weight:800; color:#4b5563 }

.empty-panel{ background:var(--soft) }
.empty-icon-wrapper{ width:100px; height:100px; margin:0 auto; background:#fff; border-radius:50%; display:flex; align-items:center; justify-content:center; border:2px solid var(--panel-sep) }
.empty-title{ font-size:1.06rem; font-weight:900; color:var(--text-1); margin-bottom:8px }
.empty-subtitle{ font-size:.92rem; color:var(--text-2); font-weight:650 }

.loading-panel{ background:var(--soft) }
.loading-text{ font-size:.9rem; font-weight:750; color:#6b7280 }

.snackbar-text{ font-size:.9rem; font-weight:750 }

.details-card{ border-radius:14px }
.details-grid{ display:grid; grid-template-columns:1fr; gap:10px }
.d-row{ display:flex; align-items:center; justify-content:space-between; border-bottom:1px dashed #e5e7eb; padding:8px 0 }
.d-label{ font-weight:800; color:#4b5563 }
.d-val{ font-weight:800; color:#111827 }

.list-fade-enter-active,.list-fade-leave-active{ transition:all .28s cubic-bezier(.22,1,.36,1) }
.list-fade-enter-from{ opacity:0; transform:translateY(10px) scale(.985) }
.list-fade-leave-to{ opacity:0; transform:translateY(-8px) scale(.985) }

@media (max-width:900px){
  .user-panel{ width:100%!important }
  .results-container{ padding:16px }
}

.book-btn {
  font-weight: 820 !important;
  text-transform: none !important;
  letter-spacing: 0.3px !important;
  padding: 0 12px !important;
  height: 28px !important;
  border-radius: 8px !important;
  box-shadow: 0 2px 6px rgba(255, 138, 0, 0.18) !important;
  transition: transform 0.16s ease, box-shadow 0.16s ease !important;
}

.book-btn:hover {
  transform: translateY(-1px) !important;
  box-shadow: 0 4px 12px rgba(255, 138, 0, 0.28) !important;
}
</style>
