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
import android.provider.ContactsContract.StreamItemPhotos;

import org.codarama.haxsync.utilities.intents.IntentBuilder;
import org.codarama.haxsync.utilities.intents.IntentUtil;


public class ThumbActivity extends Activity {
    private static final String TAG = "ThumbActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().getData() != null) {
            Cursor cursor = managedQuery(getIntent().getData(), null, null, null, null);
            if (cursor.moveToNext()) {
                String type = cursor.getString(cursor.getColumnIndex(StreamItemPhotos.SYNC1));
                String sync2 = cursor.getString(cursor.getColumnIndex(StreamItemPhotos.SYNC2));
                if (type.equals("fbphoto")) {
                    IntentBuilder builder = IntentUtil.getIntentBuilder(this);
                    Intent intent = builder.getPhotoIntent(sync2);
                /*	if (!DeviceUtil.isCallable(this, intent)){
						builder = IntentUtil.getFallbackBuilder();
						intent = builder.getPhotoIntent(owner, aid, sync2);
					}*/
                    this.startActivity(intent);

                    finish();


                } else if (type.equals("youtube")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(sync2));
                    this.startActivity(intent);
                } else if (type.equals("link")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(sync2));
                    this.startActivity(intent);
                }
            }
            finish();

        }

    }

}
