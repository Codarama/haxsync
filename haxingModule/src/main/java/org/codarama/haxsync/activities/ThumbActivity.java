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
            try (Cursor cursor = managedQuery(getIntent().getData(), null, null, null, null)) {
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
            }
            finish();

        }

    }

}
