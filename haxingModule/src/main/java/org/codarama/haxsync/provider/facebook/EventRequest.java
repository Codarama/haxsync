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

package org.codarama.haxsync.provider.facebook;

import android.os.Bundle;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;

import java.util.Calendar;

/**
 *
 */
public class EventRequest {
    private static final String TAG = "EventRequest";
    private static final String ATTENDING_EVENTS_EDGE = "me/events/attending";
    private static final String MAYBE_EVENTS_EDGE = "me/events/maybe";
    private static final String EVENT_FIELDS = "name, id, start_time, end_time, description, rsvp_status, place";
    private final boolean includeMaybe;

    public EventRequest(boolean includeMaybe) {
        this.includeMaybe = includeMaybe;
    }

    public void executeAsync(GraphRequest.Callback callback) {
        executeRequest(ATTENDING_EVENTS_EDGE, EVENT_FIELDS, callback);

        if (includeMaybe) {
            executeRequest(MAYBE_EVENTS_EDGE, EVENT_FIELDS, callback);
        }
    }

    private void executeRequest(String edge, String fields, GraphRequest.Callback callback) {
        Log.d(TAG, "Executing Graph API request :");
        Log.d(TAG, "Edge :" + edge);
        Log.d(TAG, "Fields :" + fields);

        final AccessToken accessToken = AccessToken.getCurrentAccessToken();

        GraphRequest request = GraphRequest.newGraphPathRequest(accessToken, edge, callback);
        Bundle parameters = new Bundle();
        parameters.putString("fields", fields);
        parameters.putString("since", Calendar.getInstance().getTime().toString());
        request.setParameters(parameters);
        request.executeAsync();
    }
}
