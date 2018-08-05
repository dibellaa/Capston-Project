package com.udacity.adibella.whatsinmyfridge;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
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
    private RecipeAdapter recipeAdapter;
    public static final String RECIPE_KEY = "recipe";
    public static boolean PREFERENCES_UPDATED = false;
    private FirebaseAnalytics mFirebaseAnalytics;
    private RecipeLoader recipeLoader;

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

        recipeLoader.updateRecipesRecyclerView(MainActivity.this);
    }

    private void refreshContent() {
        recipeLoader.updateRecipesRecyclerView(MainActivity.this);
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
        PREFERENCES_UPDATED = true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (PREFERENCES_UPDATED) {
            recipeLoader.updateRecipesRecyclerView(MainActivity.this);
            PREFERENCES_UPDATED = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }
}
