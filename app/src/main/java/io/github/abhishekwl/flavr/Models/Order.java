package io.github.abhishekwl.flavr.Models;

import android.os.Parcel;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;

public class Order implements android.os.Parcelable {

  @SerializedName("_id") private String id;
  @SerializedName("uid") private String uid;  //Hotel UID
  @SerializedName("latitude") private double latitude;
  @SerializedName("longitude") private double longitude;
  @SerializedName("user") private User user;
  @SerializedName("foodList") private ArrayList<Food> foodArrayList;

  public Order() {}

  public Order(String uid, double latitude, double longitude, User user, ArrayList<Food> foodArrayList) {
    this.uid = uid;
    this.latitude = latitude;
    this.longitude = longitude;
    this.user = user;
    this.foodArrayList = foodArrayList;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getUid() {
    return uid;
  }

  public void setUid(String uid) {
    this.uid = uid;
  }

  public double getLatitude() {
    return latitude;
  }

  public void setLatitude(double latitude) {
    this.latitude = latitude;
  }

  public double getLongitude() {
    return longitude;
  }

  public void setLongitude(double longitude) {
    this.longitude = longitude;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
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
    dest.writeString(this.id);
    dest.writeString(this.uid);
    dest.writeDouble(this.latitude);
    dest.writeDouble(this.longitude);
    dest.writeParcelable(this.user, flags);
    dest.writeTypedList(this.foodArrayList);
  }

  protected Order(Parcel in) {
    this.id = in.readString();
    this.uid = in.readString();
    this.latitude = in.readDouble();
    this.longitude = in.readDouble();
    this.user = in.readParcelable(User.class.getClassLoader());
    this.foodArrayList = in.createTypedArrayList(Food.CREATOR);
  }

  public static final Creator<Order> CREATOR = new Creator<Order>() {
    @Override
    public Order createFromParcel(Parcel source) {
      return new Order(source);
    }

    @Override
    public Order[] newArray(int size) {
      return new Order[size];
    }
  };
}
