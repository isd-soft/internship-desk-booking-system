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
export const horizontalDesks = [5,10,15,20,25,30,31,32,33,34,39,44,49,54,59,60,61,62,63];

const someColors=[
  {
    id: 1,
    color: "GREEN" 
  },
  {
    id: 2,
    color: "RED" 
  },
  {
    id: 3,
    color: "AMBER" 
  },
  {
    id: 4,
    color: "BLUE" 
  },
  {
    id: 5,
    color: "GRAY" 
  }
];

const DEFAULT_WIDTH = 27;
const DEFAULT_HEIGHT = 50;

const HORIZONTAL_DESK_WIDTH = 55;
const HORIZONTAL_DESK_HEIGHT = 26;

export function resetLayout() {
  layout.length = 0;
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
        typeof desk.id === 'number' &&
        typeof desk.x === 'number' &&
        typeof desk.y === 'number'
      ) {
        let current_width = DEFAULT_WIDTH;
        let current_height = DEFAULT_HEIGHT;

        if(horizontalDesks.includes(desk.id)){
          current_width = HORIZONTAL_DESK_WIDTH;
          current_height = HORIZONTAL_DESK_HEIGHT;
        }
        const colorMatch = someColors.find((c) => c.id === desk.id);
        const current_color = colorMatch ? colorMatch.color : "NOT A COLOR";

        layout.push({
          x: Math.round(desk.x),
          y: Math.round(desk.y),
          w: current_width,
          h: current_height,
          i: String(desk.id),
          static: false,
          deskName: desk.deskName,
          color: current_color
        });
      }
    });

    console.log('Desks loaded:', layout.length);
  } catch (error: any) {
    console.error('Error getting desks', error.message);

    deskCoordinates.value = [];
    resetLayout();
  }
};