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
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import org.codarama.haxsync.utilities.DeviceUtil;
import org.codarama.haxsync.utilities.intents.IntentBuilder;
import org.codarama.haxsync.utilities.intents.IntentUtil;

public class ProfileActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().getData() != null) {
            try (Cursor cursor = managedQuery(getIntent().getData(), null, null, null, null)) {
                if (cursor.moveToNext()) {
                    String username = cursor.getString(cursor.getColumnIndex("DATA1"));

                    IntentBuilder builder = IntentUtil.getIntentBuilder(this);
                    Intent intent = builder.getProfileIntent(username);
                    if (!DeviceUtil.isCallable(this, intent)) {
                        builder = IntentUtil.getFallbackBuilder();
                        intent = builder.getProfileIntent(username);
                    }
                    this.startActivity(intent);

                    finish();

                    if (DeviceUtil.isCallable(this, intent)) {
                        this.startActivity(intent);
                        //fall back to browser if user doesn't have FB App installed.
                    } else {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://m.facebook.com/profile.php?id=" + username));
                        this.startActivity(browserIntent);
                    }
                    finish();
                }
            }
        } else {
            // How did we get here without data?
            finish();
        }
    }

}
