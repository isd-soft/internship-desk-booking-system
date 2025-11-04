<template>
  <v-container class="fill-height d-flex align-center justify-center" fluid>
    <v-card width="420" class="pa-8 rounded-lg elevation-4">
      <v-card-title class="justify-center text-h5 mb-6">
        <v-img
            src="@/assets/isd-logo.png"
            alt="ISD logo"
            max-width="90"
            class="mb-3"
        ></v-img>
        Desk Booking
      </v-card-title>

      <v-form @submit.prevent="onLogin">
        <v-text-field
            v-model="email"
            label="Email"
            type="email"
            prepend-inner-icon="mdi-email-outline"
            outlined
            dense
            required
        ></v-text-field>

        <v-text-field
            v-model="password"
            label="Password"
            type="password"
            prepend-inner-icon="mdi-lock-outline"
            outlined
            dense
            required
        ></v-text-field>

        <v-btn
            color="#c97a38"
            class="mt-6"
            block
            type="submit"
            elevation="2"
        >
          Login
        </v-btn>
      </v-form>
    </v-card>
  </v-container>
</template>

<script setup>
import { ref } from "vue";

const email = ref("");
const password = ref("");

const onLogin = () => {
  console.log("Login clicked");
  console.log("Email:", email.value);
  console.log("Password:", password.value);
  // TODO: integrate with authService and store
};
</script>

<style scoped>
.v-container {
  background-color: #f3efec; /* light beige background */
  min-height: 100vh;
}

.v-card {
  background-color: #f7f4f1;
}

.v-btn {
  color: white;
  font-weight: 600;
}

.v-btn:hover {
  opacity: 0.9;
}
</style>
