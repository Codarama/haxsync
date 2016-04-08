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
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import org.codarama.haxsync.SyncContacts;
import org.codarama.haxsync.SyncPreferences;
import org.codarama.haxsync.contacts.CalendarEntries;
import org.codarama.haxsync.entities.HaxSyncContact;
import org.codarama.haxsync.utilities.DeviceUtil;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import biweekly.Biweekly;
import biweekly.ICalendar;
import biweekly.component.VEvent;

/**
 * This implementation of the {@link AbstractThreadedSyncAdapter} takes care of syncing the
 * contact photographs.
 */
public class ContactSyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String TAG = "ContactSyncAdapter";

    private static final String WEBCAL_PROTOCOL_URI_SCHEMA = "webcal://";
    private static final String HTTP_PROTOCOL_URI_SCHEMA = "http://";

    private Context context;

    /**
     * Set up the sync adapter
     */
    public ContactSyncAdapter(Context context, boolean autoInitialize) {
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
    public ContactSyncAdapter(
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

        SyncPreferences prefs = new SyncPreferences(context);
        boolean wifiOnly = prefs.getWiFiOnly();
        boolean chargingOnly = prefs.getChargingOnly();

        // step 1. check prerequisites - WiFi, battery, etc.
        if (wifiOnly && !DeviceUtil.isWifi(context)) {
            return;
        }

        if (chargingOnly && !DeviceUtil.isCharging(context)) {
            return;
        }

        // step 2. fetch URL from settings (if available)
        String birthdayCalendarAddress = prefs.getBirthdayCalendarAddress();

        if (birthdayCalendarAddress == null) {
            // invalid or missing setting, aborting sync
            Log.i(TAG, "Skipping contacts sync due to missing update URL");
            return;
        }

        // step 3. fetch remote calendar and parse it
        List<ICalendar> icals = new ArrayList<>();
        try {
            URL address = processAddress(birthdayCalendarAddress);
            icals.addAll(Biweekly.parse(address.openStream()).all());
        } catch (IOException e) {
            Log.e(TAG, "Unable to extract calendar from remote location " + birthdayCalendarAddress, e);
        }

        // step 4. go through all calendar entries and put them in a list
        CalendarEntries entries = new CalendarEntries();
        for (ICalendar calendar : icals) {
            // just a precaution in case the remote address contains more than one calendar, which is unlikely
            System.out.println("Total entries : " + calendar.getEvents().size());
            for (VEvent event : calendar.getEvents()) {
                final String summary = event.getSummary().getValue();
                final String birthday = event.getDateStart().getValue().toString();
                final String personUID = event.getUid().getValue();
                entries.add(personUID, birthday, summary);
            }
        }

        // step 5. add contacts as phone contacts
        SyncContacts syncContacts = new SyncContacts(context);
        for(HaxSyncContact contact : entries.getContacts()) {
            syncContacts.addContact(account, contact);
        }
    }

    // TODO move this to logic that gets input from the user
    URL processAddress(String address) throws MalformedURLException {
        String result = address;

        if (address.toLowerCase().startsWith(WEBCAL_PROTOCOL_URI_SCHEMA)) {
            result = address.replaceFirst(WEBCAL_PROTOCOL_URI_SCHEMA, HTTP_PROTOCOL_URI_SCHEMA);
        }

        return new URL(result);
    }
}