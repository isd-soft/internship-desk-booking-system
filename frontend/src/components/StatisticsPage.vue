
<template>
  <v-app>
    <v-app-bar color="orange"
               dark elevation="2">
      <v-btn icon @click="goBack">
        <v-icon>mdi-arrow-left</v-icon>
      </v-btn>
      <v-app-bar-title class="mx-auto" style="position: absolute; left: 50%; transform: translateX(-50%);">
        Statistics
      </v-app-bar-title>
    </v-app-bar>

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
            <v-col cols="auto">
              <v-card class="rounded-card text-center pa-4"
                      color="#f0f0f0"
                      style="max-width: 200px"
              >
                <v-icon color="green" size="48">mdi-trending-up</v-icon>
                <div class="text-h6 text-green mt-2">
                  +{{ stats.metrics.weeklyBookings.value }} % in
                </div>
                <div class="text-caption text-grey-darken-1">Weekly Bookings</div>
              </v-card>
            </v-col>

            <!-- Weekly Users -->
            <v-col cols="auto">
              <v-card class="rounded-card text-center pa-4"
                      color="#f0f0f0"
                      style="max-width: 200px"
              >
                <v-icon color="green" size="48">mdi-account-multiple</v-icon>
                <div class="text-h6 text-green mt-2">
                  +{{ stats.metrics.weeklyUsers.value }} % in
                </div>
                <div class="text-caption text-grey-darken-1">Weekly Users</div>
              </v-card>
            </v-col>

            <!-- Lifetime Bookings -->
            <v-col cols="auto">
              <v-card  class="rounded-card text-center pa-4"
                       color="#f0f0f0"
                       style="max-width: 200px"
              >
                <v-icon color="red" size="48">mdi-trending-down</v-icon>
                <div class="text-h6 text-red mt-2">
                  {{ stats.metrics.lifetimeBookings.value }} % in
                </div>
                <div class="text-caption text-grey-darken-1">Monthly Bookings</div>
              </v-card>
            </v-col>

            <!-- Most Booked Desk -->
            <v-col cols="auto">
              <v-card class="rounded-card text-center pa-4"
                      color="#f0f0f0"
                      style="max-width: 200px"
              >
                <v-icon color="yellow-darken-2" size="48">mdi-star</v-icon>
                <div class="text-h6 mt-2">{{ stats.metrics.mostBookedDesk }}</div>
                <div class="text-caption text-grey-darken-1">Most Booked Desk</div>
              </v-card>
            </v-col>

            <!-- Least Booked Desk -->
            <v-col cols="12" sm="6" md="4" lg="2">
              <v-card class="rounded-card text-center pa-4"
                      color="#f0f0f0"
                      style="max-width: 200px"
              >
                <v-icon color="grey" size="48">mdi-emoticon-sad-outline</v-icon>
                <div class="text-h6 mt-2">{{ stats.metrics.leastBookedDesk }}</div>
                <div class="text-caption text-grey-darken-1">Least Booked Desk</div>
              </v-card>
            </v-col>
          </v-row>

          <!-- Booking Analytics Card -->
          <v-card class="pa-6"
                  color="#f0f0f0"
          >
            <v-card-title class="text-h5 mb-4">Booking Analytics</v-card-title>

            <!-- View Mode Toggle -->
            <v-btn-toggle
                v-model="viewMode"
                color="orange"
                mandatory
                class="mb-6"
            >
              <v-btn rounded="lg" value="weekly">Weekly View</v-btn>
              <v-btn rounded="lg" value="monthly">Monthly View</v-btn>
            </v-btn-toggle>

            <!-- Bookings per Day Chart -->
            <v-card class="pa-6 mb-6" color="#f0f0f0">
              <v-card-title class="text-h6 mb-4">Bookings per Day</v-card-title>
              <v-card-text>
                <div style="height: 300px;">
                  <!-- Canvas only rendered when stats exist -->
                  <canvas v-if="stats" ref="bookingsChart"></canvas>
                </div>
              </v-card-text>
            </v-card>

            <!-- Booking Hours per Day Chart -->
            <v-card class="pa-6" color="#f0f0f0">
              <v-card-title class="text-h6 mb-4">Booking Hours per Day</v-card-title>
              <v-card-text>
                <div style="height: 300px;">
                  <!-- Canvas only rendered when stats exist -->
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
    const viewMode = ref('weekly');
    const bookingsChart = ref(null);
    const hoursChart = ref(null);

    let bookingsChartInstance = null;
    let hoursChartInstance = null;

    // API Service
    const apiService = {
      // GET: Fetch statistics
      async getStatistics(view) {
        const response = await fetch(`/api/statistics?view=${view}`, {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json',
            // Add authentication header if needed
            // 'Authorization': `Bearer ${token}`
          }
        });

        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }

        return await response.json();
      }
    };

    // Fetch statistics data
    const fetchStatistics = async () => {
      loading.value = true;
      error.value = null;

      try {
        // TODO: Replace with your actual API endpoint
        // const data = await apiService.getStatistics(viewMode.value);
        // stats.value = data;

        // Simulated API delay
        await new Promise(resolve => setTimeout(resolve, 500));

        // Mock data (replace this with actual API response)
        stats.value = {
          metrics: {
            weeklyBookings: { value: 13, trend: 'up' },
            lifetimeBookings: { value: -8, trend: 'down' },
            mostBookedDesk: 'Ser-01',
            leastBookedDesk: 'Dev-02',
            weeklyUsers: { value: 11, trend: 'up' }
          },
          bookingsPerDay: [
            { day: 'Mon', bookings: 12 },
            { day: 'Tue', bookings: 14 },
            { day: 'Wed', bookings: 18 },
            { day: 'Thu', bookings: 13 },
            { day: 'Fri', bookings: 19 },
            { day: 'Sat', bookings: 7 },
            { day: 'Sun', bookings: 5 }
          ],
          bookingHoursPerDay: [
            { day: 'Mon', hours: 96 },
            { day: 'Tue', hours: 112 },
            { day: 'Wed', hours: 144 },
            { day: 'Thu', hours: 104 },
            { day: 'Fri', hours: 152 },
            { day: 'Sat', hours: 56 },
            { day: 'Sun', hours: 40 }
          ]
        };

        await nextTick();
        createCharts();

      } catch (err) {
        error.value = err.message;
        console.error('Error fetching statistics:', err);
      } finally {
        loading.value = false;
      }
    };

    // Create Chart.js charts
    const createCharts = () => {
      if (!stats.value) return;

      // Destroy existing charts
      if (bookingsChartInstance) bookingsChartInstance.destroy();
      if (hoursChartInstance) hoursChartInstance.destroy();

      // Bookings per Day Bar Chart
      const bookingsCtx = bookingsChart.value?.getContext('2d');
      if (bookingsCtx) {
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
                beginAtZero: true
              }
            }
          }
        });
      }

      // Booking Hours Line Chart
      const hoursCtx = hoursChart.value?.getContext('2d');
      if (hoursCtx) {
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
            }
          }
        });
      }
    };

    const goBack = () => {
      // TODO: Implement navigation
      window.history.back();
    };

    // Watch for view mode changes
    watch(viewMode, () => {
      fetchStatistics();
    });

    // Fetch data on mount
    onMounted(() => {
      fetchStatistics();
    });

    return {
      loading,
      error,
      stats,
      viewMode,
      bookingsChart,
      hoursChart,
      fetchStatistics,
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
.rounded-card{
  border-radius:10px;
}

canvas {
  display: block;
  max-width: 100%;
  max-height: 100%;
}

</style>
