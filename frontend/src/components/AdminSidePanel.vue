<template>
  <v-navigation-drawer
    v-model="isOpen"
    :rail="!isExpanded"
    :rail-width="72"
    :width="drawerWidth"
    location="left"
    class="user-panel"
    elevation="4"
    :order="0"
  >
    <PanelHeader v-if="isExpanded" @toggle="togglePanel" />

    <AdminActionSections
        v-if="isExpanded"
        :currentType="currentType"
        :itemsCount="items.length"
        @openAdmin="openAdmin"
        @logout="logout"
    />
    <div v-if="!isExpanded" class="rail-icons">
      <v-tooltip location="right">
        <template v-slot:activator="{ props }">
          <v-btn
            icon
            variant="text"
            class="rail-icon-btn toggle-rail-btn"
            v-bind="props"
            @click="togglePanel"
          >
            <v-icon size="24">mdi-chevron-right</v-icon>
          </v-btn>
        </template>
        <span>Expand Menu</span>
      </v-tooltip>

      <v-divider class="my-2 mx-2"></v-divider>

      <v-tooltip location="left">
        <template v-slot:activator="{ props }">
          <v-btn
            icon
            variant="text"
            class="rail-icon-btn gradient-btn"
            v-bind="props"
            @click="$router.push('/dashboard')"
          >
            <v-icon size="24">mdi-account-convert</v-icon>
          </v-btn>
        </template>
        <span>Switch to User View</span>
      </v-tooltip>

      <v-tooltip location="left">
        <template v-slot:activator="{ props }">
          <v-btn
            icon
            variant="text"
            class="rail-icon-btn"
            v-bind="props"
            @click="openAdmin('map')"
          >
            <v-icon size="24">mdi-map-clock-outline</v-icon>
          </v-btn>
        </template>
        <span>Map Settings</span>
      </v-tooltip>

      <v-tooltip location="left">
        <template v-slot:activator="{ props }">
          <v-btn
            icon
            variant="text"
            class="rail-icon-btn"
            v-bind="props"
            @click="openAdmin('bookings')"
          >
            <v-icon size="24">mdi-clipboard-text-clock</v-icon>
          </v-btn>
        </template>
        <span>All Bookings</span>
      </v-tooltip>

      <v-tooltip location="left">
        <template v-slot:activator="{ props }">
          <v-btn
            icon
            variant="text"
            class="rail-icon-btn"
            v-bind="props"
            @click="openAdmin('desks')"
          >
            <v-icon size="24">mdi-desk</v-icon>
          </v-btn>
        </template>
        <span>All Desks</span>
      </v-tooltip>

      <v-tooltip location="left">
        <template v-slot:activator="{ props }">
          <v-btn
            icon
            variant="text"
            class="rail-icon-btn"
            v-bind="props"
            @click="openAdmin('users')"
          >
            <v-icon size="24">mdi-account-group</v-icon>
          </v-btn>
        </template>
        <span>All Users</span>
      </v-tooltip>

      <v-tooltip location="left">
        <template v-slot:activator="{ props }">
          <v-btn
            icon
            variant="text"
            class="rail-icon-btn"
            v-bind="props"
            @click="openAdmin('statistics')"
          >
            <v-icon size="24">mdi-chart-bar</v-icon>
          </v-btn>
        </template>
        <span>Statistics</span>
      </v-tooltip>

      <v-tooltip location="left">
        <template v-slot:activator="{ props }">
          <v-btn
            icon
            variant="text"
            class="rail-icon-btn"
            v-bind="props"
            @click="openAdmin('settings')"
          >
            <v-icon size="24">mdi-cog</v-icon>
          </v-btn>
        </template>
        <span>Settings</span>
      </v-tooltip>

      <v-tooltip location="left">
        <template v-slot:activator="{ props }">
          <v-btn
            icon
            variant="text"
            class="rail-icon-btn"
            v-bind="props"
            @click="openAdmin('deleted-desks')"
          >
            <v-icon size="24">mdi-delete-empty</v-icon>
          </v-btn>
        </template>
        <span>Deleted Desks</span>
      </v-tooltip>

      <v-tooltip location="left">
        <template v-slot:activator="{ props }">
          <v-btn
            icon
            variant="text"
            class="rail-icon-btn"
            v-bind="props"
            @click="openAdmin('background-gallery')"
          >
            <v-icon size="24">mdi-camera-burst</v-icon>
          </v-btn>
        </template>
        <span>Background gallery</span>
      </v-tooltip>

      <v-divider class="my-2 mx-2"></v-divider>

      <v-tooltip location="left">
        <template v-slot:activator="{ props }">
          <v-btn
            icon
            variant="text"
            class="rail-icon-btn danger"
            v-bind="props"
            @click="logout"
          >
            <v-icon size="24">mdi-logout</v-icon>
          </v-btn>
        </template>
        <span>Logout</span>
      </v-tooltip>
    </div>

    <v-slide-y-transition>
      <ResultsList
          v-if="items.length > 0 && isExpanded"
          :title="currentTitle"
          :items="items"
          :page="currentPage"
          :perPage="itemsPerPage"
          :currentType="currentType"
          @page="(p) => (currentPage = p)"
          @details="openDetails"
      />
    </v-slide-y-transition>

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
  </v-navigation-drawer>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount, watch } from "vue";
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

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: true
  }
});

const emit = defineEmits(['update:modelValue', 'update:expanded', 'update:drawerWidth']);

// Control panel expansion state
const isExpanded = ref(true);
const isOpen = ref(true);

// Computed width based on screen size
const drawerWidth = computed(() => {
  const vwWidth = Math.round(winW.value * 0.34);
  return Math.min(Math.max(vwWidth, 360), 500);
});

// Current effective width (rail or full)
const currentWidth = computed(() => {
  return isExpanded.value ? drawerWidth.value : 72;
});

// Watch and emit width changes
watch(currentWidth, (width) => {
  emit('update:drawerWidth', width);
}, { immediate: true });

// Toggle panel expansion
const togglePanel = () => {
  isExpanded.value = !isExpanded.value;
  emit('update:expanded', isExpanded.value);
};

// Watch modelValue changes
watch(() => props.modelValue, (val) => {
  isOpen.value = val;
});

watch(isOpen, (val) => {
  emit('update:modelValue', val);
});

onMounted(() => {
  updateLayout();
  window.addEventListener("resize", updateLayout);
});
onBeforeUnmount(() => window.removeEventListener("resize", updateLayout));

const panelStyle = computed(() => {
  const vwWidth = Math.round(window.innerWidth * 0.34);
  const w = Math.min(Math.max(vwWidth, 360), 400); // Changes the width of the side panel based on the screen size
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

function openAdmin(page: "bookings" | "desks" | "users" |"statistics" | "map" | "settings"| "deleted-desks" | "background-gallery") {
  const role = localStorage.getItem("role");

  if (String(role).toUpperCase() !== "ADMIN") {
    snackbar.value = {
      show: true,
      message: "Admin access required",
      color: "warning",
    };
    return;
  }

  const validPages = ["bookings", "desks", "users","statistics", "map", "settings", "deleted-desks", "background-gallery"];
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
  background: var(--surface) !important;
  border-right: 1px solid var(--panel-sep);
  height: 100vh;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}

.rail-icons {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12px 0;
  gap: 8px;
  flex: 1;
}

.rail-icon-btn {
  width: 48px;
  height: 48px;
  border-radius: 12px !important;
  transition: all 0.2s ease;
  color: var(--text-1) !important;
}

.rail-icon-btn.toggle-rail-btn {
  background: rgba(255, 138, 0, 0.05) !important;
  border: 1px solid rgba(255, 138, 0, 0.2);
}

.rail-icon-btn.toggle-rail-btn:hover {
  background: rgba(255, 138, 0, 0.15) !important;
  border-color: var(--accent);
}

.rail-icon-btn:hover {
  background: rgba(255, 138, 0, 0.1) !important;
  transform: translateX(-2px);
}

.rail-icon-btn.danger {
  color: #b91c1c !important;
}

.rail-icon-btn.danger:hover {
  background: rgba(185, 28, 28, 0.1) !important;
}

.snackbar-text {
  font-size: 0.9rem;
  font-weight: 750;
}

/* Ensure proper z-index for tooltips */
:deep(.v-overlay) {
  z-index: 9999 !important;
}

@media (max-width: 900px) {
  .user-panel {
    width: 100% !important;
  }
  
  .user-panel:deep(.v-navigation-drawer--rail) {
    width: 72px !important;
  }
}
</style>