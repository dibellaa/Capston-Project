package com.udacity.adibella.whatsinmyfridge;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.gson.Gson;
import com.mashape.p.spoonacularrecipefoodnutritionv1.APIHelper;
import com.mashape.p.spoonacularrecipefoodnutritionv1.Configuration;
import com.mashape.p.spoonacularrecipefoodnutritionv1.SpoonacularAPIClient;
import com.mashape.p.spoonacularrecipefoodnutritionv1.controllers.APIController;
import com.mashape.p.spoonacularrecipefoodnutritionv1.http.client.APICallBack;
import com.mashape.p.spoonacularrecipefoodnutritionv1.http.client.HttpContext;
import com.mashape.p.spoonacularrecipefoodnutritionv1.models.DynamicResponse;
import com.udacity.adibella.whatsinmyfridge.adapter.IngredientAdapter;
import com.udacity.adibella.whatsinmyfridge.model.Ingredient;
import com.udacity.adibella.whatsinmyfridge.provider.IngredientContract;
import com.udacity.adibella.whatsinmyfridge.provider.IngredientDBHelper;
import com.udacity.adibella.whatsinmyfridge.util.JSONUtils;

import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class IngredientsActivity extends AppCompatActivity {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.rv_user_ingredients)
    RecyclerView rvIngedients;
    @BindView(R.id.ingredient_edit_text)
    AutoCompleteTextView newIngredientName;
    @BindView(R.id.add_ingredient_button)
    Button addButton;

    private IngredientAdapter ingredientAdapter;
    private SQLiteDatabase mDb;
    private APIController controller;
    private Context sContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ingredients);

        ButterKnife.bind(this);

        sContext = this;

        setSupportActionBar(toolbar);

        ActionBar actionBar = this.getSupportActionBar();
        if (null != actionBar) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.ingredients_activity_title);
        }

        // Spoonacular API init
        Configuration.initialize(this);
        final SpoonacularAPIClient client = new SpoonacularAPIClient();
        controller = client.getClient();

        newIngredientName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) ||
                        (actionId == EditorInfo.IME_ACTION_DONE)) {
                    addButton.performClick();
                    return true;
                }
                return false;
            }
        });

        newIngredientName.addTextChangedListener(new TextWatcher() {
            private Timer timer=new Timer();
            private final long DELAY = 500;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(final Editable s) {
                timer.cancel();
                timer = new Timer();
                timer.schedule(
                        new TimerTask() {
                            @Override
                            public void run() {
                                getSuggestions(s.toString());
                            }
                        },
                        DELAY
                );
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvIngedients.setLayoutManager(layoutManager);

        IngredientDBHelper dbHelper = new IngredientDBHelper(this);
        mDb = dbHelper.getWritableDatabase();

        Cursor cursor = getAllIngredients();

        ingredientAdapter = new IngredientAdapter(this, cursor);

        rvIngedients.setAdapter(ingredientAdapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                long id = (long) viewHolder.itemView.getTag();
                removeIngredient(id);
                ingredientAdapter.swapCursor(getAllIngredients());
            }
        }).attachToRecyclerView(rvIngedients);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            Timber.d("onBackPressed");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void addToIngredients(View view) {
        if (newIngredientName.getText().length() == 0) {
            return;
        }

        addNewIngredient(newIngredientName.getText().toString());

        newIngredientName.getText().clear();
    }

    private void addNewIngredient(String text) {
        boolean ingredientFound = false;
        Cursor cursor = getOneIngredient(text);
        if (cursor != null) {
            if (cursor.getCount() != 0) {
                ingredientFound = true;
            }
            cursor.close();
        }
        if (!ingredientFound) {
            controller.getAutocompleteIngredientSearchAsync(text, new APICallBack<DynamicResponse>() {
                @Override
                public void onSuccess(HttpContext context, DynamicResponse response) {
                    try {
                        List<Ingredient> ingredients = APIHelper.deserialize(response.parseAsString(),
                                new TypeReference<List<Ingredient>>(){});
                        Timber.d(ingredients.get(0).toString());
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(IngredientContract.IngredientEntry.COLUMN_NAME, ingredients.get(0).getName());
                        contentValues.put(IngredientContract.IngredientEntry.COLUMN_IMAGE, ingredients.get(0).getImageWithUrl());
                        long result = mDb.insert(IngredientContract.IngredientEntry.TABLE_NAME, null, contentValues);
                        if (result > 0) {
                            ingredientAdapter.swapCursor(getAllIngredients());
                            Timber.d("ingredient added");
                            if (!MainActivity.ingredientsExist) {
                                MainActivity.INGREDIENTS_ADDED = true;
                            }
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(HttpContext context, Throwable error) {
                    error.printStackTrace();
                }
            });
        }
    }

    private Cursor getAllIngredients() {
        return mDb.query(IngredientContract.IngredientEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null);
    }

    private Cursor getOneIngredient(String name) {
        Cursor cursor = mDb.query(IngredientContract.IngredientEntry.TABLE_NAME,
                null,
                IngredientContract.IngredientEntry.COLUMN_NAME + " = ? ",
                new String[] {name},
                null,
                null,
                null);
        return cursor;
    }

    private boolean removeIngredient(long id) {
        boolean removed;
        removed = mDb.delete(IngredientContract.IngredientEntry.TABLE_NAME, IngredientContract.IngredientEntry._ID + "=" + id, null) > 0;
        if (removed) {
            Cursor cursor = getAllIngredients();
            if (cursor != null) {
                if (cursor.getCount() == 0) {
                    if (MainActivity.ingredientsExist) {
                        MainActivity.INGREDIENTS_REMOVED = true;
                    }
                }
                cursor.close();
            }
        }
        return removed;
    }

    private void getSuggestions(String s) {
        Timber.d("after text changed");
        controller.getAutocompleteIngredientSearchAsync(s, new APICallBack<DynamicResponse>() {
            @Override
            public void onSuccess(HttpContext context, DynamicResponse response) {
                try {
                    List<Ingredient> ingredients = APIHelper.deserialize(response.parseAsString(),
                            new TypeReference<List<Ingredient>>(){});
                    String[] ingredientsArray = new String[ingredients.size()];
                    for (int i = 0; i < ingredients.size(); ++i) {
                        ingredientsArray[i] = ingredients.get(i).getName();
                    }
                    ArrayAdapter<String> adapter =
                            new ArrayAdapter<>(sContext, android.R.layout.simple_dropdown_item_1line, ingredientsArray);
                    newIngredientName.setAdapter(adapter);
                } catch (ParseException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(HttpContext context, Throwable error) {
                error.printStackTrace();
            }
        });
    }
}
