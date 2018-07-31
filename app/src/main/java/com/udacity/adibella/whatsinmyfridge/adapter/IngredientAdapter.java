package com.udacity.adibella.whatsinmyfridge.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.udacity.adibella.whatsinmyfridge.R;
import com.udacity.adibella.whatsinmyfridge.model.Ingredient;
import com.udacity.adibella.whatsinmyfridge.util.NetworkUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class IngredientAdapter extends RecyclerView.Adapter<IngredientAdapter.IngredientViewHolder> {
    private List<Ingredient> ingredients;
    private Context context;

    public IngredientAdapter() {

    }

    @NonNull
    @Override
    public IngredientAdapter.IngredientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.ingredient_item, parent, false);
        return new IngredientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final IngredientAdapter.IngredientViewHolder holder, int position) {
        Ingredient ingredient = ingredients.get(position);
        holder.tvIngredient.setText(ingredient.getOriginalString());
        if (!TextUtils.isEmpty(ingredient.getImage())) {
            Picasso.get()
                    .load(ingredient.getImage())
                    .fit()
                    .placeholder(R.drawable.progress_animation)
                    .error(R.drawable.default_image)
                    .into(holder.ivIngredientImage, new Callback() {
                        @Override
                        public void onSuccess() {
//                            Timber.d("Image loaded!");
                        }

                        @Override
                        public void onError(Exception e) {
                            NetworkUtils.checkConnection(holder.ivIngredientImage.getContext());
                        }
                    });
        } else {
            Picasso.get()
                    .load(R.drawable.default_image)
                    .fit()
                    .placeholder(R.drawable.progress_animation)
                    .error(R.drawable.default_image)
                    .into(holder.ivIngredientImage);
        }
    }

    @Override
    public int getItemCount() {
        if (ingredients != null) {
            return ingredients.size();
        }
        return 0;
    }

    class IngredientViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_ingredient_image)
        ImageView ivIngredientImage;
        @BindView(R.id.tv_ingredient)
        TextView tvIngredient;

        public IngredientViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,  itemView);
        }
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
        notifyDataSetChanged();
    }
}
