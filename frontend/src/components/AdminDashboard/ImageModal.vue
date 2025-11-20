<template>
  <Teleport to="body">
    <div class="modal-overlay" @click="$emit('close')">
      
      <div class="modal-content" @click.stop>
        <button class="close-btn" @click="$emit('close')">×</button>
        
        <img 
          :src="src" 
          :alt="alt" 
          class="modal-image"
        />
        
        <div class="modal-footer">
          <p class="modal-caption">{{ alt }}</p>
          
          <button 
            class="action-btn" 
            @click="setAsBackground" 
            :disabled="isProcessing"
          >
            {{ isProcessing ? 'Saving...' : 'Make Background' }}
          </button>
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
    toast.success("Success", {
      timeout: 2000
    });
    emit('updated', props.image.id);
    emit('close');
  } catch (error) {
    console.error(error);
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
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.85);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
  padding: 20px;
  cursor: zoom-out;
}

.modal-content {
  position: relative;
  max-width: 90%;
  max-height: 90%;
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: default;
}

.modal-image {
  max-width: 100%;
  max-height: 80vh;
  object-fit: contain;
  border-radius: 4px;
  box-shadow: 0 0 20px rgba(0,0,0,0.5);
}

.modal-footer {
  margin-top: 15px;
  text-align: center;
  display: flex;
  flex-direction: column;
  gap: 10px;
  align-items: center;
}

.modal-caption {
  color: white;
  font-size: 16px;
  margin: 0;
}

.action-btn {
  background-color: #4caf50;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.2s;
  font-weight: bold;
}

.action-btn:hover {
  background-color: #45a049;
}

.action-btn:disabled {
  background-color: #888;
  cursor: not-allowed;
}

.close-btn {
  position: absolute;
  top: -40px;
  right: 0;
  background: none;
  border: none;
  color: white;
  font-size: 30px;
  cursor: pointer;
  line-height: 1;
}

.close-btn:hover {
  color: #ff5555;
}
</style>