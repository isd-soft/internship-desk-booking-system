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
    <div v-else>
      <!-- ========================================== -->
      <!-- SECTION 1: BOOKING TIME LIMITS -->
      <!-- ========================================== -->
      <div class="info-section">
        <svg class="info-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
        <div>
          <h4>Fixed Booking Duration Limits</h4>
          <p>Minimum: <strong>1 hour</strong> | Maximum: <strong>8 hours</strong> per booking</p>
        </div>
      </div>

      <div class="settings-section">
        <h3 class="section-title">
          <svg class="section-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M12 6V4m0 2a2 2 0 100 4m0-4a2 2 0 110 4m-6 8a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4m6 6v10m6-2a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4" />
          </svg>
          Configurable Booking Limits
        </h3>

        <form @submit.prevent="saveBookingLimits">
          <div class="form-row">
            <div class="form-group">
              <label for="maxDays" class="form-label">
                Maximum Days in Advance
                <span class="label-badge">Current: {{ originalLimits?.maxDaysInAdvance || 30 }}</span>
              </label>
              <input 
                id="maxDays" 
                type="number" 
                min="1" 
                max="365" 
                class="form-input"
                v-model.number="limitsForm.maxDaysInAdvance"
                @input="validateLimitsField('maxDaysInAdvance')"
                :class="{ 'input-error': limitsErrors.maxDaysInAdvance }"
                placeholder="1-365" 
              />
              <transition name="fade">
                <div v-if="limitsErrors.maxDaysInAdvance" class="error-message">
                  {{ limitsErrors.maxDaysInAdvance }}
                </div>
              </transition>
            </div>

            <div class="form-group">
              <label for="maxWeeklyHours" class="form-label">
                Maximum Weekly Hours
                <span class="label-badge">Current: {{ originalLimits?.maxHoursPerWeek || 20 }}</span>
              </label>
              <input 
                id="maxWeeklyHours" 
                type="number" 
                min="1" 
                max="168" 
                class="form-input"
                v-model.number="limitsForm.maxHoursPerWeek"
                @input="validateLimitsField('maxHoursPerWeek')"
                :class="{ 'input-error': limitsErrors.maxHoursPerWeek }"
                placeholder="1-168" 
              />
              <transition name="fade">
                <div v-if="limitsErrors.maxHoursPerWeek" class="error-message">
                  {{ limitsErrors.maxHoursPerWeek }}
                </div>
              </transition>
            </div>
          </div>

          <div class="form-actions">
            <button 
              type="button" 
              class="button button-secondary" 
              @click="resetLimitsForm" 
              :disabled="!hasLimitsChanges"
            >
              Reset
            </button>
            <button 
              type="submit" 
              class="button button-primary" 
              :disabled="!isLimitsFormValid || limitsSaving || !hasLimitsChanges"
            >
              <span v-if="!limitsSaving">Save Changes</span>
              <span v-else>Saving...</span>
            </button>
          </div>

          <!-- Success/Error Messages -->
          <transition name="fade">
            <div v-if="limitsSuccess" class="success-alert">
              <svg class="success-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
              </svg>
              {{ limitsSuccess }}
            </div>
          </transition>
          <transition name="fade">
            <div v-if="limitsError" class="error-alert-inline">
              {{ limitsError }}
            </div>
          </transition>
        </form>
      </div>

      <!-- ========================================== -->
      <!-- SECTION 2: EDIT DESK COLORS -->
      <!-- ========================================== -->
      <div class="settings-section">
        <h3 class="section-title">
          <svg class="section-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                  d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
          </svg>
          Edit Existing Desk Colors
        </h3>

        <form @submit.prevent="updateColor">
          <!-- Select Color -->
          <div class="form-row">
            <div class="form-group full-width">
              <label for="selectColor" class="form-label">Select Color to Edit</label>
  <select id="selectColor" v-model="selectedColorId" class="form-input">
                @change="loadSelectedColor"
              >
                <option :value="null" disabled>-- Choose a color --</option>
                <option v-for="color in deskColors" :key="color.id" :value="color.id">
                  {{ color.colorName }} ({{ color.colorCode }})
                </option>
              </select>
            </div>
          </div>

          <!-- Edit Fields -->
          <div v-if="selectedColorId" class="form-row">
            <div class="form-group">
              <label for="editColorName" class="form-label">Color Name</label>
              <input 
                id="editColorName"
                v-model="editForm.colorName" 
                type="text" 
                class="form-input"
                maxlength="50"
                placeholder="e.g., GREEN"
                required
              />
            </div>

            <div class="form-group">
              <label for="editColorCode" class="form-label">Color Code (Hex)</label>
              <div class="color-input-group">
                <input 
                  id="editColorCode"
                  v-model="editForm.colorCode" 
                  type="text" 
                  class="form-input"
                  pattern="^#[0-9A-Fa-f]{6}$"
                  maxlength="7"
                  placeholder="#00FF00"
                  required
                />
                <input 
                  v-model="editForm.colorCode" 
                  type="color" 
                  class="color-picker"
                />
              </div>
            </div>

            <div class="form-group">
              <label for="editColorMeaning" class="form-label">Color Meaning</label>
              <input 
                id="editColorMeaning"
                v-model="editForm.colorMeaning" 
                type="text" 
                class="form-input"
                maxlength="255"
                placeholder="Description..."
                required
              />
            </div>
          </div>

          <!-- Actions -->
          <div v-if="selectedColorId" class="form-actions">
            <button 
              type="button" 
              class="button button-secondary" 
              @click="resetEditForm"
            >
              Reset
            </button>
            <button 
              type="submit" 
              class="button button-primary" 
              :disabled="editSaving"
            >
              <span v-if="!editSaving">Update Color</span>
              <span v-else>Updating...</span>
            </button>
            <button 
              type="button" 
              class="button button-danger" 
              @click="confirmDeleteColor"
              :disabled="editSaving"
            >
              Delete Color
            </button>
          </div>

          <!-- Success/Error Messages -->
          <transition name="fade">
            <div v-if="editSuccess" class="success-alert">
              <svg class="success-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
              </svg>
              {{ editSuccess }}
            </div>
          </transition>
          <transition name="fade">
            <div v-if="editError" class="error-alert-inline">
              {{ editError }}
            </div>
          </transition>
        </form>
      </div>

      <!-- ========================================== -->
      <!-- SECTION 3: ADD NEW DESK COLOR -->
      <!-- ========================================== -->
      <div class="settings-section">
        <h3 class="section-title">
          <svg class="section-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                  d="M12 4v16m8-8H4" />
          </svg>
          Add New Desk Color
        </h3>

        <form @submit.prevent="createColor">
          <div class="form-row">
            <div class="form-group">
              <label for="newColorName" class="form-label">Color Name</label>
              <input 
                id="newColorName"
                v-model="newForm.colorName" 
                type="text" 
                class="form-input"
                maxlength="50"
                placeholder="e.g., PURPLE"
                required
              />
            </div>

            <div class="form-group">
              <label for="newColorCode" class="form-label">Color Code (Hex)</label>
              <div class="color-input-group">
                <input 
                  id="newColorCode"
                  v-model="newForm.colorCode" 
                  type="text" 
                  class="form-input"
                  pattern="^#[0-9A-Fa-f]{6}$"
                  maxlength="7"
                  placeholder="#800080"
                  required
                />
                <input 
                  v-model="newForm.colorCode" 
                  type="color" 
                  class="color-picker"
                />
              </div>
            </div>

            <div class="form-group">
              <label for="newColorMeaning" class="form-label">Color Meaning</label>
              <input 
                id="newColorMeaning"
                v-model="newForm.colorMeaning" 
                type="text" 
                class="form-input"
                maxlength="255"
                placeholder="What does this color represent?"
                required
              />
            </div>
          </div>

          <!-- Actions -->
          <div class="form-actions">
            <button 
              type="button" 
              class="button button-secondary" 
              @click="resetNewForm"
            >
              Reset
            </button>
            <button 
              type="submit" 
              class="button button-primary" 
              :disabled="newSaving"
            >
              <span v-if="!newSaving">Add Color</span>
              <span v-else>Adding...</span>
            </button>
          </div>

          <!-- Success/Error Messages -->
          <transition name="fade">
            <div v-if="newSuccess" class="success-alert">
              <svg class="success-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
              </svg>
              {{ newSuccess }}
            </div>
          </transition>
          <transition name="fade">
            <div v-if="newError" class="error-alert-inline">
              {{ newError }}
            </div>
          </transition>
        </form>
      </div>
    </div>
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

      // === BOOKING LIMITS ===
      limitsForm: {
        maxDaysInAdvance: 30,
        maxHoursPerWeek: 20,
      },
      originalLimits: null,
      limitsSaving: false,
      limitsSuccess: null,
      limitsError: null,
      limitsErrors: {},

      // === DESK COLORS ===
      deskColors: [],

      // EDIT COLOR
      selectedColorId: null,
      editForm: {
        colorName: '',
        colorCode: '#000000',
        colorMeaning: ''
      },
      originalEditForm: null,
      editSaving: false,
      editSuccess: null,
      editError: null,

      // NEW COLOR
      newForm: {
        colorName: '',
        colorCode: '#000000',
        colorMeaning: ''
      },
      newSaving: false,
      newSuccess: null,
      newError: null,
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
    // ==========================================
    // INITIAL DATA FETCH
    // ==========================================
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

    // ==========================================
    // BOOKING LIMITS
    // ==========================================
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
      this.limitsError = null;
      this.limitsSuccess = null;
      this.limitsSaving = true;

      try {
        const response = await api.put('/admin/booking-time-limits', this.limitsForm);
        this.limitsForm = { ...response.data };
        this.originalLimits = { ...this.limitsForm };
        this.limitsSuccess = 'Booking limits updated successfully!';
        setTimeout(() => (this.limitsSuccess = null), 3000);
      } catch (err) {
        console.error(err);
        this.limitsError = err.response?.data?.message || 'Failed to update booking limits';
        setTimeout(() => (this.limitsError = null), 5000);
      } finally {
        this.limitsSaving = false;
      }
    },

    resetLimitsForm() {
      if (this.originalLimits) {
        this.limitsForm = { ...this.originalLimits };
        this.limitsErrors = {};
        this.limitsError = null;
        this.limitsSuccess = null;
      }
    },

    // ==========================================
    // DESK COLORS - FETCH
    // ==========================================
    async fetchDeskColors() {
      const response = await api.get('/admin/desk-colors');
      this.deskColors = response.data;
    },

    // ==========================================
    // EDIT COLOR
    // ==========================================
    loadSelectedColor() {
      const color = this.deskColors.find(c => c.id === this.selectedColorId);
      if (color) {
        this.editForm = { ...color };
        this.originalEditForm = { ...color };
      } else {
        this.resetEditForm();
      }
      this.editError = null;
      this.editSuccess = null;
    },

    resetEditForm() {
      if (this.originalEditForm) {
        this.editForm = { ...this.originalEditForm };
      } else {
        this.editForm = {
          colorName: '',
          colorCode: '#000000',
          colorMeaning: ''
        };
      }
      this.editError = null;
      this.editSuccess = null;
    },

    async updateColor() {
      if (!this.selectedColorId) {
        this.editError = 'Please select a color to edit';
        return;
      }

      this.editError = null;
      this.editSuccess = null;
      this.editSaving = true;

      try {
        const response = await api.put(`/admin/desk-colors/${this.selectedColorId}`, {
          colorName: this.editForm.colorName,
          colorCode: this.editForm.colorCode,
          colorMeaning: this.editForm.colorMeaning
        });

        // Update local list
        const index = this.deskColors.findIndex(c => c.id === this.selectedColorId);
        if (index !== -1) {
          this.deskColors.splice(index, 1, response.data);
        }

        this.editSuccess = 'Color updated successfully!';
        this.originalEditForm = { ...response.data };
        this.editForm = { ...response.data };
        
        setTimeout(() => (this.editSuccess = null), 3000);
      } catch (err) {
        console.error(err);
        this.editError = err.response?.data?.message || 'Failed to update color';
        setTimeout(() => (this.editError = null), 5000);
      } finally {
        this.editSaving = false;
      }
    },

    confirmDeleteColor() {
      if (!this.selectedColorId) return;

      const color = this.deskColors.find(c => c.id === this.selectedColorId);
      if (!color) return;

      if (confirm(`Are you sure you want to delete "${color.colorName}"? This action cannot be undone.`)) {
        this.deleteColor();
      }
    },

    async deleteColor() {
      this.editError = null;
      this.editSuccess = null;
      this.editSaving = true;

      try {
        await api.delete(`/admin/desk-colors/${this.selectedColorId}`);

        // Remove from local list
        this.deskColors = this.deskColors.filter(c => c.id !== this.selectedColorId);

        this.editSuccess = 'Color deleted successfully!';
        this.selectedColorId = null;
        this.resetEditForm();

        setTimeout(() => (this.editSuccess = null), 3000);
      } catch (err) {
        console.error(err);
        this.editError = err.response?.data?.message || 'Failed to delete color';
        setTimeout(() => (this.editError = null), 5000);
      } finally {
        this.editSaving = false;
      }
    },

    // ==========================================
    // ADD NEW COLOR
    // ==========================================
    resetNewForm() {
      this.newForm = {
        colorName: '',
        colorCode: '#000000',
        colorMeaning: ''
      };
      this.newError = null;
      this.newSuccess = null;
    },

    async createColor() {
      this.newError = null;
      this.newSuccess = null;
      this.newSaving = true;

      try {
        const response = await api.post('/admin/desk-colors', this.newForm);

        // Add to local list
        this.deskColors.push(response.data);

        this.newSuccess = 'New color added successfully!';
        this.resetNewForm();

        setTimeout(() => (this.newSuccess = null), 3000);
      } catch (err) {
        console.error(err);
        this.newError = err.response?.data?.message || 'Failed to add new color';
        setTimeout(() => (this.newError = null), 5000);
      } finally {
        this.newSaving = false;
      }
    },
  }
};
</script>

<style scoped>
:root {
  --accent: #ff8a00;
  --accent-hover: #e67a00;
  --surface: #fffaf4;
  --soft: #fff7f0;
  --border: rgba(255, 170, 64, 0.22);
  --text-1: #0f172a;
  --text-2: #5f5b53;
  --success-bg: #d4edda;
  --success-text: #155724;
  --error-bg: #f8d7da;
  --error-text: #721c24;
  --btn-height: 48px;
  --btn-radius: 0.5rem;
}

.settings-container {
  max-width: 1200px; 
  width: 100%;
  margin: 0 auto;
  padding: 2rem;
  background: var(--surface);
  border-left: 1px solid var(--border);
  min-height: 100vh;
  font-family: "Inter", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
  color: var(--text-1);
}

.settings-header h2 {
  font-size: 1.875rem;
  font-weight: 700;
  color: #111827;
  margin-bottom: 0.5rem;
}

.settings-description {
  color: var(--text-2);
  font-size: 0.875rem;
}

/* Loading */
.loading-state {
  text-align: center;
  padding: 3rem;
}

.spinner {
  border: 4px solid var(--soft);
  border-top: 4px solid var(--accent);
  border-radius: 50%;
  width: 48px;
  height: 48px;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* Alerts */
.error-alert {
  background: var(--error-bg);
  border: 1px solid #f5c6cb;
  border-radius: 0.5rem;
  padding: 1rem;
  display: flex;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.error-icon {
  width: 24px;
  height: 24px;
  color: var(--error-text);
  flex-shrink: 0;
}

.error-title {
  font-weight: 600;
  color: var(--error-text);
  margin-bottom: 0.25rem;
}

.error-message {
  color: var(--error-text);
  font-size: 0.875rem;
}

.retry-button {
  background: var(--error-text);
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 0.375rem;
  cursor: pointer;
  font-weight: 600;
  margin-top: 0.5rem;
}

.error-alert-inline {
  background: var(--error-bg);
  color: var(--error-text);
  border: 1px solid #f5c6cb;
  padding: 0.75rem 1rem;
  border-radius: 0.5rem;
  margin-top: 1rem;
  font-size: 0.875rem;
}

.success-alert {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: var(--success-bg);
  color: var(--success-text);
  border: 1px solid #c3e6cb;
  padding: 0.75rem 1rem;
  border-radius: 0.5rem;
  margin-top: 1rem;
  font-size: 0.875rem;
}

.success-icon {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
}

/* Info Section */
.info-section {
  background: var(--soft);
  border: 1px solid var(--border);
  border-radius: 0.75rem;
  padding: 1rem;
  margin-bottom: 1.5rem;
  display: flex;
  gap: 0.75rem;
}

.info-icon {
  width: 24px;
  height: 24px;
  color: var(--accent);
  flex-shrink: 0;
}

.info-section h4 {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--accent);
  margin-bottom: 0.25rem;
}

.info-section p {
  font-size: 0.875rem;
  color: var(--text-2);
}

/* Settings Section */
.settings-section {
  background: var(--soft);
  border: 1px solid var(--border);
  border-radius: 0.75rem;
  padding: 1.5rem;
  margin-bottom: 1.5rem;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--accent);
  margin-bottom: 1.5rem;
}

.section-icon {
  width: 20px;
  height: 20px;
}

/* Form */
.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr; 
  gap: 1.5rem;
  margin-bottom: 1.5rem;
}
@media (max-width: 1024px) {
  .form-row {
    grid-template-columns: 1fr; 
  }
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group.full-width {
  grid-column: 1 / -1;
}

.form-label {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--text-2);
  margin-bottom: 0.5rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.label-badge {
  font-size: 0.75rem;
  font-weight: 500;
  background: var(--accent);
  color: white;
  padding: 0.125rem 0.5rem;
  border-radius: 0.25rem;
}

.form-input {
  width: 100%;
  box-sizing: border-box; 
  padding: 0.625rem;
  border: 2px solid var(--border);
  border-radius: 0.5rem;
  font-size: 0.875rem;
  transition: all 0.2s;
}

.form-input:focus {
  outline: none;
  border-color: var(--accent);
  box-shadow: 0 0 0 3px rgba(255, 138, 0, 0.1);
}

.form-input.input-error {
  border-color: #e14a4a;
  background-color: #fff5f5;
}

.form-input.input-error:focus {
  box-shadow: 0 0 0 3px rgba(225, 74, 74, 0.1);
}

.input-hint {
  font-size: 0.75rem;
  color: var(--text-2);
  margin-top: 0.25rem;
  opacity: 0.7;
}

.error-message {
  display: flex;
  align-items: center;
  gap: 0.375rem;
  margin-top: 0.5rem;
  padding: 0.5rem;
  background-color: #fff5f5;
  border-left: 3px solid #e14a4a;
  border-radius: 0.25rem;
  font-size: 0.8125rem;
  color: #c33;
  animation: slideDown 0.2s ease-out;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-5px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.error-icon-small {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
}


.warning-icon {
  width: 24px;
  height: 24px;
  color: #f57c00;
  flex-shrink: 0;
  margin-top: 0.125rem;
}


.validation-list {
  list-style: disc;
  padding-left: 1.25rem;
  font-size: 0.8125rem;
  color: #f57c00;
}

.validation-list li {
  margin-bottom: 0.25rem;
}

.changes-indicator {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  background-color: #e65100;
  border: 1px solid #bbdefb;
  border-radius: 0.5rem;
  margin-bottom: 1rem;
  font-size: 0.875rem;
  color: #111827;
}

.info-icon-small {
  width: 18px;
  height: 18px;
  flex-shrink: 0;
}

.form-actions {
  display: flex;
  justify-content: flex-start; 
  flex-wrap: wrap;
  gap: 1rem;
  margin-top: 1.5rem;
}

.button {
font-family: var(--btn-font-family);
  font-size: var(--btn-font-size);
  font-weight: 700;
  text-transform: none;
  border-radius: var(--btn-radius);
  height: var(--btn-height);
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0 28px;
  border: none;
  background-color: var(--btn-bg);
  color: var(--btn-color);
  transition: background-color var(--btn-transition),
              transform var(--btn-transition),
              opacity var(--btn-transition);
  user-select: none;
  white-space: nowrap;
  -webkit-font-smoothing: antialiased;
}
button:hover:not(:disabled) {
  background-color: var(--btn-hover-bg);
  transform: scale(1.02);
}

button:active:not(:disabled) {
  background-color: var(--btn-active-bg);
  transform: scale(0.98);
}

button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.button-primary,
.button-secondary {
  background-color: var(--btn-bg);
  color: var(--btn-color);
}

.saving-text {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.spinner-small {
  border: 2px solid var(--soft);
  border-top: 2px solid var(--accent);
  border-radius: 50%;
  width: 14px;
  height: 14px;
  animation: spin 0.8s linear infinite;
}

.success-alert {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background-color: #e65100;
  color: #111827;
  border: 1px solid var(--border);
  padding: 0.75rem 1rem;
  border-radius: 0.5rem;
  margin-top: 1rem;
  font-size: 0.875rem;
  animation: slideDown 0.3s ease-out;
}

.success-icon {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>