package com.udacity.adibella.whatsinmyfridge.provider;

import android.provider.BaseColumns;

public class IngredientContract {

    public static final class IngredientEntry implements BaseColumns {
        public static final String TABLE_NAME = "ingredients";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_IMAGE = "image";
    }
}
