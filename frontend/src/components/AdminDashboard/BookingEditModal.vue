<template>
  <div v-if="show" class="modal-overlay" @click.self="close">
    <div class="modal">
      <h3>Edit Booking #{{ booking?.id }}</h3>

      <form @submit.prevent="saveChanges">
        <label>
          User Name:
          <input type="text" v-model="formData.userName" />
        </label>

        <label>
          Desk ID:
          <input type="number" v-model="formData.deskId" />
        </label>

        <label>
          Date:
          <input type="date" v-model="formData.date" />
        </label>

        <div class="buttons">
          <button type="button" @click="close">Cancel</button>
          <button type="submit">Save</button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { reactive, watch } from "vue";

const props = defineProps({
  show: Boolean,
  booking: Object,
});
const emits = defineEmits(["close", "save"]);

const formData = reactive({
  userName: "",
  deskId: null,
  date: "",
});

// Populate form whenever booking changes
watch(
    () => props.booking,
    (newVal) => {
      if (newVal) {
        formData.userName = newVal.userName || "";
        formData.deskId = newVal.desk?.id || newVal.deskId || null;
        formData.date = newVal.date || newVal.startTime?.split("T")[0] || "";
      }
    },
    { immediate: true }
);

function close() {
  emits("close");
}

function saveChanges() {
  emits("save", { ...formData, id: props.booking?.id });
  close();
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.35);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}
.modal {
  background: white;
  padding: 24px;
  border-radius: 12px;
  min-width: 320px;
  max-width: 480px;
}
.buttons {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 16px;
}
</style>
