import axios from "axios";

// Base URL for authentication-related API calls
const AUTH_REST_API_BASE_URL = "http://localhost:8080/api/auth";

/**
 * Sends a registration request to the server.
 * @param registerObj - The registration details (name, username, email, password).
 */
export const registerAPICall = (registerObj) =>
  axios.post(AUTH_REST_API_BASE_URL + "/register", registerObj);

/**
 * Sends a login request to the server.
 * @param usernameOrEmail - The username or email of the user.
 * @param password - The password of the user.
 */
export const loginAPICall = (usernameOrEmail, password) =>
  axios.post(AUTH_REST_API_BASE_URL + "/login", { usernameOrEmail, password });

/**
 * Stores the authentication token in local storage.
 * @param token - The JWT token received from the server.
 */
export const storeToken = (token) => localStorage.setItem("token", token);

/**
 * Retrieves the authentication token from local storage.
 * @returns The stored token or null if not present.
 */
export const getToken = () => localStorage.getItem("token");

/**
 * Saves the logged-in user's information (username and role) in session storage.
 * @param username - The username of the authenticated user.
 * @param role - The role of the authenticated user.
 */
export const saveLoggedInUser = (username, role) => {
  sessionStorage.setItem("authenticatedUser", username);
  sessionStorage.setItem("role", role);
};

/**
 * Checks if a user is logged in by verifying session storage data.
 * @returns True if a user is logged in, false otherwise.
 */
export const isUserLoggedIn = () => {
  const username = sessionStorage.getItem("authenticatedUser");

  if (username == null) return false;
  else return true;
};

/**
 * Retrieves the logged-in user's username from session storage.
 * @returns The username of the logged-in user or null if not logged in.
 */
export const getLoggedInUser = () => {
  const username = sessionStorage.getItem("authenticatedUser");
  return username;
};

/**
 * Logs out the user by clearing both local and session storage.
 */
export const logout = () => {
  localStorage.clear();
  sessionStorage.clear();
};

/**
 * Checks if the logged-in user is an admin.
 * @returns True if the user's role is 'ROLE_ADMIN', false otherwise.
 */
export const isAdminUser = () => {
  let role = sessionStorage.getItem("role");
  return role != null && role == "ROLE_ADMIN";
};

/**
 * Checks if the logged-in user is a staff member.
 * @returns True if the user's role is 'ROLE_STAFF', false otherwise.
 */
export const isStaffUser = () => {
  let role = sessionStorage.getItem("role");
  return role === "ROLE_STAFF";
};

/**
 * Fetches all users from the server.
 */
export const getAllUsers = () => {
  const token = getToken();
  return axios.get(`${AUTH_REST_API_BASE_URL}`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
};

/**
 * Adds a new staff member to the system.
 * @param staffMember - The staff member details (name, username, email, password).
 */
export const saveStaff = (staffMember) => {
  const token = getToken();
  return axios.post(AUTH_REST_API_BASE_URL + "/user", staffMember, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
};

/**
 * Deletes a user by their ID.
 * @param id - The ID of the user to delete.
 */
export const deleteUserById = (id) => {
  const token = getToken();
  return axios.delete(`${AUTH_REST_API_BASE_URL}/user/${id}`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
};

/**
 * Updates a staff member's details by their ID.
 * @param  id - The ID of the staff member to update.
 * @param  staffMember - The updated staff member details.
 */
export const editStaff = (id, staffMember) => {
  const token = getToken();
  return axios.put(`${AUTH_REST_API_BASE_URL}/user/${id}`, staffMember, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
};

/**
 * Fetches a staff member's details by their ID.
 * @param id - The ID of the staff member to fetch.
 */
export const getStaffById = (id) => {
  const token = getToken();
  return axios.get(`${AUTH_REST_API_BASE_URL}/user/${id}`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
};
