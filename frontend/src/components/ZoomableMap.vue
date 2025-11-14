<template>
  <div class="zoomable-map-wrapper" ref="wrapperRef">
    <div
      class="map-zoom-container"
      @touchstart="handleTouchStart"
      @touchmove="handleTouchMove"
      @touchend="handleTouchEnd"
      @touchcancel="handleTouchCancel"
      :style="{
        transform: `translate(${mapTranslate.x}px, ${mapTranslate.y}px) scale(${mapScale})`,
        transformOrigin: '0 0',
        transition:
          isPanning || lastTouchDistance > 0 ? 'none' : 'transform 0.3s ease',
      }"
    >
      <slot></slot>
    </div>

    <!-- Zoom Controls -->
    <div class="zoom-controls">
      <v-btn
        class="zoom-btn"
        icon
        size="small"
        elevation="0"
        :disabled="mapScale >= maxZoom"
        @click="zoomIn"
      >
        <v-icon size="20">mdi-plus</v-icon>
      </v-btn>
      <v-btn
        class="zoom-btn"
        icon
        size="small"
        elevation="0"
        :disabled="mapScale <= minZoom"
        @click="zoomOut"
      >
        <v-icon size="20">mdi-minus</v-icon>
      </v-btn>
      <v-btn
        v-if="mapScale > minZoom"
        class="zoom-btn reset-btn"
        icon
        size="small"
        elevation="0"
        @click="resetZoom"
      >
        <v-icon size="20">mdi-restore</v-icon>
      </v-btn>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, onUnmounted } from "vue";

interface Props {
  minZoom?: number;
  maxZoom?: number;
  zoomStep?: number;
  resetOnUnmount?: boolean;
  initialZoom?: number;
}

const props = withDefaults(defineProps<Props>(), {
  minZoom: 1,
  maxZoom: 4,
  zoomStep: 0.5,
  resetOnUnmount: true,
  initialZoom: 1,
});

const emit = defineEmits<{
  (e: "zoom-change", scale: number): void;
  (e: "reset"): void;
}>();

// ✅ стартуем с initialZoom, зажатым в [minZoom, maxZoom]
const startZoom = Math.min(
  Math.max(props.initialZoom, props.minZoom),
  props.maxZoom
);

const mapScale = ref(startZoom);
const mapTranslate = ref({ x: 0, y: 0 });
const wrapperRef = ref<HTMLElement | null>(null);
const lastTouchDistance = ref(0);
const lastTouchCenter = ref({ x: 0, y: 0 });
const isPanning = ref(false);
const startPanPosition = ref({ x: 0, y: 0 });
const prefersTouchMode = ref(false);
const lastTapTime = ref(0);
const lastTapPosition = ref({ x: 0, y: 0 });

const DOUBLE_TAP_DELAY_MS = 320;
const DOUBLE_TAP_RADIUS = 30;

function updateInteractionMode() {
  if (typeof window === "undefined") return;
  prefersTouchMode.value =
    window.matchMedia?.("(pointer: coarse)").matches ||
    window.innerWidth <= 900;
}

function clampTranslate(
  next: { x: number; y: number },
  scale = mapScale.value
) {
  if (!prefersTouchMode.value) {
    return next;
  }

  const wrapper = wrapperRef.value;
  if (!wrapper) {
    return next;
  }

  if (scale <= props.minZoom + 0.01) {
    return { x: 0, y: 0 };
  }

  const container = wrapper.querySelector(".map-zoom-container");
  if (!container) {
    return next;
  }

  // Получаем реальные размеры контейнера и контента
  const wrapperWidth = wrapper.clientWidth;
  const wrapperHeight = wrapper.clientHeight;
  const contentWidth = (container as HTMLElement).scrollWidth || wrapperWidth;
  const contentHeight =
    (container as HTMLElement).scrollHeight || wrapperHeight;

  // Вычисляем масштабированные размеры контента
  const scaledContentWidth = contentWidth * scale;
  const scaledContentHeight = contentHeight * scale;

  // Вычисляем максимальные смещения на основе реальных размеров
  const maxOffsetX = Math.max(0, scaledContentWidth - wrapperWidth);
  const maxOffsetY = Math.max(0, scaledContentHeight - wrapperHeight);

  const minX = -maxOffsetX;
  const maxX = 0;
  const minY = -maxOffsetY;
  const maxY = 0;

  return {
    x: Math.min(Math.max(next.x, minX), maxX),
    y: Math.min(Math.max(next.y, minY), maxY),
  };
}

function getWrapperCenter() {
  const bounds = wrapperRef.value?.getBoundingClientRect();
  if (!bounds) {
    return { x: 0, y: 0 };
  }
  return {
    x: bounds.left + bounds.width / 2,
    y: bounds.top + bounds.height / 2,
  };
}

function applyScale(
  targetScale: number,
  focalPoint?: { x: number; y: number }
) {
  const nextScale = Math.min(
    Math.max(targetScale, props.minZoom),
    props.maxZoom
  );
  if (nextScale === mapScale.value) {
    return;
  }

  if (!focalPoint || prefersTouchMode.value === false) {
    mapScale.value = nextScale;
    mapTranslate.value =
      nextScale <= props.minZoom + 0.01
        ? { x: 0, y: 0 }
        : clampTranslate(mapTranslate.value, nextScale);
    return;
  }

  const scaleChange = nextScale / mapScale.value;
  const proposedTranslate = {
    x: focalPoint.x - (focalPoint.x - mapTranslate.value.x) * scaleChange,
    y: focalPoint.y - (focalPoint.y - mapTranslate.value.y) * scaleChange,
  };

  mapTranslate.value = clampTranslate(proposedTranslate, nextScale);
  mapScale.value = nextScale;
}

function handleDoubleTap(point: { x: number; y: number }) {
  applyScale(mapScale.value + props.zoomStep, point);
}

function getTouchDistance(touch1: Touch, touch2: Touch) {
  const dx = touch1.clientX - touch2.clientX;
  const dy = touch1.clientY - touch2.clientY;
  return Math.sqrt(dx * dx + dy * dy);
}

function getTouchCenter(touch1: Touch, touch2: Touch) {
  return {
    x: (touch1.clientX + touch2.clientX) / 2,
    y: (touch1.clientY + touch2.clientY) / 2,
  };
}

function handleTouchStart(e: TouchEvent) {
  if (e.touches.length === 2) {
    isPanning.value = false;
    lastTouchDistance.value = getTouchDistance(e.touches[0], e.touches[1]);
    lastTouchCenter.value = getTouchCenter(e.touches[0], e.touches[1]);
  } else if (e.touches.length === 1) {
    isPanning.value = true;
    startPanPosition.value = {
      x: e.touches[0].clientX - mapTranslate.value.x,
      y: e.touches[0].clientY - mapTranslate.value.y,
    };
  }
}

function handleTouchMove(e: TouchEvent) {
  if (e.cancelable) {
    e.preventDefault();
  }

  if (e.touches.length === 2) {
    const distance = getTouchDistance(e.touches[0], e.touches[1]);
    const center = getTouchCenter(e.touches[0], e.touches[1]);

    if (lastTouchDistance.value > 0) {
      const scale = distance / lastTouchDistance.value;
      const targetScale = mapScale.value * scale;
      applyScale(targetScale, center);
    }

    lastTouchDistance.value = distance;
    lastTouchCenter.value = center;
  } else if (e.touches.length === 1 && isPanning.value) {
    const nextPosition = {
      x: e.touches[0].clientX - startPanPosition.value.x,
      y: e.touches[0].clientY - startPanPosition.value.y,
    };
    mapTranslate.value = clampTranslate(nextPosition);
  }
}

function handleTouchEnd(e: TouchEvent) {
  if (e.touches.length < 2) {
    lastTouchDistance.value = 0;
  }
  if (e.touches.length === 0) {
    isPanning.value = false;
  }

  if (e.touches.length === 0 && e.changedTouches.length === 1) {
    const touch = e.changedTouches[0];
    const tapPoint = { x: touch.clientX, y: touch.clientY };
    const now = Date.now();
    const elapsed = now - lastTapTime.value;
    const distance = Math.hypot(
      tapPoint.x - lastTapPosition.value.x,
      tapPoint.y - lastTapPosition.value.y
    );

    if (elapsed < DOUBLE_TAP_DELAY_MS && distance < DOUBLE_TAP_RADIUS) {
      handleDoubleTap(tapPoint);
      lastTapTime.value = 0;
      return;
    }

    lastTapTime.value = now;
    lastTapPosition.value = tapPoint;
  }
}

function handleTouchCancel() {
  lastTouchDistance.value = 0;
  isPanning.value = false;
}

function zoomIn() {
  applyScale(mapScale.value + props.zoomStep, getWrapperCenter());
}

function zoomOut() {
  applyScale(mapScale.value - props.zoomStep, getWrapperCenter());
}

function resetZoom() {
  mapScale.value = props.minZoom;
  mapTranslate.value = { x: 0, y: 0 };
  emit("reset");
}

watch(mapScale, (newScale) => {
  emit("zoom-change", newScale);
});

watch(
  () => prefersTouchMode.value,
  () => {
    mapTranslate.value = clampTranslate(mapTranslate.value);
  }
);

onMounted(() => {
  updateInteractionMode();
  window.addEventListener("resize", updateInteractionMode, { passive: true });
});

// ✅ нормальный reset при размонтировании
onUnmounted(() => {
  window.removeEventListener("resize", updateInteractionMode);
  if (props.resetOnUnmount) {
    resetZoom();
  }
});

defineExpose({
  resetZoom,
  zoomIn,
  zoomOut,
  currentZoom: mapScale,
});
</script>

<style scoped>
.zoomable-map-wrapper {
  position: relative;
  background: #ffffff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.08);
  touch-action: none;
  user-select: none;

  /* 🔹 важно: растягиваемся на весь родительский контейнер */
  width: 100%;
  height: 100%;
}

.map-zoom-container {
  width: 100%;
  height: 100%;
  cursor: grab;
  will-change: transform;
  touch-action: none;
}

.map-zoom-container:active {
  cursor: grabbing;
}

.zoom-controls {
  position: absolute;
  top: 12px;
  right: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  z-index: 10;
}

.zoom-btn {
  width: 40px;
  height: 40px;
  min-width: 40px !important;
  background: rgba(255, 255, 255, 0.95) !important;
  backdrop-filter: blur(8px);
  border: 1px solid #e0e7ff !important;
  border-radius: 10px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
}

.zoom-btn:hover {
  background: rgba(255, 255, 255, 1) !important;
  transform: scale(1.05);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
}

.zoom-btn:active {
  transform: scale(0.95);
}

.zoom-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.zoom-btn.reset-btn {
  border-top: 1px solid #e0e7ff;
}
</style>
