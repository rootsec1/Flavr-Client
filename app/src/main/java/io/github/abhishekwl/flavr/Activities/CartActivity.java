package io.github.abhishekwl.flavr.Activities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.github.abhishekwl.flavr.Adapters.FoodItemsRecyclerViewAdapter;
import io.github.abhishekwl.flavr.Models.Food;
import io.github.abhishekwl.flavr.R;
import java.util.ArrayList;

public class CartActivity extends AppCompatActivity {

  @BindView(R.id.cartRecyclerView)
  RecyclerView cartRecyclerView;
  @BindView(R.id.cartCheckoutFAB)
  FloatingActionButton cartCheckoutFAB;

  private ArrayList<Food> foodArrayList;
  private FoodItemsRecyclerViewAdapter foodItemsRecyclerViewAdapter;
  private Unbinder unbinder;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_cart);

    unbinder = ButterKnife.bind(CartActivity.this);
    initializeComponents();
    initializeViews();
  }

  private void initializeComponents() {
    foodArrayList = getIntent().getParcelableArrayListExtra("FOOD_CHECKOUT");
    if (foodArrayList.isEmpty()) foodArrayList = new ArrayList<>();
    for (Food food: foodArrayList) food.setFoodSelcted(true);
    foodItemsRecyclerViewAdapter = new FoodItemsRecyclerViewAdapter(getApplicationContext(), foodArrayList);
  }

  private void initializeViews() {
    cartRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
    cartRecyclerView.setHasFixedSize(true);
    cartRecyclerView.setItemAnimator(new DefaultItemAnimator());
    cartRecyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL));
    cartRecyclerView.setAdapter(foodItemsRecyclerViewAdapter);
  }

  @Override
  protected void onDestroy() {
    unbinder.unbind();
    super.onDestroy();
  }
}
