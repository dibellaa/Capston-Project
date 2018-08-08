package com.udacity.adibella.whatsinmyfridge.loader;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mashape.p.spoonacularrecipefoodnutritionv1.Configuration;
import com.mashape.p.spoonacularrecipefoodnutritionv1.SpoonacularAPIClient;
import com.mashape.p.spoonacularrecipefoodnutritionv1.controllers.APIController;
import com.mashape.p.spoonacularrecipefoodnutritionv1.http.client.APICallBack;
import com.mashape.p.spoonacularrecipefoodnutritionv1.http.client.HttpContext;
import com.mashape.p.spoonacularrecipefoodnutritionv1.models.DynamicResponse;
import com.mashape.p.spoonacularrecipefoodnutritionv1.models.FindByIngredientsModel;
import com.udacity.adibella.whatsinmyfridge.MainActivity;
import com.udacity.adibella.whatsinmyfridge.R;
import com.udacity.adibella.whatsinmyfridge.adapter.RecipeAdapter;
import com.udacity.adibella.whatsinmyfridge.model.Recipe;
import com.udacity.adibella.whatsinmyfridge.provider.IngredientContract;
import com.udacity.adibella.whatsinmyfridge.provider.IngredientDBHelper;
import com.udacity.adibella.whatsinmyfridge.provider.RecipeContract;
import com.udacity.adibella.whatsinmyfridge.util.JSONUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import timber.log.Timber;

public class RecipeLoader {
    @BindView(R.id.rv_recipes)
    RecyclerView recyclerView;
    @BindView(R.id.tv_error_message)
    TextView errorMessage;
    @BindView(R.id.tv_error_message_no_favorite)
    TextView errorMessageNoFavorites;
    @BindView(R.id.pb_loading_indicator)
    ProgressBar progressBar;

    Context context;
    private static boolean isFavoriteEnabled;
    private List<Recipe> recipes;
    private APIController controller;
    private RecipeAdapter recipeAdapter;

    private SQLiteDatabase mDb;
    private IngredientDBHelper dbHelper;
    private Cursor cursor;

    public RecipeLoader(Context context, RecyclerView recyclerView, TextView errorMessage, TextView errorMessageNoFavorites, ProgressBar progressBar) {
        this.context = context;
        recipes = new ArrayList<>();
        this.recyclerView = recyclerView;
        this.errorMessage = errorMessage;
        this.errorMessageNoFavorites = errorMessageNoFavorites;
        this.progressBar = progressBar;
        recipeAdapter = (RecipeAdapter) this.recyclerView.getAdapter();
        dbHelper = new IngredientDBHelper(context);
        mDb = dbHelper.getReadableDatabase();
        cursor = getAllIngredients();
    }

    public void startProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
        errorMessageNoFavorites.setVisibility(View.INVISIBLE);
        errorMessage.setVisibility(View.INVISIBLE);
    }

    public void stopProgressBar() {
        if (progressBar.getVisibility() == View.VISIBLE) {
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void showRecipes() {
        stopProgressBar();
        recyclerView.setVisibility(View.VISIBLE);
        errorMessage.setVisibility(View.INVISIBLE);
        errorMessageNoFavorites.setVisibility(View.INVISIBLE);
    }

    private void showErrorMessage() {
        stopProgressBar();
        recyclerView.setVisibility(View.INVISIBLE);
        errorMessage.setVisibility(View.VISIBLE);
    }

    private void showErrorMessageNoFavorites() {
        stopProgressBar();
        recyclerView.setVisibility(View.INVISIBLE);
        errorMessageNoFavorites.setVisibility(View.VISIBLE);
    }

    private void updateUI() {
        stopProgressBar();
        if (!recipes.isEmpty()) {
            showRecipes();
        } else {
            if (isFavoriteEnabled) {
                showErrorMessageNoFavorites();
            } else {
                showErrorMessage();
            }
        }
    }

    public void updateRecipesRecyclerView(Context context) {
        Timber.d("updateRecipesRecyclerView");

        startProgressBar();

        if (recipes == null) {
            recipes = new ArrayList<>();
        }
        if (!recipes.isEmpty()) {
            recipes.clear();
        }

        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(context);
        isFavoriteEnabled = pref.getBoolean(context.getString(R.string.cb_key), false);
        MainActivity.isFavoriteEnabled = isFavoriteEnabled;
        if (isFavoriteEnabled) {
            loadFavoriteRecipes(context);
        } else {
            // used during development phase
//            loadRecipesFromFiles(context);
            loadRecipes(context);
        }
    }

    private void loadRecipes(Context context) {
        // Spoonacular API init
        Configuration.initialize(context);
        final SpoonacularAPIClient client = new SpoonacularAPIClient();
        controller = client.getClient();
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(context);
        String ingredients = "";
        if (cursor != null) {
            ingredients = getIngredientsString(cursor);
            Timber.d(ingredients);
        }
        if (ingredients.equals("")) {
            stopProgressBar();
            return;
        }
        Boolean limitLicense = pref.getBoolean(context.getString(R.string.cb_key_license),
                false);
        Timber.d(String.valueOf(limitLicense));
        Integer number = pref.getInt(context.getString(R.string.pref_number_key), 4);
        Timber.d(number.toString());
        Integer ranking = Integer.valueOf(pref.getString(context.getString(R.string.search_option_key),
                "1"));
        // key-value map for optional query parameters
        Map<String, Object> queryParams = new LinkedHashMap<>();
        controller.findByIngredientsAsync(ingredients, limitLicense, number, ranking, queryParams, new APICallBack<List<FindByIngredientsModel>>() {
            @Override
            public void onSuccess(HttpContext context, List<FindByIngredientsModel> response) {
                Timber.d("onSuccess");
                for (FindByIngredientsModel findByIngredientsModel : response) {
                    Recipe recipe = new Recipe();
                    recipe.addInfoFromIngredientsModel(findByIngredientsModel);
                    //get recipe information
                    getRecipeInformation(recipe);
                    //get recipe summary
                    getRecipeSummary(recipe);
                    recipes.add(recipe);
                }
                recipeAdapter.setRecipes(recipes);
                recyclerView.getLayoutManager().onRestoreInstanceState(MainActivity.savedRecyclerLayoutState );
                updateUI();
            }

            @Override
            public void onFailure(HttpContext context, Throwable error) {
                Timber.d("Load recipes failed %s", error.getMessage());
                error.printStackTrace();
            }
        });
        Timber.d("after anonymous class");
    }

    private void getRecipeInformation(final Recipe recipe) {
        controller.getRecipeInformationAsync(recipe.getId(), new APICallBack<DynamicResponse>() {
            @Override
            public void onSuccess(HttpContext context, DynamicResponse response) {
                recipe.addRecipeInformation(response);
            }

            @Override
            public void onFailure(HttpContext context, Throwable error) {
                Timber.d("Load recipe information failed");
            }
        });
    }

    private void getRecipeSummary(final Recipe recipe) {
        controller.getSummarizeRecipeAsync(recipe.getId(), new APICallBack<DynamicResponse>() {
            @Override
            public void onSuccess(HttpContext context, DynamicResponse response) {
                recipe.addRecipeSummary(response);
            }

            @Override
            public void onFailure(HttpContext context, Throwable error) {
                Timber.d("Load recipe summary failed");
            }
        });
    }

    private void loadRecipesFromFiles(Context context) {
        List<FindByIngredientsModel> findByIngredientsModels;
        findByIngredientsModels = JSONUtils.getRecipesByIngredientsFromFile(context);
        for (FindByIngredientsModel findByIngredientsModel : findByIngredientsModels) {
            Recipe recipe = new Recipe();
            recipe.addInfoFromIngredientsModel(findByIngredientsModel);
            //get recipe information
            getRecipeInformationFromFile(recipe, context);
            //get recipe summary
            getRecipeSummaryFromFile(recipe, context);
            recipes.add(recipe);
        }
        recipeAdapter.setRecipes(recipes);
        updateUI();
    }

    private void getRecipeSummaryFromFile(Recipe recipe, Context context) {
        recipe.addRecipeSummaryFromFile(context);
    }

    private void getRecipeInformationFromFile(Recipe recipe, Context context) {
        recipe.addRecipeInformationFromFile(context);
    }

    private void loadFavoriteRecipes(Context context){
        Cursor cursor = context.getContentResolver().query(RecipeContract.RecipeEntry.CONTENT_URI,
                null,
                null,
                null,
                null);
        if (cursor != null) {
            Timber.d(DatabaseUtils.dumpCursorToString(cursor));
            for (int i = 0; i < cursor.getCount(); ++i) {
                cursor.moveToPosition(i);
                int id = cursor.getInt(cursor.getColumnIndex(RecipeContract.RecipeEntry.COLUMN_RECIPE_ID));
                String title = cursor.getString(cursor.getColumnIndex(RecipeContract.RecipeEntry.COLUMN_TITLE));
                String image = cursor.getString(cursor.getColumnIndex(RecipeContract.RecipeEntry.COLUMN_IMAGE));
                String summary = cursor.getString(cursor.getColumnIndex(RecipeContract.RecipeEntry.COLUMN_SUMMARY));
                String ingredients = cursor.getString(cursor.getColumnIndex(RecipeContract.RecipeEntry.COLUMN_INGREDIENTS));
                String instructions = cursor.getString(cursor.getColumnIndex(RecipeContract.RecipeEntry.COLUMN_INSTRUCTIONS));
                String sourceUrl = cursor.getString(cursor.getColumnIndex(RecipeContract.RecipeEntry.COLUMN_SOURCE_URL));
                String sourceName = cursor.getString(cursor.getColumnIndex(RecipeContract.RecipeEntry.COLUMN_SOURCE_NAME));
                Recipe recipe = new Recipe(id,
                        title,
                        image,
                        summary,
                        ingredients,
                        instructions,
                        sourceUrl,
                        sourceName);
                recipes.add(recipe);
            }
            cursor.close();
        }
        recipeAdapter.setRecipes(recipes);
        updateUI();
    }

    public Cursor getAllIngredients() {
        return mDb.query(IngredientContract.IngredientEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null);
    }

    private String getIngredientsString(Cursor cursor) {
        Timber.d("get str builder");
        StringBuilder strBuilder = new StringBuilder();
        String delimiter = ", ";
        for (int i = 0; i < cursor.getCount(); ++i) {
            cursor.moveToPosition(i);
            strBuilder.append(cursor.getString(cursor.getColumnIndex(IngredientContract.IngredientEntry.COLUMN_NAME)));
            if (i != cursor.getCount()-1) {
                strBuilder.append(delimiter);
            }
        }
        Timber.d(strBuilder.toString());
        return strBuilder.toString();
    }
}
