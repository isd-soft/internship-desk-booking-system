import { defineStore } from "pinia";
import { ref } from "vue";
import api from "@/plugins/axios";

export const useFavouritesStore = defineStore("favourites", () => {
    const ids = ref<number[]>([]);
    const loading = ref(false);
    const loaded = ref(false);

    async function fetch() {
        if (loading.value) {
            console.log('[Favourites] Already loading, skipping...');
            return;
        }

        loading.value = true;
        try {
            const res = await api.get("/favourites");
            ids.value = res.data.map((f: any) => Number(f.deskId));
            loaded.value = true;
            console.log('[Favourites] Loaded:', ids.value.length);
        } catch (e) {
            console.error("⚠️ Failed to load favourites:", e);
            ids.value = [];
        } finally {
            loading.value = false;
        }
    }

    async function ensureLoaded() {
        if (!loaded.value && !loading.value) {
            await fetch();
        } else if (loaded.value) {
            console.log('[Favourites] Already loaded, skipping...');
        }
    }

    async function toggle(deskId: number | string) {
        const id = Number(deskId);
        const wasFavourite = ids.value.includes(id);
        const previous = [...ids.value];

        if (wasFavourite) {
            ids.value = ids.value.filter((f) => f !== id);
        } else {
            ids.value = [...ids.value, id];
        }

        try {
            if (wasFavourite) {
                await api.delete(`/favourites/${id}`);
            } else {
                await api.post(`/favourites/${id}`);
            }
            loaded.value = false;
            await fetch();
        } catch (e) {
            console.error("⚠️ Failed to toggle favourite:", e);
            ids.value = previous;
        }
    }

    async function remove(deskId: number | string) {
        const id = Number(deskId);
        if (!ids.value.includes(id)) return;
        await toggle(id);
    }

    function isFav(deskId: number | null | undefined) {
        return deskId != null && ids.value.includes(Number(deskId));
    }

    async function refresh() {
        loaded.value = false;
        await fetch();
    }

    function reset() {
        ids.value = [];
        loaded.value = false;
        loading.value = false;
    }

    return {
        ids,
        isFav,
        ensureLoaded,
        toggle,
        remove,
        refresh,
        reset,
    };
});