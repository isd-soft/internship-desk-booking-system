<template>
  <div class="results-section flex-grow-1 px-6 pb-6">
    <div class="results-container">
      <div class="results-header">
        <div class="results-title-wrap">
          <div class="results-title">{{ title }}</div>
          <div class="results-sub">{{ items.length }} items</div>
        </div>
        <v-chip
          size="small"
          :color="typeChipColor"
          variant="flat"
          class="results-badge"
        >
          {{ items.length }}
        </v-chip>
      </div>

      <div class="items-list">
        <transition-group name="list-fade" tag="div">
          <div
            v-for="(item, i) in paginated"
            :key="item.id ?? i"
            class="data-item"
          >
            <div class="index-badge">{{ startIndex + i }}</div>

            <div class="item-content">
              <div class="item-header">
                <div class="item-title">{{ item.desk }}</div>
                <div class="item-actions">
                  <v-chip
                    class="status-chip mr-1"
                    size="x-small"
                    :color="item.statusColor"
                    variant="flat"
                  >
                    {{ item.status }}
                  </v-chip>
                  <v-btn
                    size="small"
                    variant="text"
                    class="more-btn"
                    @click="$emit('details', item)"
                  >
                    Details
                    <v-icon size="16" class="ml-1">mdi-chevron-right</v-icon>
                  </v-btn>
                </div>
              </div>

              <div class="item-meta nowrap">
                <v-icon size="16" class="meta-ic">mdi-calendar</v-icon>
                <span class="meta">{{ item.date }}</span>
                <span class="dot">•</span>
                <v-icon size="16" class="meta-ic">mdi-clock-outline</v-icon>
                <span class="meta">{{ item.time }}</span>
                <span class="dot">•</span>
                <v-icon size="16" class="meta-ic">mdi-timer-outline</v-icon>
                <span class="meta">{{ item.duration }}</span>
              </div>
            </div>
          </div>
        </transition-group>
      </div>

      <div v-if="totalPages > 1" class="pagination-section">
        <div class="d-flex align-items-center justify-space-between">
          <v-btn
            icon
            variant="text"
            size="small"
            :disabled="page === 1"
            @click="$emit('page', page - 1)"
          >
            <v-icon>mdi-chevron-left</v-icon>
          </v-btn>

          <div class="pagination-info">
            <span class="page-text"
              >{{ startIndex }}-{{ endIndex }} of {{ items.length }}</span
            >
          </div>

          <v-btn
            icon
            variant="text"
            size="small"
            :disabled="page === totalPages"
            @click="$emit('page', page + 1)"
          >
            <v-icon>mdi-chevron-right</v-icon>
          </v-btn>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";

const props = defineProps<{
  title: string;
  items: any[];
  page: number;
  perPage: number;
  currentType: string;
}>();

defineEmits<{
  (e: "page", page: number): void;
  (e: "details", item: any): void;
}>();

const totalPages = computed(() =>
  Math.ceil((props.items?.length ?? 0) / props.perPage)
);
const startIndex = computed(() => (props.page - 1) * props.perPage + 1);
const endIndex = computed(() =>
  Math.min(props.page * props.perPage, props.items.length)
);
const paginated = computed(() => {
  const start = (props.page - 1) * props.perPage;
  return props.items.slice(start, start + props.perPage);
});
const typeChipColor = computed(() => "grey-darken-1");
</script>

<style scoped>
.results-section {
  overflow-y: auto;
  background: var(--soft);
}
.results-container {
  background: var(--card-bg);
  border-radius: 16px;
  padding: clamp(14px, 1.2vw, 20px);
  border: 1px solid var(--card-border);
  height: 100%;
  display: flex;
  flex-direction: column;
}
.results-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--panel-sep);
}
.results-title-wrap {
  display: flex;
  align-items: baseline;
  gap: 10px;
}
.results-title {
  font-size: clamp(1.08rem, 0.98rem + 0.3vw, 1.2rem);
  font-weight: 900;
  color: #111827;
  letter-spacing: 0.2px;
}
.results-sub {
  font-size: clamp(0.9rem, 0.86rem + 0.15vw, 1rem);
  font-weight: 750;
  color: #6b7280;
}
.results-badge {
  font-weight: 800;
  font-size: 0.78rem;
  color: #fff !important;
}
.items-list {
  flex: 1;
  overflow-y: auto;
  margin-bottom: 10px;
}
.items-list::-webkit-scrollbar {
  width: 6px;
}
.items-list::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.18);
  border-radius: 10px;
}
.items-list::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.28);
}
.data-item {
  position: relative;
  padding: 16px 16px 16px 68px;
  margin-bottom: 14px;
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  transition: transform 0.16s ease, box-shadow 0.16s ease,
    border-color 0.16s ease, background 0.16s ease;
}
.data-item:hover {
  transform: translateY(-1px);
  background: #fffdfa;
  border-color: #fbbf24;
  box-shadow: 0 8px 18px rgba(255, 138, 0, 0.1);
}
.index-badge {
  position: absolute;
  left: 14px;
  top: 14px;
  min-width: clamp(30px, 2.6vw, 36px);
  height: clamp(30px, 2.6vw, 36px);
  padding: 0 8px;
  border-radius: 999px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 900;
  font-size: clamp(0.82rem, 0.76rem + 0.2vw, 0.95rem);
  color: #000;
  background: var(--accent);
  box-shadow: 0 4px 10px rgba(255, 138, 0, 0.2);
  border: 2px solid #fff;
}
.item-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}
.item-title {
  font-weight: 900;
  font-size: clamp(1.02rem, 0.98rem + 0.25vw, 1.12rem);
  color: #0f172a;
}
.item-actions {
  display: flex;
  align-items: center;
}
.status-chip {
  font-weight: 820;
  text-transform: uppercase;
  letter-spacing: 0.35px;
}
.more-btn {
  font-weight: 820;
}
.item-meta {
  margin-top: 6px;
  font-size: clamp(0.95rem, 0.9rem + 0.2vw, 1.02rem);
  font-weight: 850;
  color: #374151;
  display: flex;
  align-items: center;
  gap: 8px;
}
.item-meta .dot {
  opacity: 0.45;
}
.meta-ic {
  opacity: 0.7;
  margin-right: 2px;
}
.nowrap {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.pagination-section {
  padding-top: 10px;
  border-top: 1px solid var(--panel-sep);
}
.pagination-info {
  text-align: center;
}
.page-text {
  font-size: 0.9rem;
  font-weight: 800;
  color: #4b5563;
}
.list-fade-enter-active,
.list-fade-leave-active {
  transition: all 0.28s cubic-bezier(0.22, 1, 0.36, 1);
}
.list-fade-enter-from {
  opacity: 0;
  transform: translateY(10px) scale(0.985);
}
.list-fade-leave-to {
  opacity: 0;
  transform: translateY(-8px) scale(0.985);
}
</style>
