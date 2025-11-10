import { reactive, ref } from "vue";
import api from "../../plugins/axios";

export const IMAGE_WIDTH_PX = 987;
export const IMAGE_HEIGHT_PX = 643;

export const colNum = IMAGE_WIDTH_PX;
export const rowHeight = 1;
export const totalRows = IMAGE_HEIGHT_PX;

export const floorImage = "/floorplan/Floor.png";

export const layout = reactive<any[]>([]);

export const deskCoordinates = ref<Array<{ id: number; x: number; y: number }>>([]);

const DEFAULT_WIDTH = 27;
const DEFAULT_HEIGHT = 50;

export function resetLayout() {
  layout.length = 0;
}

export const loadDesksFromBackend = async () => {
  try {
    const response = await api.get("/desk/coordinates");

    const data = Array.isArray(response.data) ? response.data : [];
    deskCoordinates.value = data;

    const horizontalDesks = [5,10,15,20,25,30,31,32,33,34,39,44,49,54,59,60,61,62,63];

    resetLayout();

    data.forEach((desk: any) => {
      if (
        desk &&
        typeof desk.id === 'number' &&
        typeof desk.x === 'number' &&
        typeof desk.y === 'number'
      ) {
        if(horizontalDesks.includes(desk.id)){
          layout.push({
            x: Math.round(desk.x),
            y: Math.round(desk.y),
            w: 55,
            h: 26,
            i: String(desk.id),
            static: false,
          });
        } else{
          layout.push({
            x: Math.round(desk.x),
            y: Math.round(desk.y),
            w: DEFAULT_WIDTH,
            h: DEFAULT_HEIGHT,
            i: String(desk.id),
            static: false,
          });
        }
      }
    });

    console.log('Desks loaded:', layout.length);
  } catch (error: any) {
    console.error('Error getting desks', error.message);

    deskCoordinates.value = [];
    resetLayout();
  }
};