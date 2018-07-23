package io.github.abhishekwl.flavr.Helpers;

import io.github.abhishekwl.flavr.Models.Checksum;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface PaytmApiInterface {

  //Paytm
  @FormUrlEncoded
  @POST("generate_checksum")
  Call<Checksum> getChecksum(
      @Field("MID") String mId,
      @Field("ORDER_ID") String orderId,
      @Field("CUST_ID") String custId,
      @Field("CHANNEL_ID") String channelId,
      @Field("TXN_AMOUNT") String txnAmount,
      @Field("WEBSITE") String website,
      @Field("CALLBACK_URL") String callbackUrl,
      @Field("INDUSTRY_TYPE_ID") String industryTypeId
  );

}
