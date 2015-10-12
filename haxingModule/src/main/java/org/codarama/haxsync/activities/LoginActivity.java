package org.codarama.haxsync.activities;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.codarama.haxsync.R;
import org.codarama.haxsync.utilities.FacebookUtil;

public class LoginActivity extends Activity {
    private static final String TAG = "LoginActivity";
    private CallbackManager callbackManager;

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize the SDK before executing any other operations,
        // especially, since we are using Facebook UI elements.
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions("user_friends");
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {
                AccessToken accessToken = AccessToken.getCurrentAccessToken();
                Log.i(TAG, "Facebook login successful, current token '" + accessToken.toString() + "'");
                Log.i(TAG, "Facebook token expires on" + String.valueOf(accessToken.getExpires().toString()));
                String accountName = accessToken.getUserId();
                String accountType = LoginActivity.this.getString((R.string.ACCOUNT_TYPE));
                AccountManager am = AccountManager.get(LoginActivity.this);
                if (am.getAccountsByType(accountType).length == 0) {
                    Bundle result;
                    Account account = new Account(accountName, LoginActivity.this.getString((R.string.ACCOUNT_TYPE)));
                    if (am.addAccountExplicitly(account, accessToken.toString(), null)) {
                        result = new Bundle();
                        result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
                        result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putInt("permission_level", FacebookUtil.PERMISSION_LEVEL);
                        editor.putLong("access_expires", accessToken.getExpires().getTime());
                        editor.commit();
                        LoginActivity.this.finish();
                    }
                } else {
                    Account account = am.getAccountsByType(accountType)[0];
                    am.setPassword(account, accessToken.toString());
                    LoginActivity.this.finish();
                }
            }

            @Override
            public void onError(FacebookException exception) {
                Log.e(TAG, "Facebook login failed", exception);
                LoginActivity.this.finish();
            }

            @Override
            public void onCancel() {
                Log.i(TAG, "Facebook login canceled");
                LoginActivity.this.finish();
            }
        });
    }


}


