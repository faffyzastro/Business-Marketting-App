package co.ke.tonyoa.android.vossified;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static co.ke.tonyoa.android.vossified.Utils.enableViews;
import static co.ke.tonyoa.android.vossified.Utils.hasNetwork;

public class ForgotPasswordActivity extends AppCompatActivity {

    @BindView(R.id.textInputEditText_forgot_username)
    TextInputEditText textInputEditTextUsername;
    @BindView(R.id.button_forgot_forgot)
    Button buttonForgot;
    @BindView(R.id.progressBar_forgot)
    ProgressBar progressBar;

    private FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.button_forgot_forgot)
    public void forgotPassword(){
        if (hasNetwork((ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE))) {
            String email = textInputEditTextUsername.getText().toString().trim();
            if (!Utils.isUserNameValid(email)) {
                textInputEditTextUsername.setError("Please enter an email address");
                return;
            }
            progressBar.setVisibility(View.VISIBLE);
            List<View> views = Arrays.asList(textInputEditTextUsername, buttonForgot);
            enableViews(views, false);
            firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Snackbar.make(buttonForgot, "A password reset email has been sent to " + email, Snackbar.LENGTH_SHORT).show();
                        Intent intent=new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Snackbar.make(buttonForgot, "Please confirm the email address is correct", Snackbar.LENGTH_LONG).show();
                    }
                    progressBar.setVisibility(View.GONE);
                    enableViews(views, true);
                }
            });
        } else {
            Snackbar.make(buttonForgot, "Please connect to the internet and try again", Snackbar.LENGTH_SHORT).show();
        }
    }
}
