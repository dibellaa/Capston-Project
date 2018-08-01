package com.udacity.adibella.whatsinmyfridge.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.mashape.p.spoonacularrecipefoodnutritionv1.models.DynamicResponse;
import com.mashape.p.spoonacularrecipefoodnutritionv1.models.FindByIngredientsModel;
import com.udacity.adibella.whatsinmyfridge.util.JSONUtils;

import org.json.JSONObject;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

public class Recipe implements Parcelable {
    @SerializedName(JSONUtils.ID_KEY)
    @Expose
    private int id;
    @SerializedName(JSONUtils.TITLE_KEY)
    @Expose
    private String title;
    @SerializedName(JSONUtils.IMAGE_KEY)
    @Expose
    private String image;
    @SerializedName(JSONUtils.SUMMARY_KEY)
    @Expose
    private String summary;
    @SerializedName(JSONUtils.INGREDIENTS_KEY)
    @Expose
    private List<Ingredient> ingredients;
    @SerializedName(JSONUtils.INSTRUCTIONS_KEY)
    @Expose
    private String instructions;
    @SerializedName(JSONUtils.SOURCE_URL_KEY)
    @Expose
    private String sourceUrl;
    @SerializedName(JSONUtils.SOURCE_NAME_KEY)
    @Expose
    private String sourceName;
    @SerializedName(JSONUtils.MISSED_INGREDIENT_KEY)
    @Expose
    private int missedIngredientCount;

    public Recipe() {

    }

    protected Recipe(Parcel in) {
        id = in.readInt();
        title = in.readString();
        image = in.readString();
        summary = in.readString();
        ingredients = in.createTypedArrayList(Ingredient.CREATOR);
        instructions = in.readString();
        sourceUrl = in.readString();
        sourceName = in.readString();
        missedIngredientCount = in.readInt();
    }

    public static final Creator<Recipe> CREATOR = new Creator<Recipe>() {
        @Override
        public Recipe createFromParcel(Parcel in) {
            return new Recipe(in);
        }

        @Override
        public Recipe[] newArray(int size) {
            return new Recipe[size];
        }
    };

    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(image);
        dest.writeString(summary);
        dest.writeTypedList(ingredients);
        dest.writeString(instructions);
        dest.writeString(sourceUrl);
        dest.writeString(sourceName);
        dest.writeInt(missedIngredientCount);
    }

    @Override
    public String toString() {
        return "Recipe{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", image='" + image + '\'' +
                ", summary='" + summary + '\'' +
                ", ingredients=" + ingredients.toString() +
                ", instructions='" + instructions + '\'' +
                ", sourceUrl='" + sourceUrl + '\'' +
                ", sourceName='" + sourceName + '\'' +
                ", missedIngredientCount=" + missedIngredientCount +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public int getMissedIngredientCount() {
        return missedIngredientCount;
    }

    public void setMissedIngredientCount(int missedIngredientCount) {
        this.missedIngredientCount = missedIngredientCount;
    }

    public void addInfoFromIngredientsModel(FindByIngredientsModel findByIngredientsModel) {
        this.id = findByIngredientsModel.getId();
        this.image = findByIngredientsModel.getImage();
        this.missedIngredientCount = findByIngredientsModel.getMissedIngredientCount();
        this.title = findByIngredientsModel.getTitle();
    }

    public void addRecipeInformation(DynamicResponse response) {
        try {
            Map<String, Object> responseMap = response.parseAsDictionary();
            JSONObject responseJson = new JSONObject(responseMap);
            JSONUtils.getRecipeInfoFromJson(this, responseJson);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void addRecipeSummary(DynamicResponse response) {
        try {
            Map<String, Object> responseMap = response.parseAsDictionary();
            JSONObject responseJson = new JSONObject(responseMap);
            JSONUtils.getRecipeSummaryFromJson(this, responseJson);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void addRecipeInformationFromFile(Context context) {
        JSONObject responseJson = JSONUtils.readRecipeInformationFromFile(context, this);
        JSONUtils.getRecipeInfoFromJson(this, responseJson);
    }

    public void addRecipeSummaryFromFile(Context context) {
        JSONObject responseJson = JSONUtils.readRecipeSummaryFromFile(context, this);
        JSONUtils.getRecipeSummaryFromJson(this, responseJson);
    }

    public String getIngredientsGson() {
        Gson gson = new Gson();
        return gson.toJson(ingredients);
    }
}
