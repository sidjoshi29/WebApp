import React from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { isUserLoggedIn, isAdminUser, isStaffUser, logout } from '../services/AuthService';
import '../styles/Header.css';
import ncsuLogo from '../assets/wolf-head.png';

/**
 * HeaderComponent: A React component that renders the header navigation bar.
 */
const HeaderComponent = () => {
	const isAuth = isUserLoggedIn(); // Check if the user is logged in
	const isAdmin = isAdminUser(); // Check if the user is an Admin
	const isStaff = isStaffUser(); // Check if the user is a Staff member
	const navigator = useNavigate();  // Hook to navigation to other pages

	/**
     * Logs the user out and redirects them to the login page.
     */
	function handleLogout() {
		logout();
		navigator('/login');
	}

	/**
     * Redirects the user to the appropriate home page based on their role.
     */
	function handleHomeClick() {
		if (isAuth) {
			if (isStaff) {
				navigator('/orders');
			} else if (isAdmin) {
				navigator('/users');
			} else {
				navigator('/items');
			}
		} else {
			navigator('/login');
		}
	}

	return (
		<header className="header">
			<div className="header-logo" onClick={handleHomeClick}>
				<img src={ncsuLogo} alt="NCSU Logo" className="header-logo-image" />
				<span className="header-title">WolfCafe</span>
			</div>
			<nav className="header-nav">
				{isAuth && isStaff && (
					<>
						<NavLink to='/items' className='header-link'>Menu</NavLink>
						<NavLink to='/inventory' className='header-link'>Inventory</NavLink>
         				<NavLink to='/orders' className='header-link'>Orders</NavLink>
						<NavLink to='/order-history' className='header-link'>Order History</NavLink>
					</>
				)}
				{/* {isAuth && isAdmin && (
					<NavLink to='/inventory' className='header-link'>Inventory</NavLink>
				)} */}
				{isAuth && !isAdmin && !isStaff && (
					<>
						<NavLink to='/items' className='header-link'>Menu</NavLink>
						<NavLink to='/my-orders' className='header-link'>My Orders</NavLink>
					</>
				)}
				{isAuth && isAdmin && (
					<NavLink to='/users' className='header-link'>Users</NavLink>
				)}
        		{isAuth && isAdmin && (
					<NavLink to='/tax-rate' className='header-link'>Tax Rate</NavLink>
				)}
				{isAuth ? (
					<NavLink to='/login' className='header-link' onClick={handleLogout}>Logout</NavLink>
				) : (
					<div className="auth-links">
						<NavLink to='/login' className='header-link'>Login</NavLink>
						<NavLink to='/register' className='register-button'>Sign Up</NavLink>
					</div>
				)}
			</nav>
		</header>
	);
}

export default HeaderComponent;
