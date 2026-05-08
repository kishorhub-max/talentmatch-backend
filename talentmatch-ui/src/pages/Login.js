import React, { useState } from "react";
import API from "../api";


function Login() {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");

    const handleLogin = async () => {
        try {
            const res = await API.post("/auth/login", {                username,
                password
            });

            localStorage.setItem("token", res.data.token);

            alert("Login successful!");
            window.location.href = "/dashboard"; // ✅ redirect

        } catch (err) {
            console.error(err);
            alert("Login failed");
        }
    };

    return (
        <div style={{ padding: 20 }}>
            <h2>Login</h2>

            <input
                placeholder="Username"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
            />

            <br /><br />

            <input
                type="password"
                placeholder="Password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
            />

            <br /><br />

            <button onClick={handleLogin}>Login</button>
        </div>
    );
}

export default Login;