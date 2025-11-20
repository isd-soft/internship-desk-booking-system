import { reactive, ref, markRaw } from "vue";
import api from "../../plugins/axios";

export const IMAGE_WIDTH_PX = 987;
export const IMAGE_HEIGHT_PX = 643;

//this variable is going to be initialized when image is gotten from backend
export const imageDimensions = ref({width: 0, height: 0});

export const colNum = IMAGE_WIDTH_PX;
export const rowHeight = 1;
export const totalRows = IMAGE_HEIGHT_PX;

export const layout = reactive<any[]>([]);

export const deskCoordinates = ref<Array<{ id: number; x: number; y: number }>>(
  []
);
export const DeskColors = ref<Array<{ deskId: number; deskColor: string }>>([]);

export const selectedDate = ref<string>(new Date().toISOString().split("T")[0]);

export const DEFAULT_WIDTH = 27;
export const DEFAULT_HEIGHT = 50;

export const HORIZONTAL_DESK_WIDTH = 55;
export const HORIZONTAL_DESK_HEIGHT = 26;

export const imageUrl = ref('');

export async function getBackgroundFromBackend() {
  try {
    const response = await api.get("/images/background",{
      responseType:'blob'
    });
  
    const blob = await response.data;
    const url = URL.createObjectURL(blob);
    const img = new Image();
    img.onload = () => {
      imageDimensions.value = {
        width: img.naturalWidth,
        height: img.naturalHeight
      };
      console.log('Image dimensions:', imageDimensions.value);
    };
    img.src = url;
    imageUrl.value = url;
    
  
  } catch (err) {
    console.error("Error loading image", err)
  }
}

async function getColors() {
  try {
    const response = await api.get(
      `/booking/byDate?localDate=${selectedDate.value}`
    );
    const data = Array.isArray(response.data) ? response.data : [];

    const colors = data.map((item) => ({
      deskId: item.deskColorDTO?.deskId,
      deskColor: item.deskColorDTO?.deskColor,
    }));

    return colors;
  } catch (error) {
    console.error("Error getting data from backend", error.message);
    return [];
  }
}

async function getBlue() {
  try {
    const response = await api.get(
      `/desk/blue?localDate=${selectedDate.value}`
    );
    const data = Array.isArray(response.data) ? response.data : [];

    const colors = data.map((item) => ({
      deskId: item.deskId,
      deskColor: item.deskColor,
    }));

    return colors;
  } catch (error) {
    console.error("Error getting data from backend", error.message);
    return [];
  }
}

async function getGray() {
  try {
    const response = await api.get(
      `/desk/gray?localDate=${selectedDate.value}`
    );
    const data = Array.isArray(response.data) ? response.data : [];

    const colors = data.map((item) => ({
      deskId: item.deskId,
      deskColor: item.deskColor,
    }));

    return colors;
  } catch (error) {
    console.error("Error getting data from backend", error.message);
    return [];
  }
}

export function resetLayout() {
  layout.length = 0;
}

export const loadAllColors = async () => {
  try {
    const [general, blue, gray] = await Promise.all([
      getColors(),
      getBlue(),
      getGray(),
    ]);

    const purple = await getPurpleBookedDesks();

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
  } catch (error) {
    console.error("Error getting data from backend", error.message);
    DeskColors.value = [];
  }
};

async function getPurpleBookedDesks() {
  try {
    const response = await api.get(
      `/booking/my/byDate?localDate=${selectedDate.value}`
    );
    const data = Array.isArray(response.data) ? response.data : [];

    return data.map((item) => ({
      deskId: item.desk?.id,
      deskColor: "PURPLE",
    }));
  } catch (error) {
    console.error("Error getting purple desks", error.message);
    return [];
  }
}

export const loadDesksFromBackend = async () => {
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

    console.log("Desks loaded:", layout.length);
  } catch (error: any) {
    console.error("Error getting desks", error.message);

    deskCoordinates.value = [];
    resetLayout();
  }
};
