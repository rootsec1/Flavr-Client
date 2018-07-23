package io.github.abhishekwl.flavr.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.bumptech.glide.Glide;
import io.github.abhishekwl.flavr.Models.Food;
import io.github.abhishekwl.flavr.R;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Locale;

public class FoodItemsRecyclerViewAdapter extends RecyclerView.Adapter<FoodItemsRecyclerViewAdapter.FoodItemViewHolder> {

  private Context context;
  private ArrayList<Food> foodArrayList;
  private String currencyCode;

  public FoodItemsRecyclerViewAdapter(Context context, ArrayList<Food> foodArrayList) {
    this.context = context;
    this.foodArrayList = foodArrayList;
    Currency currency = Currency.getInstance(Locale.getDefault());
    this.currencyCode = currency.getCurrencyCode();
  }

  @NonNull
  @Override
  public FoodItemsRecyclerViewAdapter.FoodItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.food_list_item, parent, false);
    return new FoodItemViewHolder(itemView);
  }

  @Override
  public void onBindViewHolder(@NonNull FoodItemsRecyclerViewAdapter.FoodItemViewHolder holder, int position) {
    Food food = foodArrayList.get(position);
    holder.bind(holder.itemView.getContext(), food);
  }

  @Override
  public int getItemCount() {
    return foodArrayList.size();
  }

  class FoodItemViewHolder extends ViewHolder {

    @BindView(R.id.foodListItemCheckBox) CheckBox itemCheckBox;
    @BindView(R.id.foodListItemImageView) ImageView itemImageView;
    @BindView(R.id.foodListItemNameTextView) TextView itemNameTextView;
    @BindView(R.id.foodListItemCostTextView) TextView itemCostTextView;
    @BindView(R.id.foodListItemRootLayout) LinearLayout rootLinearLayout;

    FoodItemViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }

    void bind(Context context, Food food) {
      Glide.with(context).load(food.getImage()).into(itemImageView);
      itemNameTextView.setText(food.getName());
      itemCostTextView.setText(currencyCode.concat(" ").concat(Double.toString(food.getCost())));
      itemCheckBox.setChecked(food.isFoodSelcted());
      itemCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> food.setFoodSelcted(isChecked));
      rootLinearLayout.setOnClickListener(v -> {
        if (food.isFoodSelcted()) {
          food.setFoodSelcted(false);
          itemCheckBox.setChecked(false);
        } else {
          food.setFoodSelcted(true);
          itemCheckBox.setChecked(true);
        }
      });
    }
  }
}
