package io.github.abhishekwl.flavr.Models;


import android.app.Activity;
import android.util.Pair;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.CardRequirements;
import com.google.android.gms.wallet.IsReadyToPayRequest;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentMethodTokenizationParameters;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.gms.wallet.TransactionInfo;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class PaymentsUtil {

  private static final BigDecimal MICROS = new BigDecimal(1000000d);

  private PaymentsUtil() {

  }

  public static PaymentsClient createPaymentsClient(Activity activity) {
    Wallet.WalletOptions walletOptions = new Wallet.WalletOptions.Builder()
        .setEnvironment(Constants.PAYMENTS_ENVIRONMENT)
        .build();

    return Wallet.getPaymentsClient(activity, walletOptions);

  }

  public static PaymentDataRequest createPaymentDataRequest(TransactionInfo transactionInfo) {
    PaymentMethodTokenizationParameters.Builder paramsBuilder = PaymentMethodTokenizationParameters.newBuilder()
            .setPaymentMethodTokenizationType(WalletConstants.PAYMENT_METHOD_TOKENIZATION_TYPE_PAYMENT_GATEWAY)
            .addParameter("gateway", Constants.GATEWAY_TOKENIZATION_NAME)
            .addParameter("gatewayMerchantId", Constants.STRIPE_API_KEY)
            .addParameter("stripe:publishableKey", Constants.STRIPE_API_KEY)
            .addParameter("publicKey", Constants.STRIPE_API_KEY)
            .addParameter("stripe:version", "5.1.0");
    for (Pair<String, String> param : Constants.GATEWAY_TOKENIZATION_PARAMETERS) {
      paramsBuilder.addParameter(param.first, param.second);
    }
    return createPaymentDataRequest(transactionInfo, paramsBuilder.build());
  }

  public static PaymentDataRequest createPaymentDataRequestDirect(TransactionInfo transactionInfo) {
    PaymentMethodTokenizationParameters params = PaymentMethodTokenizationParameters.newBuilder()
            .setPaymentMethodTokenizationType(WalletConstants.PAYMENT_METHOD_TOKENIZATION_TYPE_DIRECT)
            .addParameter("gatewayMerchantId", Constants.STRIPE_API_KEY)
            .addParameter("stripe:publishableKey", Constants.STRIPE_API_KEY)
            .addParameter("publicKey", Constants.DIRECT_TOKENIZATION_PUBLIC_KEY)
            .addParameter("stripe:version", "5.1.0")
            .build();
    return createPaymentDataRequest(transactionInfo, params);
  }


  private static PaymentDataRequest createPaymentDataRequest(TransactionInfo transactionInfo,
      PaymentMethodTokenizationParameters params) {

    return PaymentDataRequest.newBuilder()
        .setPhoneNumberRequired(true)
        .setEmailRequired(true)
        .setTransactionInfo(transactionInfo)
        .addAllowedPaymentMethods(Constants.SUPPORTED_METHODS)
        .setCardRequirements(
            CardRequirements.newBuilder()
                .addAllowedCardNetworks(Constants.SUPPORTED_NETWORKS)
                .setAllowPrepaidCards(true)
                .setBillingAddressRequired(true)
                .setBillingAddressFormat(WalletConstants.BILLING_ADDRESS_FORMAT_FULL)
                .build())
        .setPaymentMethodTokenizationParameters(params)
        .setUiRequired(true)
        .build();

  }

  public static Task<Boolean> isReadyToPay(PaymentsClient client) {
    IsReadyToPayRequest.Builder request = IsReadyToPayRequest.newBuilder();
    for (Integer allowedMethod : Constants.SUPPORTED_METHODS) {
      request.addAllowedPaymentMethod(allowedMethod);
    }
    return client.isReadyToPay(request.build());
  }


  public static TransactionInfo createTransaction(String price) {
    return TransactionInfo.newBuilder()
        .setTotalPriceStatus(WalletConstants.TOTAL_PRICE_STATUS_FINAL)
        .setTotalPrice(price)
        .setCurrencyCode(Constants.CURRENCY_CODE)
        .build();
  }


  public static String microsToString(long micros) {
    //noinspection BigDecimalMethodWithoutRoundingCalled
    return new BigDecimal(micros).divide(MICROS).setScale(2, RoundingMode.HALF_EVEN).toString();

  }

}