import React from "react";
import { useNavigate } from "react-router-dom";
import Navbar from "../components/Navbar";
import Footer from "../components/Footer";

const departments = [
    {
        name: "Cardiology & Cardiac Sciences",
        description: "Specialized care for heart diseases, blood pressure, and cardiac conditions."
    },
    {
        name: "Obstetrics & Gynaecology",
        description: "Comprehensive women's health, maternity, pregnancy, and reproductive care."
    },
    {
        name: "Orthopaedics & Joint Replacement",
        description: "Treatment for bone, joint, muscle, fractures, arthritis, and mobility-related conditions."
    },
    {
        name: "Internal Medicine",
        description: "Diagnosis and treatment of general illnesses, infections, diabetes, and other medical conditions."
    },
    {
        name: "ENT (Ear Nose Throat)",
        description: "Specialized treatment for ear, nose, throat, sinus, and hearing-related conditions."
    },
    {
        name: "Nephrology & Kidney Transplant",
        description: "Medical care for kidney diseases, renal conditions, and kidney health."
    },
    {
        name: "Endocrinology & Diabetology",
        description: "Care for diabetes, thyroid disorders, hormones, and metabolic conditions."
    },
    {
        name: "Medical Oncology & Cancer Care",
        description: "Specialized diagnosis, treatment, and management of cancer-related conditions."
    }
];

function DepartmentsPage() {
    const navigate = useNavigate();

    const handleViewDoctors = (department) => {
        navigate(`/doctors?dept=${encodeURIComponent(department)}`);
    };

    return (
        <div style={{ minHeight: "100vh", display: "flex", flexDirection: "column" }}>
            <Navbar />

            <main className="container" style={{ flex: 1, padding: "30px 16px" }}>
                <div style={{ marginBottom: "24px" }}>
                    <h1 style={{ fontSize: "24px", color: "#0f172a", marginBottom: "6px" }}>
                        Hospital Departments
                    </h1>
                    <p style={{ color: "#64748b", fontSize: "14px" }}>
                        Explore our medical departments and find doctors based on your healthcare needs.
                    </p>
                </div>

                <div
                    style={{
                        display: "grid",
                        gridTemplateColumns: "repeat(auto-fill, minmax(300px, 1fr))",
                        gap: "18px"
                    }}
                >
                    {departments.map((department) => (
                        <div
                            key={department.name}
                            className="card"
                            style={{
                                padding: "20px",
                                display: "flex",
                                flexDirection: "column",
                                justifyContent: "space-between"
                            }}
                        >
                            <div>
                                <h2
                                    style={{
                                        fontSize: "17px",
                                        color: "#0284c7",
                                        marginBottom: "10px"
                                    }}
                                >
                                    {department.name}
                                </h2>

                                <p
                                    style={{
                                        fontSize: "13px",
                                        color: "#475569",
                                        lineHeight: "1.5",
                                        marginBottom: "16px"
                                    }}
                                >
                                    {department.description}
                                </p>
                            </div>

                            <button
                                className="btn btn-primary btn-sm"
                                onClick={() => handleViewDoctors(department.name)}
                                style={{ width: "100%" }}
                            >
                                View Doctors
                            </button>
                        </div>
                    ))}
                </div>
            </main>

            <Footer />
        </div>
    );
}

export default DepartmentsPage;