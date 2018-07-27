package com.udacity.adibella.whatsinmyfridge;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;

import com.mashape.p.spoonacularrecipefoodnutritionv1.Configuration;
import com.mashape.p.spoonacularrecipefoodnutritionv1.SpoonacularAPIClient;
import com.mashape.p.spoonacularrecipefoodnutritionv1.controllers.APIController;
import com.mashape.p.spoonacularrecipefoodnutritionv1.http.client.APICallBack;
import com.mashape.p.spoonacularrecipefoodnutritionv1.http.client.HttpContext;
import com.mashape.p.spoonacularrecipefoodnutritionv1.models.DynamicResponse;
import com.mashape.p.spoonacularrecipefoodnutritionv1.models.FindByIngredientsModel;
import com.udacity.adibella.whatsinmyfridge.adapter.RecipeAdapter;
import com.udacity.adibella.whatsinmyfridge.model.Recipe;
import com.udacity.adibella.whatsinmyfridge.util.JSONUtils;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements RecipeAdapter.OnRecipeClickListener{
    @BindView(R.id.rv_recipes)
    RecyclerView recyclerView;
    APIController controller;
    private RecipeAdapter recipeAdapter;
    private List<Recipe> recipes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Timber.d("Hello! MainActivity\n");
        ButterKnife.bind(this);
        GridLayoutManager layoutManager = new GridLayoutManager(this, numberOfColumns(this));
        recyclerView.setLayoutManager(layoutManager);
        recipeAdapter = new RecipeAdapter(R.layout.recipe_grid_item,this,this, new ArrayList<Recipe>());
        recyclerView.setAdapter(recipeAdapter);

        recipes = new ArrayList<Recipe>();
//        loadRecipes(this);
        // used during development phase
        loadRecipesFromFiles(this);
    }

    private void loadRecipesFromFiles(Context context) {
        List<FindByIngredientsModel> findByIngredientsModels;
        findByIngredientsModels = JSONUtils.getRecipesByIngredientsFromFile(MainActivity.this);
        for (FindByIngredientsModel findByIngredientsModel : findByIngredientsModels) {
            Recipe recipe = new Recipe();
            recipe.addInfoFromIngredientsModel(findByIngredientsModel);
            //get recipe information
            getRecipeInformationFromFile(recipe, MainActivity.this);
            //get recipe summary
            getRecipeSummaryFromFile(recipe, MainActivity.this);
            recipes.add(recipe);
        }
        recipeAdapter.setRecipes(recipes);
    }

    private void getRecipeSummaryFromFile(Recipe recipe, Context context) {
        recipe.addRecipeSummaryFromFile(context);
    }

    private void getRecipeInformationFromFile(Recipe recipe, Context context) {
        recipe.addRecipeInformationFromFile(context);
    }

    private void loadRecipes(Context context) {
        // Spoonacular API init
        Configuration.initialize(context);
        SpoonacularAPIClient client = new SpoonacularAPIClient();
        controller = client.getClient();
        String ingredients = "apples,flour,sugar";
        Boolean limitLicense = false;
        Integer number = 5;
        Integer ranking = 1;
        // key-value map for optional query parameters
        Map<String, Object> queryParams = new LinkedHashMap<String, Object>();
        controller.findByIngredientsAsync(ingredients, limitLicense, number, ranking, queryParams, new APICallBack<List<FindByIngredientsModel>>() {
            @Override
            public void onSuccess(HttpContext context, List<FindByIngredientsModel> response) {
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
            }

            @Override
            public void onFailure(HttpContext context, Throwable error) {
                Timber.d("Load recipes failed %s", error.getMessage());
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

    private int numberOfColumns(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int scalingFactor = 200;
        int nColumns = (int)(dpWidth / scalingFactor);
        int defNbColumns = context.getResources().getInteger(R.integer.number_columns);
        if (nColumns < defNbColumns) return defNbColumns;
        return nColumns;
    }

    @Override
    public void onRecipeSelected(int position) {
        Timber.d("selected Recipe: " + position);
        Recipe selectedRecipe = recipes.get(position);
//        Intent intent = new Intent(this, RecipeActivity.class);
//        intent.putExtra(RECIPE_KEY, selectedRecipe);
//        startActivity(intent);
    }
}
