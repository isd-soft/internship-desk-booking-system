<template>
  <div class="bookings-container">
    <div class="bookings-card gallery-card">
      <div class="bookings-header">
        <div class="header-title-section">
          <div class="header-label">ADMIN PANEL</div>
          <h2 class="header-title">Image Gallery</h2>
          <span class="header-subtitle"
            >{{ images.length }} total images</span
          >
        </div>
        <div class="header-controls">
          <FileUploader @uploaded="fetchImages" />
        </div>
      </div>

      <div v-if="loading" class="loading-container">
        <v-progress-circular
          indeterminate
          size="48"
          width="4"
          color="#171717"
        />
        <p class="loading-message mt-3">Loading images…</p>
      </div>

      <v-alert
        v-else-if="error"
        type="error"
        variant="tonal"
        class="mb-4"
        density="compact"
      >
        {{ error }}
      </v-alert>

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
        <div v-if="images.length === 0" class="table-empty-state">
          <v-icon size="48" color="#a3a3a3" class="mb-3"
            >mdi-image-multiple</v-icon
          >
          <div class="table-empty-title">No images found</div>
          <div class="table-empty-subtitle">
            Upload new images to populate the gallery.
          </div>
        </div>
      </div>

      <ImageModal
        v-if="selectedImage"
        :image="selectedImage"
        @close="selectedImage = null"
        @updated="handleBackgroundUpdate"
        @deleted="handleDeleted"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import FileUploader from "./FileUploader.vue";
import ImageModal from "./AdminDashboard/ImageModal.vue";
import api from "../plugins/axios.js";

interface ImageDto {
  id: number;
  file_name: string;
  content_type: string;
  image_data: string;
  isBackground: boolean;
}

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

const handleDeleted = () => {
  fetchImages();
}

const fetchImages = async () => {
  loading.value = true;
  try {
    const response = await api.get<ImageDto[]>('/admin/images');
    images.value = response.data;
  } catch (err: any) {
    console.error(err);
    error.value = err.response?.data?.message || err.message || 'Error loading gallery';
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
@import url("https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap");

* {
  font-family: "Inter", -apple-system, BlinkMacSystemFont, "Segoe UI",
    sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

.bookings-container {
  padding: 28px;
  background: #fafafa;
  min-height: 100vh;
}

.bookings-card {
  background: #ffffff;
  border: 1px solid #e5e5e5;
  border-radius: 20px;
  padding: 28px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
}

.bookings-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 2px solid #f5f5f5;
}

.header-title-section {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.header-label {
  font-size: 11px;
  font-weight: 700;
  color: #737373;
  letter-spacing: 1.5px;
}

.header-title {
  font-size: 28px;
  font-weight: 800;
  color: #171717;
  margin: 0;
  letter-spacing: -0.5px;
  line-height: 1;
}

.header-subtitle {
  font-size: 13px;
  font-weight: 600;
  color: #737373;
  letter-spacing: 0.3px;
}

.header-controls {
  display: flex;
  align-items: center;
  gap: 12px;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
}

.loading-message {
  font-size: 15px;
  font-weight: 600;
  color: #737373;
  letter-spacing: 0.3px;
}


.gallery-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 20px; 
  margin-top: 10px;
}

.gallery-item {
  border: 1px solid #e5e5e5;
  border-radius: 12px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.gallery-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
}

.thumbnail {
  width: 100%;
  height: 150px;
  object-fit: cover;
  display: block;
  border-bottom: 1px solid #e5e5e5;
}

.info {
  padding: 8px 12px;
  font-size: 13px;
  text-align: left;
  background: #ffffff;
  color: #171717;
  font-weight: 600;
  letter-spacing: 0.3px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.bg-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  background: #171717;
  color: white;
  padding: 4px 10px;
  border-radius: 8px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.5px;
  text-transform: uppercase;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.table-empty-state {
  grid-column: 1 / -1; 
  text-align: center;
  padding: 60px 0;
  color: #737373;
}

.table-empty-title {
  font-weight: 700;
  color: #171717;
  font-size: 18px;
  margin-bottom: 8px;
  letter-spacing: -0.3px;
}

.table-empty-subtitle {
  font-weight: 600;
  color: #737373;
  font-size: 14px;
  letter-spacing: 0.3px;
}
</style>