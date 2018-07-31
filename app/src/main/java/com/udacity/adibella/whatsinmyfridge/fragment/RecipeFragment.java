package com.udacity.adibella.whatsinmyfridge.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.udacity.adibella.whatsinmyfridge.MainActivity;
import com.udacity.adibella.whatsinmyfridge.R;
import com.udacity.adibella.whatsinmyfridge.adapter.IngredientAdapter;
import com.udacity.adibella.whatsinmyfridge.model.Recipe;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class RecipeFragment extends Fragment implements View.OnClickListener{
    @BindView(R.id.tv_recipe_title)
    TextView tvRecipeTitle;
    @BindView(R.id.tv_recipe_summary)
    TextView tvRecipeSummary;
    @BindView(R.id.app_bar)
    AppBarLayout abAppBar;
    @BindView(R.id.collapsing_toolbar_layout)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.iv_recipe_image)
    ImageView ivRecipeImage;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.favorite_fab)
    FloatingActionButton favoriteFab;
    @BindView(R.id.meta_bar)
    LinearLayout metaBar;
    @BindView(R.id.rv_ingredients)
    RecyclerView rvIngedients;
    @BindView(R.id.tv_instructions_label)
    TextView tvInstructionsLabel;
    @BindView(R.id.tv_instructions)
    TextView tvInstructions;
    @BindView(R.id.tv_website_label)
    TextView tvWebSiteLabel;
    @BindView(R.id.tv_website)
    TextView tvWebSite;

    private Recipe recipe;
    private IngredientAdapter ingredientAdapter;

    public RecipeFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.recipe_activity_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_share_action) {
            Timber.d("share action");
            startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                    .setType("text/plain")
                    .setText("Some sample text")
                    .getIntent(), getString(R.string.action_share)));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            recipe = savedInstanceState.getParcelable(MainActivity.RECIPE_KEY);
        }
        View rootView = inflater.inflate(R.layout.fragment_recipe_detail, container, false);

        ButterKnife.bind(this, rootView);

        favoriteFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Timber.d("Recipe added to favorites");
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar.setNavigationOnClickListener(this);
        }

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        tvRecipeSummary.setTypeface(Typeface.createFromAsset(getResources().getAssets(), "Rosario-Regular.ttf"));

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvIngedients.setLayoutManager(layoutManager);
        ingredientAdapter = new IngredientAdapter();
        rvIngedients.setAdapter(ingredientAdapter);

        if (recipe != null) {
            rootView.setAlpha(0);
            rootView.setVisibility(View.VISIBLE);
            rootView.animate().alpha(1);
            tvRecipeTitle.setText(recipe.getTitle());
            if (Build.VERSION.SDK_INT >= 24) {
                tvRecipeSummary.setText(Html.fromHtml(recipe.getSummary().replaceAll("(\r\n|\n)", "<br />"), Html.FROM_HTML_MODE_LEGACY));
            } else {
                tvRecipeSummary.setText(Html.fromHtml(recipe.getSummary().replaceAll("(\r\n|\n)", "<br />")));
            }
            tvRecipeSummary.setMovementMethod(LinkMovementMethod.getInstance());
            int imageWidth = (int) getResources().getDimension(R.dimen.image_width);
            int imageHeight = (int) getResources().getDimension(R.dimen.image_height);
            Picasso.get()
                    .load(recipe.getImage())
                    .resize(imageWidth, imageHeight)
                    .centerInside()
                    .placeholder(R.drawable.progress_animation)
                    .error(R.drawable.default_image)
                    .into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            Timber.d("onBitmapLoaded");
                            assert ivRecipeImage != null;
                            ivRecipeImage.setImageBitmap(bitmap);
                            Palette.from(bitmap)
                                    .generate(new Palette.PaletteAsyncListener() {
                                        @Override
                                        public void onGenerated(@NonNull Palette palette) {
                                            Palette.Swatch textSwatch = palette.getVibrantSwatch();
                                            if (textSwatch == null) {
                                                Timber.d("Null swatch");
                                                return;
                                            }
                                            metaBar.setBackgroundColor(textSwatch.getRgb());
                                            tvRecipeTitle.setTextColor(textSwatch.getTitleTextColor());
                                        }
                                    });
                        }

                        @Override
                        public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                            Timber.d("error: " + e.toString() + "; " + errorDrawable.toString());
                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {

                        }
                    });
            ingredientAdapter.setIngredients(recipe.getIngredients());
            if (recipe.getInstructions().equals("null")) {
                tvInstructionsLabel.setVisibility(View.GONE);
                tvInstructions.setVisibility(View.GONE);
            } else {
                tvInstructions.setText(recipe.getInstructions().replaceAll("([.!?][ ]?)", "$1\n"));
            }
            if (!recipe.getSourceUrl().equals("null")) {
                String url = "<a href=\""+recipe.getSourceUrl()+"\">";
                if (!recipe.getSourceName().equals("null")) {
                    url += recipe.getSourceName();
                } else {
                    url += "Recipe URL";
                }
                url += "</a>";
                if (Build.VERSION.SDK_INT >= 24) {
                    tvWebSite.setText(Html.fromHtml(url, Html.FROM_HTML_MODE_LEGACY));
                } else {
                    tvWebSite.setText(Html.fromHtml(url));
                }
                tvWebSite.setMovementMethod(LinkMovementMethod.getInstance());
            } else {
                tvWebSiteLabel.setVisibility(View.GONE);
                tvWebSite.setVisibility(View.GONE);
            }
        } else {
            rootView.setVisibility(View.GONE);
            tvRecipeTitle.setText("N/A");
        }

        abAppBar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (Math.abs(verticalOffset)-appBarLayout.getTotalScrollRange() == 0) {
                    //Collapsed
                    collapsingToolbarLayout.setTitle(tvRecipeTitle.getText());
                } else {
                    //Expanded
                    collapsingToolbarLayout.setTitle("");
                }
            }
        });

        return rootView;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelable(MainActivity.RECIPE_KEY, recipe);
    }

    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }

    @Override
    public void onClick(View v) {
        NavUtils.navigateUpFromSameTask(getActivity());
    }
}
