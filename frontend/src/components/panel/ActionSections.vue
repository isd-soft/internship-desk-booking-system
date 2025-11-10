<template>
  <div class="actions-section px-6 pb-4">
    <!-- Только мобилка -->
    <v-btn
      v-if="isMobile"
      block
      variant="text"
      class="neo-btn mb-3 all-desks-btn"
      elevation="0"
      size="large"
      @click="openAllDesksModal"
    >
      <v-icon class="mr-2" size="20">mdi-desk</v-icon>
      <span class="btn-text">All Desks</span>
      <span class="blink-dot ml-2">●</span>
    </v-btn>

    <v-btn
      block
      variant="text"
      class="neo-btn mb-3"
      elevation="0"
      size="large"
      @click="$emit('load', 'bookings')"
    >
      <v-icon class="mr-2" size="20">mdi-calendar-check</v-icon>
      <span class="btn-text">My Bookings</span>
      <v-badge
        v-if="currentType === 'bookings' && itemsCount > 0"
        :content="itemsCount"
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
      @click="$emit('load', 'favourites')"
    >
      <v-icon class="mr-2" size="20">mdi-star-four-points</v-icon>
      <span class="btn-text">Favorites</span>
    </v-btn>

    <v-btn
      block
      variant="text"
      class="neo-btn mb-3"
      elevation="0"
      size="large"
      @click="$emit('load', 'upcoming')"
    >
      <v-icon class="mr-2" size="20">mdi-clock-time-eight-outline</v-icon>
      <span class="btn-text">Upcoming</span>
    </v-btn>

    <v-divider class="my-2"></v-divider>

    <v-btn
      block
      variant="text"
      class="neo-btn danger"
      elevation="0"
      size="large"
      @click="$emit('logout')"
    >
      <v-icon class="mr-2" size="20">mdi-logout</v-icon>
      <span class="btn-text">Logout</span>
    </v-btn>

    <!-- AllDesks Modal -->
    <v-dialog
      v-model="showAllDesksModal"
      :max-width="isMobile ? '100%' : '900px'"
      :fullscreen="isMobile"
      transition="dialog-bottom-transition"
      scrollable
    >
      <v-card class="all-desks-modal">
        <!-- Заголовок -->
        <div class="modal-header">
          <div class="title-wrap">
            <v-icon size="22" class="mr-2">mdi-view-grid-plus</v-icon>
            <span class="modal-title">All Desks</span>
          </div>

          <!-- Верхняя кнопка закрытия оставлена как в твоём шаблоне -->
          <v-btn
            class="neo-btn close-btn"
            variant="text"
            size="small"
            elevation="0"
            @click="showAllDesksModal = false"
          >
            <v-icon size="18" class="mr-1">mdi-close</v-icon>
            Close
          </v-btn>
        </div>

        <!-- Статистика (только мобилка) -->
        <div v-if="isMobile" class="stats-section">
          <div class="stats-grid">
            <div class="stat-card outline">
              <div class="stat-icon">
                <v-icon size="24">mdi-checkbox-marked-circle-outline</v-icon>
              </div>
              <div class="stat-value">{{ activeDesks }}</div>
              <div class="stat-label">Active</div>
            </div>
            <div class="stat-card outline">
              <div class="stat-icon">
                <v-icon size="24">mdi-power-plug-off-outline</v-icon>
              </div>
              <div class="stat-value">{{ deactivatedDesks }}</div>
              <div class="stat-label">Deactivated</div>
            </div>
            <div class="stat-card outline">
              <div class="stat-icon">
                <v-icon size="24">mdi-block-helper</v-icon>
              </div>
              <div class="stat-value">{{ unavailableTypeCount }}</div>
              <div class="stat-label">Type: Unavailable</div>
            </div>
          </div>
        </div>

        <v-card-text class="modal-content">
          <v-fade-transition group>
            <div
              v-for="(desk, idx) in allDesks"
              :key="desk.id"
              class="desk-card outline list-row"
              :class="{'is-even': idx % 2 === 0}"
            >
              <div class="desk-header">
                <div class="desk-name-zone">
                  <h3 class="desk-name">{{ desk.deskName }}</h3>
                  <span class="desk-zone">Zone: {{ desk.zone }}</span>
                </div>
                <v-chip
                  :color="getStatusColor(desk.deskStatus)"
                  size="small"
                  class="status-chip"
                  variant="flat"
                >
                  <v-icon size="16" class="mr-1" v-if="desk.deskStatus === 'ACTIVE'">mdi-check-circle</v-icon>
                  <v-icon size="16" class="mr-1" v-else>mdi-power-plug-off</v-icon>
                  {{ desk.deskStatus }}
                </v-chip>
              </div>

              <div class="desk-details">
                <div class="detail-item">
                  <v-icon size="18" class="detail-icon">
                    <template v-if="desk.deskType === 'ASSIGNED'">mdi-seat</template>
                    <template v-else-if="desk.deskType === 'SHARED'">mdi-account-multiple-outline</template>
                    <template v-else>mdi-block-helper</template>
                  </v-icon>
                  <span class="detail-label">Type:</span>
                  <span class="detail-value">{{ desk.deskType }}</span>
                </div>

                <div v-if="desk.isTemporarilyAvailable" class="detail-item temp-available">
                  <v-icon size="18" class="detail-icon">mdi-clock-check-outline</v-icon>
                  <span class="detail-label">Temporarily Available</span>
                </div>

                <div v-if="desk.temporaryAvailableFrom" class="detail-item">
                  <v-icon size="18" class="detail-icon">mdi-calendar-start-outline</v-icon>
                  <span class="detail-label">From:</span>
                  <span class="detail-value">{{ formatDateTime(desk.temporaryAvailableFrom) }}</span>
                </div>

                <div v-if="desk.temporaryAvailableUntil" class="detail-item">
                  <v-icon size="18" class="detail-icon">mdi-calendar-end-outline</v-icon>
                  <span class="detail-label">Until:</span>
                  <span class="detail-value">{{ formatDateTime(desk.temporaryAvailableUntil) }}</span>
                </div>
              </div>
            </div>
          </v-fade-transition>
        </v-card-text>

        <v-card-actions class="modal-actions">
          <v-btn
            class="neo-btn primary w-100"
            variant="text"
            size="large"
            elevation="0"
            @click="showAllDesksModal = false"
          >
            <v-icon size="18" class="mr-2">mdi-check</v-icon>
            Close
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, watch } from 'vue';

defineProps<{
  currentType: string;
  itemsCount: number;
}>();

defineEmits<{
  (e: "openMap"): void;
  (e: "load", type: "bookings" | "favourites" | "upcoming"): void;
  (e: "logout"): void;
}>();

// Mobile detection
const isMobile = ref(false);
const showAllDesksModal = ref(false);

const checkMobile = () => {
  isMobile.value = window.innerWidth <= 768;
};

onMounted(() => {
  checkMobile();
  window.addEventListener('resize', checkMobile, { passive: true });
});

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile);
});

// Закрывать модалку при переходе на десктоп
watch(isMobile, (mobile) => {
  if (!mobile) showAllDesksModal.value = false;
});

// Mock desks (используем новые enum-значения)
const allDesks = ref([
  {
    id: 1,
    deskName: 'Desk A-101',
    zone: 'Zone A',
    deskType: 'ASSIGNED',
    deskStatus: 'ACTIVE',
    isTemporarilyAvailable: false,
    temporaryAvailableFrom: null,
    temporaryAvailableUntil: null,
  },
  {
    id: 2,
    deskName: 'Desk A-102',
    zone: 'Zone A',
    deskType: 'SHARED',
    deskStatus: 'DEACTIVATED',
    isTemporarilyAvailable: false,
    temporaryAvailableFrom: null,
    temporaryAvailableUntil: null,
  },
  {
    id: 3,
    deskName: 'Desk B-201',
    zone: 'Zone B',
    deskType: 'UNAVAILABLE',
    deskStatus: 'ACTIVE',
    isTemporarilyAvailable: true,
    temporaryAvailableFrom: '2025-11-10T09:00:00',
    temporaryAvailableUntil: '2025-11-10T17:00:00',
  },
  {
    id: 4,
    deskName: 'Desk B-202',
    zone: 'Zone B',
    deskType: 'ASSIGNED',
    deskStatus: 'DEACTIVATED',
    isTemporarilyAvailable: false,
    temporaryAvailableFrom: null,
    temporaryAvailableUntil: null,
  },
  {
    id: 5,
    deskName: 'Desk C-301',
    zone: 'Zone C',
    deskType: 'SHARED',
    deskStatus: 'ACTIVE',
    isTemporarilyAvailable: true,
    temporaryAvailableFrom: '2025-11-11T08:00:00',
    temporaryAvailableUntil: '2025-11-11T18:00:00',
  },
  {
    id: 6,
    deskName: 'Desk C-302',
    zone: 'Zone C',
    deskType: 'UNAVAILABLE',
    deskStatus: 'DEACTIVATED',
    isTemporarilyAvailable: false,
    temporaryAvailableFrom: null,
    temporaryAvailableUntil: null,
  },
]);

// Статистика под новые enum’ы
const activeDesks = computed(() =>
  allDesks.value.filter(d => d.deskStatus === 'ACTIVE').length
);
const deactivatedDesks = computed(() =>
  allDesks.value.filter(d => d.deskStatus === 'DEACTIVATED').length
);
const unavailableTypeCount = computed(() =>
  allDesks.value.filter(d => d.deskType === 'UNAVAILABLE').length
);

const openAllDesksModal = () => {
  if (isMobile.value) showAllDesksModal.value = true;
};

const getStatusColor = (status: string) => {
  switch (status) {
    case 'ACTIVE':
      return 'success';
    case 'DEACTIVATED':
      return 'secondary';
    default:
      return 'default';
  }
};

const formatDateTime = (dateTime: string | null) => {
  if (!dateTime) return 'N/A';
  const date = new Date(dateTime);
  return date.toLocaleString('en-US', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
};
</script>

<style scoped>
/* Шрифт и переменные темы */
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@500;600;700;800&display=swap');

:root {
  --accent: #ff8a00;
  --accent-2: #ffb84d;
  --ink: #1f2937;
  --muted: #6b7280;
  --surface: #ffffff;
  --border: #e5e7eb;
  --border-strong: #d1d5db;
  --divider-strong: rgba(0,0,0,0.22); /* тёмный разделитель, но не «чёрный» */
  --divider-soft: rgba(0,0,0,0.10);
  --shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
}

* { font-family: 'Inter', system-ui, -apple-system, Segoe UI, Roboto, sans-serif; }

/* Общие «контуры» */
.outline { border: 1px solid var(--border); box-shadow: 0 1px 2px rgba(0,0,0,.04); }

/* Кнопки слева */
.neo-btn {
  height: clamp(46px, 4.2vh, 52px) !important;
  border-radius: 12px !important;
  text-transform: none;
  font-weight: 780;
  font-size: clamp(0.95rem, 0.9rem + 0.2vw, 1.02rem);
  justify-content: flex-start;
  letter-spacing: 0.2px;
  padding: 0 14px;
  transition: transform 160ms ease, box-shadow 160ms ease, background 160ms ease, border-color 160ms ease;
  background: var(--surface);
  border: 1px solid var(--border);
  box-shadow: 0 1px 2px rgba(0,0,0,.04);
  color: var(--ink);
  will-change: transform;
}
.neo-btn:hover {
  transform: translateY(-1px);
  background: #fff7eb;
  border-color: var(--accent);
  box-shadow: 0 6px 12px rgba(255, 138, 0, 0.1);
}
.neo-btn:active { transform: translateY(0) scale(0.98); }

.neo-btn.danger {
  background: var(--surface);
  border: 1px solid #fca5a5;
  color: #b91c1c;
}

.neo-btn.primary {
  background: linear-gradient(135deg, #fff 0%, #fffbf5 100%);
  border: 1px solid #ffd699;
}

.all-desks-btn {
  position: relative;
  background: linear-gradient(135deg, #fff 0%, #fffbf5 100%);
  border: 1px solid #ffd699;
}

.btn-text { font-weight: 820; }

.blink-dot {
  color: var(--accent);
  font-size: 1.2rem;
  animation: blink 1.5s ease-in-out infinite;
}
@keyframes blink { 0%,100%{opacity:1} 50%{opacity:.3} }

.badge-pill :deep(.v-badge__badge) {
  border-radius: 999px;
  padding: 0 8px;
  font-weight: 820;
  box-shadow: 0 4px 10px rgba(0,0,0,.08);
  color: #2b2f3a;
}

/* Модалка */
.all-desks-modal {
  border-radius: 16px !important;
  background: #fff;
  box-shadow: var(--shadow);
  overflow: hidden;
  border: 1px solid var(--border-strong);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 18px;
  background: linear-gradient(135deg, var(--accent) 0%, var(--accent-2) 100%);
  color: white;
  border-bottom: 1px solid rgba(255,255,255,.35);
}
.title-wrap {
  display: flex;
  align-items: center;
}
.modal-title {
  font-size: clamp(1.2rem, 1.1rem + 0.3vw, 1.5rem);
  font-weight: 800;
  letter-spacing: .2px;
}
.close-btn {
  color: #1a1a1a !important;
  background: rgba(255,255,255,.9);
  border-color: rgba(255,255,255,.95);
}
.close-btn:hover {
  background: #fff;
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(0,0,0,.08);
}

/* Статистика */
.stats-section {
  padding: 18px 18px 14px;
  background: linear-gradient(135deg, #ffffff 0%, #fffaf5 100%);
  border-bottom: 1px solid var(--border-strong);
}
.stats-grid {
  display: grid;
  grid-template-columns: repeat(3,1fr);
  gap: 12px;
}
.stat-card {
  background: #ffffff;
  border: 1px solid var(--border);
  border-radius: 14px;
  padding: 14px 10px 12px;
  text-align: center;
  transition: transform 160ms ease, box-shadow 160ms ease, border-color 160ms ease, background 160ms ease;
  box-shadow: 0 1px 3px rgba(0,0,0,.05);
  position: relative;
  overflow: hidden;
}
.stat-icon { margin: 0 auto 8px; }
.stat-value {
  font-size: clamp(1.45rem, 1.2rem + 0.5vw, 1.7rem);
  font-weight: 820;
  color: #1a1a1a;
  line-height: 1;
  margin-bottom: 6px;
  letter-spacing: -0.03em;
}
.stat-label {
  font-size: .72rem;
  font-weight: 700;
  color: #525252;
  text-transform: uppercase;
  letter-spacing: .08em;
}

/* Контент модалки */
.modal-content {
  padding: 18px;
  max-height: 60vh;
  background: #fff;
  /* верхний разделитель контента */
  border-top: 1px solid var(--border);
}

/* Зебра и более тёмные разделители между карточками */
.list-row { background: #fff; }
.list-row.is-even { background: #fffdfa; }

/* Карточка стола + сильные разделители */
.desk-card {
  background: #fff;
  border: 1px solid var(--border);
  border-radius: 12px;
  padding: 14px;
  margin-bottom: 0; /* убираем внешний gap — разделители будут через ::after */
  position: relative;
  transition: transform .18s ease, box-shadow .18s ease, border-color .18s ease, background .18s ease;
}
.desk-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 18px rgba(0,0,0,.08);
  border-color: #ffd699;
  background: #fffdf7;
}
/* жирный, но не «чёрный» разделитель между карточками */
.desk-card::after {
  content: '';
  position: absolute;
  left: 12px;
  right: 12px;
  bottom: -1px;
  height: 2px;
  background: var(--divider-strong);
  opacity: .85;
}
.desk-card:last-child::after { display: none; }

/* Внутри карточки */
.desk-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 10px;
  gap: 8px;
}
.desk-name-zone { display: flex; flex-direction: column; gap: 4px; }
.desk-name { font-size: 1.06rem; font-weight: 760; color: var(--ink); margin: 0; }
.desk-zone { font-size: .86rem; color: var(--muted); font-weight: 650; }
.status-chip { font-weight: 720; }

.desk-details {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding-top: 10px;
  border-top: 1px dashed var(--divider-soft);
}
.detail-item { display: flex; align-items: center; gap: 8px; font-size: .93rem; }
.detail-icon { color: var(--accent); }
.detail-label { color: var(--muted); font-weight: 640; }
.detail-value { color: var(--ink); font-weight: 750; }
.temp-available { color: #059669; font-weight: 760; }

/* Actions */
.modal-actions {
  padding: 12px 18px 18px;
  border-top: 1px solid var(--border-strong);
}

/* Только мобилки — кнопка All Desks и модалка фуллскрин остаются */
@media (min-width: 769px) {
  .all-desks-btn {
    display: none !important;
  }
}
@keyframes blink { 0%,100%{opacity:1} 50%{opacity:.3} }
</style>
