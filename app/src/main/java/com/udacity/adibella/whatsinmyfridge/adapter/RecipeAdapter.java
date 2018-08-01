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
import com.udacity.adibella.whatsinmyfridge.model.Recipe;
import com.udacity.adibella.whatsinmyfridge.util.NetworkUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {
    private final int layoutResourceId;
    private final Context context;
    private final OnRecipeClickListener clickListener;

    private List<Recipe> recipes;

    public RecipeAdapter(int layoutResourceId, Context context, OnRecipeClickListener clickListener, List<Recipe> recipes) {
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.clickListener = clickListener;
        this.recipes = recipes;
    }

    public interface OnRecipeClickListener {
        void onRecipeSelected(View view, Recipe recipe);
    }

    public void setRecipes(List<Recipe> recipes) {
        this.recipes = recipes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecipeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View rootView = layoutInflater.inflate(layoutResourceId, parent, false);
        return new ViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecipeAdapter.ViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);
        holder.recipeName.setText(recipe.getTitle());
        if (!TextUtils.isEmpty(recipe.getImage())) {
            Picasso.get()
                    .load(recipe.getImage())
                    .fit()
                    .centerInside()
                    .placeholder(R.drawable.progress_animation)
                    .error(R.drawable.default_image)
                    .into(holder.recipeImage, new Callback() {
                        @Override
                        public void onSuccess() {
//                            Timber.d("Image loaded!");
                        }

                        @Override
                        public void onError(Exception e) {
                            NetworkUtils.checkConnection(holder.recipeImage.getContext());
                        }
                    });
        } else {
            Picasso.get()
                    .load(R.drawable.default_image)
                    .fit()
                    .centerInside()
                    .placeholder(R.drawable.progress_animation)
                    .error(R.drawable.default_image)
                    .into(holder.recipeImage);
        }
    }

    @Override
    public int getItemCount() {
        if (recipes != null) {
            return recipes.size();
        }
        return 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.iv_recipe_image)
        public ImageView recipeImage;
        @BindView(R.id.tv_recipe_name)
        TextView recipeName;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            recipeImage.setOnClickListener(this);
            recipeName.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            Recipe recipe = recipes.get(position);
            clickListener.onRecipeSelected(v, recipe);
        }
    }
}
