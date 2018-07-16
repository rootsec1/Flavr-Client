package io.github.abhishekwl.flavr.Activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.AutoResolveHelper;
import com.google.android.gms.wallet.CardRequirements;
import com.google.android.gms.wallet.IsReadyToPayRequest;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentMethodTokenizationParameters;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.gms.wallet.TransactionInfo;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;
import com.google.firebase.auth.FirebaseAuth;
import io.github.abhishekwl.flavr.Adapters.CategoriesRecyclerViewAdapter;
import io.github.abhishekwl.flavr.Helpers.ApiClient;
import io.github.abhishekwl.flavr.Helpers.ApiInterface;
import io.github.abhishekwl.flavr.Models.Category;
import io.github.abhishekwl.flavr.Models.Food;
import io.github.abhishekwl.flavr.Models.Hotel;
import io.github.abhishekwl.flavr.R;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.Locale;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

  @BindView(R.id.mainRecyclerView) RecyclerView mainRecyclerView;
  @BindView(R.id.mainSwipeRefreshLayout) SwipeRefreshLayout mainSwipeRefreshLayout;
  @BindColor(R.color.colorAccent) int colorAccent;
  @BindColor(R.color.colorPrimary) int colorPrimary;
  @BindColor(R.color.colorTextDark) int colorPrimaryDark;

  private Unbinder unbinder;
  private static final int LOAD_PAYMENT_DATA_REQUEST_CODE = 979;
  private FirebaseAuth firebaseAuth;
  private ApiInterface apiInterface;
  private MessageListener messageListener;
  private Hotel currentHotel;
  private ArrayList<Category> categoryArrayList = new ArrayList<>();
  private ArrayList<Food> allFoodItems = new ArrayList<>();
  private CategoriesRecyclerViewAdapter categoriesRecyclerViewAdapter;
  private PaymentsClient paymentsClient;
  private String deviceCurrency;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    unbinder = ButterKnife.bind(MainActivity.this);
    initializeComponents();
  }

  private void initializeComponents() {
    mainSwipeRefreshLayout.setColorSchemeColors(colorAccent, colorPrimary, colorPrimaryDark);
    mainSwipeRefreshLayout.setRefreshing(true);
    firebaseAuth = FirebaseAuth.getInstance();
    apiInterface = ApiClient.getRetrofit(getApplicationContext()).create(ApiInterface.class);
    paymentsClient = Wallet.getPaymentsClient(MainActivity.this, new Wallet.WalletOptions.Builder().setEnvironment(
        WalletConstants.ENVIRONMENT_TEST).build());
    messageListener = new MessageListener(){
      @Override
      public void onFound(Message message) {
        super.onFound(message);
        identifyHotel(message);
      }

      @Override
      public void onLost(Message message) {
        super.onLost(message);
        identifyHotel(message);
      }
    };
    Currency currency = Currency.getInstance(Locale.getDefault());
    deviceCurrency = currency.getCurrencyCode();
    identifyHotel(new Message("5b4779df0d8f281cf0f7bee8:bGxNp1kQ2UhNM0kpCevP3muvT2h2".getBytes())); //Remove in production
  }

  private void identifyHotel(Message message) {
    try {
      byte[] messageBytes = message.getContent();
      String foreignId = new String(messageBytes, "UTF-8");
      String credentialsArray[] = foreignId.split(":");
      String hotelUid = credentialsArray[1];

      apiInterface.getHotel(hotelUid).enqueue(new Callback<Hotel>() {
        @Override
        public void onResponse(@NonNull Call<Hotel> call, @NonNull Response<Hotel> response) {
          if (response.body()==null) notifyMessage("Can't seem to find a hotel tied up with us nearby you :(");
          else {
            currentHotel = response.body();
            onHotelDetected();
          }
        }

        @Override
        public void onFailure(@NonNull Call<Hotel> call, @NonNull Throwable t) {
          notifyMessage(t.getMessage());
        }
      });
    } catch (Exception ex) {
      notifyMessage(ex.getMessage());
    }
  }

  private void onHotelDetected() {
    Nearby.getMessagesClient(MainActivity.this).unsubscribe(messageListener);
    Objects.requireNonNull(getSupportActionBar()).setTitle(currentHotel.getName());
    notifyMessage("Welcome to ".concat(currentHotel.getName()).concat(" :)"));
    initializeViews();
    fetchFoodItems();
  }

  private void fetchFoodItems() {
    apiInterface.getFoodItemsFromHotel(currentHotel.getId(), currentHotel.getUid()).enqueue(
        new Callback<ArrayList<Food>>() {
          @SuppressWarnings("unchecked")
          @Override
          public void onResponse(@NonNull Call<ArrayList<Food>> call, @NonNull Response<ArrayList<Food>> response) {
            if (response.body()==null || response.body().isEmpty()) notifyMessage("Folks at the place you are at hasn't uploaded the menu here yet :|");
            else new SortFoodArrayListIntoCategories().execute(response.body());
          }

          @Override
          public void onFailure(@NonNull Call<ArrayList<Food>> call, @NonNull Throwable t) {
            notifyMessage(t.getMessage());
          }
        });
  }

  private class SortFoodArrayListIntoCategories extends AsyncTask<ArrayList<Food>, Void, ArrayList<Category>> {

    @Override
    protected ArrayList<Category> doInBackground(ArrayList<Food>... arrayLists) {
      allFoodItems = arrayLists[0];
      ArrayList<Category> categoryArrayList = new ArrayList<>();
      String[] allCategories = getResources().getStringArray(R.array.categories);
      for (String categoryName: allCategories) {
        ArrayList<Food> foodArrayListTemp = new ArrayList<>();
        for (Food food: arrayLists[0]) if (food.getCategory().equalsIgnoreCase(categoryName)) foodArrayListTemp.add(food);
        if (!foodArrayListTemp.isEmpty()) categoryArrayList.add(new Category(foodArrayListTemp));
      }
      return categoryArrayList;
    }

    @Override
    protected void onPostExecute(ArrayList<Category> categories) {
      super.onPostExecute(categories);
      categoryArrayList.clear();
      categoryArrayList.addAll(categories);
      mainSwipeRefreshLayout.setRefreshing(false);
      categoriesRecyclerViewAdapter.notifyDataSetChanged();
    }
  }

  private void initializeViews() {
    mainSwipeRefreshLayout.setRefreshing(true);
    initializeRecyclerView();
    mainSwipeRefreshLayout.setOnRefreshListener(this::fetchFoodItems);
  }

  private void initializeRecyclerView() {
    mainRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
    mainRecyclerView.setItemAnimator(new DefaultItemAnimator());
    mainRecyclerView.setHasFixedSize(true);
    categoriesRecyclerViewAdapter = new CategoriesRecyclerViewAdapter(getApplicationContext(), categoryArrayList);
    mainRecyclerView.setAdapter(categoriesRecyclerViewAdapter);
  }

  private void notifyMessage(String message) {
    if (mainSwipeRefreshLayout!=null && mainSwipeRefreshLayout.isRefreshing()) mainSwipeRefreshLayout.setRefreshing(false);
    Snackbar.make(mainRecyclerView, message, Snackbar.LENGTH_SHORT).show();
  }

  @SuppressWarnings("unchecked")
  @OnClick(R.id.checkoutButton)
  public void onCheckOutButtonPress() {
    new RetrieveSelectedFoods().execute(allFoodItems);
  }

  @SuppressLint("StaticFieldLeak")
  private class RetrieveSelectedFoods extends AsyncTask<ArrayList<Food>, Void, ArrayList<Food>> {

    private double grandTotal = 0;

    @Override
    protected ArrayList<Food> doInBackground(ArrayList<Food>... arrayLists) {
      ArrayList<Food> selectedFoods = new ArrayList<>();
      for (Food food: arrayLists[0]) if (food.isFoodSelcted()) {
        grandTotal+=food.getCost();
        selectedFoods.add(food);
      }
      return selectedFoods;
    }

    @Override
    protected void onPostExecute(ArrayList<Food> foodArrayList) {
      super.onPostExecute(foodArrayList);
      checkout(foodArrayList, grandTotal);
    }
  }

  private void checkout(ArrayList<Food> foodArrayList, double grandTotal) {
    IsReadyToPayRequest request =
        IsReadyToPayRequest.newBuilder()
            .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_CARD)
            .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_TOKENIZED_CARD)
            .build();
    Task<Boolean> task = paymentsClient.isReadyToPay(request);
    task.addOnCompleteListener(
        task1 -> {
          try {
            boolean result = task1.getResult(ApiException.class);
            if (result) {
              PaymentDataRequest paymentDataRequest = createPaymentDataRequest(grandTotal);
              AutoResolveHelper.resolveTask(paymentsClient.loadPaymentData(paymentDataRequest), MainActivity.this, LOAD_PAYMENT_DATA_REQUEST_CODE);
            } else {
              notifyMessage("Google Pay isn't supported on your phone :(");
              //TODO: Tez API
            }
          } catch (ApiException exception) {
            notifyMessage(exception.getMessage());
          }
        });
  }


  private PaymentDataRequest createPaymentDataRequest(double grandTotal) {
    PaymentDataRequest.Builder request =
        PaymentDataRequest.newBuilder()
            .setTransactionInfo(
                TransactionInfo.newBuilder()
                    .setTotalPriceStatus(WalletConstants.TOTAL_PRICE_STATUS_FINAL)
                    .setTotalPrice(Double.toString(grandTotal))
                    .setCurrencyCode(deviceCurrency)
                    .build())
            .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_CARD)
            .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_TOKENIZED_CARD)
            .setCardRequirements(
                CardRequirements.newBuilder()
                    .addAllowedCardNetworks(
                        Arrays.asList(
                            WalletConstants.CARD_NETWORK_AMEX,
                            WalletConstants.CARD_NETWORK_DISCOVER,
                            WalletConstants.CARD_NETWORK_VISA,
                            WalletConstants.CARD_NETWORK_MASTERCARD))
                    .build());

    PaymentMethodTokenizationParameters params = PaymentMethodTokenizationParameters.newBuilder()
            .setPaymentMethodTokenizationType(
                WalletConstants.PAYMENT_METHOD_TOKENIZATION_TYPE_PAYMENT_GATEWAY)
            .addParameter("gateway", "example")
            .addParameter("gatewayMerchantId", "exampleGatewayMerchantId")
            .build();

    request.setPaymentMethodTokenizationParameters(params);
    return request.build();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.main_menu, menu);
    return true;
  }

  @Override
  protected void onStart() {
    super.onStart();
    if (currentHotel==null) Nearby.getMessagesClient(MainActivity.this).subscribe(messageListener).addOnSuccessListener(aVoid -> notifyMessage("Looking for nearby hotels :)")).addOnFailureListener(e -> notifyMessage(e.getMessage()));
  }

  @Override
  protected void onStop() {
    Nearby.getMessagesClient(MainActivity.this).unsubscribe(messageListener);
    super.onStop();
  }

  @Override
  protected void onDestroy() {
    unbinder.unbind();
    super.onDestroy();
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
      case LOAD_PAYMENT_DATA_REQUEST_CODE:
        switch (resultCode) {
          case Activity.RESULT_OK:
            PaymentData paymentData = PaymentData.getFromIntent(data);
            String token = paymentData.getPaymentMethodToken().getToken();
            break;
          case Activity.RESULT_CANCELED:
            break;
          case AutoResolveHelper.RESULT_ERROR:
            Status status = AutoResolveHelper.getStatusFromIntent(data);
            // Log the status for debugging.
            // Generally, there is no need to show an error to
            // the user as the Google Pay API will do that.
            break;
          default:
            // Do nothing.
        }
        break;
      default:
        // Do nothing.
    }
  }
}
