package com.udacity.adibella.whatsinmyfridge.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class RecipeHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "favoriteRecipes.db";
    private static final int DATABASE_VERSION = 1;

    public RecipeHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_RECIPES_TABLE =
                "CREATE TABLE " + RecipeContract.RecipeEntry.TABLE_NAME + " ("
                        + RecipeContract.RecipeEntry._ID + " INTEGER PRIMARY KEY, "
                        + RecipeContract.RecipeEntry.COLUMN_RECIPE_ID + " INTEGER NOT NULL, "
                        + RecipeContract.RecipeEntry.COLUMN_TITLE + " TEXT NOT NULL, "
                        + RecipeContract.RecipeEntry.COLUMN_IMAGE + " TEXT NOT NULL, "
                        + RecipeContract.RecipeEntry.COLUMN_SUMMARY + " TEXT NOT NULL, "
                        + RecipeContract.RecipeEntry.COLUMN_INGREDIENTS + " TEXT NOT NULL, "
                        + RecipeContract.RecipeEntry.COLUMN_INSTRUCTIONS + " TEXT,"
                        + RecipeContract.RecipeEntry.COLUMN_SOURCE_URL + " TEXT NOT NULL, "
                        + RecipeContract.RecipeEntry.COLUMN_SOURCE_NAME + " TEXT);"
                        + " UNIQUE (" + RecipeContract.RecipeEntry.COLUMN_RECIPE_ID + ") ON CONFLICT REPLACE);"
                ;
        db.execSQL(SQL_CREATE_RECIPES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + RecipeContract.RecipeEntry.TABLE_NAME);
        onCreate(db);
    }
}
