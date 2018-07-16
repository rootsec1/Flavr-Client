package io.github.abhishekwl.flavr.Models;

import android.os.Parcel;
import com.google.gson.annotations.SerializedName;

public class Food implements android.os.Parcelable {

  @SerializedName("_id") private String id;
  @SerializedName("name") private String name;
  @SerializedName("cost") private double cost;
  @SerializedName("image") private String image;
  @SerializedName("category") private String category;
  @SerializedName("hotel") private Hotel hotel;
  private boolean foodSelcted;

  public Food(String name, double cost, String image, String category, Hotel hotel) {
    this.name = name;
    this.cost = cost;
    this.image = image;
    this.category = category;
    this.hotel = hotel;
    this.foodSelcted = false;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public double getCost() {
    return cost;
  }

  public void setCost(double cost) {
    this.cost = cost;
  }

  public String getImage() {
    return image;
  }

  public void setImage(String image) {
    this.image = image;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public Hotel getHotel() {
    return hotel;
  }

  public void setHotel(Hotel hotel) {
    this.hotel = hotel;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }


  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(this.id);
    dest.writeString(this.name);
    dest.writeDouble(this.cost);
    dest.writeString(this.image);
    dest.writeString(this.category);
    dest.writeParcelable(this.hotel, flags);
  }

  private Food(Parcel in) {
    this.id = in.readString();
    this.name = in.readString();
    this.cost = in.readDouble();
    this.image = in.readString();
    this.category = in.readString();
    this.hotel = in.readParcelable(Hotel.class.getClassLoader());
  }

  public static final Creator<Food> CREATOR = new Creator<Food>() {
    @Override
    public Food createFromParcel(Parcel source) {
      return new Food(source);
    }

    @Override
    public Food[] newArray(int size) {
      return new Food[size];
    }
  };

  public boolean isFoodSelcted() {
    return foodSelcted;
  }

  public void setFoodSelcted(boolean foodSelcted) {
    this.foodSelcted = foodSelcted;
  }
}
