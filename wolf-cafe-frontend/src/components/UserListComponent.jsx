import React, { useEffect, useState } from "react";
import { getAllUsers, isAdminUser, deleteUserById } from "../services/AuthService";
import '../styles/UserList.css';
import { useNavigate } from 'react-router-dom'

/**
 * UserListComponent: Displays lists of customers and staff members for admin users.
 */
const UserListComponent = () => {
	const [staff, setStaff] = useState([]); // state to store staff members
	const [users, setUsers] = useState([]); // state to store customers
	const [error, setError] = useState(""); // state to store error mesages
	const navigate = useNavigate(); // hook to navigate to other pages
	const isAdmin = isAdminUser(); // checks if the user is an admin
	useEffect(() => {
		if (isAdmin) {
			fetchUsers();
		} else {
			setError("Access Denied. Only admin users can view this page.");
		}
	}, []);

	/**
     * Fetch users from the API and separate them into staff and customers.
     */
	const fetchUsers = async () => {
		try {
			const response = await getAllUsers();
			console.log(response.data)
			// Filter response data into staff and regular users
			const staffMembers = response.data.filter(user =>
				user.roles.some(role => role.name !== "ROLE_CUSTOMER")
			);
			const regularUsers = response.data.filter(user =>
				user.roles.some(role => role.name === "ROLE_CUSTOMER")
			);
			setStaff(staffMembers);
			setUsers(regularUsers);
		} catch (err) {
			setError("Failed to fetch users. Please check your connection or login status.");
			console.error(err);
		}
	};
	/**
     * Navigate to the Add Staff page.
     */
	function addNewStaff() {
		navigate('/add-staff');
	}
	/**
     * Navigate to the Edit Staff page for the selected staff member.
     * @param user - The staff member to edit
     */
	function editNewStaff(user) {
			  navigate(`/edit-staff/${user.id}`, { state: { user } });
	}
	 /**
     * Navigate to the Edit Customer page for the selected customer.
     * @param user - The customer to edit
     */
	function editNewCustomer(user) {
				  navigate(`/edit-customer/${user.id}`, { state: { user } });
	}
	/**
     * Delete a user by their ID and refresh the user list.
     * @param id - The ID of the user to delete
     */
	function deleteUser(id) {
		deleteUserById(id).then((response) => {
			fetchUsers();
			alert("User deleted successfully.");
		}).catch(error => {
			console.error(error);
		});
	}
	if (error) {
		return <div className="error-message">{error}</div>;
	}
	
	const createStaff = async () => {
	        try {
	         
	        } catch (err) {
	          setError("Failed to fetch users. Please check your connection or login status.");
	          console.error(err);
	        }
	      };

	    if (error) {
	        return <div className="error-message">{error}</div>;
	    }
	
	
	return (
		<div className="user-list-container">
		    <div className="user-list-table-wrapper">
		        <h2 className="user-list-heading">Customer List</h2>
		        <table className="user-list-table">
		            <thead>
		                <tr>
		                    <th className="user-list-th">Name</th>
		                    <th className="user-list-th">Username</th>
		                    <th className="user-list-th">Email</th>
		                    <th className="user-list-th">Edit</th>
		                </tr>
		            </thead>
		            <tbody className="user-list-tbody">
		                {users.length > 0 ? (
		                    users.map((user) => (
		                        <tr key={user.id}>
		                            <td className="user-list-td">{user.name}</td>
		                            <td className="user-list-td">{user.username}</td>
		                            <td className="user-list-td">{user.email}</td>
		                            <td className="user-list-td">
		                                {isAdmin && user.name !== "Admin User" && (
		                                    <>
		                                        <button
		                                            className="user-list-button user-list-button-update"
		                                            onClick={() => editNewCustomer(user)}
		                                        >
		                                            Edit
		                                        </button>
		                                        <button
		                                            className="user-list-button user-list-button-delete"
		                                            onClick={() => deleteUser(user.id)}
		                                        >
		                                            Delete
		                                        </button>
		                                    </>
		                                )}
		                            </td>
		                        </tr>
		                    ))
		                ) : (
		                    <tr>
		                        <td className="user-list-td" colSpan="4">No users found</td>
		                    </tr>
		                )}
		            </tbody>
		        </table>
		    </div>

		    <div className="user-list-table-wrapper">
		        <h2 className="user-list-heading">Staff List</h2>
		        <table className="user-list-table">
		            <thead>
		                <tr>
		                    <th className="user-list-th">Name</th>
		                    <th className="user-list-th">Username</th>
		                    <th className="user-list-th">Email</th>
		                    <th className="user-list-th">Edit</th>
		                </tr>
		            </thead>
		            <tbody className="user-list-tbody">
		                {staff.length > 0 ? (
		                    staff.map((staffMember) => (
		                        <tr key={staffMember.id}>
		                            <td className="user-list-td">{staffMember.name}</td>
		                            <td className="user-list-td">{staffMember.username}</td>
		                            <td className="user-list-td">{staffMember.email}</td>
		                            <td className="user-list-td">
		                                {isAdmin && staffMember.name !== "Admin User" && (
		                                    <>
		                                        <button
		                                            className="user-list-button user-list-button-update"
		                                            onClick={() => editNewStaff(staffMember)}
		                                        >
		                                            Edit
		                                        </button>
		                                        <button
		                                            className="user-list-button user-list-button-delete"
		                                            onClick={() => deleteUser(staffMember.id)}
		                                        >
		                                            Delete
		                                        </button>
		                                    </>
		                                )}
		                            </td>
		                        </tr>
		                    ))
		                ) : (
		                    <tr>
		                        <td className="user-list-td" colSpan="4">No staff members found</td>
		                    </tr>
		                )}
		            </tbody>
		        </table>
		    

		    {isAdmin && (
		        <button className="user-list-update-inventory-button" onClick={addNewStaff}>
		            Add Staff Member
		        </button>
		    )}
			</div>
		</div>
	);
};
export default UserListComponent;

