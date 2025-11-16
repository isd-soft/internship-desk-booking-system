<template>
  <div class="modern-search-toolbar">
    <div class="search-wrapper">
      <div class="search-icon">
        <v-icon size="20" color="#737373">mdi-magnify</v-icon>
      </div>
      <input
        :value="searchQuery"
        @input="handleSearchInput"
        type="text"
        placeholder="Search desks or zones..."
        class="modern-search-input"
      />
      <transition name="fade">
        <button v-if="searchQuery" @click="clearSearch" class="clear-btn">
          <v-icon size="18" color="#999">mdi-close-circle</v-icon>
        </button>
      </transition>
    </div>

    <div class="zone-wrapper">
      <div class="zone-icon">
        <v-icon size="18" color="#737373">mdi-map-marker-outline</v-icon>
      </div>
      <select
        :value="zoneFilter ?? ''"
        @change="handleZoneChange"
        class="modern-zone-select"
      >
        <option value="">All Zones</option>
        <option v-for="zone in zoneOptions" :key="zone" :value="zone">
          {{ zone }}
        </option>
      </select>
      <div class="select-chevron">
        <v-icon size="16" color="#999">mdi-chevron-down</v-icon>
      </div>
    </div>

    <div class="results-count">
      <v-icon size="18" color="#171717">mdi-desk</v-icon>
      <span class="count-number">{{ resultsCount }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  searchQuery: string;
  zoneFilter: string | null;
  zoneOptions: string[];
  resultsCount: number;
}>();

const emit = defineEmits<{
  (e: "update:searchQuery", value: string): void;
  (e: "update:zoneFilter", value: string | null): void;
}>();

function handleSearchInput(event: Event) {
  const target = event.target as HTMLInputElement;
  emit("update:searchQuery", target.value);
}

function clearSearch() {
  emit("update:searchQuery", "");
}

function handleZoneChange(event: Event) {
  const target = event.target as HTMLSelectElement;
  const value = target.value;
  emit("update:zoneFilter", value === "" ? null : value);
}
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap");

* {
  font-family: "Inter", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto,
    sans-serif;
}

.modern-search-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.search-wrapper {
  flex: 1;
  position: relative;
  display: flex;
  align-items: center;
  background: #ffffff;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.04);
  height: 52px;
}

.search-wrapper:hover {
  border-color: #d1d5db;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transform: translateY(-1px);
}

.search-wrapper:focus-within {
  border-color: #171717;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

.search-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 16px;
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.search-wrapper:focus-within .search-icon {
  transform: scale(1.1);
}

.modern-search-input {
  flex: 1;
  border: none;
  outline: none;
  padding: 0 12px;
  font-family: "Inter", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto,
    sans-serif;
  font-size: 15px;
  font-weight: 600;
  color: #171717;
  background: transparent;
  letter-spacing: 0.3px;
  transition: all 0.3s ease;
}

.modern-search-input::placeholder {
  color: #999;
  font-weight: 500;
  transition: color 0.3s ease;
}

.search-wrapper:focus-within .modern-search-input::placeholder {
  color: #bbb;
}

.clear-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 16px;
  background: transparent;
  border: none;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  opacity: 0.6;
  outline: none;
  -webkit-tap-highlight-color: transparent;
}

.clear-btn:hover {
  opacity: 1;
  transform: rotate(90deg) scale(1.1);
}

.clear-btn:active {
  transform: rotate(90deg) scale(0.95);
}

.fade-enter-active,
.fade-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.fade-enter-from {
  opacity: 0;
  transform: scale(0.8);
}

.fade-leave-to {
  opacity: 0;
  transform: scale(0.8) rotate(90deg);
}

.zone-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  background: #ffffff;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.04);
  height: 52px;
  min-width: 200px;
}

.zone-wrapper:hover {
  border-color: #d1d5db;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transform: translateY(-1px);
}

.zone-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 12px 0 16px;
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.zone-wrapper:hover .zone-icon {
  transform: scale(1.1);
}

.modern-zone-select {
  flex: 1;
  border: none;
  outline: none;
  padding: 0 8px;
  font-family: "Inter", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto,
    sans-serif;
  font-size: 15px;
  font-weight: 600;
  color: #171717;
  background: transparent;
  cursor: pointer;
  appearance: none;
  letter-spacing: 0.3px;
  transition: color 0.3s ease;
}

.modern-zone-select:hover {
  color: #000;
}

.modern-zone-select option {
  font-family: "Inter", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto,
    sans-serif;
  font-weight: 600;
  padding: 12px;
}

.select-chevron {
  display: flex;
  align-items: center;
  padding: 0 14px;
  pointer-events: none;
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.zone-wrapper:hover .select-chevron {
  transform: translateY(2px);
}

.results-count {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 20px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 12px;
  border: 1px solid #e0e0e0;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.04);
  height: 52px;
  min-width: 90px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.results-count:hover {
  border-color: #d1d5db;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

.count-number {
  font-size: 18px;
  font-weight: 800;
  color: #171717;
  letter-spacing: -0.5px;
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.results-count:hover .count-number {
  transform: scale(1.05);
}

@media (max-width: 768px) {
  .modern-search-toolbar {
    flex-direction: column;
    gap: 10px;
  }

  .search-wrapper,
  .zone-wrapper {
    width: 100%;
    min-width: unset;
    height: 48px;
  }

  .results-count {
    width: 100%;
    justify-content: center;
    height: 48px;
  }

  .modern-search-input,
  .modern-zone-select {
    font-size: 14px;
  }

  .count-number {
    font-size: 16px;
  }
}
</style>
