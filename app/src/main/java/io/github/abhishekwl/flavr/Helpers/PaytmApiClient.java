package io.github.abhishekwl.flavr.Helpers;

import android.content.Context;
import io.github.abhishekwl.flavr.R;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PaytmApiClient {
  private static Retrofit retrofit = null;

  public static Retrofit getRetrofit(Context context) {
    if (retrofit==null)
      retrofit = new Retrofit.Builder().baseUrl(context.getString(R.string.paytm_url)).addConverterFactory(GsonConverterFactory.create()).build();
    return retrofit;
  }
}
