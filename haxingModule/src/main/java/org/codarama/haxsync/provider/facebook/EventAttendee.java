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

package org.codarama.haxsync.provider.facebook;

import android.util.Log;

import org.codarama.haxsync.utilities.FacebookUtil;
import org.json.JSONException;
import org.json.JSONObject;

public class EventAttendee {
    private static final String TAG = "EventAttendee";

    private final JSONObject rawdata;

    public EventAttendee(JSONObject json) {
        rawdata = json;
    }

    public int getAttendeeStatus() {
        try {
            return FacebookUtil.convertStatus(rawdata.getString("rsvp_status"));
        } catch (JSONException e) {
            Log.e(TAG, "Unable to extract data about event attendee RSVP status", e);
            return 1;
        }
    }

    public String getName() {
        try {
            return rawdata.getString("name");
        } catch (JSONException e) {
            Log.e(TAG, "Unable to extract data about event attendee name", e);
            return "John Doe";
        }
    }

    @Override
    public String toString() {
        return "(name: " + getName() + " status: " + getAttendeeStatus() + ")";
    }
}
