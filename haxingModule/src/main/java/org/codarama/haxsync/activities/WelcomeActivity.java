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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.facebook.appevents.AppEventsLogger;

import org.codarama.haxsync.R;

/**
 * <p>Welcome screen</p>
 * <p>Displayed when the application is started for the first time. Leads to the {@link WizardActivity}.</p>
 */
public class WelcomeActivity extends Activity {

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

        ImageView image = (ImageView) findViewById(R.id.logo);
        image.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent nextIntent = new Intent(WelcomeActivity.this, WizardActivity.class);
                WelcomeActivity.this.startActivity(nextIntent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                WelcomeActivity.this.finish();
            }
        });
    }
}