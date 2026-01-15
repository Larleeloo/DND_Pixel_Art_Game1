package entity;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * RecipeManager handles loading and querying alchemy/crafting recipes.
 *
 * Recipes are loaded from data/alchemy_recipes.json and support:
 * - 1 to 3 ingredient combinations
 * - Multiple recipes for the same output
 * - Reversible recipes for deconstruction
 * - Order-independent ingredient matching
 *
 * Usage:
 *   RecipeManager.initialize();
 *   Recipe recipe = RecipeManager.findRecipe(ingredients);
 *   List<Recipe> reverseRecipes = RecipeManager.findReverseRecipes(itemId);
 */
public class RecipeManager {

    private static final String RECIPES_FILE = "data/alchemy_recipes.json";
    private static List<Recipe> recipes = new ArrayList<>();
    private static boolean initialized = false;

    /**
     * Represents a single crafting recipe.
     */
    public static class Recipe {
        public String id;
        public String name;
        public List<String> ingredients;
        public String result;
        public int resultCount;
        public String category;
        public boolean reversible;

        public Recipe() {
            ingredients = new ArrayList<>();
            resultCount = 1;
            reversible = false;
        }

        /**
         * Checks if the given ingredients match this recipe (order-independent).
         */
        public boolean matches(List<String> inputIngredients) {
            if (inputIngredients == null || inputIngredients.size() != ingredients.size()) {
                return false;
            }

            // Create sorted copies for comparison
            List<String> sortedRecipe = new ArrayList<>(ingredients);
            List<String> sortedInput = new ArrayList<>(inputIngredients);
            Collections.sort(sortedRecipe);
            Collections.sort(sortedInput);

            return sortedRecipe.equals(sortedInput);
        }

        /**
         * Gets the ingredients needed to reverse this recipe (deconstruct the result).
         * Returns null if recipe is not reversible.
         */
        public List<String> getReverseIngredients() {
            if (!reversible) return null;
            return new ArrayList<>(ingredients);
        }

        @Override
        public String toString() {
            return name + " (" + String.join(" + ", ingredients) + " -> " + resultCount + "x " + result + ")";
        }
    }

    /**
     * Initializes the recipe manager by loading recipes from JSON.
     */
    public static void initialize() {
        if (initialized) return;

        loadRecipes();
        initialized = true;
        System.out.println("RecipeManager: Loaded " + recipes.size() + " recipes");
    }

    /**
     * Loads recipes from the JSON file.
     */
    private static void loadRecipes() {
        recipes.clear();

        try {
            String jsonContent = new String(Files.readAllBytes(Paths.get(RECIPES_FILE)), StandardCharsets.UTF_8);
            parseRecipesJson(jsonContent);
        } catch (IOException e) {
            System.err.println("RecipeManager: Failed to load recipes file: " + e.getMessage());
            // Create some default recipes if file not found
            createDefaultRecipes();
        }
    }

    /**
     * Simple JSON parser for recipes (avoids external dependencies).
     */
    private static void parseRecipesJson(String json) {
        // Find the recipes array
        int recipesStart = json.indexOf("\"recipes\"");
        if (recipesStart == -1) return;

        int arrayStart = json.indexOf('[', recipesStart);
        int arrayEnd = findMatchingBracket(json, arrayStart);
        if (arrayStart == -1 || arrayEnd == -1) return;

        String recipesArray = json.substring(arrayStart + 1, arrayEnd);

        // Parse each recipe object
        int objStart = 0;
        while ((objStart = recipesArray.indexOf('{', objStart)) != -1) {
            int objEnd = findMatchingBrace(recipesArray, objStart);
            if (objEnd == -1) break;

            String recipeJson = recipesArray.substring(objStart, objEnd + 1);
            Recipe recipe = parseRecipeObject(recipeJson);
            if (recipe != null && recipe.result != null && !recipe.ingredients.isEmpty()) {
                recipes.add(recipe);
            }

            objStart = objEnd + 1;
        }
    }

    /**
     * Parses a single recipe JSON object.
     */
    private static Recipe parseRecipeObject(String json) {
        Recipe recipe = new Recipe();

        recipe.id = extractStringValue(json, "id");
        recipe.name = extractStringValue(json, "name");
        recipe.result = extractStringValue(json, "result");
        recipe.category = extractStringValue(json, "category");
        recipe.resultCount = extractIntValue(json, "resultCount", 1);
        recipe.reversible = extractBooleanValue(json, "reversible", false);

        // Parse ingredients array
        int ingredientsStart = json.indexOf("\"ingredients\"");
        if (ingredientsStart != -1) {
            int arrayStart = json.indexOf('[', ingredientsStart);
            int arrayEnd = findMatchingBracket(json, arrayStart);
            if (arrayStart != -1 && arrayEnd != -1) {
                String ingredientsStr = json.substring(arrayStart + 1, arrayEnd);
                String[] parts = ingredientsStr.split(",");
                for (String part : parts) {
                    String ingredient = part.trim();
                    // Remove quotes
                    ingredient = ingredient.replace("\"", "").trim();
                    if (!ingredient.isEmpty()) {
                        recipe.ingredients.add(ingredient);
                    }
                }
            }
        }

        return recipe;
    }

    /**
     * Extracts a string value from a JSON snippet.
     */
    private static String extractStringValue(String json, String key) {
        String searchKey = "\"" + key + "\"";
        int keyIndex = json.indexOf(searchKey);
        if (keyIndex == -1) return null;

        int colonIndex = json.indexOf(':', keyIndex);
        if (colonIndex == -1) return null;

        int quoteStart = json.indexOf('"', colonIndex + 1);
        if (quoteStart == -1) return null;

        int quoteEnd = json.indexOf('"', quoteStart + 1);
        if (quoteEnd == -1) return null;

        return json.substring(quoteStart + 1, quoteEnd);
    }

    /**
     * Extracts an integer value from a JSON snippet.
     */
    private static int extractIntValue(String json, String key, int defaultValue) {
        String searchKey = "\"" + key + "\"";
        int keyIndex = json.indexOf(searchKey);
        if (keyIndex == -1) return defaultValue;

        int colonIndex = json.indexOf(':', keyIndex);
        if (colonIndex == -1) return defaultValue;

        StringBuilder numStr = new StringBuilder();
        for (int i = colonIndex + 1; i < json.length(); i++) {
            char c = json.charAt(i);
            if (Character.isDigit(c) || c == '-') {
                numStr.append(c);
            } else if (numStr.length() > 0) {
                break;
            }
        }

        if (numStr.length() == 0) return defaultValue;

        try {
            return Integer.parseInt(numStr.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Extracts a boolean value from a JSON snippet.
     */
    private static boolean extractBooleanValue(String json, String key, boolean defaultValue) {
        String searchKey = "\"" + key + "\"";
        int keyIndex = json.indexOf(searchKey);
        if (keyIndex == -1) return defaultValue;

        int colonIndex = json.indexOf(':', keyIndex);
        if (colonIndex == -1) return defaultValue;

        String afterColon = json.substring(colonIndex + 1, Math.min(colonIndex + 20, json.length())).trim().toLowerCase();
        if (afterColon.startsWith("true")) return true;
        if (afterColon.startsWith("false")) return false;

        return defaultValue;
    }

    /**
     * Finds the matching closing bracket for an opening bracket.
     */
    private static int findMatchingBracket(String str, int openIndex) {
        if (openIndex == -1 || str.charAt(openIndex) != '[') return -1;

        int depth = 1;
        for (int i = openIndex + 1; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '[') depth++;
            else if (c == ']') {
                depth--;
                if (depth == 0) return i;
            }
        }
        return -1;
    }

    /**
     * Finds the matching closing brace for an opening brace.
     */
    private static int findMatchingBrace(String str, int openIndex) {
        if (openIndex == -1 || str.charAt(openIndex) != '{') return -1;

        int depth = 1;
        boolean inString = false;
        for (int i = openIndex + 1; i < str.length(); i++) {
            char c = str.charAt(i);

            if (c == '"' && (i == 0 || str.charAt(i - 1) != '\\')) {
                inString = !inString;
            } else if (!inString) {
                if (c == '{') depth++;
                else if (c == '}') {
                    depth--;
                    if (depth == 0) return i;
                }
            }
        }
        return -1;
    }

    /**
     * Creates default recipes if JSON file is not found.
     */
    private static void createDefaultRecipes() {
        Recipe bow = new Recipe();
        bow.id = "wooden_bow";
        bow.name = "Wooden Bow";
        bow.ingredients.add("string");
        bow.ingredients.add("planks");
        bow.ingredients.add("arrow");
        bow.result = "wooden_bow";
        bow.resultCount = 1;
        bow.category = "weapons";
        bow.reversible = true;
        recipes.add(bow);

        Recipe sword = new Recipe();
        sword.id = "iron_sword";
        sword.name = "Iron Sword";
        sword.ingredients.add("iron_ingot");
        sword.ingredients.add("iron_ingot");
        sword.ingredients.add("planks");
        sword.result = "iron_sword";
        sword.resultCount = 1;
        sword.category = "weapons";
        sword.reversible = true;
        recipes.add(sword);

        System.out.println("RecipeManager: Created " + recipes.size() + " default recipes");
    }

    // ==================== Public API ====================

    /**
     * Finds a recipe that matches the given ingredients (order-independent).
     *
     * @param ingredients List of item IDs (1-3 items)
     * @return Matching recipe, or null if no match found
     */
    public static Recipe findRecipe(List<String> ingredients) {
        initialize();

        if (ingredients == null || ingredients.isEmpty()) return null;

        // Filter out null/empty ingredients
        List<String> validIngredients = new ArrayList<>();
        for (String ing : ingredients) {
            if (ing != null && !ing.isEmpty()) {
                validIngredients.add(ing);
            }
        }

        if (validIngredients.isEmpty()) return null;

        for (Recipe recipe : recipes) {
            if (recipe.matches(validIngredients)) {
                return recipe;
            }
        }

        return null;
    }

    /**
     * Finds a recipe that matches the given ingredients.
     * Convenience method for varargs.
     */
    public static Recipe findRecipe(String... ingredients) {
        return findRecipe(Arrays.asList(ingredients));
    }

    /**
     * Finds all reversible recipes that produce the given item.
     * Used by the reverse crafting table to find deconstruction options.
     *
     * @param itemId The item ID to deconstruct
     * @return List of recipes that can produce this item (for reverse crafting)
     */
    public static List<Recipe> findReverseRecipes(String itemId) {
        initialize();

        List<Recipe> result = new ArrayList<>();
        if (itemId == null || itemId.isEmpty()) return result;

        for (Recipe recipe : recipes) {
            if (recipe.reversible && recipe.result.equals(itemId)) {
                result.add(recipe);
            }
        }

        return result;
    }

    /**
     * Checks if an item can be deconstructed.
     */
    public static boolean canDeconstruct(String itemId) {
        return !findReverseRecipes(itemId).isEmpty();
    }

    /**
     * Gets all recipes.
     */
    public static List<Recipe> getAllRecipes() {
        initialize();
        return new ArrayList<>(recipes);
    }

    /**
     * Gets all recipes in a specific category.
     */
    public static List<Recipe> getRecipesByCategory(String category) {
        initialize();

        List<Recipe> result = new ArrayList<>();
        for (Recipe recipe : recipes) {
            if (category.equals(recipe.category)) {
                result.add(recipe);
            }
        }
        return result;
    }

    /**
     * Gets the total number of loaded recipes.
     */
    public static int getRecipeCount() {
        initialize();
        return recipes.size();
    }

    /**
     * Reloads recipes from the JSON file.
     */
    public static void reload() {
        initialized = false;
        initialize();
    }
}
