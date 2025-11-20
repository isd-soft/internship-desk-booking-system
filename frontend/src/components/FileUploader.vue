<template>
  <v-dialog v-model="showDialog" max-width="500px">
    <template #activator="{ props }">
      <v-btn
        v-bind="props"
        color="#171717"
        variant="flat"
        class="control-button upload-activator-button"
        prepend-icon="mdi-plus"
        :disabled="uploading"
      >
        Upload Image
      </v-btn>
    </template>

    <v-card class="upload-dialog-card">
      <v-card-title class="dialog-header">
        <v-icon color="#171717" class="mr-2">mdi-image-plus</v-icon>
        Upload Background Image
      </v-card-title>

      <v-card-text class="dialog-content">
        <div class="upload-area" :class="{ 'has-file': selectedFile }">
          <input
            type="file"
            id="file-upload"
            accept="image/png, image/jpeg"
            @change="handleFileSelect"
            style="display: none"
          />
          <label for="file-upload" class="upload-label">
            <template v-if="!selectedFile">
              <v-icon size="40" color="#a3a3a3">mdi-cloud-upload-outline</v-icon>
              <p class="upload-text">Click or drag image to upload</p>
              <p class="upload-hint">PNG, JPG up to 10MB</p>
            </template>
            <template v-else>
              <div class="file-preview-wrapper">
                <img :src="preview" alt="Preview" class="preview-image" />
                <v-btn
                  icon
                  size="small"
                  variant="flat"
                  color="#ef4444"
                  class="clear-button"
                  @click.stop="clearSelection"
                  >
                  <v-icon size="large">mdi-close</v-icon>
                </v-btn>
              </div>
            </template>
          </label>
        </div>

        <div v-if="selectedFile" class="file-info">
          <div class="info-row">
            <span class="info-label">File name:</span>
            <span class="info-value">{{ selectedFile.name }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">Size:</span>
            <span class="info-value"
              >{{ (selectedFile.size / 1024).toFixed(2) }} KB</span
            >
          </div>
          <div class="info-row">
            <span class="info-label">Type:</span>
            <span class="info-value">{{ selectedFile.type }}</span>
          </div>
        </div>
      </v-card-text>

      <v-card-actions class="dialog-actions">
        <v-spacer></v-spacer>
        <v-btn
          variant="text"
          @click="closeDialog"
          :disabled="uploading"
          color="#525252"
        >
          Cancel
        </v-btn>
        <v-btn
          color="#171717"
          variant="flat"
          @click="uploadImage"
          :loading="uploading"
          :disabled="!selectedFile || uploading"
          class="upload-action-button"
        >
          {{ uploading ? 'Uploading...' : 'Upload' }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { ref, defineEmits } from 'vue';
import api from "../plugins/axios.js";
import { useToast } from "vue-toastification";

const toast = useToast();

const selectedFile = ref<File | null>(null);
const preview = ref<string | null>(null);
const uploading = ref(false);
const showDialog = ref(false);
const emit = defineEmits(['uploaded']);

function handleFileSelect(event: Event) {
  const target = event.target as HTMLInputElement;
  const file = target.files?.[0];
  target.value = ''; 

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

    toast.success("Image uploaded successfully!", {
      timeout: 2000
    });

    emit('uploaded');
    closeDialog();

  } catch (error: any) {
    console.error("Upload Error:", error);
    const errorMessage = error.response?.data?.message || "Failed to upload image.";
    toast.error(errorMessage, {
      timeout: 3000
    });
  } finally {
    uploading.value = false;
  }
}

function clearSelection() {
  selectedFile.value = null;
  preview.value = null;
}

function closeDialog() {
    clearSelection();
    showDialog.value = false;
}
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap");

* {
  font-family: "Inter", -apple-system, BlinkMacSystemFont, "Segoe UI",
    sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

.control-button {
  font-weight: 600 !important;
  text-transform: none !important;
  letter-spacing: 0.3px;
  border-radius: 12px !important;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08) !important;
  transition: all 0.3s ease;
  height: 40px !important;
}

.control-button:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12) !important;
  transform: translateY(-1px);
}

.upload-dialog-card {
  border-radius: 16px !important;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15) !important;
}

.dialog-header {
  font-weight: 700;
  font-size: 20px;
  color: #171717;
  padding: 24px 24px 16px;
  border-bottom: 1px solid #f5f5f5;
}

.dialog-content {
  padding: 24px 24px 16px;
}

.dialog-actions {
  padding: 16px 24px 24px;
}

.upload-action-button {
  font-weight: 600 !important;
  border-radius: 8px !important;
}

.upload-area {
  border: 2px dashed #d1d5db;
  border-radius: 12px;
  padding: 2.5rem;
  text-align: center;
  margin-bottom: 20px;
  transition: all 0.3s ease;
}

.upload-area:hover:not(.has-file) {
  border-color: #737373; 
  background-color: #fcfcfc;
}

.upload-area.has-file {
  padding: 1rem;
  border: 1px solid #e5e5e5;
}

.upload-label {
  cursor: pointer;
  display: block;
  min-height: 100px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.upload-text {
  font-weight: 600;
  color: #171717;
  font-size: 15px;
  margin: 0;
}

.upload-hint {
  font-size: 12px;
  color: #737373;
  margin: 0;
}

.file-preview-wrapper {
  position: relative;
  width: 100%;
  max-height: 300px;
  overflow: hidden;
  border-radius: 8px;
}

.preview-image {
  width: 100%;
  height: auto;
  display: block;
}

.clear-button {
  position: absolute !important;
  top: 8px;
  right: 8px;
  z-index: 10;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.file-info {
  background: #f5f5f5;
  padding: 12px 16px;
  border-radius: 12px;
  margin-bottom: 16px;
  font-size: 13px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 4px;
}

.info-label {
  font-weight: 600;
  color: #737373;
  letter-spacing: 0.3px;
}

.info-value {
  font-weight: 700;
  color: #171717;
}
</style>