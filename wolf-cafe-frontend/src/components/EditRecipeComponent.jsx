import { useState, useEffect } from 'react';
import { getRecipe, updateRecipe } from '../services/RecipesService';
import { getInventory } from '../services/InventoryService'; // Import the getInventory service
import { useNavigate, useParams } from 'react-router-dom';

/**
 * EditRecipeComponent: A React component to edit an existing recipe.
 */
const EditRecipeComponent = () => {
    // State variables for recipe id
    const [id, setId] = useState(null);
    // State variables for recipe name
    const [name, setName] = useState("");
    // State variables for recipe price
    const [price, setPrice] = useState("");
    // State variables for recipe ingredients
    const [ingredients, setIngredients] = useState([]);
    // State variables for ingredient amounts
    const [ingredientAmount, setIngredientAmount] = useState("");
    const [availableIngredients, setAvailableIngredients] = useState([]); // Store available inventory ingredients
    const [selectedIngredient, setSelectedIngredient] = useState(""); // Selected ingredient from dropdown
    const [notFound, setNotFound] = useState(false); // Track if the recipe is not found

    const { incomingName } = useParams(); // Get recipe name from the route
    const navigate = useNavigate();// Hook for navigation
    const [errors, setErrors] = useState({
        general: "",
        name: "",
        price: "",
        ingredient: "",
        amount: ""
    }); // State to store validation errors

    //Fetch the recipe and available ingredients on component mount or when `incomingName` changes.
    useEffect(() => {
        if (incomingName) {
            getRecipe(incomingName).then(response => {
                setId(response.data.id);
                setName(response.data.name);
                setPrice(response.data.price);
                const ingredientList = Object.entries(response.data.ingredients || {}).map(([key, value]) => ({
                    name: key,
                    amount: value
                }));
                setIngredients(ingredientList);
                setNotFound(false);  // Recipe was found
            }).catch(error => {
                if (error.response && error.response.status === 404) {
                    setNotFound(true);  // Recipe not found, set state to true
                } else {
                    console.error("Error fetching recipe by name:", error);
                }
            });
        }

        // Fetch available ingredients from inventory
        getInventory().then(response => {
            const inventoryIngredients = Object.keys(response.data.ingredients); // Extract ingredient names
            setAvailableIngredients(inventoryIngredients); // Store available ingredients in state
        }).catch(error => {
            console.error("Error fetching inventory ingredients:", error);
        });
    }, [incomingName]);

    /**
     * Handle changes to ingredient fields (name or amount).
     *
     * @param index - Index of the ingredient in the list.
     * @param field - The field being updated ('name' or 'amount').
     * @param value - The new value for the field.
     */
    const handleIngredientChange = (index, field, value) => {
        // Update ingredients with new values
        setIngredients(prevIngredients => prevIngredients.map((ingredient, i) => {
            if (i === index) {
                return { ...ingredient, [field]: value };
            }
            return ingredient;
        }));

        // Validate ingredient amounts immediately upon change
        if (field === 'amount') {
            const newErrors = {...errors}; // Copy existing errors
            if (value <= 0 || isNaN(value)) {
                newErrors[`ingredientAmount${index}`] = "Amount must be a positive number.";
            } else {
                delete newErrors[`ingredientAmount${index}`]; // Clear error if valid
            }
            setErrors(newErrors);
        }
    };

    /**
     * Validate the form fields before submitting.
     *
     * @returns Whether the form is valid or not.
     */
    const validateForm = () => {
        let isValid = true;
        let newErrors = {};

        if (!name.trim()) {
            newErrors.name = "Recipe name is required.";
            isValid = false;
        }

        if (!price || isNaN(price) || price <= 0) {
            newErrors.price = "Valid recipe price is required.";
            isValid = false;
        }

        ingredients.forEach((ingredient, index) => {
            if (!ingredient.name.trim()) {
                newErrors[`ingredientName${index}`] = "Ingredient name is required.";
                isValid = false;
            }
            if (!ingredient.amount || isNaN(ingredient.amount) || parseFloat(ingredient.amount) <= 0) {
                newErrors[`ingredientAmount${index}`] = "Valid ingredient amount is required.";
                isValid = false;
            }
        });

        setErrors(newErrors);
        return isValid;
    };

     /**
     * Handle form submission to update the recipe.
     *
     * @param e - Form submission event.
     */
	const handleSubmit = (e) => {
	    e.preventDefault();
	    if (validateForm()) {
	        const ingredientsMap = ingredients.reduce((map, ingredient) => {
	            map[ingredient.name] = ingredient.amount;
	            return map;
	        }, {});

	        updateRecipe(id, { name, price, ingredients: ingredientsMap }).then(() => {
	            navigate("/recipes");
	        }).catch(error => {
	            if (error.response && error.response.status === 400 && error.response.data.includes('Recipe must have at least one ingredient')) {
	                setErrors(prevErrors => ({
	                    ...prevErrors,
	                    general: 'Recipe must contain at least one ingredient.'
	                }));
	            } else {
	                setErrors(prevErrors => ({
	                    ...prevErrors,
	                    general: 'Failed to update recipe. Please try again.'
	                }));
	            }
	        });
	    }
	};

    /**
     * Add a new ingredient to the recipe.
     */
    const addIngredient = () => {
        if (!selectedIngredient || !ingredientAmount || isNaN(ingredientAmount) || ingredientAmount <= 0) {
            setErrors(prevErrors => ({
                ...prevErrors,
                ingredient: "Valid ingredient and amount are required."
            }));
            return;
        }

        if (ingredients.some(ing => ing.name === selectedIngredient)) {
            setErrors(prevErrors => ({
                ...prevErrors,
                ingredient: "Ingredient already added."
            }));
            return;
        }

        setIngredients(prevIngredients => [...prevIngredients, { name: selectedIngredient, amount: ingredientAmount }]);
        setSelectedIngredient("");
        setIngredientAmount("");
        setErrors(prevErrors => ({ ...prevErrors, ingredient: "", amount: "" }));
    };

    /**
     * Removed an ingredient from the recipe.
     */
    const removeIngredient = (index) => {
        setIngredients(prevIngredients => prevIngredients.filter((_, i) => i !== index));
    };

    // If recipe not found, display error message
    if (notFound) {
        return (
            <div className="container">
                <h2 className="text-center text-danger">Recipe not found</h2>
                <p className="text-center">The recipe "{incomingName}" does not exist.</p>
            </div>
        );
    }

    return (
        <div className="container">
            <h2>Edit Recipe</h2>
            {errors.general && <div className="alert alert-danger">{errors.general}</div>}
            <form onSubmit={handleSubmit}>
                <div className="form-group">
                    <label>Recipe Name</label>
                    <input 
                        type="text" 
                        className="form-control" 
                        value={name} 
                        onChange={e => setName(e.target.value)} 
                    />
                    {errors.name && <div className="alert alert-danger">{errors.name}</div>}
                </div>
                <div className="form-group">
                    <label>Recipe Price</label>
                    <input 
                        type="text" 
                        className="form-control" 
                        value={price} 
                        onChange={e => setPrice(e.target.value)} 
                    />
                    {errors.price && <div className="alert alert-danger">{errors.price}</div>}
                </div>
                <div className="form-group">
                    <label>Ingredients</label>
                    {ingredients.map((ingredient, index) => (
                        <div key={index} className="mb-3">
                            <div className="d-flex align-items-center">
                                <input
                                    type="text"
                                    className="form-control me-2"
                                    value={ingredient.name}
                                    onChange={e => handleIngredientChange(index, 'name', e.target.value)}
                                    placeholder="Ingredient Name"
                                    disabled // Disable input to prevent manual changes
                                />
                                <input
                                    type="number"
                                    className="form-control me-2"
                                    value={ingredient.amount}
                                    onChange={e => handleIngredientChange(index, 'amount', e.target.value)}
                                    placeholder="Amount"
                                />
                                <button type="button" className="btn btn-danger" onClick={() => removeIngredient(index)}>
                                    Remove
                                </button>
                            </div>
                            {errors[`ingredientAmount${index}`] && (
                                <div className="alert alert-danger">
                                    {errors[`ingredientAmount${index}`]}
                                </div>
                            )}
                        </div>
                    ))}
                    <div className="d-flex align-items-center">
                        <select 
                            className="form-control me-2"
                            value={selectedIngredient}
                            onChange={e => setSelectedIngredient(e.target.value)}
                        >
                            <option value="">Select Ingredient</option>
                            {availableIngredients.map(ingredient => (
                                <option key={ingredient} value={ingredient}>
                                    {ingredient}
                                </option>
                            ))}
                        </select>
                        <input
                            type="number"
                            className="form-control me-2"
                            value={ingredientAmount}
                            onChange={e => setIngredientAmount(e.target.value)}
                            placeholder="Amount"
                        />
                        <button type="button" className="btn btn-primary" onClick={addIngredient}>
                            Add Ingredient
                        </button>
                    </div>
                    {errors.ingredient && <div className="alert alert-danger">{errors.ingredient}</div>}
                </div>
                <button type="submit" className="btn btn-success">Save Recipe</button>
            </form>
        </div>
    );
};

export default EditRecipeComponent;