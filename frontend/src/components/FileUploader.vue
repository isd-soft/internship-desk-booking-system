<script setup lang="ts">
import { ref, defineEmits } from 'vue';
import api from "../plugins/axios.js";
import { useToast } from "vue-toastification";

const toast = useToast();

const selectedFile = ref<File | null>(null);
const preview = ref<string | null>(null);
const uploading = ref(false);
const emit = defineEmits(['uploaded']);

function handleFileSelect(event: Event) {
  const target = event.target as HTMLInputElement;
  const file = target.files?.[0];
  
  if (file) {
    selectedFile.value = file;
    
    const reader = new FileReader();
    reader.onloadend = () => {
      preview.value = reader.result as string;
    };
    reader.readAsDataURL(file);
  }
}

async function uploadImage() {
  if (!selectedFile.value) return;
  
  uploading.value = true;
  
  const formData = new FormData();
  formData.append('file', selectedFile.value);
  
  try {
    const response = await api.post('/admin/images/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
    
    toast.success("Success", {
      timeout: 2000
    });

    emit('uploaded');
    
    selectedFile.value = null;
   preview.value = null;
    
  } catch (error: any) {
    toast.error("Error uploading an image", {
      timeout: 2000
    });
  } finally {
    uploading.value = false;
  }
}

function clearSelection() {
  selectedFile.value = null;
  preview.value = null;
}
</script>

<template>
  <div class="upload-container">
    <h2>Upload background image</h2>
    
    <div class="upload-area">
      <input
        type="file"
        id="file-upload"
        accept="image/*"
        @change="handleFileSelect"
        style="display: none"
      />
      <label for="file-upload" class="upload-label">
        <v-icon size="40">mdi-upload</v-icon>
        <p>Click to upload image</p>
        <p class="upload-hint">PNG, JPG up to 10MB</p>
      </label>
    </div>

    <div v-if="selectedFile" class="file-info">
      <div class="info-row">
        <span>File name:</span>
        <span>{{ selectedFile.name }}</span>
      </div>
      <div class="info-row">
        <span>Size:</span>
        <span>{{ (selectedFile.size / 1024).toFixed(2) }} KB</span>
      </div>
      <div class="info-row">
        <span>Type:</span>
        <span>{{ selectedFile.type }}</span>
      </div>
    </div>
    
    
    <button
      @click="uploadImage"
      :disabled="!selectedFile || uploading"
      class="upload-button"
    >
      {{ uploading ? 'Uploading...' : 'Upload Image' }}
    </button>
  </div>
</template>

<style scoped>
.upload-container {
  max-width: 400px;
  margin: 0 auto;
  padding: 2rem;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

h2 {
  margin-bottom: 1.5rem;
  color: #333;
}

.upload-area {
  border: 2px dashed #d1d5db;
  border-radius: 12px;
  padding: 3rem;
  text-align: center;
  margin-bottom: 1.5rem;
  transition: border-color 0.3s;
}

.upload-area:hover {
  border-color: #3b82f6;
}

.upload-label {
  cursor: pointer;
  display: block;
}

.upload-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.upload-hint {
  font-size: 0.875rem;
  color: #6b7280;
  margin-top: 0.5rem;
}

.preview-image {
  max-width: 100%;
  max-height: 400px;
  display: block;
  margin: 0 auto;
  border-radius: 8px;
}

.clear-button {
  position: absolute;
  top: 0.5rem;
  right: 0.5rem;
  background: #ef4444;
  color: white;
  border: none;
  border-radius: 50%;
  width: 32px;
  height: 32px;
  cursor: pointer;
  font-size: 1.25rem;
  display: flex;
  align-items: center;
  justify-content: center;
}

.file-info {
  background: #f9fafb;
  padding: 1rem;
  border-radius: 8px;
  margin-bottom: 1.5rem;
}

.info-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.5rem;
  font-size: 0.875rem;
}

.info-row span:first-child {
  font-weight: 600;
  color: #6b7280;
}

.status {
  padding: 1rem;
  border-radius: 8px;
  margin-bottom: 1.5rem;
  font-size: 0.875rem;
  font-weight: 500;
}

.status.success {
  background: #d1fae5;
  color: #065f46;
  border: 1px solid #6ee7b7;
}

.status.error {
  background: #fee2e2;
  color: #991b1b;
  border: 1px solid #fca5a5;
}

.upload-button {
  width: 100%;
  padding: 1rem;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.upload-button:hover:not(:disabled) {
  background: #2563eb;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.4);
}

.upload-button:disabled {
  background: #d1d5db;
  cursor: not-allowed;
  transform: none;
}
</style>