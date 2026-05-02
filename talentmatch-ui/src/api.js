import axios from "axios";

const API = axios.create({
    baseURL: "http://localhost:8080"
});

// ✅ Attach token automatically
API.interceptors.request.use((req) => {
    const token = localStorage.getItem("token");

    if (token) {
        req.headers.Authorization = `Bearer ${token}`;
    }

    return req;
});

// ✅ Auto logout on 403
API.interceptors.response.use(
    (res) => res,
    (err) => {
        if (err.response?.status === 403) {
            localStorage.removeItem("token");
            window.location.href = "/login";
        }
        return Promise.reject(err);
    }
);

export default API;