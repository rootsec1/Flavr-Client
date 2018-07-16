package io.github.abhishekwl.flavr.Models;

import android.os.Parcel;
import com.google.gson.annotations.SerializedName;

public class User implements android.os.Parcelable {

  @SerializedName("_id") private String id;
  @SerializedName("uid") private String uid;
  @SerializedName("name") private String name;
  @SerializedName("email") private String email;
  @SerializedName("phone") private String phone;
  @SerializedName("image") private String image;

  public User() {}

  public User(String uid, String name, String email, String phone) {
    this.uid = uid;
    this.name = name;
    this.email = email;
    this.phone = phone;
  }

  public User(String uid, String name, String email, String phone, String image) {
    this.uid = uid;
    this.name = name;
    this.email = email;
    this.phone = phone;
    this.image = image;
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

  public String getImage() {
    return image;
  }

  public void setImage(String image) {
    this.image = image;
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
    dest.writeString(this.image);
  }

  private User(Parcel in) {
    this.id = in.readString();
    this.uid = in.readString();
    this.name = in.readString();
    this.email = in.readString();
    this.phone = in.readString();
    this.image = in.readString();
  }

  public static final Creator<User> CREATOR = new Creator<User>() {
    @Override
    public User createFromParcel(Parcel source) {
      return new User(source);
    }

    @Override
    public User[] newArray(int size) {
      return new User[size];
    }
  };
}
