package cs477.gmu.mobile_library.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import cs477.gmu.mobile_library.MainActivity;
import cs477.gmu.mobile_library.R;
import cs477.gmu.mobile_library.data.LoginDataSource;
import cs477.gmu.mobile_library.data.LoginRepository;
import cs477.gmu.mobile_library.data.Result;

public class LoginActivity extends AppCompatActivity {

    private LoginRepository loginRepository;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginRepository = LoginRepository.getInstance(new LoginDataSource());

        final TextInputEditText usernameEditText = findViewById(R.id.username);
        final TextInputEditText passwordEditText = findViewById(R.id.password);
        final Button loginButton = findViewById(R.id.login);
        final Button signupButton = findViewById(R.id.signup);
        final ProgressBar loadingProgressBar = findViewById(R.id.loading);
        final TextView errorTextView = findViewById(R.id.error);

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                errorTextView.setVisibility(View.GONE);
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);

        loginButton.setOnClickListener(v -> {
            loadingProgressBar.setVisibility(View.VISIBLE);
            String email = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString();

            loginRepository.login(email, password, result -> {
                loadingProgressBar.setVisibility(View.GONE);
                if (result instanceof Result.Success) {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    showLoginFailed(((Result.Error) result).getError().getMessage());
                }
            });
        });

        signupButton.setOnClickListener(v -> {
            loadingProgressBar.setVisibility(View.VISIBLE);
            String email = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString();

            loginRepository.signup(email, password, result -> {
                loadingProgressBar.setVisibility(View.GONE);
                if (result instanceof Result.Success) {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    showLoginFailed(((Result.Error) result).getError().getMessage());
                }
            });
        });
    }

    private void showLoginFailed(String errorString) {
        TextView errorTextView = findViewById(R.id.error);
        errorTextView.setText(errorString);
        errorTextView.setVisibility(View.VISIBLE);
    }
}

