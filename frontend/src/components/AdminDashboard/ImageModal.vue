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

          <div class="action-buttons-group">
            <v-btn
              color="error"
              variant="tonal"
              class="action-btn delete-btn"
              prepend-icon="mdi-delete-outline"
              @click="showConfirmDialog = true"
            >
              Delete
            </v-btn>

            <v-btn
              color="success"
              variant="flat"
              class="action-btn set-background-btn"
              @click="setAsBackground"
              :loading="isProcessing"
              :disabled="isProcessing"
              prepend-icon="mdi-check-circle-outline"
            >
              {{ isProcessing ? 'Saving...' : 'Set as Background' }}
            </v-btn>
          </div>
        </div>

        <v-dialog
          v-model="showConfirmDialog"
          max-width="400"
          @click:outside="showConfirmDialog = false"
        >
          <v-card class="confirm-card">
            <v-card-title class="text-h5 error--text">
              <v-icon color="error" class="mr-2">mdi-alert-circle-outline</v-icon>
              Confirm Deletion
            </v-card-title>
            <v-card-text>
              Are you sure you want to permanently delete this image? This action cannot be undone.
            </v-card-text>
            <v-card-actions>
              <v-spacer></v-spacer>
              <v-btn
                variant="text"
                @click="showConfirmDialog = false"
                :disabled="isDeleting"
              >
                Cancel
              </v-btn>
              <v-btn
                color="error"
                variant="flat"
                @click="deleteImage"
                :loading="isDeleting"
                :disabled="isDeleting"
              >
                Delete
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-dialog>
        
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

const emit = defineEmits(['close', 'updated', 'deleted']);

const isProcessing = ref(false);
const showConfirmDialog = ref(false); 
const isDeleting = ref(false);

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

const deleteImage = async () => {
  if (isDeleting.value) return;
  isDeleting.value = true;

  try {
    await api.delete(`/admin/images/delete/${props.image.id}`);
    
    toast.success("Image successfully deleted!", {
      timeout: 2000
    });
    
    emit('deleted', props.image.id); 
    showConfirmDialog.value = false; 
    emit('close');
    
  } catch (error: any) {
    console.error("Error deleting image:", error);
    const errorMessage = error.response?.data?.message || "Failed to delete image.";
    toast.error(errorMessage, {
      timeout: 3000
    });
  } finally {
    isDeleting.value = false;
  }
};


const handleKeydown = (e: KeyboardEvent) => {
  if (e.key === 'Escape' && !showConfirmDialog.value) emit('close');
  if (e.key === 'Escape' && showConfirmDialog.value) showConfirmDialog.value = false;
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
  z-index: 2000;
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
  max-width: 800px; 
  padding: 0 10px;
  gap: 20px;
}

.action-buttons-group {
    display: flex;
    gap: 10px; 
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
  font-weight: 600 !important;
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

.delete-btn {
    border: 1px solid rgba(244, 67, 54, 0.5) !important;
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