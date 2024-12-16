import React, { useState } from 'react';
import { loginAPICall, saveLoggedInUser, storeToken } from '../services/AuthService';
import { useNavigate } from 'react-router-dom';
import '../styles/Login.css';

/**
 * LoginComponent: A React component for user login.
 */
const LoginComponent = () => {
	const [usernameOrEmail, setUsernameOrEmail] = useState(''); // User input for username/email
	const [password, setPassword] = useState(''); // User input for password
	const [error, setError] = useState(''); // Error state
	const navigate = useNavigate(); // hook to navigate to other pages

	/**
	 * Handles the form submission for login.
	 * 
	 * @param e - The form submission event.
	 */
	async function handleLoginForm(e) {
		e.preventDefault();
		const loginObj = { usernameOrEmail, password };

		await loginAPICall(usernameOrEmail, password)
			.then((response) => {
				const token = 'Bearer ' + response.data.accessToken;
				const role = response.data.role;

				// Store the token and logged-in user information
				storeToken(token);
				saveLoggedInUser(usernameOrEmail, role);

				// Redirect based on user role
				if (role === 'ROLE_ADMIN') {
					navigate('/users');
				} else if (role === 'ROLE_STAFF') {
					navigate('/inventory');
				} else {
					navigate('/items'); // Redirect customers to the menu page
				}
				setError(''); // Clear any previous errors on successful login
			})
			.catch((error) => {
				console.error('Login error: ', error);
				setError('Invalid username or password. Please try again.'); // Set error message
			});
	}

	return (
		<div className='container'>
			<div className='login-card'>
				<h2 className='text-center'>Login</h2>
				<form onSubmit={handleLoginForm}>
					{error && <p className="error-message">{error}</p>} {/* Display error message if present */}
					<div className='form-group'>
						<label>Username or Email</label>
						<input
							type='text'
							name='usernameOrEmail'
							className='form-control'
							placeholder='Enter username or email'
							value={usernameOrEmail}
							onChange={(e) => setUsernameOrEmail(e.target.value)}
						/>
					</div>
					<div className='form-group'>
						<label>Password</label>
						<input
							type='password'
							name='password'
							className='form-control'
							placeholder='Enter password'
							value={password}
							onChange={(e) => setPassword(e.target.value)}
						/>
					</div>
					<div className='form-group'>
						<button type='submit' className='btn-login'>Submit</button>
					</div>
				</form>
			</div>
		</div>
	);
};

export default LoginComponent;
