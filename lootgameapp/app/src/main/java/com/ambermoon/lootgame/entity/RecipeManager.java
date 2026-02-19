package com.ambermoon.lootgame.entity;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * RecipeManager handles loading and querying alchemy/crafting recipes.
 *
 * Adapted from the Android port's RecipeManager.java for the standalone Loot Game App.
 * - Package: com.ambermoon.lootgame.entity
 *
 * Recipes are loaded from assets/data/alchemy_recipes.json and support:
 * - 1 to 3 ingredient combinations
 * - Multiple recipes for the same output
 * - Reversible recipes for deconstruction
 * - Order-independent ingredient matching
 *
 * Usage:
 *   RecipeManager.initialize(context);
 *   Recipe recipe = RecipeManager.findRecipe(ingredients);
 *   List<Recipe> reverseRecipes = RecipeManager.findReverseRecipes(itemId);
 */
public class RecipeManager {

    private static final String TAG = "RecipeManager";
    private static final String RECIPES_FILE = "data/alchemy_recipes.json";
    private static List<Recipe> recipes = new ArrayList<>();
    private static boolean initialized = false;
    private static Context appContext;

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

            List<String> sortedRecipe = new ArrayList<>(ingredients);
            List<String> sortedInput = new ArrayList<>(inputIngredients);
            Collections.sort(sortedRecipe);
            Collections.sort(sortedInput);

            return sortedRecipe.equals(sortedInput);
        }

        /**
         * Gets the ingredients needed to reverse this recipe.
         * Returns null if recipe is not reversible.
         */
        public List<String> getReverseIngredients() {
            if (!reversible) return null;
            return new ArrayList<>(ingredients);
        }

        @Override
        public String toString() {
            return name + " (" + join(" + ", ingredients) + " -> " + resultCount + "x " + result + ")";
        }

        private static String join(String delimiter, List<String> items) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < items.size(); i++) {
                if (i > 0) sb.append(delimiter);
                sb.append(items.get(i));
            }
            return sb.toString();
        }
    }

    /**
     * Initializes the recipe manager with Android context.
     * Context is required for reading from Android assets.
     *
     * @param context Android context for asset access
     */
    public static void initialize(Context context) {
        if (initialized) return;

        appContext = context.getApplicationContext();
        loadRecipes();
        initialized = true;
        Log.d(TAG, "Loaded " + recipes.size() + " recipes");
    }

    /**
     * Initializes without context (uses previously set context or defaults).
     * Provided for compatibility - prefer initialize(Context) when possible.
     */
    public static void initialize() {
        if (initialized) return;

        if (appContext != null) {
            loadRecipes();
        } else {
            Log.w(TAG, "No context available, creating default recipes");
            createDefaultRecipes();
        }

        initialized = true;
        Log.d(TAG, "Loaded " + recipes.size() + " recipes");
    }

    /**
     * Loads recipes from the Android assets JSON file.
     * Uses InputStream instead of java.nio.file.Files.readAllBytes(Paths.get()).
     */
    private static void loadRecipes() {
        recipes.clear();

        if (appContext == null) {
            Log.e(TAG, "No context available for asset loading");
            createDefaultRecipes();
            return;
        }

        try {
            AssetManager assets = appContext.getAssets();
            InputStream is = assets.open(RECIPES_FILE);
            String jsonContent = readStreamToString(is);
            is.close();
            parseRecipesJson(jsonContent);
        } catch (IOException e) {
            Log.w(TAG, "Failed to load recipes file: " + e.getMessage());
            createDefaultRecipes();
        }
    }

    /**
     * Reads an InputStream to a String.
     * Replacement for java.nio.file.Files.readAllBytes().
     */
    private static String readStreamToString(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = is.read(buffer)) != -1) {
            baos.write(buffer, 0, bytesRead);
        }
        return baos.toString("UTF-8");
    }

    /**
     * Simple JSON parser for recipes (avoids external dependencies).
     */
    private static void parseRecipesJson(String json) {
        int recipesStart = json.indexOf("\"recipes\"");
        if (recipesStart == -1) return;

        int arrayStart = json.indexOf('[', recipesStart);
        int arrayEnd = findMatchingBracket(json, arrayStart);
        if (arrayStart == -1 || arrayEnd == -1) return;

        String recipesArray = json.substring(arrayStart + 1, arrayEnd);

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
                    String ingredient = part.trim().replace("\"", "").trim();
                    if (!ingredient.isEmpty()) {
                        recipe.ingredients.add(ingredient);
                    }
                }
            }
        }

        return recipe;
    }

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

    private static boolean extractBooleanValue(String json, String key, boolean defaultValue) {
        String searchKey = "\"" + key + "\"";
        int keyIndex = json.indexOf(searchKey);
        if (keyIndex == -1) return defaultValue;

        int colonIndex = json.indexOf(':', keyIndex);
        if (colonIndex == -1) return defaultValue;

        String afterColon = json.substring(colonIndex + 1,
                Math.min(colonIndex + 20, json.length())).trim().toLowerCase();
        if (afterColon.startsWith("true")) return true;
        if (afterColon.startsWith("false")) return false;

        return defaultValue;
    }

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
        bow.id = "bow";
        bow.name = "Bow";
        bow.ingredients.add("planks");
        bow.ingredients.add("yarn");
        bow.ingredients.add("arrow");
        bow.result = "bow";
        bow.resultCount = 1;
        bow.category = "weapons";
        bow.reversible = true;
        recipes.add(bow);

        Recipe sword = new Recipe();
        sword.id = "iron_sword";
        sword.name = "Iron Sword";
        sword.ingredients.add("iron_ore");
        sword.ingredients.add("iron_ore");
        sword.ingredients.add("planks");
        sword.result = "iron_sword";
        sword.resultCount = 1;
        sword.category = "weapons";
        sword.reversible = true;
        recipes.add(sword);

        Log.d(TAG, "Created " + recipes.size() + " default recipes");
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
     * Finds all recipes that produce the given item (regardless of reversibility).
     */
    public static List<Recipe> findRecipesForResult(String itemId) {
        initialize();

        List<Recipe> result = new ArrayList<>();
        if (itemId == null || itemId.isEmpty()) return result;

        for (Recipe recipe : recipes) {
            if (recipe.result.equals(itemId)) {
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

    /**
     * Reloads recipes with context.
     */
    public static void reload(Context context) {
        initialized = false;
        initialize(context);
    }
}
