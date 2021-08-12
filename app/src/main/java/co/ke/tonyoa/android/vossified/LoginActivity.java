package co.ke.tonyoa.android.vossified;



import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.ke.tonyoa.android.vossified.Data.SyncWorker;

import static co.ke.tonyoa.android.vossified.Data.SyncWorker.LAST_MODIFIED_USERREQUESTIMAGES;
import static co.ke.tonyoa.android.vossified.Data.SyncWorker.LAST_MODIFIED_USERREQUESTS;
import static co.ke.tonyoa.android.vossified.Utils.enableViews;
import static co.ke.tonyoa.android.vossified.Utils.hasNetwork;
import static co.ke.tonyoa.android.vossified.Utils.isPasswordValid;
import static co.ke.tonyoa.android.vossified.Utils.isUserNameValid;


public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.textInputEditText_login_username)
    TextInputEditText textInputEditTextUsername;
    @BindView(R.id.textInputEditText_login_password)
    TextInputEditText textInputEditTextPassword;
    @BindView(R.id.button_login_login)
    Button buttonLogin;
    @BindView(R.id.textView_login_forgotPassword)
    TextView textViewForgot;
    @BindView(R.id.textView_login_signUp)
    TextView textViewSignUp;
    @BindView(R.id.progressBar_login)
    ProgressBar progressBar;

    private FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.button_login_login)
    public void login(){
        String email=textInputEditTextUsername.getText().toString().trim();
        String password=textInputEditTextPassword.getText().toString().trim();
        if (hasNetwork((ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE))) {
            if (!isUserNameValid(email)) {
                textInputEditTextUsername.setError("Please enter a valid email address");
                return;
            }
            if (!isPasswordValid(password)) {
                textInputEditTextPassword.setError("Please enter a password at least 6 characters long");
                return;
            }
            progressBar.setVisibility(View.VISIBLE);
            List<View> views = Arrays.asList(textInputEditTextUsername, textInputEditTextPassword,
                    textViewForgot, textViewSignUp, buttonLogin);
            enableViews(views, false);
            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        SharedPreferences sharedPreferences=getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putLong(LAST_MODIFIED_USERREQUESTS, 0);
                        editor.putLong(LAST_MODIFIED_USERREQUESTIMAGES, 0);
                        editor.apply();

                        androidx.work.Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
                        //Enqueue for syncing once the app is connected to the internet
                        OneTimeWorkRequest oneTimeWorkRequest=new OneTimeWorkRequest.Builder(SyncWorker.class).setInitialDelay(1, TimeUnit.SECONDS).setConstraints(constraints).build();
                        WorkManager.getInstance(LoginActivity.this).beginUniqueWork("oneTime", ExistingWorkPolicy.REPLACE, oneTimeWorkRequest).enqueue();

                        Intent intent=new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    else {
                        String error="User authentication failed";
                        String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
                        switch (errorCode) {
                            case "ERROR_INVALID_CUSTOM_TOKEN":

                            case "ERROR_OPERATION_NOT_ALLOWED":

                            case "ERROR_INVALID_USER_TOKEN":

                            case "ERROR_USER_TOKEN_EXPIRED":

                            case "ERROR_USER_DISABLED":

                            case "ERROR_CREDENTIAL_ALREADY_IN_USE":

                            case "ERROR_EMAIL_ALREADY_IN_USE":

                            case "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL":

                            case "ERROR_REQUIRES_RECENT_LOGIN":

                            case "ERROR_USER_MISMATCH":

                            case "ERROR_INVALID_CREDENTIAL":

                            case "ERROR_CUSTOM_TOKEN_MISMATCH":
                                break;

                            case "ERROR_INVALID_EMAIL":
                                error="Invalid email address";
                                break;

                            case "ERROR_WRONG_PASSWORD":

                            case "ERROR_WEAK_PASSWORD":
                                error="Invalid password";
                                break;

                            case "ERROR_USER_NOT_FOUND":
                                error="This user account does not exist";
                                break;
                        }
                        progressBar.setVisibility(View.GONE);
                        Snackbar.make(buttonLogin, error, Snackbar.LENGTH_LONG).show();
                        enableViews(views, true);
                    }
                }
            });
        } else {
            Snackbar.make(buttonLogin, "Please connect to the internet and try again", Snackbar.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.textView_login_forgotPassword)
    public void resetPassword(){
        Log.e("Reset", "Resetting password");
        Intent intent=new Intent(this, ForgotPasswordActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.textView_login_signUp)
    public void createAccount(){
        Intent intent=new Intent(this, SignUpActivity.class);
        startActivity(intent);
    }

}
