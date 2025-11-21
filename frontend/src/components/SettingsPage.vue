<template>
  <div class="settings-container">
    <div class="settings-card">
      <!-- Header -->
      <div class="settings-header">
        <div class="header-title-section">
          <div class="header-label">ADMIN PANEL</div>
          <h2 class="header-title">Settings</h2>
          <span class="header-subtitle">Configure system-wide booking policies and desk colors</span>
        </div>
      </div>

      <!-- Loading State -->
      <div v-if="loading" class="loading-container">
        <v-progress-circular indeterminate size="48" width="4" color="#171717" />
        <p class="loading-message mt-3">Loading settings...</p>
      </div>

      <!-- Error State -->
      <v-alert v-else-if="globalError" type="error" variant="tonal" class="mb-4" density="compact" closable>
        {{ globalError }}
        <template #append>
          <v-btn variant="text" size="small" @click="fetchAllData">Retry</v-btn>
        </template>
      </v-alert>

      <!-- Main Content -->
      <div v-else class="settings-content">

        <!-- SECTION 1: BOOKING TIME LIMITS -->
        <div class="settings-section">
          <div class="section-header">
            <v-icon color="#171717" size="20">mdi-clock-outline</v-icon>
            <h3 class="section-title">Booking Time Limits</h3>
            <v-chip color="#f5f5f5" size="x-small" class="info-chip ml-auto">
              <v-icon start size="x-small">mdi-information-outline</v-icon>
              Fixed: 1-8 hours per booking
            </v-chip>
          </div>

          <form @submit.prevent="saveBookingLimits">
            <div class="form-row">
              <v-text-field
                  v-model.number="limitsForm.maxDaysInAdvance"
                  label="Maximum Days in Advance"
                  type="number"
                  min="1"
                  max="365"
                  variant="outlined"
                  density="compact"
                  :error-messages="limitsErrors.maxDaysInAdvance"
                  @input="validateLimitsField('maxDaysInAdvance')"
                  hide-details="auto"
              >
                <template #append-inner>
                  <v-chip size="x-small" color="#171717" class="current-chip">
                    Current: {{ originalLimits?.maxDaysInAdvance || 30 }}
                  </v-chip>
                </template>
              </v-text-field>

              <v-text-field
                  v-model.number="limitsForm.maxHoursPerWeek"
                  label="Maximum Weekly Hours"
                  type="number"
                  min="1"
                  max="168"
                  variant="outlined"
                  density="compact"
                  :error-messages="limitsErrors.maxHoursPerWeek"
                  @input="validateLimitsField('maxHoursPerWeek')"
                  hide-details="auto"
              >
                <template #append-inner>
                  <v-chip size="x-small" color="#171717" class="current-chip">
                    Current: {{ originalLimits?.maxHoursPerWeek || 20 }}
                  </v-chip>
                </template>
              </v-text-field>
            </div>

            <!-- Inline notification -->
            <transition name="slide-down">
              <v-alert
                  v-if="limitsNotification"
                  :type="limitsNotification.type === 'success' ? 'success' : 'error'"
                  variant="tonal"
                  density="compact"
                  class="mt-2"
              >
                {{ limitsNotification.message }}
              </v-alert>
            </transition>

            <div class="button-group">
              <v-btn variant="outlined" color="#171717" size="small" @click="resetLimitsForm" :disabled="!hasLimitsChanges">
                Reset
              </v-btn>
              <v-btn
                  type="submit"
                  color="#171717"
                  variant="flat"
                  size="small"
                  :disabled="!isLimitsFormValid || limitsSaving || !hasLimitsChanges"
                  :loading="limitsSaving"
              >
                Save Changes
              </v-btn>
            </div>
          </form>
        </div>

        <!-- SECTION 2: EDIT DESK COLORS -->
        <div class="settings-section">
          <div class="section-header">
            <v-icon color="#171717" size="20">mdi-pencil-outline</v-icon>
            <h3 class="section-title">Edit Desk Colors</h3>
          </div>

          <form @submit.prevent="updateColor">
            <v-select
                v-model="selectedColorId"
                :items="deskColors"
                item-title="colorName"
                item-value="id"
                label="Select Color to Edit"
                variant="outlined"
                density="compact"
                @update:model-value="loadSelectedColor"
                class="mb-2"
            >
              <template #item="{ props, item }">
                <v-list-item v-bind="props">
                  <template #prepend>
                    <div
                        class="color-preview"
                        :style="{ backgroundColor: item.raw.colorCode }"
                    ></div>
                  </template>
                  <template #subtitle>
                    {{ item.raw.colorCode }}
                  </template>
                </v-list-item>
              </template>
            </v-select>

            <div v-if="selectedColorId" class="form-row">
              <v-text-field
                  v-model="editForm.colorName"
                  label="Color Name"
                  variant="outlined"
                  density="compact"
                  maxlength="50"
                  required
              />

              <v-text-field
                  v-model="editForm.colorCode"
                  label="Color Code"
                  variant="outlined"
                  density="compact"
                  maxlength="7"
                  placeholder="#00FF00"
                  required
              >
                <template #append-inner>
  <div
  class="color-picker-swatch"
  :style="{ backgroundColor: editForm.colorCode }"
  @click.stop="showColorPickerEdit = true"
></div>

  <v-dialog v-model="showColorPickerEdit" width="320">
    <v-card>
      <v-card-title>Select Color</v-card-title>

      <v-color-picker
  :model-value="editForm.colorCode"
  @update:model-value="updateEditColor"
  mode="hex"
  show-swatches
/>

      <v-card-actions>
        <v-btn variant="text" @click="showColorPickerEdit = false">
          Cancel
        </v-btn>
        <v-btn color="#171717" variant="flat" @click="showColorPickerEdit = false">
          OK
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
                </template>
              </v-text-field>
            </div>

            <v-text-field
                v-if="selectedColorId"
                v-model="editForm.colorMeaning"
                label="Color Meaning"
                variant="outlined"
                density="compact"
                maxlength="255"
                class="mt-2"
                required
            />

            <!-- Inline notification -->
            <transition name="slide-down">
              <v-alert
                  v-if="editNotification && selectedColorId"
                  :type="editNotification.type === 'success' ? 'success' : 'error'"
                  variant="tonal"
                  density="compact"
                  class="mt-2"
              >
                {{ editNotification.message }}
              </v-alert>
            </transition>

            <div v-if="selectedColorId" class="button-group">
              <v-btn variant="outlined" color="#171717" size="small" @click="resetEditForm">
                Reset
              </v-btn>
              <v-btn type="submit" color="#171717" variant="flat" size="small" :disabled="editSaving" :loading="editSaving">
                Update Color
              </v-btn>
              <v-btn color="error" variant="flat" size="small" @click="confirmDeleteColor" :disabled="editSaving">
                Delete
              </v-btn>
            </div>
          </form>
        </div>

        <!-- SECTION 3: ADD NEW DESK COLOR -->
        <div class="settings-section">
          <div class="section-header">
            <v-icon color="#171717" size="20">mdi-plus-circle-outline</v-icon>
            <h3 class="section-title">Add New Desk Color</h3>
          </div>

          <form @submit.prevent="createColor">
            <div class="form-row">
              <v-text-field
                  v-model="newForm.colorName"
                  label="Color Name"
                  variant="outlined"
                  density="compact"
                  maxlength="50"
                  placeholder="e.g., PURPLE"
                  required
              />

              <v-text-field
                  v-model="newForm.colorCode"
                  label="Color Code"
                  variant="outlined"
                  density="compact"
                  maxlength="7"
                  placeholder="#800080"
                  required
              >
                <template #append-inner>
  <div
  class="color-picker-swatch"
  :style="{ backgroundColor: newForm.colorCode }"
  @click.stop="showColorPickerNew = true"
></div>

  <v-dialog v-model="showColorPickerNew" width="320">
    <v-card>
      <v-card-title>Select Color</v-card-title>

      <v-color-picker
        v-model="newForm.colorCode"
        mode="hexa"
        hide-inputs="false"
        show-swatches
      />

      <v-card-actions>
        <v-btn variant="text" @click="showColorPickerNew = false">
          Cancel
        </v-btn>
        <v-btn color="#171717" variant="flat" @click="showColorPickerNew = false">
          OK
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>
              </v-text-field>
            </div>

            <v-text-field
                v-model="newForm.colorMeaning"
                label="Color Meaning"
                variant="outlined"
                density="compact"
                maxlength="255"
                placeholder="What does this color represent?"
                class="mt-2"
                required
            />

            <!-- Inline notification -->
            <transition name="slide-down">
              <v-alert
                  v-if="newNotification"
                  :type="newNotification.type === 'success' ? 'success' : 'error'"
                  variant="tonal"
                  density="compact"
                  class="mt-2"
              >
                {{ newNotification.message }}
              </v-alert>
            </transition>

            <div class="button-group">
              <v-btn variant="outlined" color="#171717" size="small" @click="resetNewForm">
                Reset
              </v-btn>
              <v-btn type="submit" color="#171717" variant="flat" size="small" :disabled="newSaving" :loading="newSaving">
                Add Color
              </v-btn>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- Custom Confirm Dialog -->
    <v-dialog v-model="showConfirmDialog" max-width="500">
      <v-card class="delete-dialog">
        <v-card-title class="dialog-title">
          <v-icon color="error" class="mr-2">mdi-alert-circle</v-icon>
          Confirm Deletion
        </v-card-title>
        <v-card-text class="dialog-text">
          Are you sure you want to delete "<strong>{{ colorToDelete?.colorName }}</strong>"?
          This action cannot be undone.
        </v-card-text>
        <v-card-actions class="dialog-actions">
          <v-spacer></v-spacer>
          <v-btn variant="text" @click="cancelDelete">Cancel</v-btn>
          <v-btn color="error" variant="flat" @click="proceedDelete">Delete</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script>
import api from '../plugins/axios';

export default {
  name: "AdminSettings",

  data() {
    return {
      loading: true,
      globalError: null,

      // Booking Limits
      limitsForm: {
        maxDaysInAdvance: 30,
        maxHoursPerWeek: 20,
      },
      originalLimits: null,
      limitsSaving: false,
      limitsErrors: {},
      limitsNotification: null,
      limitsNotificationTimeout: null,

      // Desk Colors
      deskColors: [],

      // Edit Color
      selectedColorId: null,
      editForm: {
        colorName: '',
        colorCode: '#000000',
        colorMeaning: ''
      },
      originalEditForm: null,
      editSaving: false,
      editNotification: null,
      editNotificationTimeout: null,

      // New Color
      newForm: {
        colorName: '',
        colorCode: '#000000',
        colorMeaning: ''
      },
      newSaving: false,
      newNotification: null,
      newNotificationTimeout: null,

      // Confirm dialog
      showConfirmDialog: false,
      colorToDelete: null,
      //ColorPicker
      showColorPickerEdit: false,
showColorPickerNew: false,
    };
  },

  computed: {
    isLimitsFormValid() {
      return (
          this.limitsForm.maxDaysInAdvance > 0 &&
          this.limitsForm.maxDaysInAdvance <= 365 &&
          this.limitsForm.maxHoursPerWeek > 0 &&
          this.limitsForm.maxHoursPerWeek <= 168 &&
          Object.keys(this.limitsErrors).length === 0
      );
    },

    hasLimitsChanges() {
      if (!this.originalLimits) return false;
      return (
          this.limitsForm.maxDaysInAdvance !== this.originalLimits.maxDaysInAdvance ||
          this.limitsForm.maxHoursPerWeek !== this.originalLimits.maxHoursPerWeek
      );
    }
  },

  mounted() {
    this.fetchAllData();
    document.addEventListener('keydown', this.handleEscKey);
  },

  beforeUnmount() {
    document.removeEventListener('keydown', this.handleEscKey);
    if (this.limitsNotificationTimeout) clearTimeout(this.limitsNotificationTimeout);
    if (this.editNotificationTimeout) clearTimeout(this.editNotificationTimeout);
    if (this.newNotificationTimeout) clearTimeout(this.newNotificationTimeout);
  },

  methods: {
    // Inline notifications
    showLimitsNotification(message, type = 'success') {
      if (this.limitsNotificationTimeout) {
        clearTimeout(this.limitsNotificationTimeout);
      }

      this.limitsNotification = { message, type };

      if (type === 'success') {
        this.limitsNotificationTimeout = setTimeout(() => {
          this.limitsNotification = null;
        }, 3000);
      }
    },

    showEditNotification(message, type = 'success') {
      if (this.editNotificationTimeout) {
        clearTimeout(this.editNotificationTimeout);
      }

      this.editNotification = { message, type };

      if (type === 'success') {
        this.editNotificationTimeout = setTimeout(() => {
          this.editNotification = null;
        }, 3000);
      }
    },

    showNewNotification(message, type = 'success') {
      if (this.newNotificationTimeout) {
        clearTimeout(this.newNotificationTimeout);
      }

      this.newNotification = { message, type };

      if (type === 'success') {
        this.newNotificationTimeout = setTimeout(() => {
          this.newNotification = null;
        }, 3000);
      }
    },

    // Fetch all data
    async fetchAllData() {
      this.loading = true;
      this.globalError = null;

      try {
        await Promise.all([
          this.fetchBookingLimits(),
          this.fetchDeskColors()
        ]);
      } catch (err) {
        this.globalError = 'Failed to load settings data';
        console.error(err);
      } finally {
        this.loading = false;
      }
    },

    // Booking Limits
    async fetchBookingLimits() {
      const response = await api.get('/admin/booking-time-limits');
      this.limitsForm = {
        maxDaysInAdvance: response.data.maxDaysInAdvance,
        maxHoursPerWeek: response.data.maxHoursPerWeek,
      };
      this.originalLimits = { ...this.limitsForm };
    },

    validateLimitsField(field) {
      delete this.limitsErrors[field];

      if (field === 'maxDaysInAdvance') {
        const val = this.limitsForm.maxDaysInAdvance;
        if (!val || val < 1) {
          this.limitsErrors.maxDaysInAdvance = 'Must be at least 1 day';
        } else if (val > 365) {
          this.limitsErrors.maxDaysInAdvance = 'Cannot exceed 365 days';
        }
      }

      if (field === 'maxHoursPerWeek') {
        const val = this.limitsForm.maxHoursPerWeek;
        if (!val || val < 1) {
          this.limitsErrors.maxHoursPerWeek = 'Must be at least 1 hour';
        } else if (val > 168) {
          this.limitsErrors.maxHoursPerWeek = 'Cannot exceed 168 hours';
        }
      }

      this.limitsErrors = { ...this.limitsErrors };
    },

    async saveBookingLimits() {
      this.limitsSaving = true;

      try {
        const response = await api.put('/admin/booking-time-limits', this.limitsForm);
        this.limitsForm = { ...response.data };
        this.originalLimits = { ...this.limitsForm };
        this.showLimitsNotification('Booking limits updated successfully!', 'success');
      } catch (err) {
        console.error(err);
        this.showLimitsNotification(err.response?.data?.message || 'Failed to update booking limits', 'error');
      } finally {
        this.limitsSaving = false;
      }
    },

    resetLimitsForm() {
      if (this.originalLimits) {
        this.limitsForm = { ...this.originalLimits };
        this.limitsErrors = {};
        this.limitsNotification = null;
      }
    },

    // Desk Colors
    async fetchDeskColors() {
      const response = await api.get('/admin/desk-colors');
      this.deskColors = response.data;
    },

    loadSelectedColor() {
      const color = this.deskColors.find(c => c.id === this.selectedColorId);
      if (color) {
        this.editForm = { ...color };
        if (!this.originalEditForm || this.originalEditForm.id !== color.id) {
          this.originalEditForm = JSON.parse(JSON.stringify(color));
        }
      } else {
        this.resetEditForm();
      }
      this.editNotification = null;
    },

    resetEditForm() {
      this.selectedColorId = null;
      this.editForm = {
        colorName: '',
        colorCode: '#000000',
        colorMeaning: ''
      };
      this.originalEditForm = null;
      this.editNotification = null;
    },

    async updateColor() {
      if (!this.selectedColorId) {
        this.showEditNotification('Please select a color to edit', 'error');
        return;
      }

      this.editSaving = true;

      try {
        const response = await api.put(`/admin/desk-colors/${this.selectedColorId}`, {
          colorName: this.editForm.colorName,
          colorCode: this.editForm.colorCode,
          colorMeaning: this.editForm.colorMeaning
        });

        const index = this.deskColors.findIndex(c => c.id === this.selectedColorId);
        if (index !== -1) {
          this.deskColors.splice(index, 1, response.data);
        }

        this.originalEditForm = { ...response.data };
        this.editForm = { ...response.data };
        this.showEditNotification('Color updated successfully!', 'success');
      } catch (err) {
        console.error(err);
        this.showEditNotification(err.response?.data?.message || 'Failed to update color', 'error');
      } finally {
        this.editSaving = false;
      }
    },

    confirmDeleteColor() {
      if (!this.selectedColorId) return;

      const color = this.deskColors.find(c => c.id === this.selectedColorId);
      if (!color) return;

      this.colorToDelete = color;
      this.showConfirmDialog = true;
    },

    cancelDelete() {
      this.showConfirmDialog = false;
      this.colorToDelete = null;
    },

    async proceedDelete() {
      this.showConfirmDialog = false;
      this.editSaving = true;

      try {
        await api.delete(`/admin/desk-colors/${this.selectedColorId}`);
        this.deskColors = this.deskColors.filter(c => c.id !== this.selectedColorId);
        this.showEditNotification('Color deleted successfully!', 'success');

        setTimeout(() => {
          this.selectedColorId = null;
          this.resetEditForm();
        }, 2000);
      } catch (err) {
        console.error(err);
        this.showEditNotification(err.response?.data?.message || 'Failed to delete color', 'error');
      } finally {
        this.editSaving = false;
        this.colorToDelete = null;
      }
    },

    // Add New Color
    resetNewForm() {
      this.newForm = {
        colorName: '',
        colorCode: '#000000',
        colorMeaning: ''
      };
      this.newNotification = null;
    },

    async createColor() {
      this.newSaving = true;

      try {
        const response = await api.post('/admin/desk-colors', this.newForm);
        this.deskColors.push(response.data);
        this.showNewNotification('New color added successfully!', 'success');

        setTimeout(() => {
          this.resetNewForm();
        }, 2000);
      } catch (err) {
        console.error(err);
        this.showNewNotification(err.response?.data?.message || 'Failed to add new color', 'error');
      } finally {
        this.newSaving = false;
      }
    },

    handleEscKey(event) {
      if (event.key === 'Escape' && this.showConfirmDialog) {
        this.cancelDelete();
      }
    },
    updateEditColor(color) {
  // Convert color object to hex string
  if (typeof color === 'string') {
    this.editForm.colorCode = color.toUpperCase();
  } else if (color && color.hex) {
    this.editForm.colorCode = color.hex.toUpperCase();
  }
},

updateNewColor(color) {
  // Convert color object to hex string
  if (typeof color === 'string') {
    this.newForm.colorCode = color.toUpperCase();
  } else if (color && color.hex) {
    this.newForm.colorCode = color.hex.toUpperCase();
  }
}
  }
};
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap");

* {
  font-family: "Inter", -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

.settings-container {
  padding: 28px;
  background: #fafafa;
  min-height: 100vh;
}

.settings-card {
  background: #ffffff;
  border: 1px solid #e5e5e5;
  border-radius: 20px;
  padding: 28px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
  max-width: 1600px;
  margin: 0 auto;
}

/* Header */
.settings-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 2px solid #f5f5f5;
}

.header-title-section {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.header-label {
  font-size: 11px;
  font-weight: 700;
  color: #737373;
  letter-spacing: 1.5px;
}

.header-title {
  font-size: 28px;
  font-weight: 800;
  color: #171717;
  margin: 0;
  letter-spacing: -0.5px;
  line-height: 1;
}

.header-subtitle {
  font-size: 13px;
  font-weight: 600;
  color: #737373;
  letter-spacing: 0.3px;
}

/* Loading */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
}

.loading-message {
  font-size: 15px;
  font-weight: 600;
  color: #737373;
  letter-spacing: 0.3px;
}

/* Settings Content - Grid Layout */
.settings-content {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(500px, 1fr));
  gap: 16px;
}

/* Settings Section */
.settings-section {
  background: #fafafa;
  border: 1px solid #f5f5f5;
  border-radius: 16px;
  padding: 16px;
  transition: all 0.2s ease;
}

.settings-section:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04);
}

.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  padding-bottom: 10px;
  border-bottom: 2px solid #f5f5f5;
}

.section-title {
  font-size: 16px;
  font-weight: 700;
  color: #171717;
  margin: 0;
  letter-spacing: -0.3px;
}

/* Info Chip */
.info-chip {
  font-weight: 600 !important;
  font-size: 11px !important;
  letter-spacing: 0.3px;
}

/* Form Row - Horizontal Layout */
.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-bottom: 8px;
}

/* Vuetify Field Customization */
:deep(.v-field) {
  border-radius: 12px !important;
  font-weight: 600;
}

:deep(.v-field--focused) {
  box-shadow: 0 0 0 3px rgba(0, 0, 0, 0.05);
}

:deep(.v-label) {
  font-weight: 600;
  font-size: 13px;
}

:deep(.v-input--density-compact .v-field) {
  --v-input-control-height: 40px;
}

/* Current Chip */
.current-chip {
  font-weight: 700 !important;
  font-size: 10px !important;
  letter-spacing: 0.3px;
  color: white !important;
}

/* Color Picker */
.color-picker-swatch {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  border: 2px solid #e5e5e5;
  cursor: pointer;
  transition: all 0.2s ease;
}

.color-picker-swatch:hover {
  border-color: #171717;
  transform: scale(1.05);
}

/* Color Preview */
.color-preview {
  width: 24px;
  height: 24px;
  border-radius: 6px;
  border: 2px solid #e5e5e5;
}

/* Buttons */
.button-group {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-top: 10px;
}

:deep(.v-btn) {
  font-weight: 600 !important;
  text-transform: none !important;
  letter-spacing: 0.3px;
  border-radius: 12px !important;
  transition: all 0.2s ease;
}

:deep(.v-btn:not(:disabled):hover) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12) !important;
}

:deep(.v-btn--variant-outlined) {
  border-width: 2px !important;
}

/* Delete Dialog */
.delete-dialog {
  border-radius: 16px !important;
}

.dialog-title {
  font-weight: 700;
  font-size: 20px;
  color: #171717;
  padding: 24px 24px 16px;
}

.dialog-text {
  font-size: 15px;
  color: #525252;
  padding: 0 24px 16px;
  line-height: 1.6;
}

.dialog-actions {
  padding: 16px 24px 24px;
}

/* Animations */
.slide-down-enter-active,
.slide-down-leave-active {
  transition: all 0.3s ease;
}

.slide-down-enter-from {
  opacity: 0;
  transform: translateY(-10px);
}

.slide-down-leave-to {
  opacity: 0;
  transform: translateY(-5px);
}

/* Responsive */
@media (max-width: 1200px) {
  .settings-content {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .settings-container {
    padding: 16px;
  }

  .settings-card {
    padding: 20px;
  }

  .header-title {
    font-size: 24px;
  }

  .form-row {
    grid-template-columns: 1fr;
  }

  .button-group {
    flex-direction: column;
  }

  :deep(.v-btn) {
    width: 100%;
  }
}
</style>