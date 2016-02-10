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
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;

import org.codarama.haxsync.utilities.DeviceUtil;
import org.codarama.haxsync.utilities.intents.IntentBuilder;
import org.codarama.haxsync.utilities.intents.IntentUtil;

public class PostActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = this.getSharedPreferences(this.getPackageName() + "_preferences", MODE_MULTI_PROCESS);
        boolean syncNew = prefs.getBoolean("status_new", true);

        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT < 15 || !syncNew) {
            finish();
        } else if (getIntent().getData() != null) {
            Cursor cursor = managedQuery(getIntent().getData(), null, null, null, null);
            if (cursor.moveToNext()) {
                //	Log.i("Cursor", Arrays.deepToString(cursor.getColumnNames()));

                String postID = cursor.getString(cursor.getColumnIndex("stream_item_sync1"));
                String uid = cursor.getString(cursor.getColumnIndex("stream_item_sync2"));
                String permalink = cursor.getString(cursor.getColumnIndex("stream_item_sync3"));

                IntentBuilder builder = IntentUtil.getIntentBuilder(this);
                Intent intent = builder.getPostIntent(postID, uid, permalink);
                if (!DeviceUtil.isCallable(this, intent)) {
                    builder = IntentUtil.getFallbackBuilder();
                    intent = builder.getPostIntent(postID, uid, permalink);
                }
                this.startActivity(intent);

                finish();
            }
        } else {
            // How did we get here without data?
            finish();
        }
    }
}
