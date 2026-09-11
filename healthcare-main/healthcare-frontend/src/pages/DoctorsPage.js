import React from "react";
import Navbar from "../components/Navbar";
import Footer from "../components/Footer";
import Doctors from "../components/Doctors";

function DoctorsPage() {
    return (
        <div style={{ minHeight: "100vh", display: "flex", flexDirection: "column" }}>
            <Navbar />

            <main className="container" style={{ flex: 1, padding: "30px 16px" }}>
                <Doctors />
            </main>

            <Footer />
        </div>
    );
}

export default DoctorsPage;