<script setup lang="ts">
import { reactive, ref, watch, computed } from "vue";
import api from "../../plugins/axios";

interface Props {
  show: boolean;
}

interface Emits {
  (e: "close"): void;
  (e: "created"): void;
}

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

// Form state
const bookingForm = reactive({
  guest: false,
  email: "",
  deskId: 0,
  startTime: "",
  endTime: "",
});

// Desk lookup
const deskNameInput = ref<string>("");
const deskName = ref<string>("");
const deskZone = ref<string>("");
const deskType = ref<string>("");
const loadingDesk = ref(false);
const deskError = ref<string>("");
const fetchedDeskId = ref<number>(0);

// General
const error = ref<string>("");
const isProcessing = ref(false);

// Computed: Can create booking
const canCreate = computed(() => {
  const hasValidUser = bookingForm.guest || bookingForm.email.trim().length > 0;
  return (
      hasValidUser &&
      fetchedDeskId.value > 0 &&
      bookingForm.startTime &&
      bookingForm.endTime
  );
});

// Debounce timer for desk lookup
let deskDebounceTimer: ReturnType<typeof setTimeout> | null = null;
watch(deskNameInput, (newDeskName) => {
  // Clear existing timer
  if (deskDebounceTimer) {
    clearTimeout(deskDebounceTimer);
  }

  if (newDeskName && newDeskName.trim().length > 0) {
    // Debounce the API call
    deskDebounceTimer = setTimeout(async () => {
      await fetchDeskDetailsByName(newDeskName.trim());
    }, 500); // Wait 500ms after user stops typing
  } else {
    deskName.value = "";
    deskZone.value = "";
    deskType.value = "";
    deskError.value = "";
    fetchedDeskId.value = 0;
    bookingForm.deskId = 0;
  }
});

// Reset form when modal closes
watch(() => props.show, (isOpen) => {
  if (!isOpen) {
    resetForm();
  }
});

// Fetch desk by name (reuse from BookingEditModal)
async function fetchDeskDetailsByName(name: string) {
  try {
    loadingDesk.value = true;
    deskError.value = "";
    const response = await api.get(`/admin/desks`);
    const allDesks = response.data;

    const matchedDesk = allDesks.find((desk: any) =>
        (desk.displayName || desk.name || "").toLowerCase().includes(name.toLowerCase())
    );

    if (matchedDesk) {
      fetchedDeskId.value = matchedDesk.id;
      bookingForm.deskId = matchedDesk.id;
      deskName.value = matchedDesk.displayName || "Unknown Desk";
      deskZone.value = matchedDesk.zoneDto?.zoneName || "Unknown Zone";
      deskType.value = matchedDesk.type || "Unknown";
    } else {
      deskError.value = "Desk not found";
      deskName.value = "";
      deskZone.value = "";
      deskType.value = "";
      fetchedDeskId.value = 0;
      bookingForm.deskId = 0;
    }
  } catch (err: any) {
    console.error("Error fetching desk details:", err);
    deskError.value = "Desk not found";
    deskName.value = "";
    deskZone.value = "";
    deskType.value = "";
    fetchedDeskId.value = 0;
    bookingForm.deskId = 0;
  } finally {
    loadingDesk.value = false;
  }
}

// Create booking
async function handleCreate() {
  if (!canCreate.value) return;

  try {
    isProcessing.value = true;
    error.value = "";

    const payload = {
      email: bookingForm.guest ? null : bookingForm.email.trim(),
      guest: bookingForm.guest,
      deskId: bookingForm.deskId,
      startTime: bookingForm.startTime,
      endTime: bookingForm.endTime,
    };

    await api.post("/admin/booking/create", payload);

    emit("created");
    closeModal();
  } catch (err: any) {
    console.error("Error creating booking:", err);
    error.value = err.response?.data?.message || "Failed to create booking";
  } finally {
    isProcessing.value = false;
  }
}

function resetForm() {
  bookingForm.email = "";
  bookingForm.guest = false;
  bookingForm.deskId = 0;
  bookingForm.startTime = "";
  bookingForm.endTime = "";

  deskNameInput.value = "";
  deskName.value = "";
  deskZone.value = "";
  deskType.value = "";
  fetchedDeskId.value = 0;
  deskError.value = "";

  error.value = "";
}

function closeModal() {
  emit("close");
}
</script>

<template>
  <v-dialog
      :model-value="show"
      @update:model-value="$event ? null : closeModal()"
      max-width="550"
      transition="dialog-bottom-transition"
      persistent
      scrollable
  >
    <v-card class="booking-card">
      <!-- Header Section -->
      <v-card-title class="card-header">
        <div class="header-content">
          <div class="d-flex align-center">
            <v-avatar color="grey-lighten-4" class="mr-4" rounded="lg">
              <v-icon color="#171717">mdi-calendar-plus</v-icon>
            </v-avatar>
            <div class="header-info">
              <div class="workspace-label">ADMIN BOOKING</div>
              <div class="desk-title">Create New Booking</div>
            </div>
          </div>
          <v-btn icon variant="text" density="comfortable" @click="closeModal">
            <v-icon>mdi-close</v-icon>
          </v-btn>
        </div>
      </v-card-title>

      <v-card-text class="card-body">
        <!-- Error Alert -->
        <v-expand-transition>
          <v-alert
              v-if="error"
              type="error"
              variant="tonal"
              class="mb-4 border-error"
              density="compact"
              closable
              icon="mdi-alert-circle-outline"
          >
            {{ error }}
          </v-alert>
        </v-expand-transition>

        <v-container class="pa-0">
          <!-- User Email Section -->
          <div class="section mb-6">
            <div class="input-label mb-2">User Email</div>
            <div class="d-flex align-center gap-3">
              <v-text-field
                  v-model="bookingForm.email"
                  type="email"
                  variant="outlined"
                  density="comfortable"
                  :disabled="bookingForm.guest"
                  :placeholder="bookingForm.guest ? 'Guest booking' : 'Enter user email'"
                  hide-details="auto"
                  :messages="bookingForm.guest ? 'A guest can\'t have an email' : ''"
                  class="modern-input flex-grow-1"
                  prepend-inner-icon="mdi-account"
              />
              <v-checkbox
                  v-model="bookingForm.guest"
                  label="Guest"
                  density="compact"
                  hide-details
                  color="#171717"
                  class="guest-checkbox flex-shrink-0"
              />
            </div>
          </div>

          <v-divider class="mb-6 border-opacity-50"></v-divider>

          <!-- Desk Name Section -->
          <div class="section mb-6">
            <div class="input-label">Target Desk</div>
            <v-text-field
                v-model="deskNameInput"
                type="text"
                variant="outlined"
                density="comfortable"
                hide-details
                class="modern-input"
                placeholder="Enter desk name"
                prepend-inner-icon="mdi-desk"
            />
          </div>

          <!-- Desk Validation Feedback -->
          <div class="section mb-6">
            <div v-if="loadingDesk" class="desk-info loading">
              <v-progress-circular
                  indeterminate
                  size="16"
                  width="2"
                  color="#171717"
              />
              <span>Loading desk details...</span>
            </div>
            <div v-else-if="deskError" class="desk-info error">
              <v-icon size="16" color="#ef4444">mdi-alert-circle</v-icon>
              <span>{{ deskError }}</span>
            </div>
            <div v-else-if="deskName" class="desk-info success">
              <v-icon size="16" color="#10b981">mdi-check-circle</v-icon>
              <div class="desk-details">
                <span class="desk-name">{{ deskName }}</span>
                <span class="desk-meta">{{ deskZone }} • {{ deskType }}</span>
              </div>
            </div>
          </div>

          <v-divider class="mb-6 border-opacity-50"></v-divider>

          <!-- Booking Timeline -->
          <div class="input-label mb-2">Booking Timeline</div>
          <v-sheet border rounded="lg" class="pa-4 bg-grey-lighten-5 mb-6">
            <v-row dense>
              <v-col cols="12" sm="6">
                <span class="sub-label">Start Time</span>
                <v-text-field
                    v-model="bookingForm.startTime"
                    type="datetime-local"
                    variant="outlined"
                    density="compact"
                    hide-details
                    class="bg-white"
                    prepend-inner-icon="mdi-clock-start"
                />
              </v-col>
              <v-col cols="12" sm="6">
                <span class="sub-label">End Time</span>
                <v-text-field
                    v-model="bookingForm.endTime"
                    type="datetime-local"
                    variant="outlined"
                    density="compact"
                    hide-details
                    class="bg-white"
                    prepend-inner-icon="mdi-clock-end"
                />
              </v-col>
            </v-row>
          </v-sheet>

          <!-- Summary Box -->
          <v-sheet class="summary-box" elevation="0">
            <div class="d-flex align-center gap-3">
              <div class="summary-icon">
                <v-icon size="24" color="white">mdi-clipboard-text-outline</v-icon>
              </div>
              <div class="flex-grow-1">
                <div class="summary-label">Booking Summary</div>
                <div class="d-flex justify-space-between align-center">
                  <div class="summary-item">
                    <span class="text-grey-lighten-1 text-caption">User Email</span>
                    <div class="text-white font-weight-bold text-truncate" style="max-width: 200px;">{{ bookingForm.email || "—" }}</div>
                  </div>
                  <div class="summary-divider"></div>
                  <div class="summary-item text-right">
                    <span class="text-grey-lighten-1 text-caption">Desk ID</span>
                    <div class="text-white font-weight-bold">{{ fetchedDeskId || "—" }}</div>
                  </div>
                </div>
              </div>
            </div>
          </v-sheet>
        </v-container>
      </v-card-text>

      <!-- Actions -->
      <v-card-actions class="card-actions">
        <v-btn
            variant="text"
            class="cancel-button"
            size="large"
            @click="closeModal"
            :disabled="isProcessing"
        >
          Cancel
        </v-btn>
        <v-btn
            class="confirm-button"
            size="large"
            @click="handleCreate"
            :loading="isProcessing"
            :disabled="!canCreate"
        >
          <v-icon start>mdi-calendar-check</v-icon>
          Create Booking
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>



<style scoped>
/* Copy all styles from BookingEditModal.vue */
@import url("https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap");

.booking-card {
  border-radius: 20px;
  background: #ffffff;
  font-family: "Inter", sans-serif;
  display: flex;
  flex-direction: column;
  max-height: 90vh;
}

.card-header {
  padding: 24px 28px;
  border-bottom: 1px solid #f3f4f6;
  background: #ffffff;
  position: sticky;
  top: 0;
  z-index: 10;
  flex-shrink: 0;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.workspace-label {
  font-size: 10px;
  font-weight: 700;
  color: #9ca3af;
  letter-spacing: 1.5px;
  text-transform: uppercase;
}

.desk-title {
  font-size: 20px;
  font-weight: 700;
  color: #111827;
  line-height: 1.2;
}

.card-body {
  padding: 28px;
  overflow-y: auto;
  overflow-x: hidden;
  flex: 1 1 auto;
  min-height: 0;
}

.card-body::-webkit-scrollbar {
  width: 6px;
}

.card-body::-webkit-scrollbar-track {
  background: #f9fafb;
  border-radius: 10px;
}

.card-body::-webkit-scrollbar-thumb {
  background: #d1d5db;
  border-radius: 10px;
}

.input-label {
  font-size: 13px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 8px;
}

.sub-label {
  font-size: 11px;
  font-weight: 600;
  color: #6b7280;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  display: block;
  margin-bottom: 6px;
}

.modern-input {
  border-radius: 8px;
}

.user-info,
.desk-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  border-radius: 8px;
  font-size: 13px;
}

.user-info.loading,
.desk-info.loading {
  background: #f3f4f6;
  color: #6b7280;
}

.user-info.error,
.desk-info.error {
  background: #fef2f2;
  color: #ef4444;
}

.user-info.success,
.desk-info.success {
  background: #f0fdf4;
  color: #10b981;
}

.user-details,
.desk-details {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.user-name,
.desk-name {
  font-weight: 600;
  color: #111827;
}

.user-meta,
.desk-meta {
  font-size: 11px;
  color: #6b7280;
}

.summary-box {
  background: linear-gradient(135deg, #171717 0%, #404040 100%);
  border-radius: 12px;
  padding: 20px;
  margin-top: 16px;
}

.summary-icon {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 10px;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.summary-label {
  font-size: 11px;
  font-weight: 700;
  color: #9ca3af;
  text-transform: uppercase;
  letter-spacing: 1px;
  margin-bottom: 8px;
}

.summary-item {
  flex: 1;
}

.summary-divider {
  width: 1px;
  height: 32px;
  background: rgba(255, 255, 255, 0.2);
  margin: 0 12px;
}

.card-actions {
  padding: 20px 28px;
  border-top: 1px solid #f3f4f6;
  gap: 12px;
  flex-shrink: 0;
}

.cancel-button {
  flex: 1;
  border-radius: 12px;
  text-transform: none;
  font-weight: 600;
  color: #6b7280;
}

.confirm-button {
  flex: 2;
  background: linear-gradient(135deg, #171717 0%, #404040 100%);
  color: white;
  border-radius: 12px;
  text-transform: none;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(23, 23, 23, 0.15);
}

.confirm-button:hover {
  box-shadow: 0 6px 16px rgba(23, 23, 23, 0.25);
}

.guest-checkbox {
  margin: 0;
  padding: 0;
}

.guest-checkbox :deep(.v-label) {
  font-size: 16px;
  font-weight: 600;
  color: #374151;
  opacity: 1;
}

.guest-checkbox :deep(.v-selection-control) {
  min-height: auto;
}

.guest-checkbox :deep(.v-selection-control__input) {
  transform: scale(1.15);
}
</style>