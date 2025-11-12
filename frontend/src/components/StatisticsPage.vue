<template>
  <v-app>
    <!-- Main Content -->
    <v-main class="bg-grey-lighten-4">
      <v-container fluid class="pa-6">
        <!-- Loading State -->
        <v-row v-if="loading" justify="center" class="mt-12">
          <v-progress-circular
              indeterminate
              color="orange"
              size="64"
          ></v-progress-circular>
        </v-row>

        <!-- Error State -->
        <v-row v-else-if="error" justify="center" class="mt-12">
          <v-col cols="12" md="6">
            <v-card>
              <v-card-title class="text-red">Error</v-card-title>
              <v-card-text>{{ error }}</v-card-text>
              <v-card-actions>
                <v-btn color="orange" @click="fetchStatistics">Retry</v-btn>
              </v-card-actions>
            </v-card>
          </v-col>
        </v-row>

        <!-- Stats Content -->
        <template v-else-if="stats">
          <!-- Stats Cards -->
          <v-row align="center" justify="center" class="mb-3">
            <!-- Weekly Bookings -->
            <v-col cols="12" sm="6" md="4" lg="auto">
              <v-card class="rounded-card text-center pa-4"
                      color="#f0f0f0"
                      style="max-width: 200px; margin: 0 auto;"
              >
                <v-icon :color="stats.weeklyBookings >= 0 ? 'green' : 'red'" size="48">
                  {{ stats.weeklyBookings >= 0 ? 'mdi-trending-up' : 'mdi-trending-down' }}
                </v-icon>
                <div class="text-h6 mt-2" :class="stats.weeklyBookings >= 0 ? 'text-green' : 'text-red'">
                  {{ stats.weeklyBookings >= 0 ? '+' : '' }}{{ stats.weeklyBookings }}%
                </div>
                <div class="text-caption text-grey-darken-1">Weekly Bookings</div>
              </v-card>
            </v-col>

            <!-- Weekly Users -->
            <v-col cols="12" sm="6" md="4" lg="auto">
              <v-card class="rounded-card text-center pa-4"
                      color="#f0f0f0"
                      style="max-width: 200px; margin: 0 auto;"
              >
                <v-icon :color="stats.weeklyUsers >= 0 ? 'green' : 'red'" size="48">
                  {{ stats.weeklyUsers >= 0 ? 'mdi-trending-up' : 'mdi-trending-down' }}
                </v-icon>
                <div class="text-h6 mt-2" :class="stats.weeklyUsers >= 0 ? 'text-green' : 'text-red'">
                  {{ stats.weeklyUsers >= 0 ? '+' : '' }}{{ stats.weeklyUsers }}%
                </div>
                <div class="text-caption text-grey-darken-1">Weekly Users</div>
              </v-card>
            </v-col>

            <!-- Monthly Bookings -->
            <v-col cols="12" sm="6" md="4" lg="auto">
              <v-card class="rounded-card text-center pa-4"
                      color="#f0f0f0"
                      style="max-width: 200px; margin: 0 auto;"
              >
                <v-icon :color="stats.monthlyUsers >= 0 ? 'green' : 'red'" size="48">
                  {{ stats.monthlyUsers >= 0 ? 'mdi-trending-up' : 'mdi-trending-down' }}
                </v-icon>
                <div class="text-h6 mt-2" :class="stats.monthlyUsers >= 0 ? 'text-green' : 'text-red'">
                  {{ stats.monthlyUsers >= 0 ? '+' : '' }}{{ stats.monthlyUsers }}%
                </div>
                <div class="text-caption text-grey-darken-1">Monthly Bookings</div>
              </v-card>
            </v-col>

            <!-- Most Booked Desk -->
            <v-col cols="12" sm="6" md="4" lg="auto">
              <v-card class="rounded-card text-center pa-4"
                      color="#f0f0f0"
                      style="max-width: 200px; margin: 0 auto;"
              >
                <v-icon color="yellow-darken-2" size="48">mdi-star</v-icon>
                <div class="text-h6 mt-2">{{ (stats.mostBookedDesk && stats.mostBookedDesk.deskName) || 'N/A' }}</div>
                <div class="text-caption text-grey-darken-1">Most Booked Desk</div>
              </v-card>
            </v-col>

            <!-- Least Booked Desk -->
            <v-col cols="12" sm="6" md="4" lg="auto">
              <v-card class="rounded-card text-center pa-4"
                      color="#f0f0f0"
                      style="max-width: 200px; margin: 0 auto;"
              >
                <v-icon color="grey" size="48">mdi-emoticon-sad-outline</v-icon>
                <div class="text-h6 mt-2">{{ (stats.leastBookedDesk && stats.leastBookedDesk.deskName) || 'N/A' }}</div>
                <div class="text-caption text-grey-darken-1">Least Booked Desk</div>
              </v-card>
            </v-col>
          </v-row>

          <!-- Booking Analytics Card -->
          <v-card class="pa-6" color="#f0f0f0">
            <v-card-title class="text-h5 mb-4">Booking Analytics</v-card-title>
            <v-row class="mb-4">
              <v-col cols="12" md="4">
                <v-text-field
                    v-model="startDate"
                    label="Start Date"
                    type="datetime-local"
                    variant="outlined"
                    density="compact"
                ></v-text-field>
              </v-col>
              <v-col cols="12" md="4">
                <v-text-field
                    v-model="endDate"
                    label="End Date"
                    type="datetime-local"
                    variant="outlined"
                    density="compact"
                ></v-text-field>
              </v-col>
              <v-col cols="12" md="4" class="d-flex align-center">
                <v-btn color="orange" @click="fetchStatisticsForRange" :loading="loading">
                  Apply Range
                </v-btn>
                <v-btn variant="text" @click="resetToDefault" class="ml-2">
                  Reset
                </v-btn>
              </v-col>
            </v-row>

            <!-- Bookings per Day Chart -->
            <v-card class="pa-6 mb-6" color="#f0f0f0">
              <v-card-title class="text-h6 mb-4">Bookings</v-card-title>
              <v-card-text>
                <div style="height: 300px;">
                  <canvas ref="bookingsChart" v-show="stats"></canvas>
                </div>
              </v-card-text>
            </v-card>

            <!-- Booking Hours per Day Chart -->
            <v-card class="pa-6" color="#f0f0f0">
              <v-card-title class="text-h6 mb-4">Users</v-card-title>
              <v-card-text>
                <div style="height: 300px;">
                  <canvas v-if="stats" ref="hoursChart"></canvas>
                </div>
              </v-card-text>
            </v-card>
          </v-card>
        </template>
      </v-container>
    </v-main>
  </v-app>
</template>

<script>
import { ref, onMounted, watch, nextTick } from 'vue';
import Chart from 'chart.js/auto';


export default {
  name: 'BookingStatistics',

  setup() {
    const loading = ref(false);
    const error = ref(null);
    const stats = ref(null);
    const startDate = ref('');
    const endDate = ref('');
    const bookingsChart = ref(null);
    const hoursChart = ref(null);

    let bookingsChartInstance = null;
    let hoursChartInstance = null;


    watch(stats, async (newStats) => {
      if (!newStats) return;
      await nextTick();
      createCharts();
    });

    const apiService = {
      async getStatistics() {
        const response = await fetch('http://localhost:8080/api/statistics', {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json',
             'Authorization': `Bearer ${localStorage.getItem('token')}`
          },
          credentials: 'include'
        });

        if (!response.ok) {
          const errorText = await response.text();
          throw new Error(`Failed to fetch statistics: ${response.status} - ${errorText}`);
        }

        return await response.json();
      },

      async getStatisticsForRange(start, end) {
        const params = new URLSearchParams({
          startDate: start,
          endDate: end
        });

        const response = await fetch(`http://localhost:8080/api/statistics/range?${params}`, {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json',
             'Authorization': `Bearer ${localStorage.getItem('token')}`
          },
          credentials: 'include'
        });

        if (!response.ok) {
          const errorText = await response.text();
          throw new Error(`Failed to fetch statistics: ${response.status} - ${errorText}`);
        }

        return await response.json();
      }
    };

    const fetchStatistics = async () => {
      loading.value = true;
      error.value = null;

      try {
        const data = await apiService.getStatistics();
        stats.value = data;

        await nextTick();
        createCharts();
      } catch (err) {
        error.value = err.message;
        console.error('Error fetching statistics:', err);
      } finally {
        loading.value = false;
      }
    };

    // Fetch statistics for custom date range
    const fetchStatisticsForRange = async () => {
      if (!startDate.value || !endDate.value) {
        error.value = 'Please select both start and end dates';
        return;
      }

      loading.value = true;
      error.value = null;

      try {
        // Convert to ISO format for backend
        const start = new Date(startDate.value).toISOString();
        const end = new Date(endDate.value).toISOString();

        const data = await apiService.getStatisticsForRange(start, end);
        stats.value = data;

        await nextTick();
        createCharts();
      } catch (err) {
        error.value = err.message;
        console.error('Error fetching statistics:', err);
      } finally {
        loading.value = false;
      }
    };

    // Reset to default view
    const resetToDefault = () => {
      startDate.value = '';
      endDate.value = '';
      fetchStatistics();
    };

    // Create Chart.js charts
    const createCharts = () => {
      if (!stats.value) return;

      // Destroy existing charts
      if (bookingsChartInstance) bookingsChartInstance.destroy();
      if (hoursChartInstance) hoursChartInstance.destroy();

      // Bookings per Day Bar Chart
      const bookingsCtx = bookingsChart.value?.getContext('2d');
      if (bookingsCtx && stats.value.bookingsPerDay) {
        bookingsChartInstance = new Chart(bookingsCtx, {
          type: 'bar',
          data: {
            labels: stats.value.bookingsPerDay.map(d => d.day),
            datasets: [{
              label: 'Number of Bookings',
              data: stats.value.bookingsPerDay.map(d => d.bookings),
              backgroundColor: '#ff8c00',
              borderColor: '#ff8c00',
              borderWidth: 1
            }]
          },
          options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
              y: {
                beginAtZero: true,
                ticks: {
                  stepSize: 1
                }
              }
            },
            plugins: {
              legend: {
                display: true,
                position: 'top'
              }
            }
          }
        });
      }

      // Booking Hours Line Chart
      const hoursCtx = hoursChart.value?.getContext('2d');
      if (hoursCtx && stats.value.bookingHoursPerDay) {
        hoursChartInstance = new Chart(hoursCtx, {
          type: 'line',
          data: {
            labels: stats.value.bookingHoursPerDay.map(d => d.day),
            datasets: [{
              label: 'Total Hours',
              data: stats.value.bookingHoursPerDay.map(d => d.hours),
              borderColor: '#10b981',
              backgroundColor: 'rgba(16, 185, 129, 0.1)',
              tension: 0.4,
              fill: true,
              pointRadius: 5,
              pointBackgroundColor: '#10b981'
            }]
          },
          options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
              y: {
                beginAtZero: true
              }
            },
            plugins: {
              legend: {
                display: true,
                position: 'top'
              }
            }
          }
        });
      }
    };

    const goBack = () => {
      window.history.back();
    };

    // Fetch data on mount
    onMounted(() => {
      fetchStatistics();
    });

    return {
      loading,
      error,
      stats,
      startDate,
      endDate,
      bookingsChart,
      hoursChart,
      fetchStatistics,
      fetchStatisticsForRange,
      resetToDefault,
      goBack
    };
  }
};
</script>

<style scoped>
.bg-grey-lighten-4 {
  background-color: #f5f5f5;
}

.v-toolbar-title {
  font-size: 2rem !important;
  font-weight: bold;
}

.rounded-card {
  border-radius: 10px;
}

canvas {
  width: 100% !important;
  height: 300px !important;
}

</style>