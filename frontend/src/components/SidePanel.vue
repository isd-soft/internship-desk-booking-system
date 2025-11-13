<template>
  <v-sheet class="user-panel d-flex flex-column" :style="panelStyle">
    <PanelHeader />

    <ActionsSection
      :currentType="currentType"
      :itemsCount="items.length"
      @load="loadData"
      @logout="logout"
    />
<v-slide-y-transition>
  <ResultsList
    v-if="items.length > 0"
    :title="currentTitle"
    :items="items"
    :page="currentPage"
    :perPage="itemsPerPage"
    :currentType="currentType"
    @page="(p) => (currentPage = p)"
    @details="openDetails"
    @refresh="refreshList"
  />
</v-slide-y-transition>


    <EmptyPanel
      v-if="items.length === 0 && !loading"
      :title="emptyTitle"
      :subtitle="emptySubtitle"
    />

    <LoadingPanel v-if="loading" :title="currentTitle" />

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
        <v-icon class="mr-3" size="20">mdi-alert-circle</v-icon>
        <span class="snackbar-text">{{ snackbar.message }}</span>
      </div>
    </v-snackbar>

    <DetailsDialog v-model="details.open" :item="details.item" />
  </v-sheet>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from "vue";
import { useRouter } from "vue-router";
import api from "../plugins/axios";


import PanelHeader from "../components/panel/PanelHeader.vue";
import ActionsSection from "../components/panel/ActionSections.vue";
import ResultsList from "../components/panel/ResultList.vue";
import EmptyPanel from "../components/panel/EmptyPanel.vue";
import LoadingPanel from "../components/panel/LoadingPanel.vue";
import DetailsDialog from "../components/panel/DetailsDialog.vue";

import {
  formatDate,
  formatTime,
  formatDuration,
  statusToColor,
} from "../utils/format";

const router = useRouter();

const items = ref<any[]>([]);
const currentTitle = ref("Data");
const currentType = ref<string>("");
const loading = ref(false);
const currentPage = ref(1);

const winW = ref(window.innerWidth);
const itemsPerPage = ref(3);
function refreshList() {
  loadData(currentType.value as "bookings" | "favourites" | "upcoming");
}


const updateLayout = () => {
  winW.value = window.innerWidth;
  itemsPerPage.value = winW.value < 900 ? 2 : 3;
};

onMounted(() => {
  updateLayout();
  window.addEventListener("resize", updateLayout);
});
onBeforeUnmount(() => window.removeEventListener("resize", updateLayout));

const panelStyle = computed(() => {
  const vwWidth = Math.round(window.innerWidth * 0.34);
  const w = Math.min(Math.max(vwWidth, 360), 640);
  return `width:${w}px;max-width:100vw;`;
});

const snackbar = ref({
  show: false,
  message: "",
  color: "error" as "error" | "info" | "success" | "primary" | "warning",
});
const details = ref<{ open: boolean; item: any | null }>({
  open: false,
  item: null,
});

const emptyTitle = computed(() => {
  if (currentType.value === "bookings") return "No Bookings Yet";
  if (currentType.value === "favourites") return "No Favorites Yet";
  if (currentType.value === "upcoming") return "No Upcoming Events";
  return "Get Started";
});

const emptySubtitle = computed(() => {
  if (currentType.value === "bookings")
    return "You have not made any bookings yet.";
  if (currentType.value === "favourites")
    return "Save desks to your favorites to see them here.";
  if (currentType.value === "upcoming") return "Nothing scheduled soon.";
  return "Select an action above to view your data.";
});

async function loadData(type: "bookings" | "favourites" | "upcoming") {
  try {
    loading.value = true;
    currentType.value = type;
    currentPage.value = 1;

    if (type === "bookings") {
      currentTitle.value = "My Bookings";
      const response = await api.get("/booking/my");
      const data = (response.data || [])
        .slice()
        .sort(
          (a: any, b: any) =>
            new Date(a.startTime).getTime() - new Date(b.startTime).getTime()
        );

items.value = data.map((b: any) => ({
  id: b.bookingId,
  desk: b.desk?.displayName || "Desk",
  zone: b.desk?.zoneDto?.zoneName || "Unknown zone",
  type: b.desk?.type || "—",
  date: formatDate(b.startTime),
  time: `${formatTime(b.startTime)} - ${formatTime(b.endTime)}`,
  duration: formatDuration(b.startTime, b.endTime),
  status: b.status,
  statusColor: statusToColor(b.status),
  raw: b,
}));
    }

    if (type === "favourites") {
      currentTitle.value = "Favorites";
      const response = await api.get("/favourites");
      const data = response.data || [];

items.value = data.map((d: any, idx: number) => ({
  id: d.deskId ?? idx,
  desk: d.displayName || "Desk",
  zone: d.zoneDto?.name || "Unknown zone",
  favourite: d.isFavourite,
  status: "",
  statusColor: "primary",
  date: "",
  time: "",
  duration: "",
  raw: d,
}));

    }

    if (type === "upcoming") {
      currentTitle.value = "Upcoming";
      const now = new Date();
      const response = await api.get("/booking/upcoming");
      const data = (response.data || [])
        .filter((b: any) => new Date(b.startTime) > now)
        .slice()
        .sort(
          (a: any, b: any) =>
            new Date(a.startTime).getTime() - new Date(b.startTime).getTime()
        );

items.value = data.map((b: any) => ({
  id: b.bookingId,
  desk: b.desk?.displayName || "Desk",
  zone: b.desk?.zoneDto?.zoneName || "Unknown zone",
  type: b.desk?.type || "—",
  date: formatDate(b.startTime),
  time: `${formatTime(b.startTime)} - ${formatTime(b.endTime)}`,
  duration: formatDuration(b.startTime, b.endTime),
  status: b.status,
  statusColor: statusToColor(b.status),
  raw: b,
}));
    }
  } catch (err) {
    console.error(err);
    items.value = [];
    snackbar.value = {
      show: true,
      message: "Failed to load data",
      color: "error",
    };
  } finally {
    loading.value = false;
  }
}

function openDetails(item: any) {
  details.value.item = item;
  details.value.open = true;
}

function logout() {
  localStorage.removeItem("token");
  router.push("/login");
}
</script>

<style scoped>
:root {
  --card-bg: #ffffff;
  --card-border: rgba(255, 138, 0, 0.16);
  --panel-sep: rgba(255, 170, 64, 0.16);
  --soft: #fff7f0;
  --surface: #fffaf4;
  --text-1: #0f172a;
  --text-2: #5f5b53;
  --accent: #ff8a00;
  --danger-soft: #ffe9ea;
  --danger-line: rgba(210, 80, 80, 0.22);
}
* {
  font-family: "Inter", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto,
    sans-serif;
}
.user-panel {
  background: var(--surface);
  border-left: 1px solid var(--panel-sep);
  height: 100vh;
  overflow: hidden;
}
.snackbar-text {
  font-size: 0.9rem;
  font-weight: 750;
}
@media (max-width: 900px) {
  .user-panel {
    width: 100% !important;
  }
}
</style>
