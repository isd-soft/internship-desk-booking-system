<template>
  <div class="settings-container">
    <div class="settings-header">
      <h2>Booking Time Limits Configuration</h2>
      <p class="settings-description">
        Configure system-wide booking policies
      </p>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading-state">
      <div class="spinner"></div>
      <p>Loading settings...</p>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="error-alert">
      <svg class="error-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
      </svg>
      <div>
        <p class="error-title">Error Loading Settings</p>
        <p class="error-message">{{ error }}</p>
        <button @click="fetchSettings" class="retry-button">Retry</button>
      </div>
    </div>

    <!-- Settings Form -->
    <form v-else @submit.prevent="saveSettings" class="settings-form">

      <!-- Fixed Limits Info -->
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

      <!-- Configurable Limits Section-->
      <div class="settings-section">
        <h3 class="section-title">
          <svg class="section-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M12 6V4m0 2a2 2 0 100 4m0-4a2 2 0 110 4m-6 8a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4m6 6v10m6-2a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4" />
          </svg>
          Configurable Limits
        </h3>

        <div class="form-row">
          <!-- Days -->
          <div class="form-group">
            <label for="maxDays" class="form-label">
              Maximum Days in Advance
              <span class="label-badge">Current: {{ originalData?.maxDaysInAdvance || 30 }} days</span>
            </label>
            <div class="input-wrapper">
              <input
                id="maxDays"
                v-model.number="formData.maxDaysInAdvance"
                @input="validateField('maxDaysInAdvance')"
                @blur="validateField('maxDaysInAdvance')"
                type="number"
                min="1"
                max="365"
                class="form-input"
                :class="{ 'input-error': validationErrors.maxDaysInAdvance }"
                placeholder="1-365"
              />
              <span class="input-hint">Max 365 days</span>
            </div>
            <transition name="fade">
              <div v-if="validationErrors.maxDaysInAdvance" class="error-message">
                <svg class="error-icon-small" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clip-rule="evenodd" />
                </svg>
                <span>{{ validationErrors.maxDaysInAdvance }}</span>
              </div>
            </transition>
          </div>

          <!-- Hours -->
          <div class="form-group">
            <label for="maxWeeklyHours" class="form-label">
              Maximum Weekly Hours
              <span class="label-badge">Current: {{ originalData?.maxHoursPerWeek || 20 }} hours</span>
            </label>
            <div class="input-wrapper">
              <input
                id="maxWeeklyHours"
                v-model.number="formData.maxHoursPerWeek"
                @input="validateField('maxHoursPerWeek')"
                @blur="validateField('maxHoursPerWeek')"
                type="number"
                min="1"
                max="168"
                class="form-input"
                :class="{ 'input-error': validationErrors.maxHoursPerWeek }"
                placeholder="1-168"
              />
              <span class="input-hint">Max 168 hours/week</span>
            </div>
            <transition name="fade">
              <div v-if="validationErrors.maxHoursPerWeek" class="error-message">
                <svg class="error-icon-small" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clip-rule="evenodd" />
                </svg>
                <span>{{ validationErrors.maxHoursPerWeek }}</span>
              </div>
            </transition>
          </div>
        </div>

               <!-- Changes indicator -->
        <div v-if="hasChanges" class="changes-indicator">
          <svg class="info-icon-small" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                  d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <span><strong>You have unsaved changes</strong></span>
        </div>

        <!-- Buttons -->
        <div class="form-actions">
          <button 
            type="button" 
            class="button button-secondary" 
            @click="resetForm"
            :disabled="!hasChanges"
          >
            Reset Changes
          </button>
          <button 
            type="submit" 
            class="button button-primary" 
            :disabled="!isFormValid || saving || !hasChanges"
            :title="getSubmitButtonTooltip()"
          >
            <span v-if="!saving">Save Changes</span>
            <span v-else class="saving-text">
              <div class="spinner-small"></div>
              Saving...
            </span>
          </button>
        </div>

        <!-- Success message -->
        <transition name="fade">
          <div v-if="successMessage" class="success-alert">
            <svg class="success-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M5 13l4 4L19 7" />
            </svg>
            {{ successMessage }}
          </div>
        </transition>
      </div>
    </form>
  </div>
</template>

<script>
import api from '../plugins/axios';

export default {
  name: "BookingSettings",
  data() {
    return {
      formData: {
        maxDaysInAdvance: 30,
        maxHoursPerWeek: 20,
      },
      originalData: null,
      loading: true,
      saving: false,
      error: null,
      successMessage: null,
      validationErrors: {},
    };
  },
  computed: {
    isFormValid() {
      return (
        this.formData.maxDaysInAdvance > 0 &&
        this.formData.maxDaysInAdvance <= 365 &&
        this.formData.maxHoursPerWeek > 0 &&
        this.formData.maxHoursPerWeek <= 168 &&
        Object.keys(this.validationErrors).length === 0
      );
    },
    hasValidationErrors() {
      return Object.keys(this.validationErrors).length > 0;
    },
    hasChanges() {
      if (!this.originalData) return false;
      return (
        this.formData.maxDaysInAdvance !== this.originalData.maxDaysInAdvance ||
        this.formData.maxHoursPerWeek !== this.originalData.maxHoursPerWeek
      );
    }
  },
  mounted() {
    this.fetchSettings();
  },
  methods: {
    async fetchSettings() {
      this.loading = true;
      this.error = null;

      try {
        console.log("[BookingSettings] Fetching active policy...");
        const response = await api.get("/admin/booking-time-limits");
        
        console.log("[BookingSettings] Active policy fetched:", response.data);
        
        this.formData = {
          maxDaysInAdvance: response.data.maxDaysInAdvance,
          maxHoursPerWeek: response.data.maxHoursPerWeek,
        };
        this.originalData = { ...this.formData };
      } catch (err) {
        console.error("[BookingSettings] Error fetching settings:", err);
        
        if (err.response?.status === 403) {
          this.error = "Access denied. You need ADMIN privileges.";
        } else if (err.response?.status === 401) {
          this.error = "Unauthorized. Please log in again.";
        } else {
          this.error = err.response?.data?.message || "Failed to load settings";
        }
      } finally {
        this.loading = false;
      }
    },

    validateField(fieldName) {
      // Clear previous error for this field
      delete this.validationErrors[fieldName];

      if (fieldName === 'maxDaysInAdvance') {
        const value = this.formData.maxDaysInAdvance;
        if (!value || value < 1) {
          this.validationErrors.maxDaysInAdvance = "Must be at least 1 day";
        } else if (value > 365) {
          this.validationErrors.maxDaysInAdvance = "Cannot exceed 365 days";
        }
      }

      if (fieldName === 'maxHoursPerWeek') {
        const value = this.formData.maxHoursPerWeek;
        if (!value || value < 1) {
          this.validationErrors.maxHoursPerWeek = "Must be at least 1 hour";
        } else if (value > 168) {
          this.validationErrors.maxHoursPerWeek = "Cannot exceed 168 hours per week)";
        }
      }

      // Force reactivity
      this.validationErrors = { ...this.validationErrors };
    },

    validateAllFields() {
      this.validationErrors = {};
      this.validateField('maxDaysInAdvance');
      this.validateField('maxHoursPerWeek');
      return Object.keys(this.validationErrors).length === 0;
    },

    getSubmitButtonTooltip() {
      if (!this.hasChanges) return "No changes to save";
      if (this.hasValidationErrors) return "Please fix validation errors";
      if (!this.isFormValid) return "Invalid values";
      return "Save configuration changes";
    },

    async saveSettings() {
      if (!this.validateAllFields()) {
        return;
      }

      this.saving = true;
      this.error = null;
      this.successMessage = null;

      try {
        console.log("[BookingSettings] Saving policy update:", this.formData);

        const policyUpdateRequest = {
          maxDaysInAdvance: this.formData.maxDaysInAdvance,
          maxHoursPerWeek: this.formData.maxHoursPerWeek,
        };

        const response = await api.put("/admin/booking-time-limits", policyUpdateRequest);

        console.log("[BookingSettings] Policy updated successfully:", response.data);

        this.formData = {
          maxDaysInAdvance: response.data.maxDaysInAdvance,
          maxHoursPerWeek: response.data.maxHoursPerWeek,
        };
        this.originalData = { ...this.formData };

        this.successMessage = "Settings saved successfully!";

        setTimeout(() => {
          this.successMessage = null;
        }, 3000);
      } catch (err) {
        console.error("[BookingSettings] Error saving settings:", err);
        
        if (err.response?.status === 403) {
          this.error = "Access denied. You need ADMIN privileges.";
        } else if (err.response?.status === 401) {
          this.error = "Unauthorized. Please log in again.";
        } else if (err.response?.status === 400) {
          this.error = err.response?.data?.message || "Invalid input. Check your values.";
        } else {
          this.error = err.response?.data?.message || "Failed to save settings";
        }
      } finally {
        this.saving = false;
      }
    },

    resetForm() {
      if (this.originalData) {
        this.formData = { ...this.originalData };
        this.validationErrors = {};
        this.error = null;
        this.successMessage = null;
      }
    },
  },
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
  --success-bg: #fff3e0;
  --success-text: #8b5d00;
  --error-bg: #fee;
  --error-border: #fcc;
  --error-text: #c33;
 --btn-height: 52px;
  --btn-radius: 9999px; 
  --btn-font-size: 1.125rem;
  --btn-font-family: "Inter", -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
  --btn-bg: #000000;
  --btn-color: #ffffff;
  --btn-hover-bg: #222222;
  --btn-active-bg: #333333;
  --btn-transition: 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.settings-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 2rem;
  background: var(--surface);
  border-left: 1px solid var(--border);
  min-height: 100vh;
  font-family: "Inter", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
  color: var(--text-1);
}

.settings-header {
  margin-bottom: 2rem;
}

.settings-header h2 {
  font-size: clamp(1.08rem, .98rem + .3vw, 1.2rem);
  font-weight: 900;
  color: #111827;
  margin-bottom: 0;
}

.settings-description {
  color: var(--text-2);
  font-size: 0.875rem;
}

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
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error-alert {
  background-color: var(--error-bg);
  border: 1px solid var(--error-border);
  border-radius: 0.75rem;
  padding: 1.5rem;
  display: flex;
  gap: 1rem;
  align-items: flex-start;
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
  color: #a22;
  font-size: 0.875rem;
  margin-bottom: 0.75rem;
}

.retry-button {
  background-color: var(--error-text);
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 0.375rem;
  cursor: pointer;
  font-size: 0.875rem;
  font-weight: 600;
}

.retry-button:hover {
  background-color: #a22;
}

.info-section {
  background-color: var(--soft);
  border: 1px solid var(--border);
  border-radius: 0.75rem;
  padding: 1rem 1.25rem;
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

.settings-section {
  background-color: var(--soft);
  border: 1px solid var(--border);
  border-radius: 0.75rem;
  padding: 1.5rem;
  margin-bottom: 1.5rem;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06);
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
  color: var(--accent);
}

.form-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
  margin-bottom: 1rem;
}

.form-group {
  display: flex;
  flex-direction: column;
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
  background-color: var(--accent);
  color: white;
  padding: 0.125rem 0.5rem;
  border-radius: 0.25rem;
}

.input-wrapper {
  position: relative;
  display: flex;
  flex-direction: column;
}

.form-input {
  width: 100%;
  padding: 0.625rem 0.75rem;
  border: 2px solid var(--border);
  border-radius: 0.5rem;
  background: #e6510080;
  font-size: 0.875rem;
  transition: all 0.2s;
  color: var(--text-1);
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
  justify-content: flex-end;
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