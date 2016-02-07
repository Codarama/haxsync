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
import android.text.TextUtils;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphRequestBatch;

import org.codarama.haxsync.utilities.DeviceUtil;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

public class ContactDetailsRequest {
    private static final String TAG = "ContactDetailsRequest";
    private static final String PROFILE_PICTURE_EDGE = "/picture";

    private final List<String> facebookFriendIds;
    private final int screenWidth;
    private final int screenHeight;

    public ContactDetailsRequest(final Set<String> facebookFriendIds, final int width, final int height) {
        super();

        this.facebookFriendIds = new ArrayList<>(facebookFriendIds);
        this.screenWidth = width;
        this.screenHeight = height;
    }

    public void executeAsync(GraphRequest.Callback callback) {
        Log.i(TAG, "Requesting facebook contacts pictures");

        final AccessToken accessToken = AccessToken.getCurrentAccessToken();

        for (int i = 0; i < facebookFriendIds.size(); i += 49) {
            int end = i + 49;
            if (end >= facebookFriendIds.size()) {
                end = facebookFriendIds.size() - 1;
            }

            List<String> sublist = facebookFriendIds.subList(i, end);

            Log.d(TAG, "Edge :" + PROFILE_PICTURE_EDGE);
            Log.d(TAG, "Ids :" + sublist.toString());
            Log.d(TAG, "Width :" + screenWidth);
            Log.d(TAG, "Height :" + screenHeight);

            GraphRequest request = GraphRequest.newGraphPathRequest(accessToken, PROFILE_PICTURE_EDGE, callback);
            Bundle parameters = new Bundle();
            parameters.putString("ids", TextUtils.join(",", sublist));
            parameters.putInt("width", screenWidth);
            parameters.putInt("height", screenHeight);
            // those naughty, naughty developers at FB would try to redirect, unless we ask them not to
            parameters.putBoolean("redirect", false);
            request.setParameters(parameters);

            request.executeAsync();
        }
    }
}
