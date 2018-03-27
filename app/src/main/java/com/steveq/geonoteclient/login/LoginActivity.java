package com.steveq.geonoteclient.login;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.steveq.geonoteclient.R;
import com.steveq.geonoteclient.services.PermissionChecker;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity implements Callback<AuthResponse> {
    private static final String TAG = LoginActivity.class.getSimpleName();

    private static final int INTERNET_REQUEST = 10;
    private static final String[] NEEDED_PERMISSIONS =
            new String[]{
                            "android.permission.INTERNET",
                            "android.permission.ACCESS_NETWORK_STATE"
            };

    @BindView(R.id.username)
    TextView usernameTextView;

    @BindView(R.id.password)
    EditText passwordEditText;

    @BindView(R.id.login_progress)
    View progressView;

    @BindView(R.id.login_form)
    View loginFormView;

    @BindView(R.id.username_sign_in_button)
    Button signInButton;

    private GeonoteAuthController geonoteAuthController;
    private PermissionChecker permissionChecker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        passwordEditText.setOnEditorActionListener(((textView, id, keyEvent) -> {
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptLogin();
                return true;
            }
            return false;
        }));

        signInButton.setOnClickListener(view -> attemptLogin());
        geonoteAuthController = new GeonoteAuthController(this);
        permissionChecker = new PermissionChecker(this);
        permissionChecker.handlePermission(NEEDED_PERMISSIONS, INTERNET_REQUEST);
    }

    private void attemptLogin() {
        if(permissionChecker.permissionNotGranted(NEEDED_PERMISSIONS).size() > 0){
            return;
        }

        //Reset errors
        usernameTextView.setError(null);
        passwordEditText.setError(null);

        //Store values at the time of the login attempt.
        String email = usernameTextView.getText().toString();
        String password = passwordEditText.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            passwordEditText.setError(getString(R.string.error_invalid_password));
            focusView = passwordEditText;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            usernameTextView.setError(getString(R.string.error_field_required));
            focusView = usernameTextView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            usernameTextView.setError(getString(R.string.error_invalid_email));
            focusView = usernameTextView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            geonoteAuthController.start();
        }
    }

    private boolean isEmailValid(String email) {
        //can be customized
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //can be customized
        return password.length() > 4;
    }

    private void showProgress(final boolean show) {
        progressView.setVisibility(show ? View.VISIBLE : View.GONE);
        loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
        showProgress(false);
        if(response.isSuccessful()){
            Log.d(TAG, String.valueOf(response.body()));
            finish();
        } else {
            Log.d(TAG, String.valueOf(response.errorBody()));
            passwordEditText.setError(getString(R.string.error_incorrect_password));
            passwordEditText.requestFocus();
        }
    }

    @Override
    public void onFailure(Call<AuthResponse> call, Throwable t) {
        Log.d(TAG, t.getMessage());
        showProgress(false);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case INTERNET_REQUEST: {
                if(grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                } else {

                }
                return;
            }
        }
    }
}

