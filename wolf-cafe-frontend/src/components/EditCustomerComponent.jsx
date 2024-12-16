import React, { useState, useEffect } from "react";
import { useParams, useLocation, useNavigate } from 'react-router-dom';
import { getStaffById, editStaff } from '../services/AuthService';
import "../styles/EditUser.css";

/**
 * EditCustomerComponent: A React component to handle editing customer details.
 */
const EditCustomerComponent = () => {
  // Variable to retrieve id from the route
	const { id } = useParams();
  // Hook to navigate to other routes
	const navigate = useNavigate();
  // Hook to get current location and state
	const location = useLocation();
  // Retreive user data from props if passed
  const userFromProps = location.state?.user || {}; // Get the user if passed
  // State variable to store user name
	const [name, setName] = useState(userFromProps.name || '');
	// State variable to store user username
	const [username, setUsername] = useState(userFromProps.username || '');
	// State variable to store user email
	const [email, setEmail] = useState(userFromProps.email || '');
	// State variable to store user password
  const [password, setPassword] = useState(userFromProps.password || '');
  // State variable to store error messages
  const [error, setError] = useState('');

  // Fetch the user data only if no user was passed as props
  useEffect(() => {
    if (!userFromProps.name) {
      const fetchCustomerData = async () => {
        try {
          const response = await getStaffById(id);
          const { name, username, email, password } = response.data;
          setName(name);
          setUsername(username);
          setEmail(email);
          setPassword(password);
        } catch (err) {
          setError("Failed to load customer data.");
          console.error(err);
        }
      };
      fetchCustomerData();
    }
  }, [id, userFromProps]);

/**
   * Handles the form submission to edit customer details.
   *
   * @param e - The form submission event.
   */
  const editCustomerDetails = async (e) => {
    e.preventDefault();
    const customer = { name, username, email, password };
    try {
      await editStaff(id, customer);
      alert("Customer updated successfully!");
      navigate('/users');
    } catch (err) {
      setError("Error editing customer.");
      console.error(err);
    }
  };

  return (
	<div className="edit-user-container">
	      <div className="edit-user-card">
	        <h2 className="edit-user-heading">Edit Customer</h2>
	        {error && <div className="edit-user-alert-danger">{error}</div>}
	        <form onSubmit={editCustomerDetails}>
	          <div className="edit-user-form-group">
	            <label className="edit-user-form-label">Customer Name:</label>
	            <input
	              type="text"
	              className="edit-user-form-control"
	              placeholder="Enter Customer Name"
	              value={name}
	              onChange={(e) => setName(e.target.value)}
	              required
	            />
	          </div>
	          <div className="edit-user-form-group">
	            <label className="edit-user-form-label">Username:</label>
	            <input
	              type="text"
	              className="edit-user-form-control"
	              placeholder="Enter Username"
	              value={username}
	              onChange={(e) => setUsername(e.target.value)}
	              required
	            />
	          </div>
	          <div className="edit-user-form-group">
	            <label className="edit-user-form-label">Email:</label>
	            <input
	              type="email"
	              className="edit-user-form-control"
	              placeholder="Enter Email"
	              value={email}
	              onChange={(e) => setEmail(e.target.value)}
	              required
	            />
	          </div>
	          <button type="submit" className="edit-user-submit-btn">
	            Submit
	          </button>
	        </form>
	      </div>
	    </div>
  );
};

export default EditCustomerComponent;
