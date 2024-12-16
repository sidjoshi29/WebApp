import axios from "axios";
import { getToken } from "./AuthService";

// Base URL for item-related API calls
const BASE_REST_API_URL = "http://localhost:8080/api/items";

axios.interceptors.request.use(
  function (config) {
    config.headers["Authorization"] = getToken();
    return config;
  },
  function (error) {
    // Do something with request error
    return Promise.reject(error);
  }
);

/**
 * Save a new item.
 * @param item - The item details to save.
 */
export const saveItem = (item) => axios.post(BASE_REST_API_URL, item);

/**
 * Fetch a single item by its ID.
 * @param id - The ID of the item to fetch.
 */
export const getItemById = (id) => axios.get(BASE_REST_API_URL + "/" + id);

/**
 * Fetch all items.
 */
export const getAllItems = () => axios.get(BASE_REST_API_URL);

/**
 * Update an existing item.
 * @param id - The ID of the item to update.
 * @param item - The updated item details.
 */
export const updateItem = (id, item) =>
  axios.put(BASE_REST_API_URL + "/" + id, item);

/**
 * Delete an item by its ID.
 * @param  id - The ID of the item to delete.
 */
export const deleteItemById = (id) =>
  axios.delete(BASE_REST_API_URL + "/" + id);
