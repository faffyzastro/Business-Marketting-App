package co.ke.tonyoa.android.vossified;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.ke.tonyoa.android.vossified.Data.VossitContract;
import co.ke.tonyoa.android.vossified.POJOs.User;

import static co.ke.tonyoa.android.vossified.Utils.enableViews;
import static co.ke.tonyoa.android.vossified.Utils.hasNetwork;
import static co.ke.tonyoa.android.vossified.Utils.isNameValid;
import static co.ke.tonyoa.android.vossified.Utils.isPasswordValid;
import static co.ke.tonyoa.android.vossified.Utils.isUserNameValid;


interface OnUserLog{
    void onUserLog();
}

public class SignUpActivity extends AppCompatActivity {

    @BindView(R.id.textInputEditText_signup_firstName)
    TextInputEditText textInputEditTextFirstName;
    @BindView(R.id.textInputEditText_signup_lastName)
    TextInputEditText textInputEditTextLastName;
    @BindView(R.id.textInputEditText_signup_username)
    TextInputEditText textInputEditTextUsername;
    @BindView(R.id.textInputEditText_signup_password)
    TextInputEditText textInputEditTextPassword;
    @BindView(R.id.textInputEditText_signup_confirm_password)
    TextInputEditText textInputEditTextConfirmPassword;
    @BindView(R.id.button_signup_signup)
    Button buttonSignUp;
    @BindView(R.id.textView_signup_login)
    TextView textViewLogin;
    @BindView(R.id.progressBar_signUp)
    ProgressBar progressBar;

    private FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        ButterKnife.bind(this);

        textInputEditTextUsername.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus){
                    if (!isUserNameValid(textInputEditTextUsername.getText().toString().trim())) {
                        textInputEditTextUsername.setError("Please enter a valid email address");
                    }
                }
            }
        });
        textInputEditTextPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus){
                    if (!isPasswordValid(textInputEditTextPassword.getText().toString().trim())) {
                        textInputEditTextPassword.setError("Please enter a password at least 6 characters long");
                    }
                }
            }
        });
        textInputEditTextConfirmPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus){
                    if (!isPasswordValid(textInputEditTextConfirmPassword.getText().toString().trim())) {
                        textInputEditTextConfirmPassword.setError("Please enter a password at least 6 characters long");
                    }
                }
            }
        });
    }

    @OnClick(R.id.button_signup_signup)
    public void signUp(){
        String firstName=textInputEditTextFirstName.getText().toString().trim();
        String lastName=textInputEditTextLastName.getText().toString().trim();
        String email=textInputEditTextUsername.getText().toString().trim();
        String password=textInputEditTextPassword.getText().toString().trim();
        String confirmPassword=textInputEditTextConfirmPassword.getText().toString().trim();
        if (hasNetwork((ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE))) {
            if (!isNameValid(firstName)){
                textInputEditTextFirstName.setError("Please enter a valid name at least 3 characters long");
                return;
            }
            if (!isNameValid(lastName)){
                textInputEditTextLastName.setError("Please enter a valid name at least 3 characters long");
                return;
            }
            if (!isUserNameValid(email)) {
                textInputEditTextUsername.setError("Please enter a valid email address");
                return;
            }
            if (!isPasswordValid(password)) {
                textInputEditTextPassword.setError("Please enter a password at least 6 characters long");
                return;
            }
            if (!isPasswordValid(confirmPassword)) {
                textInputEditTextConfirmPassword.setError("Please enter a password at least 6 characters long");
                return;
            }
            if (!password.equals(confirmPassword)){
                Snackbar.make(textInputEditTextConfirmPassword, "The two passwords do not match", Snackbar.LENGTH_SHORT).show();
                return;
            }
            progressBar.setVisibility(View.VISIBLE);
            List<View> views = Arrays.asList(textInputEditTextFirstName, textInputEditTextLastName,
                    textInputEditTextUsername, textInputEditTextPassword, textInputEditTextConfirmPassword,
                    textViewLogin, buttonSignUp);
            enableViews(views, false);
            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        ContentValues contentValues=new ContentValues();
                        contentValues.put(VossitContract.USERSENTRY.FIRSTNAMECOLUMN, firstName);
                        contentValues.put(VossitContract.USERSENTRY.LASTNAMECOLUMN, lastName);
                        contentValues.put(VossitContract.USERSENTRY.EMAILCOLUMN, email);
                        contentValues.put(VossitContract.USERSENTRY.ADMINCOLUMN, 0);
                        contentValues.put(VossitContract.USERSENTRY.USERIDCOLUMN, firebaseAuth.getUid());
                        getContentResolver().insert(Uri.withAppendedPath(VossitContract.BASEURI, VossitContract.USERSENTRY.TABLENAME),
                                contentValues);
                        Intent intent=new Intent(SignUpActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    else {
                        String error;
                        try {
                            throw task.getException();
                        } catch(FirebaseAuthWeakPasswordException e) {
                            error="The password is too weak. Use a stronger password with at least 6 characters";
                        } catch(FirebaseAuthInvalidCredentialsException e) {
                            error="The password is too weak. Use a stronger password with at least 6 characters";
                        } catch(FirebaseAuthUserCollisionException e) {
                            error="User already exists";
                        } catch(Exception e) {
                            error="An error occurred while creating your account. Please try again later";
                        }
                        progressBar.setVisibility(View.GONE);
                        Snackbar.make(buttonSignUp, error, Snackbar.LENGTH_LONG).show();
                        enableViews(views, true);
                    }
                }
            });
        } else {
            Snackbar.make(buttonSignUp, "Please connect to the internet and try again", Snackbar.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.textView_signup_login)
    public void login(){
        Intent intent=new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

}
