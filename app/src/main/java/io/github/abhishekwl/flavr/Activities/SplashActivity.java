package io.github.abhishekwl.flavr.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.util.NumberUtils;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import io.github.abhishekwl.flavr.Helpers.ApiClient;
import io.github.abhishekwl.flavr.Helpers.ApiInterface;
import io.github.abhishekwl.flavr.Models.User;
import io.github.abhishekwl.flavr.R;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashActivity extends AppCompatActivity {

  @BindView(R.id.splashLogoTextView) TextView logoTextView;
  @BindColor(R.color.colorAccent) int colorAccent;
  @BindColor(R.color.colorTextDark) int colorTextDark;

  private FirebaseAuth firebaseAuth;
  private static final int RC_GOOGLE_SIGN_IN = 987;
  private Unbinder unbinder;
  private GoogleSignInClient googleSignInClient;
  private ApiInterface apiInterface;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getWindow().setNavigationBarColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
    setContentView(R.layout.activity_splash);

    unbinder = ButterKnife.bind(SplashActivity.this);
    initializeComponents();
    initializeViews();
  }

  private void initializeComponents() {
    firebaseAuth = FirebaseAuth.getInstance();
    apiInterface = ApiClient.getRetrofit(getApplicationContext()).create(ApiInterface.class);
    if (firebaseAuth.getCurrentUser()==null) signInUser();
    else {
      apiInterface.getUser(firebaseAuth.getUid()).enqueue(new Callback<User>() {
        @Override
        public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
          if (response.body()==null) signInUser();
          else moveToNextActivity();
        }

        @Override
        public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
          promptUser(t.getMessage());
        }
      });
    }
  }

  private void signInUser() {
    GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(
        GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(getString(R.string.google_sign_in_client_id))
        .requestId()
        .requestProfile()
        .requestEmail()
        .build();
    googleSignInClient = GoogleSignIn.getClient(SplashActivity.this, googleSignInOptions);
    Intent signInIntent = googleSignInClient.getSignInIntent();
    startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
  }

  private void initializeViews() {

  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode==RC_GOOGLE_SIGN_IN && resultCode==RESULT_OK && data!=null) {
      Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
      try {
        firebaseAuthWithGoogle(task.getResult(ApiException.class));
      } catch (ApiException e) {
        Snackbar.make(logoTextView, e.getMessage(), Snackbar.LENGTH_INDEFINITE).show();
      }
    } else promptUser("Please sign in to continue");
  }

  private void firebaseAuthWithGoogle(GoogleSignInAccount result) {
    AuthCredential credential = GoogleAuthProvider.getCredential(result.getIdToken(), null);
    firebaseAuth.signInWithCredential(credential).addOnCompleteListener(
        task -> {
          if (task.isSuccessful()) {
            proceedAhead(result);
          } else promptUser(Objects.requireNonNull(task.getException()).getMessage());
        });
  }

  private void proceedAhead(GoogleSignInAccount result) {
    apiInterface.getUser(firebaseAuth.getUid()).enqueue(new Callback<User>() {
      @Override
      public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
        if (response.body()==null) signUpUser(result);
        else moveToNextActivity();
      }

      @Override
      public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
        promptUser(t.getMessage());
      }
    });
  }

  private void signUpUser(GoogleSignInAccount result) {
    Toast.makeText(getApplicationContext(), "Welcome. Creating new account.", Toast.LENGTH_SHORT).show();
    String uid = firebaseAuth.getUid();
    String name = result.getDisplayName();
    String email = result.getEmail();
    String image = result.getPhotoUrl().toString()==null?"":result.getPhotoUrl().toString();

    new MaterialDialog.Builder(SplashActivity.this)
        .title(R.string.app_name)
        .content("Please enter your contact number to continue.")
        .titleColor(Color.BLACK)
        .contentColor(colorTextDark)
        .inputType(InputType.TYPE_CLASS_PHONE)
        .positiveText("OKAY")
        .positiveColor(colorAccent)
        .input("Phone number", null, (dialog, input) -> {
            if (!TextUtils.isEmpty(input.toString()) && NumberUtils.isNumeric(input.toString()) && input.length()==10) createNewUser(uid, name, email, input.toString(), image);
            else {
              Snackbar.make(logoTextView, "Please enter a valid phone number. (Without country code)", Snackbar.LENGTH_LONG).show();
              dialog.dismiss();
              signUpUser(result);
            }
        }).show();
  }

  private void createNewUser(String uid, String name, String email, String phone, String image) {
    apiInterface.createNewUser(uid, name, email, phone, image).enqueue(new Callback<User>() {
      @Override
      public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
        if (response.body()==null) {
          Objects.requireNonNull(firebaseAuth.getCurrentUser()).delete().addOnSuccessListener(aVoid -> promptUser("There has been an error signing you up. Please try again."));
        }
        else moveToNextActivity();
      }

      @Override
      public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
        promptUser(t.getMessage());
      }
    });
  }

  private void moveToNextActivity() {
    startActivity(new Intent(SplashActivity.this, MainActivity.class));
    finish();
  }

  private void promptUser(String message) {
    Snackbar.make(logoTextView, message, Snackbar.LENGTH_INDEFINITE)
        .setActionTextColor(colorAccent)
        .setAction("RETRY", v -> signInUser())
        .show();
  }

  @Override
  protected void onDestroy() {
    unbinder.unbind();
    super.onDestroy();
  }
}
