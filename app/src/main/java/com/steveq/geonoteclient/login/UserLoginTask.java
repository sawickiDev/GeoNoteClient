package com.steveq.geonoteclient.login;

import android.os.AsyncTask;

import com.steveq.geonoteclient.R;

/**
 * Created by steveq on 27.03.18.
 */

public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

    private final String mEmail;
    private final String mPassword;
    private GeonoteAuthController geonoteAuthController;

    UserLoginTask(String email, String password) {
        mEmail = email;
        mPassword = password;
//        geonoteAuthController = new GeonoteAuthController();
    }

    @Override
    protected Boolean doInBackground(Void... params) {

//        try {
//
//        } catch (InterruptedException e) {
//            return false;
//        }
//
//        for (String credential : DUMMY_CREDENTIALS) {
//            String[] pieces = credential.split(":");
//            if (pieces[0].equals(mEmail)) {
//                // Account exists, return true if the password matches.
//                return pieces[1].equals(mPassword);
//            }
//        }
//
//        // TODO: register the new account here.
        return true;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
//        mAuthTask = null;
//        showProgress(false);
//
//        if (success) {
//            finish();
//        } else {
//            mPasswordView.setError(getString(R.string.error_incorrect_password));
//            mPasswordView.requestFocus();
//        }
    }

    @Override
    protected void onCancelled() {
//        mAuthTask = null;
//        showProgress(false);
    }
}
