package io.github.abhishekwl.flavr.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.abhishekwl.flavr.Models.Category;
import io.github.abhishekwl.flavr.R;
import java.util.ArrayList;

public class CategoriesRecyclerViewAdapter extends RecyclerView.Adapter<CategoriesRecyclerViewAdapter.CategoryViewHolder> {

  private ArrayList<Category> categoryArrayList;
  private Context context;

  public CategoriesRecyclerViewAdapter(Context context, ArrayList<Category> categoryArrayList) {
    this.context = context;
    this.categoryArrayList = categoryArrayList;
  }

  @NonNull
  @Override
  public CategoriesRecyclerViewAdapter.CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_list_item, parent, false);
    return new CategoryViewHolder(itemView);
  }

  @Override
  public void onBindViewHolder(@NonNull CategoriesRecyclerViewAdapter.CategoryViewHolder holder, int position) {
    Category category = categoryArrayList.get(position);
    holder.bind(holder.itemView.getContext(), category);
  }

  @Override
  public int getItemCount() {
    return categoryArrayList.size();
  }

  class CategoryViewHolder extends ViewHolder {

    @BindView(R.id.categoryListItemNameTextView)
    TextView categoryNameTextView;
    @BindView(R.id.categoryListItemsRecyclerView)
    RecyclerView categoryItemsRecyclerView;

    CategoryViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }

    void bind(Context context, Category category) {
      categoryNameTextView.setText(category.getCategoryName());
      categoryItemsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
      categoryItemsRecyclerView.setHasFixedSize(true);
      categoryItemsRecyclerView.setItemAnimator(new DefaultItemAnimator());
      categoryItemsRecyclerView.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));
      categoryItemsRecyclerView.setAdapter(new FoodItemsRecyclerViewAdapter(context, category.getFoodArrayList()));
    }
  }
}
