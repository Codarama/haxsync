package org.codarama.haxsync.activities;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Button;

import com.facebook.AppEventsLogger;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;

import org.codarama.haxsync.R;
import org.codarama.haxsync.utilities.FacebookUtil;
import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AccountAuthenticatorActivity {
    private static final String TAG = "LoginActivity";
    Button mLoginButton;

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        FacebookUtil.facebook.authorizeCallback(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.main);
        String[] permissions = {"offline_access", "read_stream", "user_events", "friends_events", "friends_status", "user_status", "friends_photos", "user_photos", "friends_about_me", "friends_website", "email", "friends_birthday", "friends_location"};
        FacebookUtil.facebook.authorize(LoginActivity.this, permissions, Facebook.FORCE_DIALOG_AUTH, new DialogListener() {
            @Override
            public void onComplete(Bundle values) {
                FacebookUtil.accessToken = FacebookUtil.facebook.getAccessToken();
                Log.i("access token", FacebookUtil.facebook.getAccessToken());
                JSONObject user = FacebookUtil.getSelfInfoAsync();
                AccountManager am = AccountManager.get(LoginActivity.this);
                Log.i("Expires", String.valueOf(FacebookUtil.facebook.getAccessExpires()));
                if (am.getAccountsByType(LoginActivity.this.getString((R.string.ACCOUNT_TYPE))).length == 0) {
                    try {
                        Bundle result;
                        Account account = new Account(user.getString("name"), LoginActivity.this.getString((R.string.ACCOUNT_TYPE)));
                        //	AccountManager am = AccountManager.get(LoginActivity.this);
                        if (am.addAccountExplicitly(account, FacebookUtil.facebook.getAccessToken(), null)) {
                            result = new Bundle();
                            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
                            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
                            setAccountAuthenticatorResult(result);
                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putInt("permission_level", FacebookUtil.PERMISSION_LEVEL);
                            editor.putLong("access_expires", FacebookUtil.facebook.getAccessExpires());
                            editor.commit();
                            LoginActivity.this.finish();
                        }
                    } catch (JSONException e) {
                        Log.e("Error", e.getLocalizedMessage());
                    }
                } else {
                    Account account = am.getAccountsByType(LoginActivity.this.getString(R.string.ACCOUNT_TYPE))[0];
                    am.setPassword(account, FacebookUtil.facebook.getAccessToken());
                    LoginActivity.this.finish();
                }
            }

            @Override
            public void onFacebookError(FacebookError error) {
                Log.e(TAG, (error.getErrorType() == null) ? "no error message" : error.getErrorType());
                //	Log.e(TAG, error.getMessage());
                Log.i(TAG, "fberror");
                LoginActivity.this.finish();
            }

            @Override
            public void onError(DialogError e) {
                Log.i(TAG, "error");
                LoginActivity.this.finish();
            }

            @Override
            public void onCancel() {
                Log.i(TAG, "cancel");
                LoginActivity.this.finish();
            }
        });
    }


}


