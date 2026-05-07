import { useState } from "react";
import API from "../api";

function Register() {

    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");

    const handleRegister = async (e) => {
        e.preventDefault();

        try {

            await API.post("/auth/register", {
                username,
                password
            });

            alert("User registered successfully");

        } catch (err) {

            console.log(err);

            alert("Registration failed");
        }
    };

    return (
        <div>
            <h2>Register</h2>

            <form onSubmit={handleRegister}>

                <input
                    type="text"
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

                <button type="submit">
                    Register
                </button>

            </form>
        </div>
    );
}

export default Register;