<template>
  <div class="settings-container">
    <!-- Header -->
    <div class="settings-header">
      <h2>Admin Settings</h2>
      <p class="settings-description">Configure system-wide booking policies and desk colors</p>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading-state">
      <div class="spinner"></div>
      <p>Loading settings...</p>
    </div>

    <!-- Error State -->
    <div v-else-if="globalError" class="error-alert">
      <svg class="error-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
      </svg>
      <div>
        <p class="error-title">Error Loading Settings</p>
        <p class="error-message">{{ globalError }}</p>
        <button @click="fetchAllData" class="retry-button">Retry</button>
      </div>
    </div>

    <!-- Main Content -->
    <div v-else class="settings-content">
      
      <!-- SECTION 1: BOOKING TIME LIMITS -->
      <div class="settings-card">
        <div class="card-header">
          <svg class="card-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M12 6V4m0 2a2 2 0 100 4m0-4a2 2 0 110 4m-6 8a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4m6 6v10m6-2a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4" />
          </svg>
          <h3>Booking Time Limits</h3>
        </div>

        <!-- Info Badge -->
        <div class="info-badge">
          <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <span>Fixed: 1-8 hours per booking</span>
        </div>

        <form @submit.prevent="saveBookingLimits">
          <div class="form-grid">
            <div class="form-field">
              <label>
                Maximum Days in Advance
                <span class="current-value">Current: {{ originalLimits?.maxDaysInAdvance || 30 }}</span>
              </label>
              <input 
                type="number" 
                min="1" 
                max="365" 
                v-model.number="limitsForm.maxDaysInAdvance"
                @input="validateLimitsField('maxDaysInAdvance')"
                :class="{ 'error': limitsErrors.maxDaysInAdvance }"
                placeholder="1-365"
              />
              <transition name="fade">
                <span v-if="limitsErrors.maxDaysInAdvance" class="field-error">
                  {{ limitsErrors.maxDaysInAdvance }}
                </span>
              </transition>
            </div>

            <div class="form-field">
              <label>
                Maximum Weekly Hours
                <span class="current-value">Current: {{ originalLimits?.maxHoursPerWeek || 20 }}</span>
              </label>
              <input 
                type="number" 
                min="1" 
                max="168" 
                v-model.number="limitsForm.maxHoursPerWeek"
                @input="validateLimitsField('maxHoursPerWeek')"
                :class="{ 'error': limitsErrors.maxHoursPerWeek }"
                placeholder="1-168"
              />
              <transition name="fade">
                <span v-if="limitsErrors.maxHoursPerWeek" class="field-error">
                  {{ limitsErrors.maxHoursPerWeek }}
                </span>
              </transition>
            </div>
          </div>

          <div class="button-group">
            <button type="button" class="btn btn-secondary" @click="resetLimitsForm" :disabled="!hasLimitsChanges">
              Reset
            </button>
            <button type="submit" class="btn btn-primary" :disabled="!isLimitsFormValid || limitsSaving || !hasLimitsChanges">
              {{ limitsSaving ? 'Saving...' : 'Save Changes' }}
            </button>
          </div>
        </form>
      </div>

      <!-- SECTION 2: EDIT DESK COLORS -->
      <div class="settings-card">
        <div class="card-header">
          <svg class="card-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                  d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
          </svg>
          <h3>Edit Desk Colors</h3>
        </div>

        <form @submit.prevent="updateColor">
          <div class="form-field full-width">
            <label>Select Color to Edit</label>
            <select v-model="selectedColorId" @change="loadSelectedColor">
              <option :value="null" disabled>-- Choose a color --</option>
              <option v-for="color in deskColors" :key="color.id" :value="color.id">
                {{ color.colorName }} ({{ color.colorCode }})
              </option>
            </select>
          </div>

          <div v-if="selectedColorId" class="form-grid">
            <div class="form-field">
              <label>Color Name</label>
              <input 
                v-model="editForm.colorName" 
                type="text" 
                maxlength="50"
                placeholder="e.g., GREEN"
                required
              />
            </div>

            <div class="form-field">
              <label>Color Code</label>
              <div class="color-input">
                <input 
                  v-model="editForm.colorCode" 
                  type="text" 
                  pattern="^#[0-9A-Fa-f]{6}$"
                  maxlength="7"
                  placeholder="#00FF00"
                  required
                />
                <input v-model="editForm.colorCode" type="color" class="color-picker" />
              </div>
            </div>

            <div class="form-field full-width">
              <label>Color Meaning</label>
              <input 
                v-model="editForm.colorMeaning" 
                type="text" 
                maxlength="255"
                placeholder="Description..."
                required
              />
            </div>
          </div>

          <div v-if="selectedColorId" class="button-group">
            <button type="button" class="btn btn-secondary" @click="resetEditForm">
              Reset
            </button>
            <button type="submit" class="btn btn-primary" :disabled="editSaving">
              {{ editSaving ? 'Updating...' : 'Update Color' }}
            </button>
            <button type="button" class="btn btn-danger" @click="confirmDeleteColor" :disabled="editSaving">
              Delete
            </button>
          </div>
        </form>
      </div>

      <!-- SECTION 3: ADD NEW DESK COLOR -->
      <div class="settings-card">
        <div class="card-header">
          <svg class="card-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
          </svg>
          <h3>Add New Desk Color</h3>
        </div>

        <form @submit.prevent="createColor">
          <div class="form-grid">
            <div class="form-field">
              <label>Color Name</label>
              <input 
                v-model="newForm.colorName" 
                type="text" 
                maxlength="50"
                placeholder="e.g., PURPLE"
                required
              />
            </div>

            <div class="form-field">
              <label>Color Code</label>
              <div class="color-input">
                <input 
                  v-model="newForm.colorCode" 
                  type="text" 
                  pattern="^#[0-9A-Fa-f]{6}$"
                  maxlength="7"
                  placeholder="#800080"
                  required
                />
                <input v-model="newForm.colorCode" type="color" class="color-picker" />
              </div>
            </div>

            <div class="form-field full-width">
              <label>Color Meaning</label>
              <input 
                v-model="newForm.colorMeaning" 
                type="text" 
                maxlength="255"
                placeholder="What does this color represent?"
                required
              />
            </div>
          </div>

          <div class="button-group">
            <button type="button" class="btn btn-secondary" @click="resetNewForm">
              Reset
            </button>
            <button type="submit" class="btn btn-primary" :disabled="newSaving">
              {{ newSaving ? 'Adding...' : 'Add Color' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- Toast Notifications -->
    <transition name="slide-up">
      <div v-if="showToast" class="toast" :class="toastType">
        <svg v-if="toastType === 'success'" class="toast-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
        </svg>
        <svg v-else class="toast-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
        <span>{{ toastMessage }}</span>
      </div>
    </transition>

    <!-- Custom Confirm Dialog -->
    <transition name="fade">
      <div v-if="showConfirmDialog" class="modal-overlay" @click="cancelDelete">
        <div class="confirm-dialog" @click.stop>
          <div class="confirm-header">
            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                    d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
            </svg>
            <h3>Confirm Deletion</h3>
          </div>
          <p class="confirm-message">
            Are you sure you want to delete "<strong>{{ colorToDelete?.colorName }}</strong>"? 
            This action cannot be undone.
          </p>
          <div class="confirm-actions">
            <button class="btn btn-secondary" @click="cancelDelete">Cancel</button>
            <button class="btn btn-danger" @click="proceedDelete">Delete</button>
          </div>
        </div>
      </div>
    </transition>
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

      // New Color
      newForm: {
        colorName: '',
        colorCode: '#000000',
        colorMeaning: ''
      },
      newSaving: false,

      // Toast notifications
      showToast: false,
      toastMessage: '',
      toastType: 'success',

      // Confirm dialog
      showConfirmDialog: false,
      colorToDelete: null,
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
  },

  methods: {
    // Toast notification
    showNotification(message, type = 'success') {
      this.toastMessage = message;
      this.toastType = type;
      this.showToast = true;
      setTimeout(() => {
        this.showToast = false;
      }, 3000);
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
        this.showNotification('Booking limits updated successfully!', 'success');
      } catch (err) {
        console.error(err);
        this.showNotification(err.response?.data?.message || 'Failed to update booking limits', 'error');
      } finally {
        this.limitsSaving = false;
      }
    },

    resetLimitsForm() {
      if (this.originalLimits) {
        this.limitsForm = { ...this.originalLimits };
        this.limitsErrors = {};
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
    },

    resetEditForm() {
  this.selectedColorId = null;
  this.editForm = {
    colorName: '',
    colorCode: '#000000',
    colorMeaning: ''
  };
  this.originalEditForm = null;
},

    async updateColor() {
      if (!this.selectedColorId) {
        this.showNotification('Please select a color to edit', 'error');
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
        this.showNotification('Color updated successfully!', 'success');
      } catch (err) {
        console.error(err);
        this.showNotification(err.response?.data?.message || 'Failed to update color', 'error');
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
        this.showNotification('Color deleted successfully!', 'success');
        this.selectedColorId = null;
        this.resetEditForm();
      } catch (err) {
        console.error(err);
        this.showNotification(err.response?.data?.message || 'Failed to delete color', 'error');
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
    },

    async createColor() {
      this.newSaving = true;

      try {
        const response = await api.post('/admin/desk-colors', this.newForm);
        this.deskColors.push(response.data);
        this.showNotification('New color added successfully!', 'success');
        this.resetNewForm();
      } catch (err) {
        console.error(err);
        this.showNotification(err.response?.data?.message || 'Failed to add new color', 'error');
      } finally {
        this.newSaving = false;
      }
    },
  }
};
</script>

<style scoped>
.settings-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
  background: #fffaf4;
  min-height: 100vh;
  font-family: "Inter", -apple-system, sans-serif;
}

/* Header */
.settings-header {
  margin-bottom: 2rem;
}

.settings-header h2 {
  font-size: 1.875rem;
  font-weight: 700;
  color: #111827;
  margin-bottom: 0.5rem;
}

.settings-description {
  color: #6b7280;
  font-size: 0.875rem;
}

/* Loading */
.loading-state {
  text-align: center;
  padding: 3rem;
}

.spinner {
  border: 4px solid #fff7f0;
  border-top: 4px solid #ff8a00;
  border-radius: 50%;
  width: 48px;
  height: 48px;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* Error Alert */
.error-alert {
  background: #fee;
  border: 1px solid #fcc;
  border-radius: 0.75rem;
  padding: 1.5rem;
  display: flex;
  gap: 1rem;
}

.error-icon {
  width: 24px;
  height: 24px;
  color: #c33;
  flex-shrink: 0;
}

.error-title {
  font-weight: 600;
  color: #c33;
  margin-bottom: 0.25rem;
}

.error-message {
  color: #a22;
  font-size: 0.875rem;
  margin-bottom: 0.5rem;
}

.retry-button {
  background: #c33;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 0.375rem;
  cursor: pointer;
  font-weight: 600;
}

/* Settings Content */
.settings-content {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

/* Settings Card */
.settings-card {
  background: #fff;
  border: 1px solid rgba(255, 170, 64, 0.22);
  border-radius: 0.75rem;
  padding: 1.5rem;
  transition: box-shadow 0.2s;
}

.settings-card:hover {
  box-shadow: 0 4px 12px rgba(255, 138, 0, 0.08);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid rgba(255, 170, 64, 0.15);
}

.card-icon {
  width: 24px;
  height: 24px;
  color: #ff8a00;
  flex-shrink: 0;
}

.card-header h3 {
  font-size: 1.125rem;
  font-weight: 600;
  color: #111827;
  margin: 0;
}

/* Info Badge */
.info-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  background: #fff7f0;
  border: 1px solid rgba(255, 170, 64, 0.3);
  padding: 0.5rem 1rem;
  border-radius: 0.5rem;
  font-size: 0.875rem;
  color: #6b7280;
  margin-bottom: 1.5rem;
}

.info-badge svg {
  width: 16px;
  height: 16px;
  color: #ff8a00;
}

/* Form Grid */
.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(1200px, 1fr));
  gap: 1.5rem;
  margin-bottom: 1.5rem;
}

.form-field {
  display: flex;
  flex-direction: column;
}

.form-field.full-width {
  grid-column: 1 / -1;
}

.form-field label {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.875rem;
  font-weight: 600;
  color: #374151;
  margin-bottom: 0.5rem;
}

.current-value {
  font-size: 0.75rem;
  font-weight: 500;
  background: #ff8a00;
  color: white;
  padding: 0.125rem 0.5rem;
  border-radius: 0.25rem;
}

.form-field input[type="text"],
.form-field input[type="number"],
.form-field select {
  width: 100%;
  padding: 0.625rem 0.75rem;
  border: 2px solid rgba(255, 170, 64, 0.22);
  border-radius: 0.5rem;
  font-size: 0.875rem;
  transition: all 0.2s;
  background: #fff;
}

.form-field input:focus,
.form-field select:focus {
  outline: none;
  border-color: #ff8a00;
  box-shadow: 0 0 0 3px rgba(255, 138, 0, 0.1);
}

.form-field input.error {
  border-color: #e14a4a;
  background: #fff5f5;
}

.field-error {
  display: block;
  margin-top: 0.375rem;
  font-size: 0.75rem;
  color: #e14a4a;
}

/* Color Input */
.color-input {
  display: flex;
  gap: 0.5rem;
}

.color-input input[type="text"] {
  flex: 1;
}

.color-picker {
  width: 60px;
  height: 38px;
  padding: 2px;
  border: 2px solid rgba(255, 170, 64, 0.22);
  border-radius: 0.5rem;
  cursor: pointer;
}

.color-picker:hover {
  border-color: #ff8a00;
}

/* Buttons */
.button-group {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.btn {
  padding: 0.625rem 1.5rem;
  border: none;
  border-radius: 0.5rem;
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-primary {
  background: #ff8a00;
  color: white;
}

.btn-primary:not(:disabled):hover {
  background: #e67a00;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(255, 138, 0, 0.3);
}

.btn-secondary {
  background: #fff;
  color: #374151;
  border: 2px solid rgba(255, 170, 64, 0.22);
}

.btn-secondary:not(:disabled):hover {
  border-color: #ff8a00;
  background: #fffaf4;
}

.btn-danger {
  background: #ef4444;
  color: white;
}

.btn-danger:not(:disabled):hover {
  background: #dc2626;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(239, 68, 68, 0.3);
}

/* Toast Notification */
.toast {
  position: fixed;
  bottom: 2rem;
  right: 2rem;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem 1.5rem;
  border-radius: 0.75rem;
  font-size: 0.875rem;
  font-weight: 500;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  min-width: 300px;
  max-width: 500px;
}

.toast.success {
  background: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}

.toast.error {
  background: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}

.toast-icon {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
}

/* Confirm Dialog */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}

.confirm-dialog {
  background: white;
  border-radius: 0.75rem;
  padding: 2rem;
  max-width: 500px;
  width: 90%;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1);
}

.confirm-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 1rem;
}

.confirm-header svg {
  width: 32px;
  height: 32px;
  color: #f59e0b;
}

.confirm-header h3 {
  font-size: 1.25rem;
  font-weight: 600;
  color: #111827;
  margin: 0;
}

.confirm-message {
  color: #6b7280;
  font-size: 0.875rem;
  line-height: 1.6;
  margin-bottom: 1.5rem;
}

.confirm-message strong {
  color: #111827;
  font-weight: 600;
}

.confirm-actions {
  display: flex;
  gap: 0.75rem;
  justify-content: flex-end;
}

/* Animations */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.slide-up-enter-active {
  animation: slideUp 0.3s ease-out;
}

.slide-up-leave-active {
  animation: slideUp 0.3s ease-out reverse;
}

@keyframes slideUp {
  from {
    transform: translateY(100%);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

/* Responsive */
@media (max-width: 768px) {
  .settings-container {
    padding: 1rem;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }

  .button-group {
    flex-direction: column;
  }

  .btn {
    width: 100%;
  }

  .toast {
    bottom: 1rem;
    right: 1rem;
    left: 1rem;
    min-width: auto;
  }

  .confirm-dialog {
    padding: 1.5rem;
  }
}
</style>