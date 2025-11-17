import { onMounted, ref } from "vue";
import {
  layout,
  resetLayout,
  horizontalDesks,
  DEFAULT_HEIGHT,
  DEFAULT_WIDTH,
  HORIZONTAL_DESK_HEIGHT,
  HORIZONTAL_DESK_WIDTH
} from "../VisualFloorMap/floorLayout";
import api from "../../plugins/axios.js";

const zones = ref([]);

export const getAllDesksFromBackend = async () => {
  try {
    const response = await api.get("/admin/desks");

    const data = Array.isArray(response.data) ? response.data : [];

    resetLayout();

    console.log(data);

    data.forEach((desk: any) => {
      if (
        desk &&
        typeof desk.id === "number" &&
        typeof desk.currentX === "number" &&
        typeof desk.currentY === "number"
      ) {
        let current_width = DEFAULT_WIDTH;
        let current_height = DEFAULT_HEIGHT;

        if (horizontalDesks.includes(desk.id)) {
          current_width = HORIZONTAL_DESK_WIDTH;
          current_height = HORIZONTAL_DESK_HEIGHT;
        }
        const zone = {
          zoneId: desk.zoneDto.id,
          zoneName: desk.zoneDto.zoneName,
          zoneAbv: desk.zoneDto.zoneAbv
        }
        
        if (!zones.value.some(z => z.zoneName === zone.zoneName)) {
          zones.value.push(zone);
        }

        layout.push({
          x: Math.round(desk.currentX),
          y: Math.round(desk.currentY),
          w: current_width,
          h: current_height,
          i: String(desk.id),
          static: false,
          deskName: desk.displayName,
          zone: zone 
        });
      }
    });

    console.log("Desks loaded:", layout.length);
  } catch (error: any) {
    console.error("Error getting desks", error.message);
    resetLayout();
  }
};