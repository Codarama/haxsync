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

public class BirthdayRequest {
    private static final String TAG = "BirthdayRequest";
    private static final String FRIENDS_EDGE = "me/friends";
    private static final String EVENT_FIELDS = "name, birthday";

    public void executeAsync(GraphRequest.Callback callback) {
        String fields = FRIENDS_EDGE;
        String edge = EVENT_FIELDS;
        executeRequest(edge, fields, callback);
    }

    private void executeRequest(String edge, String fields, GraphRequest.Callback callback) {
        Log.d(TAG, "Executing Graph API request :");
        Log.d(TAG, "Edge :" + edge);
        Log.d(TAG, "Fields :" + fields);

        final AccessToken accessToken = AccessToken.getCurrentAccessToken();

        GraphRequest request = GraphRequest.newGraphPathRequest(accessToken, edge, callback);
        Bundle parameters = new Bundle();
        parameters.putString("fields", fields);
        request.setParameters(parameters);
        request.executeAsync();
    }
}
