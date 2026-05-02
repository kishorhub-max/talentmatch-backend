import React, { useState, useEffect } from "react";
import API from "../api";

function Dashboard() {
    const [file, setFile] = useState(null);
    const [resumes, setResumes] = useState([]);
    const [result, setResult] = useState([]);

    const [searchSkill, setSearchSkill] = useState("");
    const [searchExp, setSearchExp] = useState("");

    const [name, setName] = useState("");
    const [email, setEmail] = useState("");
    const [skills, setSkills] = useState("");
    const [experience, setExperience] = useState("");

    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");
    const [message, setMessage] = useState("");

    const [editingResume, setEditingResume] = useState(null);
    const [editForm, setEditForm] = useState({
        name: "",
        email: "",
        skills: "",
        experience: 0
    });

    // ✅ Fetch resumes
    useEffect(() => {
        fetchResumes();
    }, []);

    const fetchResumes = async () => {
        try {
            const res = await API.get("/resumes");
            setResumes(res.data);
        } catch (err) {
            setError("Failed to fetch resumes");
        }
    };

    // ✅ Upload
    const handleUpload = async () => {
        if (!file) return alert("Select file first");

        setLoading(true);
        setError("");
        setMessage("");

        try {
            const formData = new FormData();
            formData.append("file", file);
            formData.append("name", name);
            formData.append("email", email);
            formData.append("skills", skills);
            formData.append("experience", experience);

            await API.post("/resumes/upload", formData, {
                headers: { "Content-Type": "multipart/form-data" }
            });

            setMessage("Uploaded successfully!");
            fetchResumes();
        } catch (err) {
            setError("Upload failed");
        }

        setLoading(false);
    };

    // ✅ Delete
    const handleDelete = async (id) => {
        if (!window.confirm("Are you sure?")) return;

        try {
            await API.delete(`/resumes/${id}`);
            setMessage("Deleted!");
            fetchResumes();
        } catch {
            setError("Delete failed");
        }
    };

    // ✅ Download
    const handleDownload = async (id) => {
        try {
            const res = await API.get(`/resumes/download/${id}`, {
                responseType: "blob"
            });

            const url = window.URL.createObjectURL(new Blob([res.data]));
            const link = document.createElement("a");
            link.href = url;
            link.setAttribute("download", "resume.pdf");
            document.body.appendChild(link);
            link.click();
        } catch {
            setError("Download failed");
        }
    };

    // ✅ Edit
    const startEdit = (resume) => {
        setEditingResume(resume);
        setEditForm({
            name: resume.name,
            email: resume.email,
            skills: resume.skills,
            experience: resume.experience
        });
    };

    const handleUpdate = async () => {
        try {
            await API.put(`/resumes/${editingResume.id}`, editForm);
            setMessage("Updated!");
            setEditingResume(null);
            fetchResumes();
        } catch {
            setError("Update failed");
        }
    };

    // ✅ Search
    const handleSearch = async () => {
        setLoading(true);
        try {
            const res = await API.get(
                `/resumes/match?skill=${searchSkill}&experience=${searchExp}`
            );
            setResult(res.data);
        } catch {
            setError("Search failed");
        }
        setLoading(false);
    };

    // ✅ Default Match
    const getMatches = async () => {
        setLoading(true);
        try {
            const res = await API.get(`/resumes/match?skill=java&experience=1`);
            setResult(res.data);
        } catch {
            setError("Match failed");
        }
        setLoading(false);
    };

    // ✅ Logout
    const handleLogout = () => {
        localStorage.removeItem("token");
        window.location.href = "/login";
    };

    return (
        <div style={{ maxWidth: 1000, margin: "auto", padding: 20 }}>
            <h2>TalentMatch AI</h2>

            <button onClick={handleLogout}>Logout</button>

            {loading && <p>Loading...</p>}
            {error && <p style={{ color: "red" }}>{error}</p>}
            {message && <p style={{ color: "green" }}>{message}</p>}

            <hr />

            {/* EDIT */}
            {editingResume && (
                <div>
                    <h3>Edit Resume</h3>

                    <input value={editForm.name}
                           onChange={(e) => setEditForm({ ...editForm, name: e.target.value })} />

                    <input value={editForm.email}
                           onChange={(e) => setEditForm({ ...editForm, email: e.target.value })} />

                    <input value={editForm.skills}
                           onChange={(e) => setEditForm({ ...editForm, skills: e.target.value })} />

                    <input type="number" value={editForm.experience}
                           onChange={(e) => setEditForm({ ...editForm, experience: e.target.value })} />

                    <button onClick={handleUpdate}>Update</button>
                    <button onClick={() => setEditingResume(null)}>Cancel</button>
                </div>
            )}

            <hr />

            {/* UPLOAD */}
            <h3>Add Resume</h3>

            <input type="file" onChange={(e) => setFile(e.target.files[0])} />
            <input placeholder="Name" onChange={(e) => setName(e.target.value)} />
            <input placeholder="Email" onChange={(e) => setEmail(e.target.value)} />
            <input placeholder="Skills" onChange={(e) => setSkills(e.target.value)} />
            <input type="number" placeholder="Experience" onChange={(e) => setExperience(e.target.value)} />

            <button onClick={handleUpload} disabled={loading}>
                {loading ? "Uploading..." : "Upload"}
            </button>

            <hr />

            {/* SEARCH */}
            <h3>Search</h3>
            <input value={searchSkill} onChange={(e) => setSearchSkill(e.target.value)} />
            <input type="number" value={searchExp} onChange={(e) => setSearchExp(e.target.value)} />
            <button onClick={handleSearch}>Search</button>

            <hr />

            {/* LIST */}
            <h3>Your Resumes</h3>
            {resumes.length === 0 ? <p>No resumes</p> : (
                <ul>
                    {resumes.map(r => (
                        <li key={r.id}>
                            {r.name} - {r.skills}
                            <button onClick={() => startEdit(r)}>Edit</button>
                            <button onClick={() => handleDownload(r.id)}>Download</button>
                            <button onClick={() => handleDelete(r.id)}>Delete</button>
                        </li>
                    ))}
                </ul>
            )}

            <hr />

            {/* MATCH */}
            <h3>Matches</h3>
            <button onClick={getMatches}>Find Matches</button>

            {!loading && result.length === 0 && <p>No matches found</p>}

            <div style={{ display: "grid", gridTemplateColumns: "repeat(3,1fr)", gap: 20 }}>
                {result.map((r, i) => (
                    <div key={i} style={{ border: "1px solid #ccc", padding: 10 }}>
                        <h4>{r?.summary?.name}</h4>
                        <p>{r?.summary?.skills}</p>
                        <p>Score: {r?.score}</p>

                        <button onClick={() => handleDownload(r.summary.id)}>
                            Download
                        </button>
                    </div>
                ))}
            </div>
        </div>
    );
}

export default Dashboard;