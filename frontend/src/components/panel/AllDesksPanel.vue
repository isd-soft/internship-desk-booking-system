<template>
  <div class="actions-section px-6 pb-4">
    
    <v-btn
      block
      variant="text"
      class="neo-btn all-desks-btn mb-3"
      elevation="0"
      size="large"
      @click="showAllDesksModal = true"
    >
      <v-icon class="mr-2" size="20">mdi-view-grid-plus</v-icon>
      <span class="btn-text">All Desks</span>
    </v-btn>

    <v-divider class="my-2"></v-divider>

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
 <BookingModal
  v-model="showBookingModal"
  :desk="selectedDesk"
  :isBooked="false"
:selected-date-i-s-o="selectedDateISO"
  @created="handleBookingCreated"
/>


</div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from "vue";
import BookingModal from "../VisualFloorMap/BookingModal.vue"

defineProps<{
  currentType: string;
  itemsCount: number;
}>();

defineEmits<{
  (e: "load", type: "bookings" | "favourites" | "upcoming"): void;
  (e: "logout"): void;
}>();

const isMobile = ref(false);
const showAllDesksModal = ref(false);
const showBookingModal = ref(false);
const selectedDesk = ref<any>(null);

const selectedDateISO = ref(new Date().toISOString().substring(0, 10));

const checkMobile = () => {
  isMobile.value = window.innerWidth <= 768;
};

onMounted(() => {
  checkMobile();
  window.addEventListener("resize", checkMobile, { passive: true });
});

onUnmounted(() => {
  window.removeEventListener("resize", checkMobile);
});

watch(isMobile, (mobile) => {
  if (!mobile) showAllDesksModal.value = false;
});

const allDesks = ref([
  {
    id: 1,
    displayName: "Desk A-101",
    zoneDto: { zoneAbv: "A1" },
    type: "ASSIGNED",
    deskStatus: "ACTIVE"
  },
  {
    id: 2,
    displayName: "Desk A-102",
    zoneDto: { zoneAbv: "A1" },
    type: "SHARED",
    deskStatus: "DEACTIVATED"
  },
  {
    id: 3,
    displayName: "Desk B-201",
    zoneDto: { zoneAbv: "B" },
    type: "UNAVAILABLE",
    deskStatus: "UNAVAILABLE"
  },
]);

function openBookingFromDesks(desk: any) {
  selectedDesk.value = {
    ...desk,
    i: desk.id,
    deskName: desk.displayName
  };

  showAllDesksModal.value = false;
  showBookingModal.value = true;
}

function handleBookingCreated() {
  showBookingModal.value = false;
}
</script>

<style scoped>
.neo-btn {
  height: clamp(46px, 4.2vh, 52px) !important;
  border-radius: 12px !important;
  text-transform: none;
  font-weight: 780;
  font-size: clamp(0.95rem, 0.9rem + 0.2vw, 1.02rem);
  justify-content: flex-start;
  letter-spacing: 0.2px;
  padding: 0 14px;
  background: var(--surface);
  border: 1px solid var(--border);
  transition: transform 160ms ease, box-shadow 160ms ease;
}

.neo-btn:hover {
  transform: translateY(-1px);
  background: #fff7eb;
  border-color: var(--accent);
  box-shadow: 0 6px 12px rgba(255, 138, 0, 0.1);
}

.neo-btn:active {
  transform: translateY(0) scale(0.98);
}

.neo-btn.danger {
  background: var(--surface);
  border: 1px solid #fca5a5;
  color: #b91c1c;
}

.all-desks-btn .v-icon {
  color: var(--accent);
}

.btn-text {
  font-weight: 820;
}

.badge-pill :deep(.v-badge__badge) {
  border-radius: 999px;
  padding: 0 8px;
  font-weight: 820;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.08);
  color: #2b2f3a;
}
</style>
