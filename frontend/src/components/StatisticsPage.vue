<template>
  <div class="admin-statistics">
    <div class="admin-card">
      <div class="admin-header">
        <div class="title-wrap">
          <div class="workspace-label">ADMIN PANEL</div>
          <h2 class="title">Booking Analytics</h2>
          <span class="sub">Overview of your workspace performance</span>
        </div>
      </div>

      <v-alert
          v-if="error"
          type="error"
          variant="tonal"
          class="mb-4"
          density="compact"
      >
        {{ error }}
      </v-alert>

      <div v-if="loading" class="loading-wrap">
        <v-progress-circular indeterminate size="48" width="4" color="#171717" />
        <p class="loading-text mt-3">Loading analytics…</p>
      </div>

      <template v-else-if="stats">
        <div class="filter-section">
          <v-text-field
              v-model="startDate"
              label="Start Date"
              type="datetime-local"
              variant="outlined"
              density="compact"
              hide-details
              class="date-field"
          />
          <v-text-field
              v-model="endDate"
              label="End Date"
              type="datetime-local"
              variant="outlined"
              density="compact"
              hide-details
              class="date-field"
          />
          <v-btn
              color="#171717"
              variant="flat"
              @click="fetchStatisticsForRange"
              :loading="loading"
              class="apply-btn"
          >
            Apply Range
          </v-btn>
          <v-btn
              variant="outlined"
              @click="resetToDefault"
              class="reset-btn-outline"
          >
            Reset
          </v-btn>
        </div>

        <div class="stats-grid">
          <div class="stat-card">
            <div class="stat-icon-wrap" :class="(stats.weeklyBookings || 0) >= 0 ? 'positive' : 'negative'">
              <v-icon size="32">
                {{ (stats.weeklyBookings || 0) >= 0 ? 'mdi-trending-up' : 'mdi-trending-down' }}
              </v-icon>
            </div>
            <div class="stat-value" :class="(stats.weeklyBookings || 0) >= 0 ? 'positive' : 'negative'">
              {{ (stats.weeklyBookings || 0) >= 0 ? '+' : '' }}{{ stats.weeklyBookings || 0 }}%
            </div>
            <div class="stat-label">Weekly Bookings</div>
            <div class="stat-count">{{ stats.weeklyBookings || 0 }} bookings</div>
          </div>

          <div class="stat-card">
            <div class="stat-icon-wrap" :class="(stats.weeklyUsers || 0) >= 0 ? 'positive' : 'negative'">
              <v-icon size="32">
                {{ (stats.weeklyUsers || 0) >= 0 ? 'mdi-trending-up' : 'mdi-trending-down' }}
              </v-icon>
            </div>
            <div class="stat-value" :class="(stats.weeklyUsers || 0) >= 0 ? 'positive' : 'negative'">
              {{ (stats.weeklyUsers || 0) >= 0 ? '+' : '' }}{{ stats.weeklyUsers || 0 }}
            </div>
            <div class="stat-label">Weekly Users</div>
            <div class="stat-count">Active this week</div>
          </div>

          <div class="stat-card">
            <div class="stat-icon-wrap" :class="(stats.monthlyBookings || 0) >= 0 ? 'positive' : 'negative'">
              <v-icon size="32">
                {{ (stats.monthlyBookings || 0) >= 0 ? 'mdi-trending-up' : 'mdi-trending-down' }}
              </v-icon>
            </div>
            <div class="stat-value" :class="(stats.monthlyBookings || 0) >= 0 ? 'positive' : 'negative'">
              {{ stats.monthlyBookings || 0 }}
            </div>
            <div class="stat-label">Monthly Bookings</div>
            <div class="stat-count">{{ stats.monthlyUsers || 0 }} users</div>
          </div>

          <div class="stat-card neutral">
            <div class="stat-icon-wrap neutral">
              <v-icon size="32">mdi-cancel</v-icon>
            </div>
            <div class="stat-value">{{ stats.weeklyCancelledBookings || 0 }}</div>
            <div class="stat-label">Weekly Cancelled</div>
            <div class="stat-count">Total in 7 days</div>
          </div>

          <div class="stat-card neutral">
            <div class="stat-icon-wrap neutral">
              <v-icon size="32">mdi-cancel</v-icon>
            </div>
            <div class="stat-value">{{ stats.monthlyCancelledBookings || 0 }}</div>
            <div class="stat-label">Monthly Cancelled</div>
            <div class="stat-count">Total in 30 days</div>
          </div>

          <div class="stat-card" :class="(stats.weeklyCancellationRate || 0) > 10 ? 'negative' : 'positive'">
            <div class="stat-icon-wrap" :class="(stats.weeklyCancellationRate || 0) > 10 ? 'negative' : 'positive'">
              <v-icon size="32">mdi-percent</v-icon>
            </div>
            <div class="stat-value" :class="(stats.weeklyCancellationRate || 0) > 10 ? 'negative' : 'positive'">
              {{ (stats.weeklyCancellationRate || 0).toFixed(1) }}%
            </div>
            <div class="stat-label">Weekly Cancel Rate</div>
            <div class="stat-count">Higher is worse</div>
          </div>

          <div class="stat-card" :class="(stats.monthlyCancellationRate || 0) > 10 ? 'negative' : 'positive'">
            <div class="stat-icon-wrap" :class="(stats.monthlyCancellationRate || 0) > 10 ? 'negative' : 'positive'">
              <v-icon size="32">mdi-percent</v-icon>
            </div>
            <div class="stat-value" :class="(stats.monthlyCancellationRate || 0) > 10 ? 'negative' : 'positive'">
              {{ (stats.monthlyCancellationRate || 0).toFixed(1) }}%
            </div>
            <div class="stat-label">Monthly Cancel Rate</div>
            <div class="stat-count">Higher is worse</div>
          </div>

          <div class="stat-card highlight">
            <div class="stat-icon-wrap highlight">
              <v-icon size="32">mdi-star</v-icon>
            </div>
            <div class="stat-value">{{ getMostBookedDeskName() }}</div>
            <div class="stat-label">Most Booked Desk</div>
            <div class="stat-count">{{ getMostBookedCount() }} bookings</div>
          </div>

          <div class="stat-card neutral">
            <div class="stat-icon-wrap neutral">
              <v-icon size="32">mdi-emoticon-sad-outline</v-icon>
            </div>
            <div class="stat-value">{{ getLeastBookedDeskName() }}</div>
            <div class="stat-label">Least Booked Desk</div>
            <div class="stat-count">{{ getLeastBookedCount() }} bookings</div>
          </div>

          <div class="stat-card negative">
            <div class="stat-icon-wrap negative">
              <v-icon size="32">mdi-alert-octagon-outline</v-icon>
            </div>
            <div class="stat-value">{{ getMostCancelledDeskName() }}</div>
            <div class="stat-label">Most Cancelled Desk</div>
            <div class="stat-count">{{ getMostCancelledCount() }} cancellations</div>
          </div>
        </div>

        <div class="charts-section">
          <div class="chart-card">
            <div class="chart-header">
              <h3 class="chart-title">Daily Bookings</h3>
              <v-chip size="small" color="#171717" variant="flat" class="chart-chip">Bar Chart</v-chip>
            </div>
            <div class="chart-container" v-if="hasBookingsPerDay()">
              <canvas ref="bookingsChartCanvas"></canvas>
            </div>
            <div v-else class="no-data">
              <v-icon size="48" color="#a3a3a3" class="mb-3">mdi-chart-bar</v-icon>
              <div>No daily booking data</div>
            </div>
          </div>

          <div class="chart-card">
            <div class="chart-header">
              <h3 class="chart-title">Weekly Bookings</h3>
              <v-chip size="small" color="#171717" variant="flat" class="chart-chip">Line Chart</v-chip>
            </div>
            <div class="chart-container" v-if="hasBookingsPerWeek()">
              <canvas ref="hoursChartCanvas"></canvas>
            </div>
            <div v-else class="no-data">
              <v-icon size="48" color="#a3a3a3" class="mb-3">mdi-chart-line</v-icon>
              <div>No weekly booking data</div>
            </div>
          </div>

          <div class="chart-card">
            <div class="chart-header">
              <h3 class="chart-title">Daily Cancellations</h3>
              <v-chip size="small" color="#dc2626" variant="flat" class="chart-chip">Bar Chart</v-chip>
            </div>
            <div class="chart-container" v-if="hasCancelledPerDay()">
              <canvas ref="cancelledDayChartCanvas"></canvas>
            </div>
            <div v-else class="no-data">
              <v-icon size="48" color="#a3a3a3" class="mb-3">mdi-chart-bar-stacked</v-icon>
              <div>No daily cancellation data</div>
            </div>
          </div>

          <div class="chart-card">
            <div class="chart-header">
              <h3 class="chart-title">Weekly Cancellations</h3>
              <v-chip size="small" color="#dc2626" variant="flat" class="chart-chip">Line Chart</v-chip>
            </div>
            <div class="chart-container" v-if="hasCancelledPerWeek()">
              <canvas ref="cancelledWeekChartCanvas"></canvas>
            </div>
            <div v-else class="no-data">
              <v-icon size="48" color="#a3a3a3" class="mb-3">mdi-chart-line-variant</v-icon>
              <div>No weekly cancellation data</div>
            </div>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, watch, nextTick } from 'vue';
import Chart from 'chart.js/auto';
import api from "../plugins/axios";

export default {
  name: 'BookingStatistics',

  setup() {
    // Data Refs
    const loading = ref(false);
    const error = ref(null);
    const stats = ref(null);
    const startDate = ref('');
    const endDate = ref('');

    // Canvas Refs
    const bookingsChartCanvas = ref(null);
    const hoursChartCanvas = ref(null);
    const cancelledDayChartCanvas = ref(null);
    const cancelledWeekChartCanvas = ref(null);

    // Chart Instances
    let bookingsChartInstance = null;
    let hoursChartInstance = null;
    let cancelledDayChartInstance = null;
    let cancelledWeekChartInstance = null;

    // Watcher
    watch(stats, async (newStats) => {
      if (!newStats) return;
      await nextTick();
      setTimeout(() => {
        createCharts();
      }, 100);
    });

    // API Calls
    const apiService = {
      async getStatistics() {
        try {
          const response = await api.get("/admin/statistics");
          return response.data;
        } catch (err) {
          throw new Error(err?.response?.data?.message || err?.message || "Failed to fetch statistics");
        }
      },
      async getStatisticsForRange(start, end) {
        try {
          const params = { startDate: start, endDate: end };
          const response = await api.get("/admin/statistics/range", { params });
          return response.data;
        } catch (err) {
          throw new Error(err?.response?.data?.message || err?.message || "Failed to fetch range statistics");
        }
      }
    };

    const fetchStatistics = async () => {
      try {
        loading.value = true;
        error.value = null;
        const data = await apiService.getStatistics();
        console.log("Fetched statistics:", data);
        stats.value = data;
      } catch (err) {
        console.error("Error fetching statistics:", err);
        error.value = err.message || "Failed to fetch statistics";
      } finally {
        loading.value = false;
      }
    };

    const fetchStatisticsForRange = async () => {
      if (!startDate.value || !endDate.value) {
        error.value = "Please select both start and end dates";
        return;
      }
      try {
        loading.value = true;
        error.value = null;
        const start = new Date(startDate.value).toISOString();
        const end = new Date(endDate.value).toISOString();
        const data = await apiService.getStatisticsForRange(start, end);
        stats.value = data;
      } catch (err) {
        error.value = err.message || "Failed to fetch statistics";
      } finally {
        loading.value = false;
      }
    };

    const resetToDefault = () => {
      startDate.value = '';
      endDate.value = '';
      fetchStatistics();
    };

    // --- Helpers ---

    const hasBookingsPerDay = () => stats.value?.bookingHoursPerDay?.length > 0;
    const hasBookingsPerWeek = () => stats.value?.bookingHoursPerWeek?.length > 0;
    const hasCancelledPerDay = () => stats.value?.cancelledBookingsPerDay?.length > 0;
    const hasCancelledPerWeek = () => stats.value?.cancelledBookingsPerWeek?.length > 0;

    const getMostBookedDeskName = () => stats.value?.mostBookedDesk?.deskName || 'N/A';
    const getMostBookedCount = () => stats.value?.mostBookedDesk?.bookingCount || 0;
    const getLeastBookedDeskName = () => stats.value?.leastBookedDesk?.deskName || 'N/A';
    const getLeastBookedCount = () => stats.value?.leastBookedDesk?.bookingCount || 0;
    const getMostCancelledDeskName = () => stats.value?.mostCancelledDesk?.deskName || 'N/A';
    const getMostCancelledCount = () => stats.value?.mostCancelledDesk?.bookingCount || 0;

    const parseChartDate = (item) => {
      let date;
      if (Array.isArray(item.date)) {
        date = new Date(item.date[0], item.date[1] - 1, item.date[2]);
      } else {
        date = new Date(item.date);
      }
      return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
    };

    // --- Chart Creation ---

    const createCharts = () => {
      if (!stats.value) return;

      // Destroy existing
      if (bookingsChartInstance) bookingsChartInstance.destroy();
      if (hoursChartInstance) hoursChartInstance.destroy();
      if (cancelledDayChartInstance) cancelledDayChartInstance.destroy();
      if (cancelledWeekChartInstance) cancelledWeekChartInstance.destroy();

      // 1. Daily Bookings
      if (bookingsChartCanvas.value && hasBookingsPerDay()) {
        const data = stats.value.bookingHoursPerDay;
        bookingsChartInstance = new Chart(bookingsChartCanvas.value.getContext('2d'), {
          type: 'bar',
          data: {
            labels: data.map(parseChartDate),
            datasets: [{
              label: 'Bookings per Day',
              data: data.map(i => i.count),
              backgroundColor: '#171717',
              borderColor: '#171717',
              borderWidth: 0,
              borderRadius: 8
            }]
          },
          options: getChartOptions()
        });
      }

      // 2. Weekly Bookings
      if (hoursChartCanvas.value && hasBookingsPerWeek()) {
        const data = stats.value.bookingHoursPerWeek;
        hoursChartInstance = new Chart(hoursChartCanvas.value.getContext('2d'), {
          type: 'line',
          data: {
            labels: data.map(parseChartDate),
            datasets: [{
              label: 'Bookings per Week',
              data: data.map(i => i.count),
              borderColor: '#171717',
              backgroundColor: 'rgba(23, 23, 23, 0.1)',
              tension: 0.4,
              fill: true,
              pointRadius: 6,
              pointBackgroundColor: '#171717',
              pointBorderColor: '#ffffff',
              borderWidth: 3
            }]
          },
          options: getChartOptions()
        });
      }

      // 3. Daily Cancelled
      if (cancelledDayChartCanvas.value && hasCancelledPerDay()) {
        const data = stats.value.cancelledBookingsPerDay;
        cancelledDayChartInstance = new Chart(cancelledDayChartCanvas.value.getContext('2d'), {
          type: 'bar',
          data: {
            labels: data.map(parseChartDate),
            datasets: [{
              label: 'Cancellations per Day',
              data: data.map(i => i.count),
              backgroundColor: '#dc2626',
              borderRadius: 8
            }]
          },
          options: getChartOptions()
        });
      }

      // 4. Weekly Cancelled
      if (cancelledWeekChartCanvas.value && hasCancelledPerWeek()) {
        const data = stats.value.cancelledBookingsPerWeek;
        cancelledWeekChartInstance = new Chart(cancelledWeekChartCanvas.value.getContext('2d'), {
          type: 'line',
          data: {
            labels: data.map(parseChartDate),
            datasets: [{
              label: 'Cancellations per Week',
              data: data.map(i => i.count),
              borderColor: '#dc2626',
              backgroundColor: 'rgba(220, 38, 38, 0.1)',
              tension: 0.4,
              fill: true,
              pointRadius: 6,
              pointBackgroundColor: '#dc2626',
              pointBorderColor: '#ffffff',
              borderWidth: 3
            }]
          },
          options: getChartOptions()
        });
      }
    };

    // Shared Options for consistency
    const getChartOptions = () => ({
      responsive: true,
      maintainAspectRatio: false,
      scales: {
        y: {
          beginAtZero: true,
          ticks: { stepSize: 1, font: { family: 'Inter', weight: '600', size: 12 }, color: '#737373' },
          grid: { color: '#f5f5f5' }
        },
        x: {
          ticks: { font: { family: 'Inter', weight: '600', size: 12 }, color: '#737373' },
          grid: { display: false }
        }
      },
      plugins: {
        legend: {
          display: true,
          position: 'top',
          labels: { font: { family: 'Inter', weight: '600', size: 13 }, color: '#171717', usePointStyle: true, padding: 15 }
        }
      }
    });

    onMounted(() => {
      fetchStatistics();
    });

    return {
      loading, error, stats, startDate, endDate,
      bookingsChartCanvas, hoursChartCanvas, cancelledDayChartCanvas, cancelledWeekChartCanvas,
      fetchStatistics, fetchStatisticsForRange, resetToDefault,
      hasBookingsPerDay, hasBookingsPerWeek, hasCancelledPerDay, hasCancelledPerWeek,
      getMostBookedDeskName, getMostBookedCount, getLeastBookedDeskName, getLeastBookedCount,
      getMostCancelledDeskName, getMostCancelledCount
    };
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

.admin-statistics {
  padding: 28px;
  background: #fafafa;
  min-height: 100vh;
}

.admin-card {
  background: #ffffff;
  border: 1px solid #e5e5e5;
  border-radius: 20px;
  padding: 28px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
}

.admin-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 32px;
  padding-bottom: 24px;
  border-bottom: 2px solid #f5f5f5;
}

.title-wrap {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.workspace-label {
  font-size: 11px;
  font-weight: 700;
  color: #737373;
  letter-spacing: 1.5px;
}

.title {
  font-size: 28px;
  font-weight: 800;
  color: #171717;
  margin: 0;
  letter-spacing: -0.5px;
  line-height: 1;
}

.sub {
  font-size: 13px;
  font-weight: 600;
  color: #737373;
  letter-spacing: 0.3px;
}

.loading-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
}

.loading-text {
  font-size: 15px;
  font-weight: 600;
  color: #737373;
  letter-spacing: 0.3px;
}

.filter-section {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 32px;
  padding: 20px;
  background: #fafafa;
  border-radius: 16px;
  border: 1px solid #f5f5f5;
}

.date-field {
  flex: 1;
  max-width: 280px;
}

.date-field :deep(.v-field) {
  border-radius: 12px !important;
  border: 2px solid #e5e5e5 !important;
  font-weight: 600;
}

.date-field :deep(.v-field:hover) {
  border-color: #a3a3a3 !important;
}

.date-field :deep(.v-field--focused) {
  border-color: #171717 !important;
  box-shadow: 0 0 0 3px rgba(0, 0, 0, 0.05);
}

.apply-btn {
  font-weight: 600 !important;
  text-transform: none !important;
  letter-spacing: 0.3px;
  border-radius: 12px !important;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08) !important;
  transition: all 0.3s ease;
  padding: 0 24px !important;
}

.apply-btn:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12) !important;
  transform: translateY(-1px);
}

.reset-btn-outline {
  font-weight: 600 !important;
  text-transform: none !important;
  letter-spacing: 0.3px;
  border-radius: 12px !important;
  border: 2px solid #e5e5e5 !important;
  color: #171717 !important;
  transition: all 0.3s ease;
}

.reset-btn-outline:hover {
  border-color: #171717 !important;
  background: #fafafa !important;
}

/* Stats Grid */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 32px;
}

.stat-card {
  background: #ffffff;
  border: 1px solid #f5f5f5;
  border-radius: 16px;
  padding: 24px;
  text-align: center;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  border-color: #e5e5e5;
}

.stat-icon-wrap {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
  transition: all 0.3s ease;
}

.stat-icon-wrap.positive {
  background: #dcfce7;
  color: #16a34a;
}

.stat-icon-wrap.negative {
  background: #fee2e2;
  color: #dc2626;
}

.stat-icon-wrap.highlight {
  background: #fef3c7;
  color: #ca8a04;
}

.stat-icon-wrap.neutral {
  background: #f5f5f5;
  color: #737373;
}

.stat-value {
  font-size: 24px;
  font-weight: 800;
  letter-spacing: -0.5px;
  margin-bottom: 8px;
}

.stat-value.positive {
  color: #16a34a;
}

.stat-value.negative {
  color: #dc2626;
}

.stat-card.highlight .stat-value {
  color: #171717;
}

.stat-card.neutral .stat-value {
  color: #171717;
}

.stat-label {
  font-size: 13px;
  font-weight: 600;
  color: #737373;
  letter-spacing: 0.3px;
  margin-bottom: 4px;
}

.stat-count {
  font-size: 12px;
  font-weight: 600;
  color: #a3a3a3;
  letter-spacing: 0.2px;
}

/* Charts Section */
.charts-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(500px, 1fr));
  gap: 24px;
}

.chart-card {
  background: #ffffff;
  border: 1px solid #f5f5f5;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.chart-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 2px solid #f5f5f5;
}

.chart-title {
  font-size: 18px;
  font-weight: 700;
  color: #171717;
  letter-spacing: -0.3px;
  margin: 0;
}

.chart-chip {
  font-weight: 700 !important;
  font-size: 11px !important;
  color: #fff !important;
  letter-spacing: 0.5px;
  text-transform: uppercase;
}

.chart-container {
  height: 300px;
  position: relative;
}

.chart-container canvas {
  max-width: 100%;
  max-height: 100%;
}

.no-data {
  text-align: center;
  padding: 60px 20px;
  color: #737373;
}

.no-data div {
  font-weight: 600;
  font-size: 14px;
  letter-spacing: 0.3px;
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  .charts-section {
    grid-template-columns: 1fr;
  }
  .filter-section {
    flex-direction: column;
    align-items: stretch;
  }
  .date-field {
    max-width: 100%;
  }
}
</style>