package com.udacity.adibella.whatsinmyfridge.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.RemoteViews;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.udacity.adibella.whatsinmyfridge.MainActivity;
import com.udacity.adibella.whatsinmyfridge.R;
import com.udacity.adibella.whatsinmyfridge.RecipeActivity;
import com.udacity.adibella.whatsinmyfridge.model.Recipe;
import com.udacity.adibella.whatsinmyfridge.util.NetworkUtils;

/**
 * Implementation of App Widget functionality.
 */
public class NewAppWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        Recipe recipe = NewAppWidgetConfigureActivity.readWidgetRecipe(context, appWidgetId);

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.new_app_widget);

        if (recipe != null) {
            views.setTextViewText(R.id.appwidget_title, recipe.getTitle());
            views.setTextViewText(R.id.appwidget_ingredients, recipe.getIngredientsString());

            loadImage(recipe, views, appWidgetId);

            Intent intent = new Intent(context, RecipeActivity.class);
            intent.putExtra(MainActivity.RECIPE_KEY, recipe);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            views.setOnClickPendingIntent(R.id.widget, pendingIntent);
        }

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private static void loadImage(Recipe recipe, RemoteViews views, int appWidgetId) {
        if (!TextUtils.isEmpty(recipe.getImage())) {
            Picasso.get()
                    .load(recipe.getImage())
                    .into(views, R.id.iv_recipe_image, new int[]{appWidgetId});
        } else {
            Picasso.get()
                    .load(R.drawable.default_image)
                    .into(views, R.id.iv_recipe_image, new int[]{appWidgetId});
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            NewAppWidgetConfigureActivity.deleteWidgetRecipe(context, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

