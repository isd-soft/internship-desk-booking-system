import "vuetify/styles";
import { createVuetify } from "vuetify";
import * as components from "vuetify/components";
import * as directives from "vuetify/directives";
import "@mdi/font/css/materialdesignicons.css";

const vuetify = createVuetify({
  components,
  directives,
  icons: {
    defaultSet: "mdi",
  },
  theme: {
    defaultTheme: "light",
    themes: {
      light: {
        colors: {
          primary: "#FF9800",
          secondary: "#F57C00",
          background: "#FFF7F1",
          "orange-darken-2": "#E65100",
        },
      },
    },
  },
});

export default vuetify;
