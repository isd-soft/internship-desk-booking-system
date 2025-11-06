import { createApp } from "vue";
import App from "./App.vue";
import router from "./router";
import vuetify from "./plugins/vuetify";
import VueKonva from "vue-konva";

createApp(App).use(router).use(vuetify).use(VueKonva).mount("#app");
