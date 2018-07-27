package com.udacity.adibella.whatsinmyfridge.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.udacity.adibella.whatsinmyfridge.R;

public class NetworkUtils {
    private static Toast sToast;

    private static final String baseIngedientUrl = "https://spoonacular.com/cdn/ingredients_100x100/";

    public static NetworkInfo getNetworkInfo(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getActiveNetworkInfo();
    }

    public static boolean isConnected(Context context) {
        NetworkInfo networkInfo = getNetworkInfo(context);
        return networkInfo != null && networkInfo.isConnected();
    }

    public static boolean checkConnection(Context context) {
        boolean isConnected = isConnected(context);
        if (!isConnected) {
            showToast(context, R.string.no_connection_msg, true);
        }
        return isConnected;
    }

    private static void showToast(Context context, int resId, boolean showLong) {
        if (sToast != null) {
            sToast.cancel();
        }
        String msg = context.getString(resId);
        sToast = Toast.makeText(context, msg, showLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
        sToast.show();
    }

    public static String buildIngredientImageUrl(String s) {
        return baseIngedientUrl+s;
    }
}
