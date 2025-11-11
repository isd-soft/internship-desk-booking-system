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
            <label for="maxDays" class="form-label" title="How far ahead users can book">
              Maximum Days in Advance
            </label>
            <div class="input-wrapper">
              <input
                id="maxDays"
                v-model.number="formData.maxDaysInAdvance"
                type="number"
                min="1"
                max="365"
                class="form-input"
                :class="{ 'input-error': validationErrors.maxDaysInAdvance }"
              />
              <span v-if="validationErrors.maxDaysInAdvance" class="error-text">
                {{ validationErrors.maxDaysInAdvance }}
              </span>
            </div>
          </div>

          <!-- Hours -->
          <div class="form-group">
            <label for="maxWeeklyHours" class="form-label" title="Total booking hours per user per week">
              Maximum Weekly Hours
            </label>
            <div class="input-wrapper">
              <input
                id="maxWeeklyHours"
                v-model.number="formData.maxHoursPerWeek"
                type="number"
                min="1"
                max="168"
                class="form-input"
                :class="{ 'input-error': validationErrors.maxHoursPerWeek }"
              />
              <span v-if="validationErrors.maxHoursPerWeek" class="error-text">
                {{ validationErrors.maxHoursPerWeek }}
              </span>
            </div>
          </div>
        </div>

        <!-- Buttons -->
        <div class="form-actions">
          <button type="button" class="button button-secondary" @click="resetForm">
            Reset Changes
          </button>
          <button type="submit" class="button button-primary" :disabled="!isFormValid || saving">
            Save Changes
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
import axios from "axios";

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
  },
  mounted() {
    this.fetchSettings();
  },
  methods: {
    async fetchSettings() {
      this.loading = true;
      this.error = null;

      try {
    const token = localStorage.getItem('token');
    const response = await axios.get("/api/v1/admin/booking-time-limits", {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
    
    console.log('Response:', response.data);
    this.formData = {
      maxDaysInAdvance: response.data.maxDaysInAdvance,
      maxHoursPerWeek: response.data.maxHoursPerWeek,
    };
    this.originalData = { ...this.formData };
  } catch (err) {
    console.error("Full error:", err);
    console.error("Error response:", err.response);
    this.error = err.response?.data?.message || "Failed to load settings";
  } finally {
    this.loading = false;
      }
    },

   async saveSettings() {
  this.validationErrors = {};
  this.error = null;

  if (this.formData.maxDaysInAdvance > 365) {
    this.validationErrors.maxDaysInAdvance = "Value must be ≤ 365 days";
  }
  if (this.formData.maxHoursPerWeek > 168) {
    this.validationErrors.maxHoursPerWeek = "Value must be ≤ 168 hours";
  }
  if (Object.keys(this.validationErrors).length > 0) return;

  this.saving = true;

  try {
    console.log("[BookingSettings] Saving policy update:", this.formData);

    const response = await axios.put("/api/v1/admin/booking-time-limits", {
      maxDaysInAdvance: this.formData.maxDaysInAdvance,
      maxHoursPerWeek: this.formData.maxHoursPerWeek,
    });

    console.log("[BookingSettings] Policy updated successfully:", response.data);

    this.successMessage = "Settings saved successfully!";
    this.originalData = { ...response.data };

    // refresh datele din DB pentru consistență
    await this.fetchSettings();

    setTimeout(() => {
      this.successMessage = null;
    }, 2500);
  } catch (err) {
    console.error("[BookingSettings] Error saving settings:", err);
    this.error = err.response?.data?.message || "Failed to save settings";
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
/* ---------- ROOT COLOR SCHEME ---------- */
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
}

/* ---------- LAYOUT ---------- */
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

/* ---------- HEADER ---------- */
.settings-header {
  margin-bottom: 2rem;
}

.settings-header h2 {
  font-size: 1.875rem;
  font-weight: 700;
  color: var(--accent);
  margin-bottom: 0.5rem;
}

.settings-description {
  color: var(--text-2);
  font-size: 0.875rem;
}

/* ---------- LOADING ---------- */
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

/* ---------- INFO SECTION ---------- */
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

/* ---------- SETTINGS SECTION ---------- */
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

/* ---------- FORM ---------- */
.form-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
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
  flex-direction: column;
  gap: 0.25rem;
}

.label-hint {
  font-size: 0.75rem;
  color: var(--text-2);
  opacity: 0.7;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.form-input {
  width: 100%;
  padding: 0.625rem 4rem 0.625rem 0.75rem;
  border: 1px solid var(--border);
  border-radius: 0.5rem;
  background: #fff;
  font-size: 0.875rem;
  transition: all 0.15s;
  color: var(--text-1);
}

.form-input:focus {
  outline: none;
  border-color: var(--accent);
  box-shadow: 0 0 0 3px rgba(255, 138, 0, 0.15);
}

.form-input.input-error {
  border-color: #e14a4a;
}

.input-suffix {
  position: absolute;
  right: 0.75rem;
  font-size: 0.875rem;
  color: var(--text-2);
  pointer-events: none;
}

.error-text {
  margin-top: 0.25rem;
  font-size: 0.75rem;
  color: #e14a4a;
}

/* ---------- CURRENT VALUES ---------- */
.current-values {
  background-color: var(--soft);
  border: 1px solid var(--border);
  border-radius: 0.75rem;
  padding: 1.25rem;
  margin-bottom: 1.5rem;
}

.current-values h4 {
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--accent);
  margin-bottom: 0.75rem;
}

.summary-grid {
  display: grid;
  gap: 0.5rem;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  padding: 0.5rem 0;
  border-bottom: 1px solid var(--border);
}

.summary-item:last-child {
  border-bottom: none;
}

.summary-label {
  font-size: 0.875rem;
  color: var(--text-2);
}

.summary-value {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--text-1);
}

/* ---------- BUTTONS ---------- */
.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
}

.button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0.625rem 1.25rem;
  font-size: 0.875rem;
  font-weight: 600;
  border-radius: 0.5rem;
  cursor: pointer;
  transition: background-color 0.2s, transform 0.1s;
}

.button:hover {
  transform: translateY(-1px);
}

.button-primary {
  background-color: #fff;
  border: 1px solid var(--border);
  color: var(--text-1);
}

.button-primary:hover {
  background-color: var(--accent-hover);
}

.button-secondary {
  background-color: #fff;
  border: 1px solid var(--border);
  color: var(--text-1);
}

.button-secondary:hover {
  background-color: var(--soft);
}

/* ---------- SUCCESS ALERT ---------- */
.success-alert {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background-color: var(--success-bg);
  color: var(--success-text);
  border: 1px solid var(--border);
  padding: 0.75rem 1rem;
  border-radius: 0.5rem;
  margin-top: 1rem;
}

.success-icon {
  width: 20px;
  height: 20px;
}

/* ---------- TRANSITION ---------- */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.4s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
