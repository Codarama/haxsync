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

package org.codarama.haxsync.services;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import com.facebook.FacebookSdk;

import org.codarama.haxsync.SyncPreferences;
import org.codarama.haxsync.contacts.ContactsService;
import org.codarama.haxsync.provider.facebook.ContactDetailsRequest;
import org.codarama.haxsync.provider.facebook.callbacks.ContactDetailsCallback;
import org.codarama.haxsync.utilities.DeviceUtil;

import java.util.Map;
import java.util.Set;

/**
 * This implementation of the {@link AbstractThreadedSyncAdapter} takes care of syncing the
 * contact photographs.
 */
public class ContactPhotoSyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String TAG = "ContactPhotoSyncAdapter";

    private Context context;

    /**
     * Set up the sync adapter
     */
    public ContactPhotoSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        /*
         * If your app uses a content resolver, get an instance of it
         * from the incoming Context
         */
        this.context = context;
    }

    /**
     * Set up the sync adapter. This form of the
     * constructor maintains compatibility with Android 3.0
     * and later platform versions
     */
    public ContactPhotoSyncAdapter(
            Context context,
            boolean autoInitialize,
            boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        this.context = context;
    }

    @Override
    public void onPerformSync(
            final Account account,
            final Bundle extras,
            final String authority,
            final ContentProviderClient provider,
            final SyncResult syncResult) {
        Log.i(TAG, "Initiating facebook contacts profile photo sync");

        SyncPreferences prefs = new SyncPreferences(context);
        boolean wifiOnly = prefs.getWiFiOnly();
        boolean chargingOnly = prefs.getChargingOnly();

        // step 1. check prerequisites - WiFi, battery, etc.
        if (wifiOnly && !DeviceUtil.isWifi(context)) {
            prefs.setMissedCalendarSync(true);
            return;
        }

        if (chargingOnly && !DeviceUtil.isCharging(context)) {
            prefs.setMissedCalendarSync(true);
            return;
        }

        // step 2. make sure Facebook SDK is initialized
        FacebookSdk.sdkInitialize(context);

        // step 3. extract list of contacts to sync
        ContentResolver resolver = context.getContentResolver();

        ContactsService manager = new ContactsService(resolver);
        Map<String, Long> friends = manager.fetchFriends();

        // step 4. perform sync per contact
        final Set<String> friendIds = friends.keySet();
        final int screenWidth = DeviceUtil.getScreenWidth(context);
        final int screenHeight = DeviceUtil.getScreenHeight(context);

        ContactDetailsRequest request = new ContactDetailsRequest(friendIds, screenWidth, screenHeight);
        ContactDetailsCallback callback = new ContactDetailsCallback(context, friends);
        request.executeAsync(callback);
    }
}
