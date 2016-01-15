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
