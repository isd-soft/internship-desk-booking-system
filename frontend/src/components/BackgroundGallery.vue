<template>
  <div class="gallery-container">
    <h2>Gallery</h2>
    <FileUploader @uploaded="fetchImages"/>
    
    <div v-if="loading">Loading...</div>
    <div v-else-if="error" class="error">{{ error }}</div>
    
    <div v-else class="gallery-grid">
      <div 
        v-for="image in images" 
        :key="image.id" 
        class="gallery-item"
        @click="selectedImage = image"
      >
        <img 
          :src="getImageSrc(image)" 
          :alt="image.file_name" 
          class="thumbnail"
        />
        <div v-if="image.isBackground" class="bg-badge">Background</div>
        <div class="info">
          {{ image.file_name }}
        </div>
      </div>
    </div>

    <ImageModal 
      v-if="selectedImage" 
      :image="selectedImage" 
      @close="selectedImage = null" 
      @updated="handleBackgroundUpdate"
    />
    
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import FileUploader from "./FileUploader.vue";
import ImageModal from "./AdminDashboard/ImageModal.vue"; 
import api from "../plugins/axios.js"; 
import { useToast } from "vue-toastification";

interface ImageDto {
  id: number;
  file_name: string;
  content_type: string;
  image_data: string; 
  isBackground: boolean;
}
const toast = useToast();

const images = ref<ImageDto[]>([]);
const loading = ref(false);
const error = ref('');
const selectedImage = ref<ImageDto | null>(null);

const handleBackgroundUpdate = (newBackgroundId: number) => {
  images.value = images.value.map(img => {
    if (img.id === newBackgroundId) {
      return { ...img, isBackground: true };
    } else {
      return { ...img, isBackground: false };
    }
  });
  
  if (selectedImage.value && selectedImage.value.id === newBackgroundId) {
      selectedImage.value.isBackground = true;
  }
};

const fetchImages = async () => {
  loading.value = true;
  try {
    const response = await api.get<ImageDto[]>('/admin/images');
    images.value = response.data;
  } catch (err) {
    console.error(err);
    error.value = 'Error loading gallery';
  } finally {
    loading.value = false;
  }
};

const getImageSrc = (image: ImageDto) => {
  if (!image.image_data) return '';
  return `data:${image.content_type};base64,${image.image_data}`;
};

onMounted(() => {
  fetchImages();
});
</script>

<style scoped>
.gallery-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 10px;
  margin-top: 20px;
}

.gallery-item {
  border: 1px solid #ccc;
  border-radius: 8px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  cursor: pointer;
  transition: transform 0.2s;
  position:relative;
}
.bg-badge {
  position: absolute;
  top: 5px;
  right: 5px;
  background: #4caf50;
  color: white;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 10px;
  font-weight: bold;
}

.gallery-item:hover {
  transform: scale(1.02);
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
}

.thumbnail {
  width: 100%;
  height: 150px;
  object-fit: cover;
  display: block;
}

.info {
  padding: 5px;
  font-size: 12px;
  text-align: center;
  background: #f9f9f9;
}
</style>