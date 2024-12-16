import React, { useState } from 'react';
import { saveStaff } from '../services/AuthService';
import { useNavigate } from 'react-router-dom';
import '../styles/AddStaff.css';

/**
 * AddStaffComponent: A React component to handle adding a new staff member.
 */
const AddStaffComponent = () => {
    // State variable for staff name
    const [name, setName] = useState('');
    // State variable for staff username
    const [username, setUsername] = useState('');
    // State variable for staff email
    const [email, setEmail] = useState('');
    // State variable for staff password
    const [password, setPassword] = useState('');
    // State variable for error messages
    const [error, setError] = useState('');
    // Hook to navigate to another page
    const navigate = useNavigate();

    /**
     * Handles the form submission to add a new staff member.
     * 
     * @param e - The form submission event.
     */
    const saveNewStaff = async (e) => {
        e.preventDefault();

        const staffMember = { name, username, email, password };
        
        try {
            await saveStaff(staffMember);
            alert("Staff added successfully!");
            navigate('/users'); 
        } catch (error) {
            setError("Error adding staff.");
            console.error("Error adding staff:", error);
        }
    };

    return (
        <div className="add-staff-container">
            <div className="add-staff-card">
                <h2 className="add-staff-heading">Add Staff Member</h2>
                {error && <div className="add-staff-error-message">{error}</div>}
                <form onSubmit={saveNewStaff}>
                    <div className="add-staff-form-group">
                        <label className="add-staff-form-label">Staff Name:</label>
                        <input 
                            type="text"
                            className="add-staff-form-control"
                            placeholder="Enter Staff Name"
                            value={name}
                            onChange={(e) => setName(e.target.value)}
                            required
                        />
                    </div>

                    <div className="add-staff-form-group">
                        <label className="add-staff-form-label">Username:</label>
                        <input 
                            type="text"
                            className="add-staff-form-control"
                            placeholder="Enter Username"
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                            required
                        />
                    </div>

                    <div className="add-staff-form-group">
                        <label className="add-staff-form-label">Email:</label>
                        <input 
                            type="email"
                            className="add-staff-form-control"
                            placeholder="Enter Email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            required
                        />
                    </div>

                    <div className="add-staff-form-group">
                        <label className="add-staff-form-label">Password:</label>
                        <input 
                            type="password"
                            className="add-staff-form-control"
                            placeholder="Enter Password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            required
                        />
                    </div>

                    <button type="submit" className="add-staff-submit-btn">
                        Submit
                    </button>
                </form>
            </div>
        </div>
    );
};

export default AddStaffComponent;
