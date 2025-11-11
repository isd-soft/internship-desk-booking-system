import { defineStore } from "pinia";
import { ref } from "vue";
import api from "@/plugins/axios";

export const useFavouritesStore = defineStore("favourites", () => {
  const ids = ref<number[]>([]);
  const loading = ref(false);

  async function fetch() {
    loading.value = true;
    try {
      const res = await api.get("/favourites");
      ids.value = res.data.map((f: any) => Number(f.deskId));
    } catch (e) {
      console.error("⚠️ Failed to load favourites:", e);
      ids.value = [];
    } finally {
      loading.value = false;
    }
  }

  async function ensureLoaded() {
    await fetch();
  }

  async function toggle(deskId: number | string) {
    const id = Number(deskId);

    if (ids.value.includes(id)) {
      await api.delete(`/favourites/${id}`);
    } else {
      await api.post(`/favourites/${id}`);
    }

    await fetch();
  }

  function isFav(deskId: number | null | undefined) {
    return deskId != null && ids.value.includes(Number(deskId));
  }

  return {
    ids,
    isFav,
    ensureLoaded,
    toggle,
  };
});
