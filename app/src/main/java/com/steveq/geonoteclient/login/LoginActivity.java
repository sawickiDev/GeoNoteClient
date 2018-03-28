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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.steveq.geonoteclient.R;
import com.steveq.geonoteclient.services.PermissionChecker;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity implements Callback<AuthResponse> {
    private static final String TAG = LoginActivity.class.getSimpleName();

    private static final int INTERNET_REQUEST = 10;
    private static final String[] NEEDED_PERMISSIONS =
            new String[]{
                            "android.permission.INTERNET",
                            "android.permission.ACCESS_NETWORK_STATE"
            };

    @BindView(R.id.credentialsErrorTextView)
    TextView credentialErrorTextView;

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

    @BindView(R.id.registerTextView)
    TextView registerTextView;

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
        registerTextView.setOnClickListener(view -> attemptRegister());
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
        credentialErrorTextView.setVisibility(View.GONE);

        //Store values at the time of the login attempt.
        String username = usernameTextView.getText().toString();
        String password = passwordEditText.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            passwordEditText.setError(getString(R.string.error_invalid_password));
            focusView = passwordEditText;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(username)) {
            usernameTextView.setError(getString(R.string.error_field_required));
            focusView = usernameTextView;
            cancel = true;
        } else if (!isUsernameValid(username)) {
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

    private void attemptRegister(){
        //TODO: implement register
    }

    private boolean isUsernameValid(String username) {
        //can be customized
        return username.length() > 4;
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
            AuthError ae = parseAuthError(response.errorBody());
            if("Bad credentials".equals(ae.getErrorDescription())){
                credentialErrorTextView.setText(getResources().getString(R.string.error_invalid_credentials));
                credentialErrorTextView.setVisibility(View.VISIBLE);
            }
        }
    }

    private AuthError parseAuthError(ResponseBody errorResponse){
        Gson gson = new Gson();
        AuthError ae = new AuthError();
        try {
            ae = gson.fromJson(errorResponse.string(), AuthError.class);
        } catch (IOException | NullPointerException e) {
            Snackbar
                    .make(loginFormView, getResources().getString(R.string.incorrect_response), Snackbar.LENGTH_SHORT)
                    .show();
            e.printStackTrace();
        }

        return ae;
    }

    @Override
    public void onFailure(Call<AuthResponse> call, Throwable t) {
        Log.d(TAG, t.getMessage());
        showProgress(false);
        Snackbar
            .make(loginFormView, getResources().getString(R.string.connection_error), Snackbar.LENGTH_SHORT)
            .show();

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

