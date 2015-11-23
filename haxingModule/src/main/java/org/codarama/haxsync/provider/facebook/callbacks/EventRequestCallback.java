/*
 * Copyright (c) 2015 Codarama.Org, All Rights Reserved
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 */

package org.codarama.haxsync.provider.facebook.callbacks;

import static org.codarama.haxsync.calendar.SyncCalendar.CALENDAR_TYPES;

import android.accounts.Account;
import android.content.Context;
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

/**
 * <p>This implementation of the {@link com.facebook.GraphRequest.Callback} parses requests for
 * Facebook events and makes sure they are added in the respective {@link SyncCalendar}</p>
 * <p>The callback assumes a request is made that would return a single "data" {@link JSONObject},
 * containing several events in a {@link JSONArray}</p>
 */
public class EventRequestCallback implements GraphRequest.Callback {
    private static final String TAG = "EventRequestCallback";
    private final Context context;
    private final Account account;

    public EventRequestCallback(Context context, Account account) {
        this.context = context;
        this.account = account;
    }

    @Override
    public void onCompleted(GraphResponse graphResponse) {
        if (graphResponse == null || graphResponse.getRawResponse() == null) {
            Log.i(TAG, "Shitty fuck !");
            return;
        }

        Log.i(TAG, "Received Facebook response : ");
        Log.d(TAG, graphResponse.getRawResponse());

        // handle errors, should probably think of some more elaborate solution here
        if (graphResponse.getError() != null) {
            FacebookRequestError error = graphResponse.getError();
            Log.e(TAG, "Unfortunately Facebook says that " + error.getErrorMessage(), error.getException());
            return;
        }

        // attempt to parse the data returned by Facebook
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
        SyncCalendar calendar = SyncCalendar.getCalendar(context, account, CALENDAR_TYPES.EVENTS);
        ArrayList<Event> events = new ArrayList<Event>();
        SyncPreferences prefs = new SyncPreferences(context);

        try {
            JSONArray results = jsonObject.getJSONArray("data");
            for (int i = 0; i < results.length(); i++) {
                events.add(new Event(results.getJSONObject(i)));
            }
        } catch (JSONException e) {
            Log.e(TAG, "Failed while fetching events", e);
        }

        // add events to calendar
        for (Event event : events) {
            long id = calendar.addEvent(event);

            // add event attendees
            // TODO

            // add event reminders
            if (prefs.shouldRemindForEvents()) {
                long minutes = prefs.getEventReminderMinutes();
                calendar.addReminder(id, minutes);
            }
        }
    }
}
