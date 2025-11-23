<template>
  <v-app class="app-root">
    <router-view />
  </v-app>
</template>

<script setup lang="ts">
import { onMounted } from "vue";
import { useFavouritesStore } from "@/stores/favourites";

const favStore = useFavouritesStore();

onMounted(() => {
  favStore.ensureLoaded();
});
</script>

<style>
html,
body,
#app,
.app-root {
  height: 100%;
  margin: 0;
  padding: 0;
}

.app-root {
  display: flex;
}
</style>
