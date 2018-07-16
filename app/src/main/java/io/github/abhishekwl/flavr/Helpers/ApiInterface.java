package io.github.abhishekwl.flavr.Helpers;

import io.github.abhishekwl.flavr.Models.Food;
import io.github.abhishekwl.flavr.Models.Hotel;
import io.github.abhishekwl.flavr.Models.User;
import java.util.ArrayList;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiInterface {

  //USERS
  @FormUrlEncoded
  @POST("users")
  Call<User> createNewUser(@Field("uid") String uid, @Field("name") String name, @Field("email") String email, @Field("phone") String phone, @Field("image") String image);
  @GET("users/{uid}")
  Call<User> getUser(@Path("uid") String uid);

  //HOTEL
  @GET("hotels/{uid}")
  Call<Hotel> getHotel(@Path("uid") String uid);

  //FOOD
  @GET("food/{id}")
  Call<ArrayList<Food>> getFoodItemsFromHotel(@Path("id") String hotelId, @Query("uid") String hotelUid);

}
