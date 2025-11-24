import { reactive, ref } from "vue";
import api from "../../plugins/axios";

export const IMAGE_WIDTH_PX = 987;
export const IMAGE_HEIGHT_PX = 643;
export const colNum = IMAGE_WIDTH_PX;
export const rowHeight = 1;
export const totalRows = IMAGE_HEIGHT_PX;
export const DEFAULT_WIDTH = 27;
export const DEFAULT_HEIGHT = 50;
export const HORIZONTAL_DESK_WIDTH = 55;
export const HORIZONTAL_DESK_HEIGHT = 26;

export const imageDimensions = ref({ width: 0, height: 0 });
export const layout = reactive<any[]>([]);
export const deskCoordinates = ref<Array<{ id: number; x: number; y: number }>>([]);
export const DeskColors = ref<Array<{ deskId: number; deskColor: string }>>([]);
export const selectedDate = ref<string>(new Date().toISOString().split("T")[0]);
export const imageUrl = ref('');

const isFetchingColors = ref(false);
const isLoadingDesks = ref(false);
const isLoadingBackground = ref(false);

/**
 * MASTER FUNCTION: Call this from your Vue component watchers/onMounted.
 * It ensures the date is updated and data loads in the correct order.
 */
export const refreshMapData = async (newDate?: string) => {
    if (newDate) {
        selectedDate.value = newDate;
    }

    await loadAllColors();
    await loadDesksFromBackend();
};

export function resetLayout() {
    layout.length = 0;
}

export async function getBackgroundFromBackend() {
    if (isLoadingBackground.value) {
        console.log('[floorLayout] Already loading background, skipping...');
        return;
    }

    isLoadingBackground.value = true;
    try {
        const response = await api.get("/images/background", {
            responseType: 'blob'
        });

        const blob = await response.data;
        const url = URL.createObjectURL(blob);
        const img = new Image();
        img.onload = () => {
            imageDimensions.value = {
                width: img.naturalWidth,
                height: img.naturalHeight
            };
        };
        img.src = url;
        imageUrl.value = url;
    } catch (err) {
        console.error("Error loading image", err);
    } finally {
        isLoadingBackground.value = false;
    }
}

async function getColors() {
    try {
        const response = await api.get(`/booking/byDate?localDate=${selectedDate.value}`);
        const data = Array.isArray(response.data) ? response.data : [];
        return data.map((item) => ({
            deskId: item.deskColorDTO?.deskId,
            deskColor: item.deskColorDTO?.deskColor,
        }));
    } catch (error: any) {
        console.warn("Error getting general colors (likely 404/empty)", error.message);
        return [];
    }
}

async function getBlue() {
    try {
        const response = await api.get(`/desk/blue?localDate=${selectedDate.value}`);
        const data = Array.isArray(response.data) ? response.data : [];
        return data.map((item) => ({
            deskId: item.deskId,
            deskColor: item.deskColor,
        }));
    } catch (error: any) {
        console.error("Error getting blue desks", error.message);
        return [];
    }
}

async function getGray() {
    try {
        const response = await api.get(`/desk/gray?localDate=${selectedDate.value}`);
        const data = Array.isArray(response.data) ? response.data : [];
        return data.map((item) => ({
            deskId: item.deskId,
            deskColor: item.deskColor,
        }));
    } catch (error: any) {
        console.error("Error getting gray desks", error.message);
        return [];
    }
}

async function getPurpleBookedDesks() {
    try {
        const response = await api.get(`/booking/my/byDate?localDate=${selectedDate.value}`);
        const data = Array.isArray(response.data) ? response.data : [];
        return data.map((item) => ({
            deskId: item.desk?.id,
            deskColor: "PURPLE",
        }));
    } catch (error: any) {
        console.error("Error getting purple desks", error.message);
        return [];
    }
}

export const loadAllColors = async () => {
    if (isFetchingColors.value) {
        console.log('[floorLayout] Already loading colors, skipping...');
        return;
    }

    isFetchingColors.value = true;

    try {
        const [general, blue, gray, purple] = await Promise.all([
            getColors(),
            getBlue(),
            getGray(),
            getPurpleBookedDesks()
        ]);

        const colorMap = new Map<number, string>();

        [...general, ...blue, ...gray].forEach((c) => {
            if (c.deskId != null) colorMap.set(c.deskId, c.deskColor);
        });

        purple.forEach((c) => {
            if (c.deskId != null) colorMap.set(c.deskId, c.deskColor);
        });

        DeskColors.value = Array.from(colorMap.entries()).map(
            ([deskId, deskColor]) => ({
                deskId,
                deskColor,
            })
        );
    } catch (error: any) {
        console.error("Error loading colors", error.message);
        DeskColors.value = [];
    } finally {
        isFetchingColors.value = false;
    }
};

export const loadDesksFromBackend = async () => {
    if (isLoadingDesks.value) {
        console.log('[floorLayout] Already loading desks, skipping...');
        return;
    }

    isLoadingDesks.value = true;

    try {
        const response = await api.get("/desk/coordinates");
        const data = Array.isArray(response.data) ? response.data : [];

        deskCoordinates.value = data;

        resetLayout();

        data.forEach((desk: any) => {
            if (
                desk &&
                typeof desk.id === "number" &&
                typeof desk.x === "number" &&
                typeof desk.y === "number"
            ) {

                const colorMatch = DeskColors.value.find((c) => c.deskId === desk.id);
                const current_color = colorMatch ? colorMatch.deskColor : "GREEN";

                const isStatic = ["GRAY", "BLUE"].includes(current_color);

                layout.push({
                    x: Math.round(desk.x),
                    y: Math.round(desk.y),
                    w: desk.width,
                    h: desk.height,
                    i: String(desk.id),
                    static: false,
                    deskName: desk.deskName,
                    color: current_color,
                    isNonInteractive: isStatic,
                });
            }
        });

    } catch (error: any) {
        console.error("Error getting desks", error.message);
        deskCoordinates.value = [];
        resetLayout();
    } finally {
        isLoadingDesks.value = false;
    }
};

/**
 * Update colors of existing desks in layout without reloading coordinates
 * Use this when only the date changes and desk positions remain the same
 */
export const updateDeskColors = () => {
    layout.forEach(desk => {
        const colorMatch = DeskColors.value.find((c) => c.deskId === Number(desk.i));
        if (colorMatch) {
            desk.color = colorMatch.deskColor;
            desk.isNonInteractive = ["GRAY", "BLUE"].includes(colorMatch.deskColor);
        } else {
            desk.color = "GREEN";
            desk.isNonInteractive = false;
        }
    });
};