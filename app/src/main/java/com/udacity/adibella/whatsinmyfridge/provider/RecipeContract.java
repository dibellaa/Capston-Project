package com.udacity.adibella.whatsinmyfridge.provider;

import android.net.Uri;
import android.provider.BaseColumns;

import com.udacity.adibella.whatsinmyfridge.BuildConfig;

public class RecipeContract {
    public static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".provider";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final String PATH_RECIPES = "favoriteRecipes";

    public static final class RecipeEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_RECIPES)
                .build();
        public static final String TABLE_NAME = "favoriteRecipes";
        public static final String COLUMN_RECIPE_ID = "recipeId";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_IMAGE = "image";
        public static final String COLUMN_SUMMARY = "summary";
        public static final String COLUMN_INGREDIENTS = "ingredients";
        public static final String COLUMN_INSTRUCTIONS = "instructions";
        public static final String COLUMN_SOURCE_URL = "sourceUrl";
        public static final String COLUMN_SOURCE_NAME = "sourceName";

    }
}