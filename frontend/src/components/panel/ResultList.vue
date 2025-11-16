<template>
  <div class="results-section flex-grow-1 px-6 pb-6">
    <div class="results-container">
      <div class="results-header">
        <div class="results-title-wrap">
          <div class="results-title">{{ title }}</div>
          <div class="results-sub">{{ items.length }} itemа аs</div>
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
        <transition-group name="list-fade" tag="div" mode="out-in">
          <div
            v-for="(item, i) in paginated"
            :key="`${page}-${item.id ?? i}`"
            class="data-item"
          >
            <div class="index-badge">{{ startIndex + i }}</div>

            <div class="item-content">
              <div class="item-header">
                <div class="item-title">
                  {{ item.desk }}
                  <span v-if="item.zoneAbv" class="desk-zone-small">
                    ({{ item.zoneAbv }})
                  </span>
                </div>

                <div class="fav-zone" v-if="item.zone">
                  Zone: {{ item.zone }}
                  <span v-if="item.zoneAbv" class="small-zone">
                    ({{ item.zoneAbv }})
                  </span>
                </div>

                <div class="item-actions">
                  <v-btn
                    v-if="currentType === 'favourites'"
                    size="small"
                    color="red"
                    variant="text"
                    class="remove-fav-btn"
                    @click.stop="$emit('remove-favourite', item)"
                  >
                    Remove
                    <v-icon size="16" class="ml-1">mdi-heart-off</v-icon>
                  </v-btn>

                  <v-btn
                    v-if="
                      currentType === 'upcoming' &&
                      (item.status === 'ACTIVE' || item.status === 'SCHEDULED')
                    "
                    size="small"
                    color="red"
                    variant="text"
                    class="more-btn"
                    @click.stop="cancelItem(item)"
                  >
                    Cancel
                    <v-icon size="16" class="ml-1">mdi-close</v-icon>
                  </v-btn>

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

              <div class="item-meta" v-if="item.date">
                <div class="meta-row">
                  <v-icon size="16" class="meta-ic">mdi-calendar</v-icon>
                  <span class="meta">{{ item.date }}</span>
                </div>

                <span class="dot">•</span>

                <div class="meta-row">
                  <v-icon size="16" class="meta-ic">mdi-clock-outline</v-icon>
                  <span class="meta">{{ item.time }}</span>
                </div>

                <span class="dot">•</span>

                <div class="meta-row">
                  <v-icon size="16" class="meta-ic">mdi-timer-outline</v-icon>
                  <span class="meta">{{ item.duration }}</span>
                </div>
              </div>
            </div>
          </div>
        </transition-group>
      </div>

      <div v-if="totalPages > 1" class="pagination-section">
        <div class="pagination-wrapper">
          <v-btn
            icon
            variant="text"
            size="small"
            :disabled="page === 1"
            @click="$emit('page', page - 1)"
            class="pagination-btn"
          >
            <v-icon>mdi-chevron-left</v-icon>
          </v-btn>

          <div class="pagination-info">
            <span class="page-text">
              {{ startIndex }}-{{ endIndex }} of {{ items.length }}
            </span>
          </div>

          <v-btn
            icon
            variant="text"
            size="small"
            :disabled="page === totalPages"
            @click="$emit('page', page + 1)"
            class="pagination-btn"
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
import { layout } from "../VisualFloorMap/floorLayout";
import api from "@/plugins/axios";

const emit = defineEmits<{
  (e: "page", page: number): void;
  (e: "details", item: any): void;
  (e: "refresh", item: any): void;
  (e: "remove-favourite", item: any): void;
}>();

const props = defineProps<{
  title: string;
  items: any[];
  page: number;
  perPage: number;
  currentType: string;
}>();

async function cancelItem(item: any) {
  try {
    await api.post(`/booking/${item.id}/cancel`);

    const isoTime = item.raw.startTime.split("T")[0];
    emit("refresh", { deskId: item.raw.desk.id, date: isoTime });
  } catch (e: any) {
    console.error("Failed to cancel booking", e);
  }
}

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
  padding-left: clamp(16px, 3vw, 24px);
  padding-right: clamp(16px, 3vw, 24px);
  padding-bottom: clamp(16px, 2vw, 24px);
}

.results-container {
  background: var(--card-bg);
  border-radius: clamp(12px, 1.5vw, 20px);
  padding: clamp(16px, 1.8vw, 24px);
  border: 1px solid var(--card-border);
  height: 100%;
  display: flex;
  flex-direction: column;
  max-width: 1600px;
  margin: 0 auto;
}

.results-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: clamp(12px, 1.2vw, 16px);
  padding-bottom: clamp(10px, 1vw, 14px);
  border-bottom: 1px solid var(--panel-sep);
  flex-wrap: wrap;
  gap: clamp(8px, 1vw, 12px);
}

.results-title-wrap {
  display: flex;
  align-items: baseline;
  gap: clamp(8px, 1vw, 12px);
  flex-wrap: wrap;
}

.results-title {
  font-size: clamp(1rem, 0.9rem + 0.5vw, 1.3rem);
  font-weight: 900;
  color: #111827;
  letter-spacing: 0.2px;
  line-height: 1.2;
}

.results-sub {
  font-size: clamp(0.85rem, 0.8rem + 0.25vw, 1.05rem);
  font-weight: 750;
  color: #6b7280;
}

.results-badge {
  font-weight: 800;
  font-size: clamp(0.7rem, 0.68rem + 0.15vw, 0.82rem);
  color: #fff !important;
  padding: 0 clamp(8px, 0.8vw, 12px);
}

.items-list {
  flex: 1;
  overflow-y: auto;
  margin-bottom: clamp(10px, 1vw, 14px);
  position: relative;
  padding: 2px;
}

.items-list::-webkit-scrollbar {
  width: clamp(4px, 0.5vw, 8px);
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
  padding: clamp(14px, 1.5vw, 20px) clamp(14px, 1.5vw, 20px)
    clamp(14px, 1.5vw, 20px) clamp(56px, 6vw, 72px);
  margin-bottom: clamp(12px, 1.2vw, 16px);
  background: #fff;
  border-radius: clamp(10px, 1.2vw, 14px);
  border: 1px solid #e5e7eb;
  transition: transform 0.18s ease, box-shadow 0.18s ease,
    border-color 0.18s ease, background 0.18s ease;
  cursor: pointer;
}

.data-item:hover {
  transform: translateY(-2px);
  background: #fffdfa;
  border-color: #fbbf24;
  box-shadow: 0 8px 20px rgba(255, 138, 0, 0.12);
}

.index-badge {
  position: absolute;
  left: clamp(10px, 1.2vw, 16px);
  top: clamp(10px, 1.2vw, 16px);
  min-width: clamp(32px, 3vw, 40px);
  height: clamp(32px, 3vw, 40px);
  padding: 0 clamp(4px, 0.6vw, 8px);
  border-radius: 999px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 900;
  font-size: clamp(0.8rem, 0.75rem + 0.25vw, 1rem);
  color: #000;
  background: var(--accent);
  box-shadow: 0 4px 12px rgba(255, 138, 0, 0.22);
  border: 2px solid #fff;
}

.item-content {
  width: 100%;
}

.item-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: clamp(8px, 1vw, 12px);
  margin-bottom: clamp(8px, 0.8vw, 12px);
  flex-wrap: wrap;
}

.item-title {
  font-weight: 900;
  font-size: clamp(0.95rem, 0.9rem + 0.3vw, 1.15rem);
  color: #0f172a;
  word-break: break-word;
  flex: 1;
  min-width: min(180px, 100%);
  line-height: 1.3;
}

.item-actions {
  display: flex;
  align-items: center;
  gap: clamp(4px, 0.5vw, 8px);
  flex-shrink: 0;
  flex-wrap: wrap;
}

.remove-fav-btn {
  font-weight: 780;
}

.status-chip {
  font-weight: 820;
  text-transform: uppercase;
  letter-spacing: 0.35px;
  font-size: clamp(0.65rem, 0.62rem + 0.15vw, 0.75rem) !important;
  padding: 0 clamp(6px, 0.6vw, 10px);
}

.more-btn {
  font-weight: 820;
  font-size: clamp(0.8rem, 0.78rem + 0.15vw, 0.9rem) !important;
  padding: 0 clamp(6px, 0.8vw, 10px) !important;
  min-height: 32px;
}

.item-meta {
  display: flex;
  align-items: center;
  gap: clamp(5px, 0.6vw, 8px);
  font-size: clamp(0.85rem, 0.82rem + 0.18vw, 1rem);
  font-weight: 850;
  color: #374151;
  flex-wrap: wrap;
  line-height: 1.5;
}

.meta-row {
  display: flex;
  align-items: center;
  gap: clamp(3px, 0.4vw, 5px);
  white-space: nowrap;
}

.item-meta .meta {
  word-break: break-word;
}

.item-meta .dot {
  opacity: 0.45;
  flex-shrink: 0;
  font-size: clamp(0.8rem, 0.75rem + 0.2vw, 1rem);
}

.meta-ic {
  opacity: 0.7;
  flex-shrink: 0;
}

.pagination-section {
  padding-top: clamp(10px, 1vw, 14px);
  border-top: 1px solid var(--panel-sep);
}

.pagination-wrapper {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: clamp(8px, 1vw, 12px);
}

.pagination-btn {
  flex-shrink: 0;
  min-width: clamp(36px, 4vw, 44px);
  min-height: clamp(36px, 4vw, 44px);
}

.pagination-info {
  text-align: center;
  flex: 1;
}

.page-text {
  font-size: clamp(0.82rem, 0.8rem + 0.15vw, 0.95rem);
  font-weight: 800;
  color: #4b5563;
  white-space: nowrap;
}

.list-fade-enter-active,
.list-fade-leave-active {
  transition: all 0.4s cubic-bezier(0.22, 1, 0.36, 1);
}

.list-fade-enter-from {
  opacity: 0;
  transform: translateX(20px) scale(0.97);
}

.list-fade-leave-to {
  opacity: 0;
  transform: translateX(-20px) scale(0.97);
}

.list-fade-leave-active {
  position: absolute;
  width: 100%;
}

@media (min-width: 1920px) {
  .results-container {
    padding: 28px;
  }

  .data-item {
    padding: 22px 22px 22px 76px;
  }

  .index-badge {
    min-width: 44px;
    height: 44px;
    font-size: 1.05rem;
  }

  .item-title {
    font-size: 1.2rem;
  }

  .item-meta {
    font-size: 1.05rem;
  }
}

@media (min-width: 1441px) and (max-width: 1919px) {
  .results-container {
    padding: 22px;
  }
}

@media (min-width: 1025px) and (max-width: 1440px) {
  .results-section {
    padding-left: 20px;
    padding-right: 20px;
  }

  .results-container {
    padding: 18px;
  }

  .data-item {
    padding: 16px 16px 16px 60px;
  }
}

@media (min-width: 768px) and (max-width: 1024px) {
  .results-section {
    padding-left: 16px;
    padding-right: 16px;
    padding-bottom: 16px;
  }

  .results-container {
    padding: 16px;
    border-radius: 14px;
  }

  .results-header {
    margin-bottom: 12px;
    padding-bottom: 10px;
  }

  .data-item {
    padding: 14px 14px 14px 56px;
    margin-bottom: 12px;
  }

  .index-badge {
    min-width: 36px;
    height: 36px;
    font-size: 0.85rem;
  }

  .item-header {
    gap: 10px;
  }

  .item-title {
    font-size: 1rem;
  }

  .item-meta {
    font-size: 0.9rem;
  }

  .pagination-btn {
    min-width: 40px;
    min-height: 40px;
  }
}

@media (min-width: 481px) and (max-width: 767px) {
  .results-section {
    padding-left: 14px;
    padding-right: 14px;
    padding-bottom: 14px;
  }

  .results-container {
    padding: 14px;
    border-radius: 12px;
  }

  .results-header {
    margin-bottom: 10px;
    padding-bottom: 8px;
    gap: 8px;
  }

  .data-item {
    padding: 13px 13px 13px 52px;
    margin-bottom: 11px;
    border-radius: 11px;
  }

  .data-item:active {
    transform: scale(0.98);
  }

  .index-badge {
    left: 10px;
    top: 10px;
    min-width: 34px;
    height: 34px;
    font-size: 0.82rem;
  }

  .item-header {
    gap: 8px;
  }

  .item-title {
    font-size: 0.97rem;
    min-width: 140px;
  }

  .item-actions {
    gap: 6px;
  }

  .status-chip {
    font-size: 0.68rem !important;
  }

  .more-btn {
    font-size: 0.82rem !important;
    min-height: 34px;
  }

  .item-meta {
    font-size: 0.87rem;
    gap: 5px;
  }

  .meta-row {
    gap: 3px;
  }

  .pagination-btn {
    min-width: 38px;
    min-height: 38px;
  }
}

@media (min-width: 361px) and (max-width: 480px) {
  .results-section {
    padding-left: 12px !important;
    padding-right: 12px !important;
    padding-bottom: 16px !important;
  }

  .results-container {
    padding: 12px;
    border-radius: 12px;
  }

  .results-header {
    margin-bottom: 10px;
    padding-bottom: 8px;
    gap: 6px;
  }

  .results-title {
    font-size: 1.05rem;
  }

  .results-sub {
    font-size: 0.88rem;
  }

  .results-badge {
    font-size: 0.72rem;
  }

  .data-item {
    padding: 12px 12px 12px 50px;
    margin-bottom: 10px;
    border-radius: 10px;
  }

  .data-item:active {
    transform: scale(0.97);
    transition: transform 0.1s ease;
  }

  .index-badge {
    left: 9px;
    top: 9px;
    min-width: 32px;
    height: 32px;
    font-size: 0.78rem;
  }

  .item-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .item-title {
    font-size: 0.95rem;
    width: 100%;
    line-height: 1.35;
  }

  .item-actions {
    width: 100%;
    justify-content: flex-start;
  }

  .status-chip {
    font-size: 0.65rem !important;
  }

  .more-btn {
    font-size: 0.8rem !important;
    min-height: 32px;
  }

  .item-meta {
    font-size: 0.85rem;
    gap: 4px;
  }

  .meta-row {
    gap: 3px;
  }

  .pagination-section {
    padding-top: 8px;
  }

  .pagination-btn {
    min-width: 36px;
    min-height: 36px;
  }

  .page-text {
    font-size: 0.82rem;
  }
}

@media (max-width: 360px) {
  .results-section {
    padding-left: 10px !important;
    padding-right: 10px !important;
    padding-bottom: 14px !important;
  }

  .results-container {
    padding: 10px;
    border-radius: 10px;
  }

  .results-header {
    margin-bottom: 8px;
    padding-bottom: 6px;
    gap: 6px;
  }

  .results-title {
    font-size: 1rem;
  }

  .results-sub {
    font-size: 0.85rem;
  }

  .results-badge {
    font-size: 0.7rem;
    padding: 0 6px;
  }

  .data-item {
    padding: 10px 10px 10px 46px;
    margin-bottom: 9px;
    border-radius: 9px;
  }

  .index-badge {
    left: 8px;
    top: 8px;
    min-width: 28px;
    height: 28px;
    font-size: 0.75rem;
    border: 1.5px solid #fff;
  }

  .item-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 6px;
    margin-bottom: 6px;
  }

  .item-title {
    font-size: 0.9rem;
    width: 100%;
  }

  .item-actions {
    width: 100%;
    gap: 4px;
  }

  .status-chip {
    font-size: 0.6rem !important;
    padding: 0 5px;
  }

  .more-btn {
    font-size: 0.75rem !important;
    padding: 0 6px !important;
    min-height: 30px;
  }

  .item-meta {
    font-size: 0.8rem;
    gap: 3px;
  }

  .meta-row {
    gap: 2px;
  }

  .meta-ic {
    width: 14px;
    height: 14px;
  }

  .pagination-section {
    padding-top: 6px;
  }

  .pagination-btn {
    min-width: 34px;
    min-height: 34px;
  }

  .page-text {
    font-size: 0.78rem;
  }
}

@media (max-width: 767px) and (orientation: landscape) {
  .results-section {
    padding-top: 8px;
    padding-bottom: 8px;
  }

  .results-container {
    padding: 10px 14px;
  }

  .results-header {
    margin-bottom: 8px;
    padding-bottom: 6px;
  }

  .data-item {
    padding: 10px 12px 10px 48px;
    margin-bottom: 8px;
  }

  .item-meta {
    gap: 4px;
  }
}

@media (hover: none) and (pointer: coarse) {
  .data-item {
    padding: 14px 14px 14px 54px;
  }

  .pagination-btn,
  .more-btn {
    min-height: 40px;
    min-width: 40px;
  }

  .data-item:active {
    transform: scale(0.98);
    transition: transform 0.12s ease;
  }

  .more-btn:active,
  .pagination-btn:active {
    opacity: 0.7;
  }
}

@media (prefers-reduced-motion: reduce) {
  .data-item,
  .list-fade-enter-active,
  .list-fade-leave-active {
    transition: none;
  }

  .data-item:hover {
    transform: none;
  }
}
.fav-zone {
  margin-top: 4px;
  font-size: 0.82rem;
  font-weight: 750;
  color: #6b7280;
}

.desk-zone-small {
  opacity: 0.6;
  font-size: 0.88em;
}
</style>
