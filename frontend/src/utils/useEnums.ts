import api from "@/plugins/axios";
import { ref } from "vue";

export const statusColorMap = ref<Record<string, string>>({});
export const statusBookingOptions = ref([{ title: "All", value: "ALL" }]);
export const typeDeskOptions = ref([{ title: "All", value: "ALL" }]);
export const statusDeskOptions = ref([{ title: "All", value: "ALL" }]);

export const error = ref<string | null>(null);
export const loading = ref(false);

// Cache flag to prevent multiple fetches
let colorsFetched = false;

function formatTitle(value: string): string {
    return value.charAt(0) + value.slice(1).toLowerCase();
}

async function fetchEnum(
    endpoint: string,
    includeAll: boolean = true
): Promise<{ title: string; value: string }[]> {
    try {
        loading.value = true;
        error.value = null;

        const response = await api.get(endpoint);

        let results = response.data.map((item: string) => ({
            title: formatTitle(item),
            value: item,
        }));

        if (includeAll) {
            results.unshift({ title: "All", value: "ALL" });
        }

        return results;
    } catch (err: any) {
        console.error(`Error fetching ${endpoint}:`, err);
        error.value = err.response?.data?.message || "Failed to load data";
        return includeAll ? [{ title: "All", value: "ALL" }] : [];
    } finally {
        loading.value = false;
    }
}


export async function fetchDeskStatusEnum(includeAll = true) {
    statusDeskOptions.value = await fetchEnum("/admin/desk-status", includeAll);
}

export async function fetchDeskTypeEnum(includeAll = true) {
    typeDeskOptions.value = await fetchEnum("/admin/desk-type", includeAll);
}

export async function fetchBookingStatus(includeAll = true) {
    statusBookingOptions.value = await fetchEnum("/admin/booking-status", includeAll);
}

export async function fetchColors() {
    // Return early if already fetched
    if (colorsFetched) {
        console.log("Colors already cached, skipping fetch");
        return;
    }

    try {
        const response = await api.get("/admin/desk-colors");

        const colorMap: Record<string, string> = {};

        response.data.forEach((item: any) => {
            // Map both colorMeaning and colorName to colorCode
            colorMap[item.colorMeaning] = item.colorCode;
            if (item.colorName) {
                colorMap[item.colorName] = item.colorCode;
            }
        });

        statusColorMap.value = colorMap;
        colorsFetched = true;

        console.log("Color map loaded:", statusColorMap.value);
    } catch (err) {
        console.error("Error fetching colors:", err);
    }
}

export function getColor(status: string): string {
    return statusColorMap.value[status] || "#737373";
}
