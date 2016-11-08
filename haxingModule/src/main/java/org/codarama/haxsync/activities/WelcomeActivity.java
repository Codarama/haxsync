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

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.facebook.appevents.AppEventsLogger;

import org.codarama.haxsync.R;

/**
 * <p>Welcome screen</p>
 */
public class WelcomeActivity extends AppCompatActivity {

    private static final String TAG = "WelcomeActivity";

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_welcome);

        // Keeping this code for now, until we move away from using the ugly wizard

//        ImageView image = (ImageView) findViewById(R.id.logo);
//        image.setOnClickListener(new OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                Intent nextIntent = new Intent(WelcomeActivity.this, WizardActivity.class);
//                WelcomeActivity.this.startActivity(nextIntent);
//                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
//                WelcomeActivity.this.finish();
//            }
//        });

        Toolbar myToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(myToolbar);
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
                Log.i(TAG, "Navigating home");
            }

            case R.id.nav_settings: {
                Log.i(TAG, "Navigating to settings");
                Intent nextIntent = new Intent(WelcomeActivity.this, PreferencesActivity.class);
                WelcomeActivity.this.startActivity(nextIntent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                WelcomeActivity.this.finish();
                return true;
            }

            case R.id.nav_manual_sync: {
                Log.i(TAG, "Executing manual sync");
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