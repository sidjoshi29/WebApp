import axios from "axios"

/** Base URL for the Ingredient API - Corresponds to methods in Backend's Ingredient Controller. */
const REST_API_BASE_URL = "http://localhost:8080/api/ingredients"

/** GET Ingredients - lists all ingredients */
export const listIngredients = () => axios.get(REST_API_BASE_URL)

/** POST Ingredient - creates a new ingredient */
export const createIngredient = (ingredient) => axios.post(REST_API_BASE_URL, ingredient)

/** GET Ingredient - gets a single ingredient by id */
export const getIngredient = (id) => axios.get(REST_API_BASE_URL + "/" + id)

/** DELETE Ingredient - deletes the ingredient with the given id */
export const deleteIngredient = (id) => axios.delete(REST_API_BASE_URL + "/" + id)
