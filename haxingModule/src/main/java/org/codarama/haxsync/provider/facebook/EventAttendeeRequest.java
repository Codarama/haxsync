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
public class EventAttendeeRequest {
    private static final String TAG = "EventRequest";
    private static final String ATTENDING_EVENTS_EDGE = "/attending";

    private final long eventID;

    public EventAttendeeRequest(long eventID) {
        this.eventID = eventID;
    }

    public void executeAsync(GraphRequest.Callback callback) {
        executeRequest(eventID + ATTENDING_EVENTS_EDGE, callback);
    }

    private void executeRequest(String edge, GraphRequest.Callback callback) {
        Log.d(TAG, "Executing Graph API request :");
        Log.d(TAG, "Edge :" + edge);

        final AccessToken accessToken = AccessToken.getCurrentAccessToken();

        GraphRequest request = GraphRequest.newGraphPathRequest(accessToken, edge, callback);
        Bundle parameters = new Bundle();
        request.setParameters(parameters);
        request.executeAsync();
    }
}
