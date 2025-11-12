import { createApp } from "vue";
import App from "./App.vue";
import router from "./router";
import vuetify from "./plugins/vuetify";
import VueKonva from "vue-konva";
import { createPinia } from "pinia";
import { GridLayout, GridItem } from "grid-layout-plus";

// ✅ ИМПОРТИРУЕМ STORE ДО createApp
import { useFavouritesStore } from "@/stores/favourites";

const app = createApp(App);
const pinia = createPinia();

app.use(pinia);
app.use(router);
app.use(vuetify);
app.use(VueKonva);

app.component("GridLayout", GridLayout);
app.component("GridItem", GridItem);

// ✅ ПЕРЕД МОНТИРОВАНИЕМ — ГРУЗИМ ФАВОРИТЫ
const favStore = useFavouritesStore();
favStore.ensureLoaded(); // НЕ await → не блокирует загрузку UI

app.mount("#app");
