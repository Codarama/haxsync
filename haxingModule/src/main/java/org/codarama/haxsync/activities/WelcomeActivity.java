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

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

import org.codarama.haxsync.R;
import org.codarama.haxsync.SyncAccount;
import org.codarama.haxsync.fragments.HomeFragment;

/**
 * <p>Welcome screen</p>
 */
public class WelcomeActivity extends AppCompatActivity {

    private static final String TAG = "WelcomeActivity";
    private static final int WELCOME_ACTIVITY_REQUEST_CODE = 1;

    @Override
    protected void onResume() {
        super.onResume();
        AppEventsLogger.activateApp(getApplication());
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());

        setContentView(R.layout.activity_welcome);

        // build aup the custom action bar, thank you very much
        Toolbar myToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(myToolbar);

        // However, if we're being restored from a previous state, then we don't need to do
        // anything and should return or else we could end up with overlapping fragments.
        if (savedInstanceState != null) {
            return;
        }

        // Create a new Fragment to be placed in the activity layout
        Fragment homeFragment = new HomeFragment();

        // In case this activity was started with special instructions from an Intent, pass the
        // Intent's extras to the fragment as arguments
        homeFragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, homeFragment).commit();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, WELCOME_ACTIVITY_REQUEST_CODE);
            return;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case WELCOME_ACTIVITY_REQUEST_CODE: {
                if (grantResults.length <= 0 || grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // User has specifically denied permissions, we need to display a friendly message
                    new AlertDialog.Builder(getApplicationContext())
                            .setTitle(getResources().getString(R.string.welcome_screen_permission_denied_title))
                            .setMessage(R.string.welcome_screen_permission_denied_text)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.navigation_items, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_home: {
                // Create fragment and give it an argument specifying the article it should show
                HomeFragment newFragment = new HomeFragment();
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                // Replace whatever is in the fragment_container view with this fragment,
                // and add the transaction to the back stack so the user can navigate back
                transaction.replace(R.id.fragment_container, newFragment);
                transaction.addToBackStack(null);

                // Commit the transaction
                transaction.commit();
                return true;
            }

            case R.id.nav_settings: {
                Intent preferences = new Intent(getApplicationContext(), PreferencesActivity.class);
                startActivity(preferences);
                return true;
            }

            // as per https://developer.android.com/training/sync-adapters/running-sync-adapter.html
            // we might want to reconsider allowing manual sync - the basic flaw in this design is
            // that the user could not be trusted to know when it is the best time to sync
            case R.id.nav_manual_sync: {
                Log.d(TAG, "User requested manual sync");
                SyncAccount syncAccount = new SyncAccount(AccountManager.get(WelcomeActivity.this));
                Account account = syncAccount.getHaxSyncAccount(getResources().getString(R.string.ACCOUNT_TYPE));

                // Pass the settings flags by inserting them in a bundle
                Bundle settingsBundle = new Bundle();
                settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
                settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);

                ContentResolver.requestSync(account, "com.android.contacts", settingsBundle);
                ContentResolver.requestSync(account, "com.android.calendar", settingsBundle);
                return true;
            }

            default: {
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
            }
        }
    }
}