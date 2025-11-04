import axios from "axios";

const API_URL = "http://localhost:8080/api/auth";

export const login = (email, password) => {
    // TODO: connect to backend
    return axios.post(`${API_URL}/login`, { email, password });
};
