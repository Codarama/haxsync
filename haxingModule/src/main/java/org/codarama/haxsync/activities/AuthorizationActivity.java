/*
 * Copyright (c) 2016 Codarama.org, All Rights Reserved
 *
 * Codarama HaxSync is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * Codarama HaxSync is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.codarama.haxsync.activities;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.ContactsContract;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import org.codarama.haxsync.R;
import org.codarama.haxsync.utilities.FacebookUtil;

public class AuthorizationActivity extends Activity {
    public final String TAG = "AuthorizationActivity";
    private CallbackManager callbackManager;

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        AccessToken accessToken = loginResult.getAccessToken();
                        Log.i(TAG, "Successful facebook login");
                        Log.i(TAG, "Login token expires on " + accessToken.getExpires().toString());

                        final String accountType = AuthorizationActivity.this.getString(R.string.ACCOUNT_TYPE);

                        AccountManager am = AccountManager.get(AuthorizationActivity.this);
                        Account account = am.getAccountsByType(accountType)[0];
                        am.setPassword(account, accessToken.toString());

                        SharedPreferences prefs = AuthorizationActivity.this.getSharedPreferences(AuthorizationActivity.this.getPackageName() + "_preferences", MODE_MULTI_PROCESS);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putInt("permission_level", FacebookUtil.PERMISSION_LEVEL);
                        editor.putLong("access_expires", accessToken.getExpires().getTime());
                        editor.commit();

                        ContentResolver.requestSync(account, ContactsContract.AUTHORITY, new Bundle());
                        ContentResolver.requestSync(account, CalendarContract.AUTHORITY, new Bundle());

                        AuthorizationActivity.this.finish();
                    }

                    @Override
                    public void onCancel() {
                        Log.w(TAG, "Facebook login was canceled");
                        AuthorizationActivity.this.finish();
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Log.e(TAG, "Facebook login failed", exception);
                        AuthorizationActivity.this.finish();
                    }
                });
    }
}
