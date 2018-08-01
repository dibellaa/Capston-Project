package com.udacity.adibella.whatsinmyfridge.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class RecipeProvider extends ContentProvider {
    public static final int CODE_RECIPES = 100;
    public static final int CODE_RECIPE_WITH_ID = 110;

    private RecipeHelper helper;
    private static final UriMatcher uriMatcher = buildUriMatcher();

    public static UriMatcher buildUriMatcher() {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(RecipeContract.AUTHORITY, RecipeContract.PATH_RECIPES, CODE_RECIPES);
        matcher.addURI(RecipeContract.AUTHORITY, RecipeContract.PATH_RECIPES + "/#", CODE_RECIPE_WITH_ID);
        return matcher;
    }
    @Override
    public boolean onCreate() {
        helper = new RecipeHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Cursor cursor;
        switch (uriMatcher.match(uri)) {
            case CODE_RECIPES: {
                cursor = helper.getReadableDatabase().query(RecipeContract.RecipeEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            case CODE_RECIPE_WITH_ID: {
                String[] selectionArguments = new String[]{uri.getLastPathSegment()};
                cursor = helper.getReadableDatabase().query(RecipeContract.RecipeEntry.TABLE_NAME,
                        projection,
                        RecipeContract.RecipeEntry.COLUMN_RECIPE_ID + " = ? ",
                        selectionArguments,
                        null,
                        null,
                        sortOrder);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        Uri returnUri;
        final SQLiteDatabase db = helper.getWritableDatabase();

        switch (uriMatcher.match(uri)) {
            case CODE_RECIPES:
                long id = db.insert(RecipeContract.RecipeEntry.TABLE_NAME, null, values);
                if (id > 0) {
                    returnUri = ContentUris.withAppendedId(RecipeContract.RecipeEntry.CONTENT_URI, id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int numRowsDeleted;
        if (null == selection) selection = "1";

        switch (uriMatcher.match(uri)) {
            case CODE_RECIPES:
                numRowsDeleted = helper.getWritableDatabase().delete(
                        RecipeContract.RecipeEntry.TABLE_NAME,
                        selection,
                        selectionArgs
                );
                break;
            case CODE_RECIPE_WITH_ID:
                selection = "_id=?";
                selectionArgs = new String[]{uri.getLastPathSegment()};
                numRowsDeleted = helper.getWritableDatabase().delete(
                        RecipeContract.RecipeEntry.TABLE_NAME,
                        selection,
                        selectionArgs
                );
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (numRowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return numRowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
