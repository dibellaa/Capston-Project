package com.udacity.adibella.whatsinmyfridge;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
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
import com.udacity.adibella.whatsinmyfridge.adapter.RecipeAdapter;
import com.udacity.adibella.whatsinmyfridge.model.Recipe;
import com.udacity.adibella.whatsinmyfridge.util.JSONUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements RecipeAdapter.OnRecipeClickListener {
    @BindView(R.id.rv_recipes)
    RecyclerView recyclerView;
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.tv_error_message)
    TextView errorMessage;
    @BindView(R.id.pb_loading_indicator)
    ProgressBar progressBar;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    APIController controller;
    private RecipeAdapter recipeAdapter;
    private List<Recipe> recipes;
    public static final String RECIPE_KEY = "recipe";

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Timber.d("Hello! MainActivity\n");
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        GridLayoutManager layoutManager = new GridLayoutManager(this, numberOfColumns(this));
        recyclerView.setLayoutManager(layoutManager);
        recipeAdapter = new RecipeAdapter(R.layout.recipe_grid_item,this,this, new ArrayList<Recipe>());
        recyclerView.setAdapter(recipeAdapter);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshContent();
            }
        });

        recipes = new ArrayList<Recipe>();
        progressBar.setVisibility(View.VISIBLE);
//        loadRecipes(this);
        // used during development phase
        loadRecipesFromFiles(this);
    }

    private void refreshContent() {
        recipes.clear();
        loadRecipesFromFiles(MainActivity.this);
//        loadRecipes(MainActivity.this);
        swipeRefreshLayout.setRefreshing(false);
    }

    private void loadRecipesFromFiles(Context context) {
        stopProgressBar();
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
                showRecipes();
            }

            @Override
            public void onFailure(HttpContext context, Throwable error) {
                Timber.d("Load recipes failed %s", error.getMessage());
                if (recipes.isEmpty()) {
                    showErrorMessage();
                }
            }
        });
    }

    private void stopProgressBar() {
        if (progressBar.getVisibility() == View.VISIBLE) {
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void showRecipes() {
        stopProgressBar();
        recyclerView.setVisibility(View.VISIBLE);
        errorMessage.setVisibility(View.INVISIBLE);
    }

    private void showErrorMessage() {
        stopProgressBar();
        recyclerView.setVisibility(View.INVISIBLE);
        errorMessage.setVisibility(View.VISIBLE);
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
    public void onRecipeSelected(View view, int position) {
        Timber.d("selected Recipe: " + position);
        Recipe selectedRecipe = recipes.get(position);
        Bundle bundle;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Timber.d("animation");
            bundle = ActivityOptionsCompat
                    .makeSceneTransitionAnimation(MainActivity.this, view , "recipe")
                    .toBundle();
        } else {
            bundle = ActivityOptionsCompat
                    .makeSceneTransitionAnimation(MainActivity.this)
                    .toBundle();
        }
        Intent intent = new Intent(this, RecipeActivity.class);
        intent.putExtra(RECIPE_KEY, selectedRecipe);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            startActivity(intent,bundle);
        } else {
            startActivity(intent);
        }
    }
}
