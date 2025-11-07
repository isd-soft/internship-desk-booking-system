<template>
  <div class="admin-bookings">
    <h2>Admin Bookings</h2>

    <div v-if="loading">Loading...</div>

    <div v-else-if="error">Error: {{ error }}</div>

    <table v-else>
      <thead>
      <tr>
        <th>ID</th>
        <th>Desk Name</th>
        <th>Zone</th>
        <th>Type</th>
        <th>Start Time</th>
        <th>End Time</th>
        <th>Status</th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="booking in bookings" :key="booking.id">
        <td>{{ booking.id }}</td>
        <td>{{ booking.desk?.deskName || 'N/A' }}</td>
        <td>{{ booking.desk?.zone || 'N/A' }}</td>
        <td>{{ booking.desk?.deskType || 'N/A' }}</td>
        <td>{{ formatDateTime(booking.startTime) }}</td>
        <td>{{ formatDateTime(booking.endTime) }}</td>
        <td>{{ booking.status }}</td>
      </tr>
      </tbody>
    </table>

    <div v-if="!loading && bookings.length === 0">No bookings found</div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import api from '../plugins/axios';

const bookings = ref([]);
const loading = ref(false);
const error = ref(null);

const fetchBookings = async () => {
  try {
    loading.value = true;
    error.value = null;
    const response = await api.get('/booking/all');
    bookings.value = response.data;
  } catch (err) {
    console.error('Error fetching bookings:', err);
    error.value = err.message || 'Failed to fetch bookings';
  } finally {
    loading.value = false;
  }
};

const formatDateTime = (dateStr) => {
  if (!dateStr) return 'N/A';
  const date = new Date(dateStr);
  return date.toLocaleString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
};

onMounted(() => {
  fetchBookings();
});
</script>

<style scoped>
.admin-bookings {
  padding: 20px;
}

h2 {
  margin-bottom: 20px;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th, td {
  border: 1px solid #ddd;
  padding: 8px;
  text-align: left;
}

th {
  background-color: #f2f2f2;
  font-weight: bold;
}

tr:nth-child(even) {
  background-color: #f9f9f9;
}
</style>