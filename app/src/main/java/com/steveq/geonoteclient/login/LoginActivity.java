package com.steveq.geonoteclient.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.steveq.geonoteclient.R;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = LoginActivity.class.getSimpleName();

    private GeonoteAuthController geonoteAuthController;
    private TokensPersistant tokensPersistant;

    @BindView(R.id.credentialsErrorTextView)
    TextView credentialErrorTextView;

    @BindView(R.id.usernameTextInputLayout)
    TextInputLayout usernameTextInputLayout;

    @BindView(R.id.usernameEditText)
    TextInputEditText usernameEditText;

    @BindView(R.id.passwordTextInputLayout)
    TextInputLayout passwordTextInputLayout;

    @BindView(R.id.passwordEditText)
    TextInputEditText passwordEditText;

    @BindView(R.id.login_progress)
    View progressView;

    @BindView(R.id.login_form)
    View loginFormView;

    @BindView(R.id.username_sign_in_button)
    Button signInButton;

    @BindView(R.id.registerTextView)
    TextView registerTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity_layout);
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
        tokensPersistant = new TokensPersistant();
        geonoteAuthController = new GeonoteAuthController();
    }

    private void attemptLogin() {

        usernameEditText.setError(null);
        passwordEditText.setError(null);
        credentialErrorTextView.setVisibility(View.GONE);

        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            passwordEditText.setError(getString(R.string.error_invalid_password));
            focusView = passwordEditText;
            cancel = true;
        }

        if (TextUtils.isEmpty(username)) {
            usernameEditText.setError(getString(R.string.error_field_required));
            focusView = usernameEditText;
            cancel = true;
        } else if (!isUsernameValid(username)) {
            usernameEditText.setError(getString(R.string.error_invalid_email));
            focusView = usernameEditText;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            geonoteAuthController
                    .prepareLoginCall(username, password)
                    .enqueue(new Callback<AuthResponse>() {
                        @Override
                        public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                            showProgress(false);
                            if(response.isSuccessful()){
                                Log.d(TAG, String.valueOf(response.body()));
                                AuthResponse authResponse = response.body();
                                tokensPersistant.saveAccessToken(authResponse.getAccessToken());
                                tokensPersistant.saveRefreshToken(authResponse.getRefreshToken());
                                redirectToMap();
                            } else {
                                AuthError ae = parseAuthError(response.errorBody());
                                if("Bad credentials".equals(ae.getErrorDescription())){
                                    passwordTextInputLayout.setError(getResources().getString(R.string.error_invalid_credentials));
                                    usernameTextInputLayout.setError(getResources().getString(R.string.error_invalid_credentials));
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<AuthResponse> call, Throwable t) {
                            Log.d(TAG, t.getMessage());
                            showProgress(false);
                            Snackbar
                                .make(loginFormView, getResources().getString(R.string.connection_error), Snackbar.LENGTH_SHORT)
                                .show();
                        }
                    });
        }
    }

    private void redirectToMap(){
        Intent intent =
                new Intent(LoginActivity.this, MapActivity.class);
        startActivity(intent);
    }

    private void attemptRegister(){

        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        showProgress(true);
        geonoteAuthController
            .prepareRegisterCall(new RegisterData(username, password))
            .enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    showProgress(false);
                    if(response.isSuccessful()){
                        Log.d(TAG, String.valueOf(response.body()));
                        if("OK".equals(response.body())){
                            Snackbar
                                    .make(loginFormView, getResources().getString(R.string.successfully_registered), Snackbar.LENGTH_SHORT)
                                    .show();
                            attemptLogin();
                        } else if ("KO".equals(response.body())){
                            Snackbar
                                    .make(loginFormView, getResources().getString(R.string.user_exists), Snackbar.LENGTH_SHORT)
                                    .show();
                        }
                    } else {
                        Log.d(TAG, String.valueOf(response.body()));
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    showProgress(false);
                    Snackbar
                        .make(loginFormView, getResources().getString(R.string.connection_error), Snackbar.LENGTH_SHORT)
                        .show();
                }
            });
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
    }

    private AuthError parseAuthError(ResponseBody errorResponse){
        Gson gson = new Gson();
        AuthError ae = new AuthError();
        try {
            ae = gson.fromJson(errorResponse.string(), AuthError.class);
        } catch (IOException | NullPointerException e) {
            Snackbar
                    .make(loginFormView, getResources().getString(R.string.incorrect_response), Snackbar.LENGTH_LONG)
                    .show();
            e.printStackTrace();
        }

        return ae;
    }
}

