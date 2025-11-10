<template>
  <v-container class="pa-6" fluid>
    <!-- Header -->
    <v-row justify="center" class="mb-6">
      <v-col cols="12" class="text-center">
        <v-sheet color="orange" class="pa-4 rounded-lg">
          <h2 class="white--text mb-0">Statistics</h2>
        </v-sheet>
      </v-col>
    </v-row>

    <!-- Stats Cards -->
    <v-row justify="center" align="stretch" dense>
      <!-- Weekly Bookings -->
      <v-col cols="12" sm="6" md="4">
        <v-card class="pa-6 d-flex flex-column align-center" color="grey lighten-3" elevation="2">
          <v-icon
              :color="stats.weeklyBookings >= 0 ? 'green' : 'red'"
              size="40"
          >
            {{ stats.weeklyBookings >= 0 ? 'mdi-arrow-up-bold' : 'mdi-arrow-down-bold' }}
          </v-icon>
          <h3>{{ formatPercent(stats.weeklyBookings) }} in</h3>
          <p>Weekly Bookings</p>
        </v-card>
      </v-col>

      <!-- Lifetime Bookings -->
      <v-col cols="12" sm="6" md="4">
        <v-card class="pa-6 d-flex flex-column align-center" color="grey lighten-3" elevation="2">
          <v-icon
              :color="stats.lifetimeBookings >= 0 ? 'green' : 'red'"
              size="40"
          >
            {{ stats.lifetimeBookings >= 0 ? 'mdi-arrow-up-bold' : 'mdi-arrow-down-bold' }}
          </v-icon>
          <h3>{{ formatPercent(stats.lifetimeBookings) }} in</h3>
          <p>Lifetime Bookings</p>
        </v-card>
      </v-col>

      <!-- Most Booked Desk -->
      <v-col cols="12" sm="6" md="4">
        <v-card class="pa-6 d-flex flex-column align-center" color="grey lighten-3" elevation="2">
          <v-icon color="yellow darken-2" size="40">mdi-star</v-icon>
          <h3>{{ stats.mostBookedDesk }}</h3>
          <p>Most Booked Desk</p>
        </v-card>
      </v-col>

      <!-- Least Booked Desk -->
      <v-col cols="12" sm="6" md="4">
        <v-card class="pa-6 d-flex flex-column align-center" color="grey lighten-3" elevation="2">
          <v-icon color="grey" size="40">mdi-emoticon-neutral-outline</v-icon>
          <h3>{{ stats.leastBookedDesk }}</h3>
          <p>Least Booked Desk</p>
        </v-card>
      </v-col>

      <!-- Weekly Users -->
      <v-col cols="12" sm="6" md="4">
        <v-card class="pa-6 d-flex flex-column align-center" color="grey lighten-3" elevation="2">
          <v-icon
              :color="stats.weeklyUsers >= 0 ? 'green' : 'red'"
              size="40"
          >
            {{ stats.weeklyUsers >= 0 ? 'mdi-arrow-up-bold' : 'mdi-arrow-down-bold' }}
          </v-icon>
          <h3>{{ formatPercent(stats.weeklyUsers) }} in</h3>
          <p>Weekly Users</p>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup>
import { ref, onMounted } from 'vue'

// Simulated backend stats (replace with API call)
const stats = ref({
  weeklyBookings: 20,
  lifetimeBookings: -20,
  mostBookedDesk: 'Ser-01',
  leastBookedDesk: 'Dev-02',
  weeklyUsers: 20
})

// Simulate backend updates every 5 seconds
onMounted(() => {
  setInterval(() => {
    stats.value.weeklyBookings = getRandomChange()
    stats.value.lifetimeBookings = getRandomChange()
    stats.value.weeklyUsers = getRandomChange()
  }, 5000)
})

// Helper functions
const getRandomChange = () => Math.floor(Math.random() * 41) - 20 // random -20 to +20
const formatPercent = (val) => `${val > 0 ? '+' : ''}${val} %`
</script>

<style scoped>
h3 {
  margin: 8px 0 0;
  font-weight: 600;
}
p {
  margin: 4px 0 0;
  color: #333;
}
</style>
