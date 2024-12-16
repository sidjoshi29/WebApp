import React, { useEffect, useState } from "react";
import { isAdminUser } from "../services/AuthService";
import { getTaxRate, setTaxRate } from "../services/TaxRateService";
import '../App.css';

/**
 * TaxRateComponent: Allows admin users to view and update the tax rate.
 */
const TaxRateComponent = () => {
    const [isAdmin, setIsAdmin] = useState(false); // Checks is the user is the admin
    const [rate, setRate] = useState(""); // the rate for the tax
    const [loading, setLoading] = useState(true); // state to manage loading state

    /**
     * useEffect: Checks if the user is an admin and fetches the tax rate if true.
     */
    useEffect(() => {
        const checkAdminAndFetchTaxRate = async () => {
            const adminStatus = isAdminUser();
            setIsAdmin(adminStatus);

            if (adminStatus) {
                try {
                    const response = await getTaxRate();
                    // Multiply by 100 to display the percentage value
                    setRate((response.data.rate * 100).toFixed(2));
                } catch (error) {
                    console.error("Error fetching tax rate:", error);
                    alert("Failed to fetch tax rate.");
                }
            }

            setLoading(false);
        };

        checkAdminAndFetchTaxRate();
    }, []);

    if (!isAdmin && !loading) {
        return (
            <div className="container">
                <p className="text-center mt-5">You do not have permission to access this page.</p>
            </div>
        );
    }

    /**
     * Handles the form submission for updating the tax rate.
     * Validates the input and sends the new rate to the backend.
     * 
     * @param e - The form submission event
     */
    async function handleTaxForm(e) {
        e.preventDefault();

        const formattedRate = parseFloat(rate) / 100;

        if (isNaN(formattedRate) || formattedRate < 0 || formattedRate > 1) {
            alert("Please enter a valid rate between 0 and 100");
            return;
        }

        console.log("Tax Rate:", formattedRate);

        try {
            const response = await setTaxRate(formattedRate);
            if (response.status === 200) {
                alert("Tax rate updated successfully!");
            } else {
                alert("Failed to update tax rate.");
            }
        } catch (error) {
            console.error("Error setting tax rate:", error);
            alert("An error occurred while updating the tax rate.");
        }
    }

    return loading ? (
        <div className="container">
            <p className="text-center mt-5">Loading...</p>
        </div>
    ) : (
        <div className="container">
            <div className="login-card">
                <h2 className="text-center">Tax Rate</h2>
                <form onSubmit={handleTaxForm}>
                    <div className="form-group">
                        <input
                            type="text"
                            name="rate"
                            className="form-control"
                            placeholder="Enter Rate (e.g., 5 for 5%)"
                            value={rate}
                            onChange={(e) => setRate(e.target.value)}
                        />
                    </div>
                    <div className="form-group">
                        <button className="btn-login" type="submit">
                            Submit
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default TaxRateComponent;
