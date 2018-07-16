package io.github.abhishekwl.flavr.Models;

import android.os.Parcel;
import java.util.ArrayList;

public class Category implements android.os.Parcelable {

  private String categoryName;
  private String categoryImageUrl;
  private ArrayList<Food> foodArrayList;

  public Category(ArrayList<Food> foodArrayList) {
    this.foodArrayList = foodArrayList.isEmpty()? new ArrayList<>() : foodArrayList;
    this.categoryName = foodArrayList.get(0).getCategory();
    this.categoryImageUrl = foodArrayList.get(0).getImage();
  }

  public String getCategoryName() {
    return categoryName;
  }

  public void setCategoryName(String categoryName) {
    this.categoryName = categoryName;
  }

  public String getCategoryImageUrl() {
    return categoryImageUrl;
  }

  public void setCategoryImageUrl(String categoryImageUrl) {
    this.categoryImageUrl = categoryImageUrl;
  }

  public ArrayList<Food> getFoodArrayList() {
    return foodArrayList;
  }

  public void setFoodArrayList(
      ArrayList<Food> foodArrayList) {
    this.foodArrayList = foodArrayList;
  }


  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(this.categoryName);
    dest.writeString(this.categoryImageUrl);
    dest.writeTypedList(this.foodArrayList);
  }

  private Category(Parcel in) {
    this.categoryName = in.readString();
    this.categoryImageUrl = in.readString();
    this.foodArrayList = in.createTypedArrayList(Food.CREATOR);
  }

  public static final Creator<Category> CREATOR = new Creator<Category>() {
    @Override
    public Category createFromParcel(Parcel source) {
      return new Category(source);
    }

    @Override
    public Category[] newArray(int size) {
      return new Category[size];
    }
  };
}
