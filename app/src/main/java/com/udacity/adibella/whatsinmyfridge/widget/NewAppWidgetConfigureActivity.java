package com.udacity.adibella.whatsinmyfridge.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.udacity.adibella.whatsinmyfridge.MainActivity;
import com.udacity.adibella.whatsinmyfridge.R;
import com.udacity.adibella.whatsinmyfridge.adapter.RecipeAdapter;
import com.udacity.adibella.whatsinmyfridge.model.Recipe;
import com.udacity.adibella.whatsinmyfridge.provider.RecipeContract;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class NewAppWidgetConfigureActivity extends AppCompatActivity implements RecipeAdapter.OnRecipeClickListener {
    private static final String PREFS_NAME = "com.udacity.whatsinmyfridge.NewAppWidget";
    private static final String PREF_PREFIX_KEY = "appwidget_";
    private int widgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    private RecipeAdapter recipeAdapter;
    private List<Recipe> recipes;

    @BindView(R.id.widget_error_message)
    TextView errorMessage;

    @BindView(R.id.recipes_in_the_widget_label)
    TextView label;

    @BindView(R.id.rv_recipes)
    RecyclerView recyclerView;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    public NewAppWidgetConfigureActivity() {
        super();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED);
        setContentView(R.layout.new_app_widget_configure);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        ActionBar actionBar = this.getSupportActionBar();
        if (null != actionBar) {
            actionBar.setTitle(R.string.widget_configuration_title);
        }

        loadLabel();

        GridLayoutManager layoutManager = new GridLayoutManager(this, MainActivity.numberOfColumns(this));
        recyclerView.setLayoutManager(layoutManager);
        recipeAdapter = new RecipeAdapter(R.layout.recipe_grid_item,this, this, new ArrayList<Recipe>());
        recyclerView.setAdapter(recipeAdapter);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            widgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        loadRecipes();
    }

    private void loadRecipes() {
        if (recipes != null) {
            recipes.clear();
        } else {
            recipes = new ArrayList<>();
        }
        Cursor cursor = getContentResolver().query(RecipeContract.RecipeEntry.CONTENT_URI,
                null,
                null,
                null,
                null);
        if (cursor != null) {
            Timber.d(DatabaseUtils.dumpCursorToString(cursor));
            if (cursor.getCount() > 0) {
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
                    Timber.d(recipe.toString());
                    recipes.add(recipe);
                }
                recipeAdapter.setRecipes(recipes);
            } else {
                loadErrorMessage();
            }
            cursor.close();
        }
        Timber.d("Cursor is null");
    }

    private void loadErrorMessage() {
        errorMessage.setVisibility(View.VISIBLE);
        label.setVisibility(View.INVISIBLE);
    }

    private void loadLabel() {
        errorMessage.setVisibility(View.INVISIBLE);
        label.setVisibility(View.VISIBLE);
    }

    @Override
    public void onRecipeSelected(View view, Recipe recipeSelected) {
        final Context context = NewAppWidgetConfigureActivity.this;
        
        saveWidgetRecipe(context, widgetId, recipeSelected);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        NewAppWidget.updateAppWidget(context, appWidgetManager, widgetId);

        Intent result = new Intent();
        result.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        setResult(RESULT_OK, result);
        finish();
    }

    private void saveWidgetRecipe(Context context, int widgetId, Recipe recipeSelected) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
//        prefs.putString(PREF_PREFIX_KEY + widgetId + RecipeContract.RecipeEntry.COLUMN_TITLE, recipeSelected.getTitle());
//        prefs.putString(PREF_PREFIX_KEY + widgetId + RecipeContract.RecipeEntry.COLUMN_IMAGE, recipeSelected.getImage());
//        prefs.putString(PREF_PREFIX_KEY + widgetId + RecipeContract.RecipeEntry.COLUMN_INGREDIENTS, recipeSelected.getIngredientsGson());
        Gson gson = new Gson();
        String jsonRecipe = gson.toJson(recipeSelected);
        prefs.putString(PREF_PREFIX_KEY + widgetId, jsonRecipe);
        prefs.apply();
    }

    public static Recipe readWidgetRecipe(Context context, int widgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        Gson gson = new Gson();
        String jsonRecipe = prefs.getString(PREF_PREFIX_KEY + widgetId, "");
        Recipe recipe = gson.fromJson(jsonRecipe, Recipe.class);
        return recipe;
    }

    public static void deleteWidgetRecipe(Context context, int widgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        prefs.remove(PREF_PREFIX_KEY + widgetId);
        prefs.apply();
    }
}
