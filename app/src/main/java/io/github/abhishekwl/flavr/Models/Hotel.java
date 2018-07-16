package io.github.abhishekwl.flavr.Models;

import android.os.Parcel;
import com.google.gson.annotations.SerializedName;

public class Hotel implements android.os.Parcelable {

  @SerializedName("_id") private String id;
  @SerializedName("uid") private String uid;
  @SerializedName("name") private String name;
  @SerializedName("email") private String email;
  @SerializedName("phone") private String phone;
  @SerializedName("latitude") private double latitude;
  @SerializedName("longitude") private double longitude;

  public Hotel(String uid, String name, String email, String phone, double latitude, double longitude) {
    this.uid = uid;
    this.name = name;
    this.email = email;
    this.phone = phone;
    this.latitude = latitude;
    this.longitude = longitude;
  }

  public String getUid() {
    return uid;
  }

  public void setUid(String uid) {
    this.uid = uid;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
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

  public String getId() {
    return id;
  }

  public void set_id(String id) {
    this.id = id;
  }


  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(this.id);
    dest.writeString(this.uid);
    dest.writeString(this.name);
    dest.writeString(this.email);
    dest.writeString(this.phone);
    dest.writeDouble(this.latitude);
    dest.writeDouble(this.longitude);
  }

  private Hotel(Parcel in) {
    this.id = in.readString();
    this.uid = in.readString();
    this.name = in.readString();
    this.email = in.readString();
    this.phone = in.readString();
    this.latitude = in.readDouble();
    this.longitude = in.readDouble();
  }

  public static final Creator<Hotel> CREATOR = new Creator<Hotel>() {
    @Override
    public Hotel createFromParcel(Parcel source) {
      return new Hotel(source);
    }

    @Override
    public Hotel[] newArray(int size) {
      return new Hotel[size];
    }
  };
}
