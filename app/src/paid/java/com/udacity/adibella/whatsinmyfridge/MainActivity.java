package com.udacity.adibella.whatsinmyfridge;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
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
import com.udacity.adibella.whatsinmyfridge.provider.RecipeContract;
import com.udacity.adibella.whatsinmyfridge.util.JSONUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements RecipeAdapter.OnRecipeClickListener,
        LoaderManager.LoaderCallbacks<List<Recipe>>, SharedPreferences.OnSharedPreferenceChangeListener {
    @BindView(R.id.rv_recipes)
    RecyclerView recyclerView;
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.tv_error_message)
    TextView errorMessage;
    @BindView(R.id.tv_error_message_no_favorite)
    TextView errorMessageNoFavorites;
    @BindView(R.id.pb_loading_indicator)
    ProgressBar progressBar;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    APIController controller;
    private RecipeAdapter recipeAdapter;
    public static final String RECIPE_KEY = "recipe";
    public static boolean PREFERENCES_UPDATED = false;
    public static final int RECIPES_LOADER_ID = 0;
    private boolean isFavoriteEnabled;

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

        LoaderManager.LoaderCallbacks<List<Recipe>> callback = MainActivity.this;

        getSupportLoaderManager().initLoader(RECIPES_LOADER_ID, null, callback);

        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }

    private void refreshContent() {
        getSupportLoaderManager().restartLoader(RECIPES_LOADER_ID, null, this);
        swipeRefreshLayout.setRefreshing(false);
    }

    private List<Recipe> loadRecipesFromFiles(Context context) {
        List<Recipe> recipes = new ArrayList<Recipe>();
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
        return recipes;
//        recipeAdapter.setRecipes(recipes);
    }

    private void getRecipeSummaryFromFile(Recipe recipe, Context context) {
        recipe.addRecipeSummaryFromFile(context);
    }

    private void getRecipeInformationFromFile(Recipe recipe, Context context) {
        recipe.addRecipeInformationFromFile(context);
    }

    private List<Recipe> loadRecipes(Context context) {
        final List<Recipe> recipes = new ArrayList<Recipe>();

        // Spoonacular API init
        Configuration.initialize(context);
        final SpoonacularAPIClient client = new SpoonacularAPIClient();
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
            }

            @Override
            public void onFailure(HttpContext context, Throwable error) {
                Timber.d("Load recipes failed %s", error.getMessage());
            }
        });

        return recipes;
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

    public static int numberOfColumns(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int scalingFactor = 200;
        int nColumns = (int)(dpWidth / scalingFactor);
        int defNbColumns = context.getResources().getInteger(R.integer.number_columns);
        if (nColumns < defNbColumns) return defNbColumns;
        return nColumns;
    }

    @Override
    public void onRecipeSelected(View view, Recipe selectedRecipe) {
        Timber.d("selected Recipe: " + selectedRecipe.toString());
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

    @SuppressLint("StaticFieldLeak")
    @Override
    public Loader<List<Recipe>> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<List<Recipe>>(this) {
            List<Recipe> recipesData = null;

            @Override
            protected void onStartLoading() {
                progressBar.setVisibility(View.VISIBLE);
                if (recipesData != null) {
                    deliverResult(recipesData);
                } else {
                    forceLoad();
                }
            }

            public void deliverResult(List<Recipe> recipes) {
                recipesData = recipes;
                super.deliverResult(recipes);
            }

            @Override
            public List<Recipe> loadInBackground() {
                List<Recipe> returnRecipes = null;
                SharedPreferences pref = PreferenceManager
                        .getDefaultSharedPreferences(MainActivity.this);
                isFavoriteEnabled = pref.getBoolean(MainActivity.this.getString(R.string.cb_key), false);
                if (isFavoriteEnabled) {
                    Cursor cursor = getContentResolver().query(RecipeContract.RecipeEntry.CONTENT_URI,
                            null,
                            null,
                            null,
                            null);
                    if (cursor != null) {
                        Timber.d(DatabaseUtils.dumpCursorToString(cursor));
                        if (cursor.getCount() > 0) {
                            returnRecipes = new ArrayList<>();
                        }
                        for (int i = 0; i < cursor.getCount(); ++i) {
                            cursor.moveToPosition(i);
                            int id = cursor.getInt(cursor.getColumnIndex(RecipeContract.RecipeEntry.COLUMN_RECIPE_ID));
                            String title = cursor.getString(cursor.getColumnIndex(RecipeContract.RecipeEntry.COLUMN_TITLE));
                            String image = cursor.getString(cursor.getColumnIndex(RecipeContract.RecipeEntry.COLUMN_IMAGE));
                            String summary = cursor.getString(cursor.getColumnIndex(RecipeContract.RecipeEntry.COLUMN_SUMMARY));
                            String ingredients = cursor.getString(cursor.getColumnIndex(RecipeContract.RecipeEntry.COLUMN_INGREDIENTS));
                            Timber.d("ingredients: " + ingredients);
                            String instructions = cursor.getString(cursor.getColumnIndex(RecipeContract.RecipeEntry.COLUMN_INSTRUCTIONS));
                            Timber.d("instructions: " + instructions);
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
                            Timber.d(recipe.toString());
                            returnRecipes.add(recipe);
                        }
                        cursor.close();
                    }
                } else {
                    // used during development phase
                    returnRecipes = loadRecipesFromFiles(getContext());
//                    returnRecipes = loadRecipes(getContext());
                }
                return returnRecipes;
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<List<Recipe>> loader, List<Recipe> data) {
        if (data != null) {
            recipeAdapter.setRecipes(data);
            showRecipes();
        } else {
            if (isFavoriteEnabled) {
                showErrorMessageNoFavorites();
            } else {
                showErrorMessage();
            }
        }

    }

    @Override
    public void onLoaderReset(Loader<List<Recipe>> loader) {

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        PREFERENCES_UPDATED = true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (PREFERENCES_UPDATED) {
            Timber.d("Loader restarted after preferences updates");
            getSupportLoaderManager().restartLoader(RECIPES_LOADER_ID, null, this);
            PREFERENCES_UPDATED = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }
}
