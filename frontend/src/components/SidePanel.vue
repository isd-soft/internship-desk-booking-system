<template>
  <v-sheet class="user-panel pa-4 d-flex flex-column justify-space-between" width="340">
    <div>
      <v-divider class="mb-4"></v-divider>

      <v-btn
        block
        color="primary"
        variant="flat"
        prepend-icon="mdi-star"
        class="mb-3 rounded-lg text-none"
        @click="loadData('favourites')"
      >
        Favorites
      </v-btn>

      <v-btn
        block
        color="teal"
        variant="flat"
        prepend-icon="mdi-seat"
        class="mb-3 rounded-lg text-none"
        @click="loadData('bookings')"
      >
        My Bookings
      </v-btn>

      <v-btn
        block
        color="success"
        variant="flat"
        prepend-icon="mdi-calendar-clock"
        class="mb-3 rounded-lg text-none"
        @click="loadData('upcoming')"
      >
        Upcoming
      </v-btn>

      <v-btn
        block
        variant="outlined"
        prepend-icon="mdi-refresh"
        class="rounded-lg text-none"
        @click="refreshData"
      >
        Refresh
      </v-btn>
    </div>

    <v-slide-y-transition>
      <v-card
        v-if="items.length > 0"
        elevation="2"
        rounded="xl"
        class="api-display pa-4 mt-6"
      >
        <div class="text-subtitle-2 font-weight-bold mb-2">Data:</div>

        <v-slide-y-transition group>
          <v-card
            v-for="(item, i) in items"
            :key="i"
            elevation="1"
            rounded="lg"
            class="pa-3 mb-3 data-card"
          >
            <div class="font-weight-medium text-body-1 mb-1">{{ item.title }}</div>
            <div class="text-body-2 text-grey-darken-1">{{ item.desc }}</div>
          </v-card>
        </v-slide-y-transition>
      </v-card>
    </v-slide-y-transition>
  </v-sheet>
</template>

<script setup>
import { ref } from 'vue'

const items = ref([])

function loadData(type) {
  if (type === 'favourites') {
    items.value = [
      { title: 'Desk A12', desc: 'Near window, IT Dept' },
      { title: 'Desk B05', desc: 'By the wall, Design Dept' },
    ]
  } else if (type === 'bookings') {
    items.value = [
      { title: 'Meeting Room 2', desc: 'Today 14:00 - 15:00' },
      { title: 'Desk C03', desc: 'Tomorrow 09:00 - 17:00' },
    ]
  } else if (type === 'upcoming') {
    items.value = [
      { title: 'Desk D21', desc: 'Monday 08:00 - 17:00' },
      { title: 'Team Room', desc: 'Tuesday 10:00 - 12:00' },
    ]
  }
}

function refreshData() {
  items.value = []
}
</script>

<style scoped>
.user-panel {
  background: #ffffff;
  border-left: 1px solid #e0e0e0;
  height: 100vh;
  overflow-y: auto;
}

.v-btn {
  font-weight: 600;
  text-transform: none;
}

.api-display {
  background: #fafafa;
  max-height: 45%;
  overflow-y: auto;
}

.data-card {
  background-color: #ffffff;
  transition: all 0.25s ease;
}
.data-card:hover {
  transform: translateY(-3px);
  box-shadow: 0px 6px 16px rgba(0, 0, 0, 0.06);
}
</style>
