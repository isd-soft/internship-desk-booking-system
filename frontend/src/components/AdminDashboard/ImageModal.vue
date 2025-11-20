<template>
  <Teleport to="body">
    <div class="modal-overlay" @click="$emit('close')">
      <div class="modal-content" @click.stop>
        <v-btn
          icon
          variant="text"
          size="large"
          class="close-btn"
          @click="$emit('close')"
        >
          <v-icon>mdi-close</v-icon>
        </v-btn>

        <img :src="src" :alt="alt" class="modal-image" />

        <div class="modal-footer">
          <p class="modal-caption">{{ alt }}</p>

          <v-btn
            color="success"
            variant="flat"
            class="action-btn"
            @click="setAsBackground"
            :loading="isProcessing"
            :disabled="isProcessing"
            prepend-icon="mdi-check-circle-outline"
          >
            {{ isProcessing ? 'Saving...' : 'Set as Background' }}
          </v-btn>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, computed, ref } from 'vue';
import api from "../../plugins/axios.js";
import { useToast } from "vue-toastification";

const props = defineProps<{
  image: {
    id: number;
    content_type: string;
    image_data: string;
    file_name: string;
    isBackground: boolean;
  }
}>();

const toast = useToast();

const emit = defineEmits(['close', 'updated']);

const isProcessing = ref(false);

const src = computed(() => {
  if (!props.image.image_data) return '';
  return `data:${props.image.content_type};base64,${props.image.image_data}`;
});
const alt = computed(() => props.image.file_name);

const setAsBackground = async () => {
  if (isProcessing.value) return;
  isProcessing.value = true;

  try {
    await api.patch(`/admin/images/setBackground/${props.image.id}`);
    toast.success("Background image updated successfully!", {
      timeout: 2000
    });
    emit('updated', props.image.id);
    emit('close');
  } catch (error: any) {
    console.error("Error setting background:", error);
    const errorMessage = error.response?.data?.message || "Failed to set background.";
    toast.error(errorMessage, {
      timeout: 3000
    });
  } finally {
    isProcessing.value = false;
  }
};

const handleKeydown = (e: KeyboardEvent) => {
  if (e.key === 'Escape') emit('close');
};

onMounted(() => {
  document.body.style.overflow = 'hidden';
  window.addEventListener('keydown', handleKeydown);
});

onUnmounted(() => {
  document.body.style.overflow = '';
  window.removeEventListener('keydown', handleKeydown);
});
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap");

* {
  font-family: "Inter", -apple-system, BlinkMacSystemFont, "Segoe UI",
    sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.9);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
  padding: 20px;
  cursor: zoom-out;
  backdrop-filter: blur(4px);
}

.modal-content {
  position: relative;
  max-width: 90%;
  max-height: 90%;
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: default;
  background: #171717;
  border-radius: 16px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.4);
  padding: 20px;
}

.modal-image {
  max-width: 100%;
  max-height: 80vh;
  object-fit: contain;
  border-radius: 12px;
}

.modal-footer {
  margin-top: 20px;
  text-align: center;
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  max-width: 700px; 
  padding: 0 10px;
  gap: 20px;
}

.modal-caption {
  color: #f5f5f5;
  font-size: 16px;
  font-weight: 600;
  margin: 0;
  flex-grow: 1;
  text-align: left;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.action-btn {
  font-weight: 700 !important;
  text-transform: none !important;
  letter-spacing: 0.3px;
  border-radius: 12px !important;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08) !important;
  transition: all 0.3s ease;
  padding: 0 16px !important;
}

.action-btn:hover:not(:disabled) {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12) !important;
  transform: translateY(-1px);
}

.close-btn {
  position: absolute !important;
  top: 10px;
  right: 10px;
  background-color: rgba(255, 255, 255, 0.1) !important;
  color: #fff !important;
  z-index: 10;
  transition: background-color 0.2s, transform 0.2s;
}

.close-btn:hover {
  background-color: rgba(255, 255, 255, 0.2) !important;
  transform: rotate(90deg);
}
</style>