import { createApp } from "vue";
import App from "./App.vue";
import router from "./router";
import vuetify from "./plugins/vuetify";
import VueKonva from "vue-konva";
import { GridLayout, GridItem } from "grid-layout-plus";


createApp(App)
.use(router)
.use(vuetify)
.use(VueKonva)
.component("GridLayout", GridLayout)
.component("GridItem", GridItem)
.mount("#app");
