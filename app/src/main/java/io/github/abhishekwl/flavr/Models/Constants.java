package io.github.abhishekwl.flavr.Models;

import android.util.Pair;
import com.google.android.gms.wallet.WalletConstants;
import java.util.Arrays;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

public class Constants {

  //PayTm
  public static final String M_ID = "Vector37314639002764";
  public static final String CHANNEL_ID = "WAP";
  public static final String INDUSTRY_TYPE_ID = "Retail";
  public static final String WEBSITE = "http://abhishekwl.github.io/";
  public static final String CALLBACK_URL = "https://pguat.paytm.com/paytmchecksum/paytmCallback.jsp";

  //Google Pay
  public static final int PAYMENTS_ENVIRONMENT = WalletConstants.ENVIRONMENT_TEST;
  public static final List<Integer> SUPPORTED_NETWORKS = Arrays.asList(
      WalletConstants.CARD_NETWORK_AMEX,
      WalletConstants.CARD_NETWORK_VISA,
      WalletConstants.CARD_NETWORK_MASTERCARD,
      WalletConstants.CARD_NETWORK_OTHER
  );
  public static final List<Integer> SUPPORTED_METHODS = Arrays.asList(
      WalletConstants.PAYMENT_METHOD_CARD,
      WalletConstants.PAYMENT_METHOD_TOKENIZED_CARD
  );
  public static final String CURRENCY_CODE = Currency.getInstance(Locale.getDefault()).getCurrencyCode();
  public static final String GATEWAY_TOKENIZATION_NAME = "stripe";
  public static final List<Pair<String, String>> GATEWAY_TOKENIZATION_PARAMETERS = Collections
      .singletonList(
          Pair.create("gatewayMerchantId", "exampleGatewayMerchantId")
      );
  public static final String STRIPE_API_KEY = "pk_test_oAKuNzwMpYBjGdEM3QI1i4F1";
  public static final String DIRECT_TOKENIZATION_PUBLIC_KEY = "REPLACE_ME";
}
