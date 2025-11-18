<template>
  <div class="actions-section px-6 pb-4">
    <v-btn
        v-if="isAdmin"
        block
        variant="flat"
        class="neo-btn mb-3 admin-gradient-btn"
        elevation="3"
        size="large"
        @click="$router.push('/admin-dashboard/map')"
    >
      <v-icon class="mr-2" size="20">mdi-shield-crown-outline</v-icon>
      <span class="btn-text">Switch to Admin Dashboard</span>
    </v-btn>

    <v-btn
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
      class="neo-btn mb-3 mobile-only"
      elevation="0"
      size="large"
      @click="openMobileMap"
    >
      <v-icon class="mr-2" size="20">mdi-map</v-icon>
      <span class="btn-text">Map</span>
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

    <v-dialog
      v-model="showAllDesksModal"
      :max-width="isMobile ? '100%' : '900px'"
      :fullscreen="isMobile"
      transition="dialog-bottom-transition"
      scrollable
    >
      <v-card class="all-desks-modal">
        <div class="modal-header">
          <div class="title-wrap">
            <v-icon size="22" class="mr-2">mdi-view-grid-plus</v-icon>
            <span class="modal-title">All Desks</span>
          </div>

          <div
            class="modal-actions-right d-flex align-center"
            style="gap: 12px"
          >
            <DatePicker v-model:date="selectedDate" />

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
        </div>

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
          <div v-if="desksLoading" class="py-8 text-center">
            <v-progress-circular
              indeterminate
              size="40"
              width="4"
              color="#171717"
            />
            <div class="mt-3">Loading desks…</div>
          </div>

          <v-alert
            v-else-if="desksError"
            type="error"
            variant="tonal"
            class="mb-4"
          >
            {{ desksError }}
          </v-alert>

          <div v-else>
            <SearchToolbar
              v-model:search-query="searchQuery"
              v-model:zone-filter="zoneFilter"
              :zone-options="zoneOptions"
              :results-count="filteredDesks.length"
            />
            <div v-if="filteredDesks.length === 0" class="empty-search-state">
              <v-icon size="34" class="mb-2"
                >mdi-emoticon-confused-outline</v-icon
              >
              <p class="empty-title">No desks match "{{ searchQuery }}"</p>
              <p class="empty-subtitle">Try a different desk name or zone.</p>
            </div>

            <v-fade-transition v-else group>
              <div class="desks-grid">
                <div
                  v-for="(desk, idx) in filteredDesks"
                  :key="desk.id"
                  class="desk-card list-row"
                  :class="{ 'is-even': idx % 2 === 0 }"
                  :data-occupancy="
                    desk.occupancy?.toLowerCase().replace(' ', '-')
                  "
                >
                  <div class="desk-header">
                    <div class="desk-name-zone">
                      <h3 class="desk-name">{{ desk.deskName }}</h3>
                      <span class="desk-zone">Zone: {{ desk.zone }}</span>
                    </div>
                    <div class="header-right">
                      <v-chip
                        :color="getStatusColor(desk.deskStatus)"
                        size="small"
                        class="status-chip"
                        variant="flat"
                      >
                        <v-icon
                          size="16"
                          class="mr-1"
                          v-if="desk.deskStatus === 'ACTIVE'"
                          >mdi-check-circle</v-icon
                        >
                        <v-icon size="16" class="mr-1" v-else
                          >mdi-power-plug-off</v-icon
                        >
                        {{ desk.deskStatus }}
                      </v-chip>

                      <div v-if="desk.occupancy" class="occupancy-badge ml-2">
                        <span
                          class="occupancy-dot"
                          :data-color="desk.deskColor"
                        ></span>
                        <small class="occupancy-text">{{
                          desk.occupancy
                        }}</small>
                      </div>

                      <v-btn
                        icon
                        class="fav-btn ml-3"
                        size="small"
                        variant="text"
                        @click.stop="toggleFav(desk)"
                      >
                        <v-icon :color="desk.isFavourite ? 'red' : ''">
                          {{
                            desk.isFavourite ? "mdi-heart" : "mdi-heart-outline"
                          }}
                        </v-icon>
                      </v-btn>

                      <v-btn
                        class="neo-btn book-btn ml-2"
                        variant="text"
                        size="small"
                        elevation="0"
                        @click.stop="bookDesk(desk)"
                      >
                        <v-icon class="mr-2" size="18"
                          >mdi-calendar-plus</v-icon
                        >
                        <span>Book</span>
                      </v-btn>
                    </div>
                  </div>

                  <div class="desk-details">
                    <div class="detail-item">
                      <v-icon size="18" class="detail-icon">
                        <template v-if="desk.deskType === 'ASSIGNED'"
                          >mdi-seat</template
                        >
                        <template v-else-if="desk.deskType === 'SHARED'"
                          >mdi-account-multiple-outline</template
                        >
                        <template v-else>mdi-block-helper</template>
                      </v-icon>
                      <span class="detail-label">Type:</span>
                      <span class="detail-value">{{ desk.deskType }}</span>
                    </div>

                    <div
                      v-if="desk.isTemporarilyAvailable"
                      class="detail-item temp-available"
                    >
                      <v-icon size="18" class="detail-icon"
                        >mdi-clock-check-outline</v-icon
                      >
                      <span class="detail-label">Temporarily Available</span>
                    </div>

                    <div v-if="desk.temporaryAvailableFrom" class="detail-item">
                      <v-icon size="18" class="detail-icon"
                        >mdi-calendar-start-outline</v-icon
                      >
                      <span class="detail-label">From:</span>
                      <span class="detail-value">
                        {{ formatDateTime(desk.temporaryAvailableFrom) }}
                      </span>
                    </div>

                    <div
                      v-if="desk.temporaryAvailableUntil"
                      class="detail-item"
                    >
                      <v-icon size="18" class="detail-icon"
                        >mdi-calendar-end-outline</v-icon
                      >
                      <span class="detail-label">Until:</span>
                      <span class="detail-value">
                        {{ formatDateTime(desk.temporaryAvailableUntil) }}
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </v-fade-transition>
          </div>
        </v-card-text>
      </v-card>
    </v-dialog>

    <v-dialog
      v-model="showMobileMap"
      fullscreen
      transition="dialog-bottom-transition"
      content-class="mobile-map-dialog-content"
    >
      <v-card
        class="booking-card mobile-map-booking-card"
        style="
          border-radius: 20px;
          background: #fff;
          border: 1px solid #e5e7eb;
          box-shadow: 0 20px 50px rgba(0, 0, 0, 0.15);
          overflow: hidden;
          width: 100vw;
          height: calc(100vh - env(safe-area-inset-top, 0px));
          margin-top: env(safe-area-inset-top, 0px);
        "
      >
        <v-card-title class="mobile-map-header">
          <div class="mobile-map-header-content">
            <div class="mobile-map-header-info">
              <div class="workspace-label">FLOOR MAP</div>
              <div class="desk-title">
                <v-icon size="20" class="desk-title-icon">mdi-map</v-icon>
                Office Layout
              </div>
              <DatePicker v-model:date="selectedDate" />
            </div>
            <v-btn
              icon
              variant="text"
              size="small"
              @click="showMobileMap = false"
            >
              <v-icon size="20">mdi-close</v-icon>
            </v-btn>
          </div>
        </v-card-title>

        <v-card-text class="card-body mobile-map-body">
          <div class="mobile-map-container">
            <div class="mobile-map-panel">
              <ZoomableMap
                ref="zoomableMapRef"
                :min-zoom="1"
                :max-zoom="5"
                :zoom-step="0.3"
                :initial-zoom="1.5"
                :show-reset-button="true"
                @zoom-change="handleZoomChange"
                @reset="handleZoomReset"
              >
                <OfficeMapOverlay :selectedDateISO="selectedDate" />
              </ZoomableMap>
            </div>

            <div class="mobile-legend-bottom">
              <div class="legend-bar legend-bar--mobile">
                <div
                  v-for="(legend, idx) in legendItems"
                  :key="idx"
                  class="legend-badge"
                >
                  <div
                    class="badge-glow"
                    :style="{ background: `${legend.color}20` }"
                  ></div>
                  <div
                    class="badge-dot"
                    :style="{ background: legend.color }"
                  ></div>
                  <span class="badge-text">{{ legend.label }}</span>
                </div>
              </div>
            </div>
          </div>
        </v-card-text>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, watch, nextTick } from "vue";
import api from "@/plugins/axios";
import DatePicker from "@/components/DatePicker.vue";
import OfficeMapOverlay from "@/components/VisualFloorMap/OfficeMapOverlay.vue";
import SearchToolbar from "@/components/panel/SearchToolbar.vue";
import ZoomableMap from "../ZoomableMap.vue";

import {
  loadAllColors,
  DeskColors,
  selectedDate as sharedSelectedDate,
  resetLayout,
  loadDesksFromBackend,
} from "@/components/VisualFloorMap/floorLayout";
import { useFavouritesStore } from "@/stores/favourites";
import Dashboard from "@/components/Dashboard.vue";

const zoomableMapRef = ref<InstanceType<typeof ZoomableMap> | null>(null);

defineProps<{
  currentType: string;
  itemsCount: number;
}>();

function handleZoomChange(scale: number) {
  console.log("[Mobile Map] Zoom changed to:", scale);
}

function handleZoomReset() {
  console.log("[Mobile Map] Zoom reset");
}
const emit = defineEmits<{
  (e: "openMap"): void;
  (e: "load", type: "bookings" | "favourites" | "upcoming"): void;
  (e: "logout"): void;
  (
    e: "book-desk",
    payload: { desk: any; selectedDateISO?: string | null }
  ): void;
  (e: "favourite-toggled"): void;
}>();

const isMobile = ref(false);
const showAllDesksModal = ref(false);
const showMobileMap = ref(false);

const checkMobile = () => {
  isMobile.value = window.innerWidth <= 768;
};
const isAdmin = ref(false);
onMounted(() => {
  const role = localStorage.getItem("role");
  isAdmin.value = String(role).toUpperCase() === "ADMIN";
  checkMobile();
  window.addEventListener("resize", checkMobile, { passive: true });
});

onUnmounted(() => {
  window.removeEventListener("resize", checkMobile);
});

watch(isMobile, (mobile) => {
  if (!mobile) showAllDesksModal.value = false;
});

const searchQuery = ref("");
const zoneFilter = ref<string | null>(null);

watch(
  () => showAllDesksModal.value,
  (open) => {
    if (open) {
      loadDesksFromApi();
    } else {
      searchQuery.value = "";
      zoneFilter.value = null;
    }
  }
);

const allDesks = ref<any[]>([]);
const desksLoading = ref(false);
const desksError = ref<string | null>(null);
const selectedDate = sharedSelectedDate;
const favStore = useFavouritesStore();
const legendItems = [
  { color: "#50c878", label: "Available", icon: "mdi-check-circle" },
  { color: "#ee4b2b", label: "Fully booked", icon: "mdi-close-circle" },
  { color: "#ffbf00", label: "Partially booked", icon: "mdi-clock-outline" },
  { color: "#7393b3", label: "Assigned", icon: "mdi-account" },
  { color: "#818589", label: "Unavailable", icon: "mdi-minus-circle" },
  { color: "#E1BEE7", label: "Your booking", icon: "mdi-minus-circle" },
];

watch(selectedDate, async () => {
  if (showAllDesksModal.value && allDesks.value.length > 0) {
    try {
      sharedSelectedDate.value = selectedDate.value;
      await loadAllColors();
      const colors = DeskColors.value || [];
      const colorMap = new Map(
        colors.map((c: any) => [Number(c.deskId), c.deskColor])
      );

      allDesks.value = allDesks.value.map((desk: any) => {
        const cid = Number(desk.id);
        const deskColor = colorMap.get(cid) || "GREEN";
        let occupancy = "Available";
        switch (deskColor) {
          case "PURPLE":
            occupancy = "Mine";
            break;
          case "RED":
            occupancy = "Busy";
            break;
          case "AMBER":
            occupancy = "Limited";
            break;
          case "BLUE":
            occupancy = "Reserved";
            break;
          case "GRAY":
            occupancy = "Unavailable";
            break;
          default:
            occupancy = "Available";
        }

        return {
          ...desk,
          deskColor,
          occupancy,
        };
      });
    } catch (e) {
      console.warn("Could not reload desk colors on date change", e);
    }
  }
});
async function loadDesksFromApi() {
  desksLoading.value = true;
  desksError.value = null;
  try {
    let response;
    try {
      response = await api.get("/desk");
    } catch (e) {
      try {
        response = await api.get("/desk/all");
      } catch (e2) {
        response = await api.get("/admin/desks");
      }
    }

    const data = Array.isArray(response.data) ? response.data : [];

    allDesks.value = data.map((d: any) => ({
      id: d.id ?? d.deskId ?? d.desk_id,
      deskName:
        d.displayName ?? d.deskName ?? d.name ?? `Desk ${d.id ?? d.deskId}`,
      zone: d.zoneDto?.zoneName ?? d.zone ?? d.zoneName ?? "Unknown zone",
      deskType: d.type ?? d.deskType ?? d.desk_type ?? "-",
      deskStatus: d.deskStatus ?? d.status ?? d.desk_status ?? "ACTIVE",
      isTemporarilyAvailable:
        d.isTemporarilyAvailable ?? d.is_temporary_available ?? false,
      temporaryAvailableFrom:
        d.temporaryAvailableFrom ?? d.temporary_available_from ?? null,
      temporaryAvailableUntil:
        d.temporaryAvailableUntil ?? d.temporary_available_until ?? null,
    }));

    try {
      sharedSelectedDate.value = selectedDate.value;
      await loadAllColors();
      const colors = DeskColors.value || [];
      const colorMap = new Map(
        colors.map((c: any) => [Number(c.deskId), c.deskColor])
      );

      allDesks.value = allDesks.value.map((desk: any) => {
        const cid = Number(desk.id);
        const deskColor = colorMap.get(cid) || "GREEN";
        let occupancy = "Available";
        switch (deskColor) {
          case "PURPLE":
            occupancy = "Mine";
            break;
          case "RED":
            occupancy = "Busy";
            break;
          case "AMBER":
            occupancy = "Limited";
            break;
          case "BLUE":
            occupancy = "Reserved";
            break;
          case "GRAY":
            occupancy = "Unavailable";
            break;
          default:
            occupancy = "Available";
        }

        return {
          ...desk,
          deskColor,
          occupancy,
        };
      });
    } catch (e) {
      console.warn("Could not load desk colors for All Desks", e);
    }
  } catch (err: any) {
    desksError.value =
      err?.response?.data?.message || err.message || "Failed to load desks";
    allDesks.value = [];
    console.error("Error loading desks:", err);
  } finally {
    desksLoading.value = false;
  }
}

async function toggleFav(desk: any) {
  try {
    console.log('[ActionSections] Toggling favourite for desk:', desk.id);
    await favStore.toggle(desk.id);
    console.log('[ActionSections] Toggle complete, emitting event');
    // refresh local flag
    desk.isFavourite = favStore.isFav(desk.id);
    // Notify parent to refresh favourites list
    emit("favourite-toggled");
  } catch (e) {
    console.error("[ActionSections] Failed to toggle favourite", e);
  }
}

const activeDesks = computed(
  () => allDesks.value.filter((d) => d.deskStatus === "ACTIVE").length
);
const deactivatedDesks = computed(
  () => allDesks.value.filter((d) => d.deskStatus === "DEACTIVATED").length
);
const unavailableTypeCount = computed(
  () => allDesks.value.filter((d) => d.deskType === "UNAVAILABLE").length
);

const zoneOptions = computed(() => {
  const zones = new Set<string>();
  allDesks.value.forEach((desk) => {
    if (desk.zone) zones.add(String(desk.zone));
  });
  return Array.from(zones).sort((a, b) => a.localeCompare(b));
});

const zoneSelectItems = computed(() => [
  { label: "All zones", value: null },
  ...zoneOptions.value.map((zone) => ({ label: zone, value: zone })),
]);

const filteredDesks = computed(() => {
  const query = searchQuery.value.trim().toLowerCase();
  return allDesks.value.filter((desk) => {
    const name = String(desk.deskName ?? "").toLowerCase();
    const zone = String(desk.zone ?? "").toLowerCase();
    const matchesQuery = !query || name.includes(query) || zone.includes(query);
    const matchesZone = !zoneFilter.value || desk.zone === zoneFilter.value;
    return matchesQuery && matchesZone;
  });
});

watch(zoneOptions, (options) => {
  if (zoneFilter.value && !options.includes(zoneFilter.value)) {
    zoneFilter.value = null;
  }
});

const openAllDesksModal = () => {
  showAllDesksModal.value = true;
};

const openMobileMap = async () => {
  try {
    if (typeof resetLayout === "function") resetLayout();
    if (typeof loadDesksFromBackend === "function")
      await loadDesksFromBackend();
    sharedSelectedDate.value = selectedDate.value;
    await loadAllColors();
  } catch (e) {
    console.warn("Could not fully reload map for mobile map", e);
  }
  showMobileMap.value = true;
};

watch(showMobileMap, (isShown) => {
  if (isShown) {
    nextTick(() => {
      zoomableMapRef.value?.resetZoom();
    });
  }
});

watch(selectedDate, async (val) => {
  if (!showMobileMap.value) return;
  try {
    sharedSelectedDate.value = val;
    resetLayout();
    await loadDesksFromBackend();
    await loadAllColors();
    console.log("[Mobile Map] Date updated to:", val);
  } catch (e) {
    console.warn("Could not fully reload map on mobile date change", e);
  }
});

watch(sharedSelectedDate, async (val) => {
  if (!showMobileMap.value) return;
  try {
    selectedDate.value = val;
    await loadAllColors();
    console.log("[Mobile Map] Shared date updated to:", val);
  } catch (e) {
    console.warn("Could not reload map colors on shared date change", e);
  }
});

function bookDesk(desk: any) {
  showAllDesksModal.value = false;
  emit("book-desk", { desk, selectedDateISO: selectedDate.value });
}

const getStatusColor = (status: string) => {
  switch (status) {
    case "ACTIVE":
      return "success";
    case "DEACTIVATED":
      return "secondary";
    default:
      return "default";
  }
};

const formatDateTime = (dateTime: string | null) => {
  if (!dateTime) return "N/A";
  const date = new Date(dateTime);
  return date.toLocaleString("en-US", {
    month: "short",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
};
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Inter:wght@500;600;700;800&display=swap");

:root {
  --accent: #6366f1;
  --accent-2: #8b5cf6;
  --ink: #1f2937;
  --muted: #6b7280;
  --surface: #ffffff;
  --bg-light: #f3f4f6;
  --bg-modal: #eff2f5;
  --border: #d1d5db;
  --border-strong: #9ca3af;
  --divider-strong: rgba(0, 0, 0, 0.15);
  --divider-soft: rgba(0, 0, 0, 0.08);
  --shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  --shadow-lg: 0 8px 20px rgba(0, 0, 0, 0.12);
}

* {
  font-family: "Inter", system-ui, -apple-system, Segoe UI, Roboto, sans-serif;
}

.outline {
  border: 1px solid var(--border);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
}

.neo-btn {
  height: clamp(40px, 4.2vh, 52px) !important;
  border-radius: 12px !important;
  text-transform: none;
  font-weight: 780;
  font-size: clamp(0.85rem, 0.8rem + 0.2vw, 1.02rem);
  justify-content: flex-start;
  letter-spacing: 0.2px;
  padding: 0 14px;
  transition: transform 160ms ease, box-shadow 160ms ease, background 160ms ease,
    border-color 160ms ease;
  background: var(--surface);
  border: 1px solid var(--border);
  box-shadow: var(--shadow);
  color: var(--ink);
  will-change: transform;
}
.neo-btn:hover {
  transform: translateY(-1px);
  background: var(--bg-light);
  border-color: var(--accent);
  box-shadow: var(--shadow-lg);
}
.neo-btn:active {
  transform: translateY(0) scale(0.98);
}

.neo-btn.danger {
  background: var(--surface);
  border: 1px solid #fca5a5;
  color: #b91c1c;
}

.neo-btn.primary {
  background: linear-gradient(135deg, var(--surface) 0%, var(--bg-light) 100%);
  border: 1px solid var(--accent);
  color: var(--accent);
}

.all-desks-btn {
  position: relative;
  background: linear-gradient(135deg, var(--surface) 0%, var(--bg-light) 100%);
  border: 1px solid var(--accent);
  color: var(--accent);
}

.btn-text {
  font-weight: 820;
}

.blink-dot {
  color: var(--accent);
  font-size: 1.2rem;
  animation: blink 1.5s ease-in-out infinite;
}
@keyframes blink {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.3;
  }
}

.badge-pill :deep(.v-badge__badge) {
  border-radius: 999px;
  padding: 0 8px;
  font-weight: 820;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.08);
  color: #2b2f3a;
}

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
  background: linear-gradient(135deg, #f3f4f6 0%, #f9fafb 100%);
  color: var(--ink);
  border-bottom: 1px solid var(--border);
}
.title-wrap {
  display: flex;
  align-items: center;
}
.modal-title {
  font-size: clamp(1.2rem, 1.1rem + 0.3vw, 1.5rem);
  font-weight: 800;
  letter-spacing: 0.2px;
}
.close-btn {
  color: var(--ink) !important;
  background: var(--surface);
  border-color: var(--border);
}
.close-btn:hover {
  background: var(--bg-light);
  transform: translateY(-1px);
  box-shadow: var(--shadow-lg);
}

.stats-section {
  padding: 18px 18px 14px;
  background: var(--bg-light);
  border-bottom: 1px solid var(--border);
}
.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}
.stat-card {
  background: #ffffff;
  border: 1px solid var(--border);
  border-radius: 14px;
  padding: 14px 10px 12px;
  text-align: center;
  transition: transform 160ms ease, box-shadow 160ms ease,
    border-color 160ms ease, background 160ms ease;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  position: relative;
  overflow: hidden;
}
.stat-icon {
  margin: 0 auto 8px;
}

.stat-value {
  font-size: clamp(1.45rem, 1.2rem + 0.5vw, 1.7rem);
  font-weight: 820;
  color: #1a1a1a;
  line-height: 1;
  margin-bottom: 6px;
  letter-spacing: -0.03em;
}
.stat-label {
  font-size: 0.72rem;
  font-weight: 700;
  color: #525252;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.modal-content {
  padding: 18px;
  max-height: 68vh;
  background: var(--bg-modal);
  border-top: 1px solid var(--border);
  overflow-y: auto;
}

.empty-search-state {
  text-align: center;
  padding: 48px 16px;
  border: 2px dashed var(--border);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.7);
  color: var(--muted);
}

.empty-title {
  font-size: 1.1rem;
  font-weight: 780;
  color: var(--ink);
  margin-bottom: 6px;
}

.empty-subtitle {
  margin: 0;
  font-weight: 600;
}

.list-row {
  background: transparent;
}
.list-row.is-even {
  background: transparent;
}

.desk-card {
  background: var(--surface);
  border: 2px solid var(--border-strong);
  border-left: 5px solid var(--accent);
  border-radius: 10px;
  padding: 14px 16px;
  margin-bottom: 12px;
  position: relative;
  transition: transform 0.18s ease, box-shadow 0.18s ease,
    border-color 0.18s ease, background 0.18s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
  display: flex;
  flex-direction: column;
}

.desk-card[data-occupancy="busy"] {
  background: #fef2f2;
  border-left-color: #dc2626;
  border-color: #fee2e2;
}

.desk-card[data-occupancy="limited"] {
  background: #fffbeb;
  border-left-color: #d97706;
  border-color: #fef3c7;
}

.desk-card[data-occupancy="available"] {
  background: #f0fdf4;
  border-left-color: #16a34a;
  border-color: #dcfce7;
}

.desk-card[data-occupancy="reserved"] {
  background: #f0f9ff;
  border-left-color: #0369a1;
  border-color: #e0f2fe;
}

.desk-card[data-occupancy="unavailable"] {
  background: #f1f5f9;
  border-left-color: #64748b;
  border-color: #cbd5e1;
}

.desk-card[data-occupancy="mine"] {
  background: #faf5ff;
  border-left-color: #9333ea;
  border-color: #f3e8ff;
}

.desk-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
  filter: brightness(0.98);
}
.desk-card::after {
  display: none;
}
.desk-card:last-child::after {
  display: none;
}

.desks-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 14px;
}

@media (max-width: 1200px) {
  .desks-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .desks-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }
}

.desk-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
  gap: 12px;
  flex-wrap: wrap;
}

.header-right {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  margin-left: auto;
}
.desk-name-zone {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.desk-name {
  font-size: 1.06rem;
  font-weight: 760;
  color: var(--ink);
  margin: 0;
}
.desk-zone {
  font-size: 0.86rem;
  color: var(--muted);
  font-weight: 650;
}
.status-chip {
  font-weight: 760;
  padding: 6px 10px;
  border-radius: 999px;
  font-size: 0.86rem;
  background: #f0fdf4 !important;
  color: #15803d !important;
}

.status-chip :deep(.v-chip__content) {
  color: #15803d;
}

.occupancy-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin-left: 8px;
}
.occupancy-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  display: inline-block;
}
.occupancy-dot[data-color="GREEN"] {
  background: #50c878;
}
.occupancy-dot[data-color="RED"] {
  background: #ee4b2b;
}
.occupancy-dot[data-color="AMBER"] {
  background: #ffbf00;
}
.occupancy-dot[data-color="BLUE"] {
  background: #7393b3;
}
.occupancy-dot[data-color="GRAY"] {
  background: #818589;
}
.occupancy-dot[data-color="PURPLE"] {
  background: #b77fb4;
}
.occupancy-text {
  font-size: 0.82rem;
  color: var(--muted);
  font-weight: 700;
}

.desk-details {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding-top: 10px;
  border-top: 1px dashed var(--divider-soft);
}
.detail-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.93rem;
}
.detail-icon {
  color: var(--accent);
}
.detail-label {
  color: var(--muted);
  font-weight: 640;
}
.detail-value {
  color: var(--ink);
  font-weight: 750;
}
.temp-available {
  color: #059669;
  font-weight: 760;
}

.modal-actions {
  padding: 12px 18px 18px;
  border-top: 1px solid var(--border-strong);
}

.mobile-only {
  display: none;
}

.mobile-map-booking-card {
  display: flex;
  flex-direction: column;
  height: 100vh;
  width: 100vw;
}

:deep(.mobile-map-dialog-content) {
  align-items: flex-start !important;
  justify-content: flex-start !important;
  padding: 0 !important;
  width: 100% !important;
  height: 100% !important;
  margin: 0 !important;
}

:deep(.mobile-map-dialog-content .mobile-map-booking-card) {
  margin: 0;
}

.mobile-map-header {
  padding: 18px 20px 12px;
  border-bottom: 1px solid #f0f0f0;
  background: #ffffff;
}

.mobile-map-header-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.mobile-map-header-info {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.mobile-map-header-info .workspace-label {
  font-size: 10px;
  font-weight: 700;
  color: #737373;
  letter-spacing: 1.2px;
}

.mobile-map-header-info .desk-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 20px;
  font-weight: 800;
  color: #111827;
  margin: 0;
}

.desk-title-icon {
  color: #111827;
}

.date-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  background: #f8fafc;
  border-radius: 10px;
  border: 1px solid #e0e7ff;
  box-shadow: 0 2px 4px rgba(15, 23, 42, 0.05);
}

.date-icon {
  color: #6b7280;
}

.date-content {
  display: flex;
  flex-direction: column;
  gap: 1px;
}

.date-day {
  font-size: 10px;
  font-weight: 800;
  color: #6b7280;
  letter-spacing: 0.7px;
}

.date-full {
  font-size: 13px;
  font-weight: 600;
  color: #111827;
}

.legend-bar {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(95px, 1fr));
  gap: 6px;
  padding: 8px 10px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 14px;
  border: 1px solid rgba(0, 0, 0, 0.08);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.legend-bar--mobile {
  width: 100%;
  max-width: 520px;
}

.legend-badge {
  position: relative;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 9px;
  background: #ffffff;
  border-radius: 12px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.badge-glow {
  position: absolute;
  inset: 0;
  opacity: 0;
  transition: opacity 0.25s ease;
}

.legend-badge:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 18px rgba(0, 0, 0, 0.08);
}

.legend-badge:hover .badge-glow {
  opacity: 1;
}

.badge-text {
  font-size: 11px;
  font-weight: 650;
  color: #1f2937;
  position: relative;
  z-index: 1;
}

.badge-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  position: relative;
  z-index: 1;
}

@keyframes blink {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.3;
  }
}

@media (max-width: 768px) {
  .neo-btn {
    height: 38px !important;
    font-size: 13px !important;
    padding: 0 10px !important;
  }

  .btn-text {
    font-size: 12px;
  }

  .blink-dot {
    font-size: 10px;
  }

  .modal-content {
    padding: 12px;
  }

  .desk-card {
    padding: 12px 14px;
    margin-bottom: 10px;
  }

  .desk-header {
    flex-direction: column;
    align-items: flex-start;
    margin-bottom: 10px;
  }

  .header-right {
    width: 100%;
    margin-left: 0;
    margin-top: 8px;
  }

  .desk-name {
    font-size: 1rem;
  }

  .desk-zone {
    font-size: 0.8rem;
  }

  .desk-details {
    padding-top: 8px;
    gap: 6px;
  }

  .detail-item {
    font-size: 0.88rem;
    gap: 6px;
  }

  .status-chip {
    font-size: 0.75rem;
    padding: 4px 8px;
  }

  .occupancy-text {
    font-size: 0.75rem;
  }

  .book-btn {
    font-size: 0.75rem !important;
  }

  .fav-btn {
    font-size: 0.8rem !important;
  }

  .all-desks-modal :deep(.v-card__title) {
    font-size: 0.95rem;
  }
}

@media (min-width: 769px) and (max-width: 1189px) {
  .mobile-map-panel {
    position: absolute;
    inset: 0;
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;
  }

  .mobile-map-body {
    padding: 0;
    overflow: hidden;
    background: #f9fafb;
    flex: 1;
    display: flex;
    flex-direction: column;
  }

  .mobile-controls-overlay {
    position: absolute;
    bottom: 12px;
    left: 50%;
    transform: translateX(-50%);
    z-index: 1000;
    width: calc(100% - 32px);
    max-width: 600px;
    pointer-events: none;
  }

  .mobile-controls-overlay > * {
    pointer-events: auto;
  }
}

@media (max-width: 1189px) {
  .mobile-only {
    display: block;
  }
}
.mobile-map-body {
  padding: 0;
  overflow: hidden;
  background: #f9fafb;
  flex: 1;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.mobile-map-container {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
}

.mobile-map-panel {
  flex: 1;
  width: 100%;
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
}

.mobile-map-panel :deep(.zoomable-map-container) {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
}

.mobile-legend-bottom {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  width: 100%;
  padding: 12px 16px calc(16px + env(safe-area-inset-bottom, 0px));
  background: linear-gradient(
    to top,
    rgba(255, 255, 255, 1) 0%,
    rgba(255, 255, 255, 0.98) 70%,
    rgba(255, 255, 255, 0) 100%
  );
  z-index: 10;
  pointer-events: none;
}

.mobile-legend-bottom > * {
  pointer-events: auto;
}

.mobile-map-wrapper {
  display: none;
}

.mobile-controls-overlay {
  display: none;
}

.admin-gradient-btn {
  background: linear-gradient(135deg, #e16531 0%, #eadf66 100%) !important;
  color: white !important;
}
</style>

