import axios from "axios";

/*
 * API endpoint for tax rate backend
 */
const API_BASE_URL = "http://localhost:8080/api";

/*
 * Gets the admin token for authorization
 */
export const getToken = () => localStorage.getItem("token");

/**
 * Retrieves the tax rate (accessible by admin only).
 */
export const getTaxRate = () => {
  return axios.get(API_BASE_URL + "/taxRate");
};

/**
 * Set the tax rate (accessible by admin only).
 */
export const setTaxRate = (rate) => {
  const token = getToken();
  return axios.put(API_BASE_URL + "/taxRate", rate, {
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
  });
};
