package com.udacity.adibella.whatsinmyfridge.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.widget.Toast;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mashape.p.spoonacularrecipefoodnutritionv1.APIHelper;
import com.mashape.p.spoonacularrecipefoodnutritionv1.models.FindByIngredientsModel;
import com.udacity.adibella.whatsinmyfridge.R;
import com.udacity.adibella.whatsinmyfridge.model.Ingredient;
import com.udacity.adibella.whatsinmyfridge.model.Recipe;
import com.fasterxml.jackson.core.type.TypeReference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class JSONUtils {
    public final static String ID_KEY = "id";
    public final static String IMAGE_KEY = "image";
    public final static String NAME_KEY = "name";
    public final static String AMOUNT_KEY = "amount";
    public final static String UNIT_KEY = "unit";
    public final static String ORIGINAL_STRING_KEY = "originalString";
    public final static String TITLE_KEY = "title";
    public final static String SUMMARY_KEY = "summary";
    public final static String INGREDIENTS_KEY = "extendedIngredients";
    public final static String INSTRUCTIONS_KEY = "instructions";
    public final static String SOURCE_URL_KEY = "sourceUrl";
    public final static String MISSED_INGREDIENT_KEY = "missedIngredientCount";
    public final static String SOURCE_NAME_KEY = "sourceName";

    public static void getRecipeInfoFromJson(Recipe recipe, JSONObject jsonObject) {
        recipe.setInstructions(jsonObject.optString(INSTRUCTIONS_KEY));
        recipe.setSourceUrl(jsonObject.optString(SOURCE_URL_KEY));
        recipe.setSourceName(jsonObject.optString(SOURCE_NAME_KEY));
        try {
            recipe.setIngredients(getListIngredientsFromJsonArray(jsonObject.getJSONArray(INGREDIENTS_KEY)));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static List<Ingredient> getListIngredientsFromJsonArray(JSONArray jsonArray) {
        ArrayList<Ingredient> ingredients = new ArrayList<>();
        if (jsonArray != null) {
            for (int i = 0; i < jsonArray.length(); ++i) {
                Ingredient ingredient = null;
                try {
                    ingredient = getIngredientFromJson(jsonArray.getJSONObject(i));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                ingredients.add(ingredient);
            }
        }
        return ingredients;
    }

    private static Ingredient getIngredientFromJson(JSONObject jsonObject) {
        Ingredient ingredient = new Ingredient();
        ingredient.setId(jsonObject.optInt(ID_KEY));
        ingredient.setName(jsonObject.optString(NAME_KEY));
        ingredient.setAmount(jsonObject.optString(AMOUNT_KEY));
        ingredient.setUnit(jsonObject.optString(UNIT_KEY));
        ingredient.setImage(NetworkUtils.buildIngredientImageUrl(jsonObject.optString(IMAGE_KEY)));
        ingredient.setOriginalString(jsonObject.optString(ORIGINAL_STRING_KEY));
        return ingredient;
    }

    public static void getRecipeSummaryFromJson(Recipe recipe, JSONObject responseJson) {
        recipe.setSummary(responseJson.optString(SUMMARY_KEY));
    }

    public static List<FindByIngredientsModel> getRecipesByIngredientsFromFile(Context context) {
        String json = null;
        List<FindByIngredientsModel> recipes = new ArrayList<>();
        try {
            json = readJSONFile(context, "findByIngredientsResponse.json");
            recipes = APIHelper.deserialize(json, new TypeReference<List<FindByIngredientsModel>>() {
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return recipes;
    }

    private static String readJSONFile(Context context, String fileName) throws IOException {
        AssetManager assetManager = context.getAssets();
        BufferedReader bufferedReader = null;
        bufferedReader = new BufferedReader(
                new InputStreamReader(assetManager.open(fileName)));
        StringBuilder stringBuilder = new StringBuilder();
        String string;

        while ((string = bufferedReader.readLine()) != null) {
            stringBuilder.append(string);
        }
        bufferedReader.close();

        return stringBuilder.toString();
    }

    private static JSONObject getJSONObject(Context context, String fileName) {
        JSONObject returnValue = null;
        try {
            String json = readJSONFile(context, fileName);
            JsonObject jsonObject;
            JsonParser parser = new JsonParser();
            JsonElement jsonElement = parser.parse(json);
            jsonObject = jsonElement.getAsJsonObject();
            returnValue = new JSONObject(jsonObject.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return returnValue;
    }

    public static JSONObject readRecipeInformationFromFile(Context context, Recipe recipe) {
        String fileName = "recipeInfo" + recipe.getId() + ".json";
        return getJSONObject(context, fileName);
    }

    public static JSONObject readRecipeSummaryFromFile(Context context, Recipe recipe) {
        String fileName = "summerize" + recipe.getId() + ".json";
        return getJSONObject(context, fileName);
    }
}
