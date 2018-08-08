package com.udacity.adibella.whatsinmyfridge;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Parcelable;
import android.preference.PreferenceManager;
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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.udacity.adibella.whatsinmyfridge.adapter.RecipeAdapter;
import com.udacity.adibella.whatsinmyfridge.loader.RecipeLoader;
import com.udacity.adibella.whatsinmyfridge.model.Recipe;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements RecipeAdapter.OnRecipeClickListener,
        SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String BUNDLE_RECYCLER_LAYOUT = "recipes_recycler";
    public static Parcelable savedRecyclerLayoutState;
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
    @BindView(R.id.no_ingredients_layout)
    LinearLayout noIngredientsLayout;
    @BindView(R.id.add_button)
    Button addButton;
    private RecipeAdapter recipeAdapter;
    public static final String RECIPE_KEY = "recipe";
    public static boolean PREFERENCES_UPDATED = false;
    private FirebaseAnalytics mFirebaseAnalytics;
    private RecipeLoader recipeLoader;
    public static boolean ingredientsExist;
    public static boolean isFavoriteEnabled;
    public static boolean INGREDIENTS_ADDED = false;
    public static boolean INGREDIENTS_REMOVED = false;

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
            case R.id.action_ingredients:
                startActivity(new Intent(this, IngredientsActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            Timber.d("restore instance state");
            savedRecyclerLayoutState = savedInstanceState.getParcelable(BUNDLE_RECYCLER_LAYOUT);
            recyclerView.getLayoutManager().onRestoreInstanceState(savedRecyclerLayoutState);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Timber.d("save instance state");
        outState.putParcelable(BUNDLE_RECYCLER_LAYOUT, recyclerView.getLayoutManager().onSaveInstanceState());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Timber.d("Hello! MainActivity\n");
        ButterKnife.bind(this);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Timber.d("button clicked");
                startActivity(new Intent(MainActivity.this, IngredientsActivity.class));
            }
        });

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

        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.setAnalyticsCollectionEnabled(true);

        if (recipeLoader == null) {
            recipeLoader = new RecipeLoader(MainActivity.this,
                    recyclerView,
                    errorMessage,
                    errorMessageNoFavorites,
                    progressBar);
        }

        Cursor cursor = recipeLoader.getAllIngredients();

        ingredientsExist = false;
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                ingredientsExist = true;
            }
            cursor.close();
        }

        chooseLayout();
    }

    private void refreshContent() {
        chooseLayout();
        swipeRefreshLayout.setRefreshing(false);
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

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (!key.equals("App Restrictions")) {
            PREFERENCES_UPDATED = true;
        }
        Timber.d(key);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (PREFERENCES_UPDATED) {
            Timber.d("Loader restarted after preferences updates");
            recipeLoader.updateRecipesRecyclerView(MainActivity.this);
            if (isFavoriteEnabled) {
                hideNoIngredientsLayout();
            } else {
                if (!ingredientsExist) {
                    showNoIngredientsLayout();
                }
            }
            PREFERENCES_UPDATED = false;
        }
        if (INGREDIENTS_REMOVED) {
            showNoIngredientsLayout();
            ingredientsExist = false;
            INGREDIENTS_REMOVED = false;
        }
        if (INGREDIENTS_ADDED) {
            Timber.d("ingredients added");
            hideNoIngredientsLayout();
            recipeLoader.updateRecipesRecyclerView(MainActivity.this);
            ingredientsExist = true;
            INGREDIENTS_ADDED = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }

    private void showNoIngredientsLayout() {
        hideAllViewsForNoIngredientsLayout();
        noIngredientsLayout.setVisibility(View.VISIBLE);
    }

    private void hideAllViewsForNoIngredientsLayout() {
        errorMessageNoFavorites.setVisibility(View.INVISIBLE);
        errorMessage.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
        recyclerView.setVisibility(View.INVISIBLE);
    }

    private void hideNoIngredientsLayout() {
        noIngredientsLayout.setVisibility(View.INVISIBLE);
    }

    private void chooseLayout() {
        if (ingredientsExist) {
            recipeLoader.updateRecipesRecyclerView(MainActivity.this);
        } else {
            showNoIngredientsLayout();
        }
    }
}
