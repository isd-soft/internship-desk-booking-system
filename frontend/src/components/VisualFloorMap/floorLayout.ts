import { reactive, ref } from "vue";
import api from "../../plugins/axios";

export const IMAGE_WIDTH_PX = 987;
export const IMAGE_HEIGHT_PX = 643;

export const colNum = IMAGE_WIDTH_PX;
export const rowHeight = 1;
export const totalRows = IMAGE_HEIGHT_PX;

export const floorImage = "/floorplan/Floor.png";

export const layout = reactive<any[]>([]);

export const deskCoordinates = ref<Array<{ id: number; x: number; y: number }>>(
  []
);
export const horizontalDesks = [
  5, 10, 15, 20, 25, 30, 31, 32, 33, 34, 39, 44, 49, 54, 59, 60, 61, 62, 63,
];
export const DeskColors = ref<Array<{ deskId: number; deskColor: string }>>([]);

export const selectedDate = ref<string>(new Date().toISOString().split("T")[0]);

const DEFAULT_WIDTH = 27;
const DEFAULT_HEIGHT = 50;

const HORIZONTAL_DESK_WIDTH = 55;
const HORIZONTAL_DESK_HEIGHT = 26;

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
    const response = await api.get("/desk/blue");
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
    const response = await api.get("/desk/gray");
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

    [...general, ...blue, ...gray].forEach(c => {
      if (c.deskId != null) colorMap.set(c.deskId, c.deskColor);
    });

    purple.forEach(c => {
      if (c.deskId != null) colorMap.set(c.deskId, c.deskColor);
    });

    DeskColors.value = Array.from(colorMap.entries()).map(([deskId, deskColor]) => ({
      deskId,
      deskColor
    }));

  } catch(error){
    console.error("Error getting data from backend", error.message);
    DeskColors.value = [];
  }
};


async function getPurpleBookedDesks() {
  try {
    const response = await api.get(`/booking/my/byDate?localDate=${selectedDate.value}`);
    const data = Array.isArray(response.data) ? response.data : [];

    return data.map(item => ({
      deskId: item.desk?.id,
      deskColor: "PURPLE"
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
        let current_width = DEFAULT_WIDTH;
        let current_height = DEFAULT_HEIGHT;

        if (horizontalDesks.includes(desk.id)) {
          current_width = HORIZONTAL_DESK_WIDTH;
          current_height = HORIZONTAL_DESK_HEIGHT;
        }
        const colorMatch = DeskColors.value.find((c) => c.deskId === desk.id);
        const current_color = colorMatch ? colorMatch.deskColor : "GREEN";

        const isStatic = ["GRAY", "BLUE"].includes(current_color);

        layout.push({
          x: Math.round(desk.x),
          y: Math.round(desk.y),
          w: current_width,
          h: current_height,
          i: String(desk.id),
          static: false,
          deskName: desk.deskName,
          color: current_color,
          isNonInteractive: isStatic
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
