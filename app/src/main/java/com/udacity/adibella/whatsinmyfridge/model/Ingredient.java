package com.udacity.adibella.whatsinmyfridge.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.udacity.adibella.whatsinmyfridge.util.JSONUtils;

public class Ingredient implements Parcelable{
    @SerializedName(JSONUtils.ID_KEY)
    @Expose
    private int id;
    @SerializedName(JSONUtils.NAME_KEY)
    @Expose
    private String name;
    @SerializedName(JSONUtils.AMOUNT_KEY)
    @Expose
    private String amount;
    @SerializedName(JSONUtils.UNIT_KEY)
    @Expose
    private String unit;
    @SerializedName(JSONUtils.IMAGE_KEY)
    @Expose
    private String image;
    @SerializedName(JSONUtils.ORIGINAL_STRING_KEY)
    @Expose
    private String originalString;

    public Ingredient() {

    }

    protected Ingredient(Parcel in) {
        id = in.readInt();
        name = in.readString();
        amount = in.readString();
        unit = in.readString();
        image = in.readString();
        originalString = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(amount);
        dest.writeString(unit);
        dest.writeString(image);
        dest.writeString(originalString);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Ingredient> CREATOR = new Creator<Ingredient>() {
        @Override
        public Ingredient createFromParcel(Parcel in) {
            return new Ingredient(in);
        }

        @Override
        public Ingredient[] newArray(int size) {
            return new Ingredient[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getOriginalString() {
        return originalString;
    }

    public void setOriginalString(String originalString) {
        this.originalString = originalString;
    }

    @Override
    public String toString() {
        return "Ingredient{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", amount='" + amount + '\'' +
                ", unit='" + unit + '\'' +
                ", image='" + image + '\'' +
                ", originalString='" + originalString + '\'' +
                '}';
    }
}
