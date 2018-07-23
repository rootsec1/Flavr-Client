package io.github.abhishekwl.flavr.Activities;

import android.annotation.SuppressLint;
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
import android.view.MenuItem;
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
import com.google.android.gms.wallet.CardInfo;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.gms.wallet.TransactionInfo;
import com.google.firebase.auth.FirebaseAuth;
import com.stripe.android.model.Token;
import io.github.abhishekwl.flavr.Adapters.CategoriesRecyclerViewAdapter;
import io.github.abhishekwl.flavr.Helpers.ApiClient;
import io.github.abhishekwl.flavr.Helpers.ApiInterface;
import io.github.abhishekwl.flavr.Models.Category;
import io.github.abhishekwl.flavr.Models.Constants;
import io.github.abhishekwl.flavr.Models.Food;
import io.github.abhishekwl.flavr.Models.Hotel;
import io.github.abhishekwl.flavr.Models.PaymentsUtil;
import io.github.abhishekwl.flavr.Models.User;
import io.github.abhishekwl.flavr.R;
import java.util.ArrayList;
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

  private static final int GOOGLE_PAY_RC_CODE = 908;
  private Unbinder unbinder;
  private FirebaseAuth firebaseAuth;
  private ApiInterface apiInterface;
  private MessageListener messageListener;
  private Hotel currentHotel;
  private ArrayList<Category> categoryArrayList = new ArrayList<>();
  private ArrayList<Food> allFoodItems = new ArrayList<>();
  private CategoriesRecyclerViewAdapter categoriesRecyclerViewAdapter;
  private String deviceCurrency;
  private PaymentsClient paymentsClient;
  private User currentUser;

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
    currentUser = getIntent().getParcelableExtra("USER");
    firebaseAuth = FirebaseAuth.getInstance();
    apiInterface = ApiClient.getRetrofit(getApplicationContext()).create(ApiInterface.class);
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
    deviceCurrency = Constants.CURRENCY_CODE;
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
    checkIfGooglePayIsAvailable();
  }

  private void checkIfGooglePayIsAvailable() {
    paymentsClient = PaymentsUtil.createPaymentsClient(MainActivity.this);
    PaymentsUtil.isReadyToPay(paymentsClient).addOnCompleteListener(
        task -> {
          try {
            boolean result = task.getResult(ApiException.class);
            if (!result) {
              notifyMessage("Google pay isn't available on your device. You won't be able to order without Google Pay :(");
            }
          } catch (ApiException exception) {
            notifyMessage(exception.getMessage());
          }
        });
  }

  private void initializeRecyclerView() {
    mainRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
    mainRecyclerView.setItemAnimator(new DefaultItemAnimator());
    mainRecyclerView.setHasFixedSize(true);
    categoriesRecyclerViewAdapter = new CategoriesRecyclerViewAdapter(getApplicationContext(), categoryArrayList);
    mainRecyclerView.setAdapter(categoriesRecyclerViewAdapter);
  }

  private void checkout(double transactionAmount) {
    String finalPriceInString = PaymentsUtil.microsToString((long) transactionAmount);
    TransactionInfo transactionInfo = PaymentsUtil.createTransaction(finalPriceInString);
    PaymentDataRequest paymentDataRequest = PaymentsUtil.createPaymentDataRequest(transactionInfo);
    Task<PaymentData> futurePaymentData = paymentsClient.loadPaymentData(paymentDataRequest);
    AutoResolveHelper.resolveTask(futurePaymentData, MainActivity.this, GOOGLE_PAY_RC_CODE);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    switch (requestCode) {
      case GOOGLE_PAY_RC_CODE:
        switch (resultCode) {
          case RESULT_OK:
            PaymentData paymentData = PaymentData.getFromIntent(data);
            handlePaymentSuccess(Objects.requireNonNull(paymentData));
            break;
          case RESULT_CANCELED:
            notifyMessage("Operation cancelled by the user.");
            break;
          case AutoResolveHelper.RESULT_ERROR:
            Status status = AutoResolveHelper.getStatusFromIntent(data);
            notifyMessage(Objects.requireNonNull(status).getStatusMessage());
        }
    }
  }

  private void handlePaymentSuccess(PaymentData paymentData) {
    String rawToken = Objects.requireNonNull(paymentData.getPaymentMethodToken()).getToken();
    CardInfo cardInfo = paymentData.getCardInfo();
    Token stripeToken = Token.fromString(rawToken);
    if (stripeToken!=null) performNetworkRequest(stripeToken.getId());
    notifyMessage("Payment successful :)\nWe'll notify you once your dish is done.");
  }

  private void performNetworkRequest(String id) {
    //TODO: Perform network request
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
      checkout(grandTotal);
    }
  }

  private class RetrieveFoodItemsForCart extends AsyncTask<ArrayList<Food>, Void, ArrayList<Food>> {

    @Override
    protected ArrayList<Food> doInBackground(ArrayList<Food>... arrayLists) {
      ArrayList<Food> selectedFoods = new ArrayList<>();
      for (Food food: arrayLists[0]) if (food.isFoodSelcted()) selectedFoods.add(food);
      return selectedFoods;
    }

    @Override
    protected void onPostExecute(ArrayList<Food> foodArrayListSelected) {
      super.onPostExecute(foodArrayListSelected);
      if (foodArrayListSelected.isEmpty()) notifyMessage("Your cart is currently empty.");
      else {
        Intent cartIntent = new Intent(MainActivity.this, CartActivity.class);
        cartIntent.putExtra("FOOD_CHECKOUT", foodArrayListSelected);
        startActivity(cartIntent);
      }
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.main_menu, menu);
    return true;
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.menuItemCart:
        new RetrieveFoodItemsForCart().execute(allFoodItems);
        break;

    }
    return super.onOptionsItemSelected(item);
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
}
