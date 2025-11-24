<template>
  <div class="admin-statistics">
    <div class="admin-card">
      <!-- Header Section -->
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
        <!-- Date Range Filters -->
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

        <!-- Stats Cards Grid -->
        <div class="stats-grid">
          <!-- Weekly Bookings -->
          <div class="stat-card">
            <div class="stat-icon-wrap" :class="stats.weeklyBookings >= 0 ? 'positive' : 'negative'">
              <v-icon size="32">
                {{ stats.weeklyBookings >= 0 ? 'mdi-trending-up' : 'mdi-trending-down' }}
              </v-icon>
            </div>
            <div class="stat-value" :class="stats.weeklyBookings >= 0 ? 'positive' : 'negative'">
              {{ stats.weeklyBookings >= 0 ? '+' : '' }}{{ stats.weeklyBookings }}%
            </div>
            <div class="stat-label">Weekly Bookings</div>
            <div class="stat-count">{{ stats.weeklyBookings || 0 }} bookings</div>
          </div>

          <!-- Weekly Users -->
          <div class="stat-card">
            <div class="stat-icon-wrap" :class="stats.weeklyUsers >= 0 ? 'positive' : 'negative'">
              <v-icon size="32">
                {{ stats.weeklyUsers >= 0 ? 'mdi-trending-up' : 'mdi-trending-down' }}
              </v-icon>
            </div>
            <div class="stat-value" :class="stats.weeklyUsers >= 0 ? 'positive' : 'negative'">
              {{ stats.weeklyUsers >= 0 ? '+' : '' }}{{ stats.weeklyUsers }}
            </div>
            <div class="stat-label">Weekly Users</div>
            <div class="stat-count">Active this week</div>
          </div>

          <!-- Monthly Bookings -->
          <div class="stat-card">
            <div class="stat-icon-wrap" :class="stats.monthlyBookings >= 0 ? 'positive' : 'negative'">
              <v-icon size="32">
                {{ stats.monthlyBookings >= 0 ? 'mdi-trending-up' : 'mdi-trending-down' }}
              </v-icon>
            </div>
            <div class="stat-value" :class="stats.monthlyBookings >= 0 ? 'positive' : 'negative'">
              {{ stats.monthlyBookings || 0 }}
            </div>
            <div class="stat-label">Monthly Bookings</div>
            <div class="stat-count">{{ stats.monthlyUsers || 0 }} users</div>
          </div>

          <!-- Most Booked Desk -->
          <div class="stat-card highlight">
            <div class="stat-icon-wrap highlight">
              <v-icon size="32">mdi-star</v-icon>
            </div>
            <div class="stat-value">{{ getMostBookedDeskName() }}</div>
            <div class="stat-label">Most Booked Desk</div>
            <div class="stat-count">{{ getMostBookedCount() }} bookings</div>
          </div>

          <!-- Least Booked Desk -->
          <div class="stat-card neutral">
            <div class="stat-icon-wrap neutral">
              <v-icon size="32">mdi-emoticon-sad-outline</v-icon>
            </div>
            <div class="stat-value">{{ getLeastBookedDeskName() }}</div>
            <div class="stat-label">Least Booked Desk</div>
            <div class="stat-count">{{ getLeastBookedCount() }} bookings</div>
          </div>
        </div>

        <!-- Charts Section -->
        <div class="charts-section">
          <!-- Bookings Per Day Chart -->
          <div class="chart-card">
            <div class="chart-header">
              <h3 class="chart-title">Daily Bookings</h3>
              <v-chip size="small" color="#171717" variant="flat" class="chart-chip">
                Bar Chart
              </v-chip>
            </div>
            <div class="chart-container" v-if="hasBookingsPerDay()">
              <canvas ref="bookingsChartCanvas"></canvas>
            </div>
            <div v-else class="no-data">
              <v-icon size="48" color="#a3a3a3" class="mb-3">mdi-chart-bar</v-icon>
              <div>No daily booking data available</div>
            </div>
          </div>

          <!-- Bookings Per Week Chart -->
          <div class="chart-card">
            <div class="chart-header">
              <h3 class="chart-title">Weekly Bookings</h3>
              <v-chip size="small" color="#171717" variant="flat" class="chart-chip">
                Line Chart
              </v-chip>
            </div>
            <div class="chart-container" v-if="hasBookingsPerWeek()">
              <canvas ref="hoursChartCanvas"></canvas>
            </div>
            <div v-else class="no-data">
              <v-icon size="48" color="#a3a3a3" class="mb-3">mdi-chart-line</v-icon>
              <div>No weekly booking data available</div>
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
    const loading = ref(false);
    const error = ref(null);
    const stats = ref(null);
    const startDate = ref('');
    const endDate = ref('');
    const bookingsChartCanvas = ref(null);
    const hoursChartCanvas = ref(null);

    let bookingsChartInstance = null;
    let hoursChartInstance = null;

    watch(stats, async (newStats) => {
      console.log('Stats updated:', newStats);
      if (!newStats) return;
      await nextTick();
      setTimeout(() => {
        createCharts();
      }, 100);
    });

    const apiService = {
      async getStatistics() {
        try {
          const response = await api.get("/admin/statistics");
          return response.data;
        } catch (err) {
          throw new Error(
              err?.response?.data?.message ||
              err?.message ||
              "Failed to fetch statistics"
          );
        }
      },

      async getStatisticsForRange(start, end) {
        try {
          const params = { startDate: start, endDate: end };
          const response = await api.get("/admin/statistics/range", { params });
          return response.data;
        } catch (err) {
          throw new Error(
              err?.response?.data?.message ||
              err?.message ||
              "Failed to fetch range statistics"
          );
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
        console.log("Fetched range statistics:", data);
        stats.value = data;

      } catch (err) {
        console.error("Error fetching range statistics:", err);
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

    // Helper functions to extract data from the API response
    const hasBookingsPerDay = () => {
      return stats.value?.bookingHoursPerDay &&
          Array.isArray(stats.value.bookingHoursPerDay) &&
          stats.value.bookingHoursPerDay.length > 0;
    };

    const hasBookingsPerWeek = () => {
      return stats.value?.bookingHoursPerWeek &&
          Array.isArray(stats.value.bookingHoursPerWeek) &&
          stats.value.bookingHoursPerWeek.length > 0;
    };

    const getMostBookedDeskName = () => {
      return stats.value?.mostBookedDesk?.deskName || 'N/A';
    };

    const getMostBookedCount = () => {
      return stats.value?.mostBookedDesk?.bookingCount || 0;
    };

    const getLeastBookedDeskName = () => {
      return stats.value?.leastBookedDesk?.deskName || 'N/A';
    };

    const getLeastBookedCount = () => {
      return stats.value?.leastBookedDesk?.bookingCount || 0;
    };

    const createCharts = () => {
      console.log('Creating charts with stats:', stats.value);

      if (!stats.value) {
        console.warn('No stats available for charts');
        return;
      }

      // Destroy existing charts
      if (bookingsChartInstance) {
        bookingsChartInstance.destroy();
        bookingsChartInstance = null;
      }
      if (hoursChartInstance) {
        hoursChartInstance.destroy();
        hoursChartInstance = null;
      }

      // Daily Bookings Chart
      if (bookingsChartCanvas.value && hasBookingsPerDay()) {
        const ctx = bookingsChartCanvas.value.getContext('2d');

        // Backend now sends aggregated data: [{date: "2025-11-09", count: 10}, ...]
        const dailyData = stats.value.bookingHoursPerDay;

        console.log('Daily data from backend:', dailyData);

        try {
          const labels = dailyData.map(item => {
            // Parse the date string (format: "2025-11-09" or array [2025, 11, 9])
            let date;
            if (Array.isArray(item.date)) {
              // Handle LocalDate as array [year, month, day]
              date = new Date(item.date[0], item.date[1] - 1, item.date[2]);
            } else {
              date = new Date(item.date);
            }

            return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
          });

          const bookingCounts = dailyData.map(item => item.count);

          bookingsChartInstance = new Chart(ctx, {
            type: 'bar',
            data: {
              labels: labels,
              datasets: [{
                label: 'Bookings per Day',
                data: bookingCounts,
                backgroundColor: '#171717',
                borderColor: '#171717',
                borderWidth: 0,
                borderRadius: 8
              }]
            },
            options: {
              responsive: true,
              maintainAspectRatio: false,
              scales: {
                y: {
                  beginAtZero: true,
                  ticks: {
                    stepSize: 1,
                    font: {
                      family: 'Inter',
                      weight: '600',
                      size: 12
                    },
                    color: '#737373'
                  },
                  grid: {
                    color: '#f5f5f5'
                  }
                },
                x: {
                  ticks: {
                    font: {
                      family: 'Inter',
                      weight: '600',
                      size: 12
                    },
                    color: '#737373'
                  },
                  grid: {
                    display: false
                  }
                }
              },
              plugins: {
                legend: {
                  display: true,
                  position: 'top',
                  labels: {
                    font: {
                      family: 'Inter',
                      weight: '600',
                      size: 13
                    },
                    color: '#171717',
                    usePointStyle: true,
                    padding: 15
                  }
                }
              }
            }
          });
          console.log('Daily bookings chart created successfully');
        } catch (err) {
          console.error('Error creating daily bookings chart:', err);
        }
      }

      // Weekly Bookings Chart
      if (hoursChartCanvas.value && hasBookingsPerWeek()) {
        const ctx = hoursChartCanvas.value.getContext('2d');

        // Backend now sends aggregated data: [{date: "2025-11-02", count: 1}, ...]
        const weeklyData = stats.value.bookingHoursPerWeek;

        console.log('Weekly data from backend:', weeklyData);

        try {
          const labels = weeklyData.map(item => {
            // Parse the date string (format: "2025-11-09" or array [2025, 11, 9])
            let date;
            if (Array.isArray(item.date)) {
              // Handle LocalDate as array [year, month, day]
              date = new Date(item.date[0], item.date[1] - 1, item.date[2]);
            } else {
              date = new Date(item.date);
            }

            return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
          });

          const bookingCounts = weeklyData.map(item => item.count);

          hoursChartInstance = new Chart(ctx, {
            type: 'line',
            data: {
              labels: labels,
              datasets: [{
                label: 'Bookings per Week',
                data: bookingCounts,
                borderColor: '#171717',
                backgroundColor: 'rgba(23, 23, 23, 0.1)',
                tension: 0.4,
                fill: true,
                pointRadius: 6,
                pointBackgroundColor: '#171717',
                pointBorderColor: '#ffffff',
                pointBorderWidth: 2,
                borderWidth: 3
              }]
            },
            options: {
              responsive: true,
              maintainAspectRatio: false,
              scales: {
                y: {
                  beginAtZero: true,
                  ticks: {
                    stepSize: 1,
                    font: {
                      family: 'Inter',
                      weight: '600',
                      size: 12
                    },
                    color: '#737373'
                  },
                  grid: {
                    color: '#f5f5f5'
                  }
                },
                x: {
                  ticks: {
                    font: {
                      family: 'Inter',
                      weight: '600',
                      size: 12
                    },
                    color: '#737373'
                  },
                  grid: {
                    display: false
                  }
                }
              },
              plugins: {
                legend: {
                  display: true,
                  position: 'top',
                  labels: {
                    font: {
                      family: 'Inter',
                      weight: '600',
                      size: 13
                    },
                    color: '#171717',
                    usePointStyle: true,
                    padding: 15
                  }
                }
              }
            }
          });
          console.log('Weekly bookings chart created successfully');
        } catch (err) {
          console.error('Error creating weekly bookings chart:', err);
        }
      }
    };

    onMounted(() => {
      console.log('Component mounted, fetching statistics...');
      fetchStatistics();
    });

    return {
      loading,
      error,
      stats,
      startDate,
      endDate,
      bookingsChartCanvas,
      hoursChartCanvas,
      fetchStatistics,
      fetchStatisticsForRange,
      resetToDefault,
      hasBookingsPerDay,
      hasBookingsPerWeek,
      getMostBookedDeskName,
      getMostBookedCount,
      getLeastBookedDeskName,
      getLeastBookedCount
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

/* Filter Section */
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