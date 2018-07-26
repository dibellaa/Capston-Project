package com.udacity.adibella.whatsinmyfridge;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.mashape.p.spoonacularrecipefoodnutritionv1.Configuration;
import com.mashape.p.spoonacularrecipefoodnutritionv1.SpoonacularAPIClient;
import com.mashape.p.spoonacularrecipefoodnutritionv1.controllers.APIController;
import com.mashape.p.spoonacularrecipefoodnutritionv1.http.client.APICallBack;
import com.mashape.p.spoonacularrecipefoodnutritionv1.http.client.HttpContext;
import com.mashape.p.spoonacularrecipefoodnutritionv1.models.DynamicResponse;

import java.text.ParseException;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity {
    String xMashapeKey = "9tGEoMPra7mshodjhkj1RmJTEVoDp1yFDcAjsnYTsabhBfMSjY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Timber.d("Hello! MainActivity\n");
        Configuration.initialize(this);
        SpoonacularAPIClient client = new SpoonacularAPIClient();
        APIController controller = client.getClient();
        controller.getSummarizeRecipeAsync(4632, new APICallBack<DynamicResponse>() {
            @Override
            public void onSuccess(HttpContext context, DynamicResponse response) {
                Timber.d("Success\n");
                try {
                    Timber.d(response.parseAsString());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(HttpContext context, Throwable error) {
                Timber.d("Failure\n");

            }
        });
    }
}
