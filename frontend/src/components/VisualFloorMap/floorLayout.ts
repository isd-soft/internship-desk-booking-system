import { reactive } from "vue";

export const IMAGE_WIDTH_PX = 987;
export const IMAGE_HEIGHT_PX = 643;

export const colNum = IMAGE_WIDTH_PX;
export const rowHeight = 1;
export const totalRows = IMAGE_HEIGHT_PX;

export const layout = reactive<any[]>([]);

export const floorImage = "/floorplan/Floor.png";

let counter = 0;
let offsetX = 0;
let baseWidth = 27;
let baseHeight = 50;
let clusterSpacing = 105;
let horizontalDeskHeight = 26;
let horizontalDeskWidth = 55;

export function addDesk(
  clusterDesks,
  xCoord,
  startY,
  reverseFlag = false,
  makeStatic = false
) {
  for (const desk of clusterDesks) {
    let direction = 1;

    if (reverseFlag) {
      direction = -1;
    }

    counter++;
    layout.push({
      x: xCoord - desk.dx,
      y: startY - desk.dy * direction,
      w: desk.w,
      h: desk.h,
      i: String(counter),
      static: makeStatic,
    });
  }
}

export function makeBottomClusters(
  startX: number,
  startY: number,
  totalClusters = 4,
  makeStatic = false
) {
  for (let cluster = 0; cluster < totalClusters; cluster++) {
    const xCoord = startX - offsetX - cluster * clusterSpacing;

    const clusterDesks = [
      { dx: 0, dy: 0, h: baseHeight, w: baseWidth },
      { dx: baseWidth, dy: 0, h: baseHeight, w: baseWidth },
      { dx: 0, dy: baseHeight, h: baseHeight, w: baseWidth },
      { dx: baseWidth, dy: baseHeight, h: baseHeight, w: baseWidth },
      {
        dx: baseWidth,
        dy: baseHeight + baseWidth - 1,
        h: horizontalDeskHeight,
        w: horizontalDeskWidth,
      },
    ];
    addDesk(clusterDesks, xCoord, startY);
  }
}

export function makeTopClusters(
  startX: number,
  startY: number,
  totalClusters = 4,
  makeStatic = false
) {
  for (let cluster = 0; cluster < totalClusters; cluster++) {
    const xCoord = startX - offsetX - cluster * clusterSpacing;

    const clusterDesks = [
      { dx: 0, dy: 0, h: baseHeight, w: baseWidth },
      { dx: baseWidth, dy: 0, h: baseHeight, w: baseWidth },
      { dx: 0, dy: baseHeight, h: baseHeight, w: baseWidth },
      { dx: baseWidth, dy: baseHeight, h: baseHeight, w: baseWidth },
      {
        dx: baseWidth,
        dy: baseHeight + baseWidth - 1,
        h: horizontalDeskHeight,
        w: horizontalDeskWidth,
      },
    ];
    addDesk(clusterDesks, xCoord, startY, true);
  }
}

export function makeLeftClusters(
  startX: number,
  startY: number,
  totalClusters = 1,
  makeStatic = false
) {
  for (let cluster = 0; cluster < totalClusters; cluster++) {
    let yCoord = startY - (clusterSpacing - 10) * cluster;
    const clusterDesks = [
      { dx: 0, dy: 0, h: horizontalDeskHeight, w: horizontalDeskWidth },
      {
        dx: 0,
        dy: horizontalDeskHeight,
        h: horizontalDeskHeight,
        w: horizontalDeskWidth,
      },
    ];
    addDesk(clusterDesks, startX, yCoord);
  }
}
