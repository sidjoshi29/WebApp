import axios from "axios";
//Generative AI used
/**
 * Creates an Axios instance specifically for interacting with the Unsplash API to generate images for items
 *
 */
const unsplashClient = axios.create({
  baseURL: "https://api.unsplash.com/",
  headers: {
    Authorization: `Client-ID ${import.meta.env.VITE_UNSPLASH_API_KEY}`, // Use Client-ID
  },
});

export default unsplashClient;
