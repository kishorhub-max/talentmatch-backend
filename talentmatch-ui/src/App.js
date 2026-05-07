import React from "react";
import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";
import Register from "./pages/Register";
import Login from "./pages/Login";
import Dashboard from "./pages/Dashboard";

function App() {
    const token = localStorage.getItem("token");

    return (
        <Router>
            <Routes>

                {/* Default route */}
                <Route
                    path="/"
                    element={token ? <Navigate to="/dashboard" /> : <Navigate to="/login" />}
                />

                {/* Login Page */}
                <Route path="/login" element={<Login />} />
                <Route path="/register" element={<Register />} />


                {/* Protected Dashboard */}
                <Route
                    path="/dashboard"
                    element={token ? <Dashboard /> : <Navigate to="/login" />}
                />

            </Routes>
        </Router>
    );
}

export default App;