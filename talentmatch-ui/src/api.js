import axios from "axios";

const API = axios.create({
    baseURL: "https://talentmatch-backend-1sb6.onrender.com"
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
        console.log(err.response);
        return Promise.reject(err);
    }
);
export default API;