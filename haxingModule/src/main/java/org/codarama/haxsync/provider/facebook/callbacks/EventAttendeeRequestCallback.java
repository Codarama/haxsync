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

import android.accounts.Account;
import android.content.Context;
import android.util.Log;

import com.facebook.FacebookRequestError;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;

import org.codarama.haxsync.SyncPreferences;
import org.codarama.haxsync.calendar.SyncCalendar;
import org.codarama.haxsync.provider.facebook.Event;
import org.codarama.haxsync.provider.facebook.EventAttendee;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static org.codarama.haxsync.calendar.SyncCalendar.CALENDAR_TYPES;

/**
 * <p>This implementation of the {@link GraphRequest.Callback} parses requests for Facebook events
 * attendees and makes sure they are added in the respective {@link SyncCalendar} event</p>
 * <p>The callback assumes a request is made that would return a single "data" {@link JSONObject},
 * containing several event attendees in a {@link JSONArray}</p>
 */
public class EventAttendeeRequestCallback implements GraphRequest.Callback {
    private static final String TAG = "EventRequestCallback";
    private final Context context;
    private final Account account;
    private final long eventID;

    /**
     * <p>Constructor</p>
     *
     * @param context the {@link Context} in which this {@link EventAttendeeRequestCallback} is called
     * @param account the {@link Account} that executed the event
     * @param eventID the event ID
     */
    public EventAttendeeRequestCallback(Context context, Account account, long eventID) {
        this.context = context;
        this.account = account;
        this.eventID = eventID;
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
        ArrayList<EventAttendee> eventAttendees = new ArrayList<EventAttendee>();
        SyncPreferences prefs = new SyncPreferences(context);

        try {
            JSONArray results = jsonObject.getJSONArray("data");
            for (int i = 0; i < results.length(); i++) {
                eventAttendees.add(new EventAttendee(results.getJSONObject(i)));
            }
        } catch (JSONException e) {
            Log.e(TAG, "Failed while fetching events", e);
        }

        // add events to calendar
        for (EventAttendee attendee : eventAttendees) {
            // TODO filter out non-friend attendees
            calendar.addAttendee(context, eventID, attendee);
        }
    }
}
