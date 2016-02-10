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

package org.codarama.haxsync.provider.facebook.callbacks;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import com.facebook.FacebookRequestError;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;

import org.codarama.haxsync.SyncPreferences;
import org.codarama.haxsync.calendar.SyncCalendar;
import org.codarama.haxsync.provider.facebook.Event;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

import static org.codarama.haxsync.calendar.SyncCalendar.CALENDAR_TYPES;

public class BirthdayRequestCallback implements GraphRequest.Callback {
    private static final String TAG = "BirthdayRequestCallback";
    private final Context context;
    private final Account account;

    public BirthdayRequestCallback(Context context, Account account) {
        this.context = context;
        this.account = account;
    }

    @Override
    public void onCompleted(GraphResponse graphResponse) {
        Log.i(TAG, "Received Facebook response : ");


        // handle errors, should probably think of some more elaborate solution here
        if (graphResponse.getError() != null) {
            FacebookRequestError error = graphResponse.getError();
            Log.e(TAG, "Unfortunately Facebook says that " + error.getErrorMessage(), error.getException());
            return;
        }

        // attempt to parse the data returned by Facebook"world wide fx"
        if (graphResponse.getJSONObject() != null) {
            handleRespose(graphResponse.getJSONObject());
        }

        // visit any remaining pages from the response
        GraphRequest nextPageRequest = graphResponse.getRequestForPagedResults(GraphResponse.PagingDirection.NEXT);
        if (nextPageRequest != null) {
            nextPageRequest.setCallback(this);
            nextPageRequest.executeAsync();
        }
    }

    private void handleRespose(JSONObject jsonObject) {
        SyncCalendar calendar = SyncCalendar.getCalendar(context, account, CALENDAR_TYPES.BIRTHDAYS);
        SyncPreferences prefs = new SyncPreferences(context);
        long reminderTime = prefs.getBirthdayReminderMinutes();
        boolean phoneOnly = prefs.syncPhoneContectsOnly();
        boolean birthdayReminders = prefs.shouldRemindForBirthdays();

        ArrayList<Event> birthdays = new ArrayList<Event>();
        Set<String> friends = getFriends();

        try {
            JSONArray results = jsonObject.getJSONArray("data");
            for (int i = 0; i < results.length(); i++) {
                JSONObject result = results.getJSONObject(i);
                if (result.get("birthday") != null) {
                    String name = result.getString("name");
                    String birthday = result.getString("birthday");
                    if (phoneOnly && !friends.contains(name)) {
                        Log.d(TAG, "Skipping birthday, because contact is not in phone list");
                    } else {
                        long birthdayMillis = parseBirthday(birthday);
                        long eventId = calendar.addBirthday(name, birthdayMillis);
                        if (birthdayReminders) {
                            calendar.addReminder(eventId, reminderTime);
                        }
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Failed while fetching birthdays", e);
        }
    }

    private Set<String> getFriends() {
        HashSet<String> friends = new HashSet<String>();
        ContentResolver resolver = context.getContentResolver();
        Uri rawContactUri = ContactsContract.RawContacts.CONTENT_URI.buildUpon()
                .appendQueryParameter(ContactsContract.RawContacts.ACCOUNT_NAME, account.name)
                .appendQueryParameter(ContactsContract.RawContacts.ACCOUNT_TYPE, account.type)
                .build();
        Cursor c1 = resolver.query(rawContactUri, new String[]{ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY}, null, null, null);
        while (c1.moveToNext()) {
            friends.add(c1.getString(0));
        }
        c1.close();
        return friends;
    }

    private long parseBirthday(String date) {
        int month = Integer.valueOf(date.split("/")[0]);
        int day = Integer.valueOf(date.split("/")[1]);
        GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.set(Calendar.getInstance().get(Calendar.YEAR), month - 1, day, 0, 0, 0);
        long millis = cal.getTimeInMillis();
        return millis;
    }
}
