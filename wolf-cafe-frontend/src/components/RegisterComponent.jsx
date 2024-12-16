import React, { useState } from 'react';
import { registerAPICall } from '../services/AuthService';
import '../styles/Register.css';

/**
 * RegisterComponent: Handles user registration, including form inputs and validation.
 */
const RegisterComponent = () => {
  const [name, setName] = useState(''); // Customer name
  const [username, setUsername] = useState(''); // Customer username
  const [email, setEmail] = useState(''); // Customer email
  const [password, setPassword] = useState(''); // Customer password
  const [confirmPassword, setConfirmPassword] = useState(''); // Customer confirm password
  const [error, setError] = useState(''); // State to store error messages
  const [success, setSuccess] = useState(''); // Success state

  /**
   * Handles the form submission for user registration.
   * Validates the inputs and sends the registration request to the server.
   * 
   * @param e - The form submission event
   */
  function handleRegistrationForm(e) {
    e.preventDefault();

    if (password !== confirmPassword) {
      setError("Passwords do not match.");
      setSuccess(''); // Clear success message if there’s an error
      return;
    }

    const register = { name, username, email, password };

    registerAPICall(register)
      .then((response) => {
        console.log(response.data);
        setSuccess("Registration successful! You can now log in."); // Set success message
        setError(''); // Clear any previous error
      })
      .catch((error) => {
        console.error(error);
        setError("Registration failed. Please try again."); // Set error message
        setSuccess(''); // Clear success message if there’s an error
      });
  }

  return (
    <div className="register-container">
      <div className="register-card">
        <h2 className="register-header">Sign Up</h2>
        <form className="register-form">
          <div className="form-group">
            <label>Name</label>
            <input
              type="text"
              name="name"
              className="form-control"
              placeholder="Enter name"
              value={name}
              onChange={(e) => setName(e.target.value)}
            />
          </div>

          <div className="form-group">
            <label>Username</label>
            <input
              type="text"
              name="username"
              className="form-control"
              placeholder="Enter username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
            />
          </div>

          <div className="form-group">
            <label>Email</label>
            <input
              type="email"
              name="email"
              className="form-control"
              placeholder="Enter email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
            />
          </div>

          <div className="form-group">
            <label>Password</label>
            <input
              type="password"
              name="password"
              className="form-control"
              placeholder="Enter password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
          </div>

          <div className="form-group">
            <label>Re-enter Password</label>
            <input
              type="password"
              name="confirmPassword"
              className="form-control"
              placeholder="Re-enter password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
            />
          </div>

          {/* Display error or success message */}
          {error && <p className="error-text">{error}</p>}
          {success && <p className="success-text">{success}</p>}

          <button className="btn-primary" onClick={handleRegistrationForm}>
            Submit
          </button>
        </form>
      </div>
    </div>
  );
};

export default RegisterComponent;
