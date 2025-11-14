<template>
  <v-sheet class="user-panel d-flex flex-column" :style="panelStyle">
    <PanelHeader />

    <AdminActionSections
        :currentType="currentType"
        :itemsCount="items.length"
        @load="loadData"
        @openAdmin="openAdmin"
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
import ResultsList from "../components/panel/ResultList.vue";
import DetailsDialog from "../components/panel/DetailsDialog.vue";
import AdminActionSections from "../components/panel/AdminActionSections.vue";

const router = useRouter();

const items = ref<any[]>([]);
const currentTitle = ref("Data");
const currentType = ref<string>("");
const currentPage = ref(1);

const winW = ref(window.innerWidth);
const itemsPerPage = ref(3);

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

function openAdmin(page: "bookings" | "desks" | "statistics" | "map" | "settings"| "deleted-desks") {
  const role = localStorage.getItem("role");

  if (String(role).toUpperCase() !== "ADMIN") {
    snackbar.value = {
      show: true,
      message: "Admin access required",
      color: "warning",
    };
    return;
  }

  const validPages = ["bookings", "desks", "statistics", "map", "settings", "deleted-desks"];
  const path = validPages.includes(page)
      ? `/admin-dashboard/${page}`
      : "/admin-dashboard";

  router.push(path);
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
