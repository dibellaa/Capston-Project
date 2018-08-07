package com.udacity.adibella.whatsinmyfridge.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class IngredientDBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "ingredients.db";
    private static final int DATABASE_VERSION = 1;

    public IngredientDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_INGREDIENTS_TABLE =
                "CREATE TABLE " + IngredientContract.IngredientEntry.TABLE_NAME + " ("
                        + IngredientContract.IngredientEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + IngredientContract.IngredientEntry.COLUMN_NAME + " TEXT NOT NULL, "
                        + IngredientContract.IngredientEntry.COLUMN_IMAGE + " TEXT);"
                ;
        db.execSQL(SQL_CREATE_INGREDIENTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + IngredientContract.IngredientEntry.TABLE_NAME);
        onCreate(db);
    }
}
