package org.codarama.haxsync.provider.facebook;

import android.provider.CalendarContract;
import android.util.Log;

import org.codarama.haxsync.utilities.CalendarUtil;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * <p>Represents a Facebook event, extracted from the graph API</p>
 */
public class Event {

    private static final String TAG = "Event";

    private JSONObject json;

    public Event(JSONObject json) {
        this.json = json;
    }

    public long getEventID() {
        try {
            return json.getLong("id");
        } catch (JSONException e) {
            Log.e(TAG, "Unable to parse event ID, because " + e.getMessage(), e);
            return -2;
        }
    }

    public String getLocation() {
        try {
            return json.getJSONObject("place").getString("name");
        } catch (JSONException e) {
            Log.e(TAG, "Unable to parse event location, because " + e.getMessage(), e);
            return "No location data available";
        }
    }

    public long getStartTime() {
        try {
            return CalendarUtil.convertTime(json.getLong("start_time") * 1000);
        } catch (JSONException e) {
            try {
                String timeString = json.getString("start_time");
                return CalendarUtil.ISOtoEpoch(timeString);
            } catch (JSONException e1) {
                Log.e("Error", e.getLocalizedMessage());
                return -2;
            }
        }
    }

    public long getEndTime() {
        try {
            return CalendarUtil.convertTime(json.getLong("end_time") * 1000);
        } catch (JSONException e) {
            try {
                String timeString = json.getString("end_time");
                if (timeString.equals("null")) {
                    return getStartTime();
                }
                return CalendarUtil.ISOtoEpoch(timeString);
            } catch (JSONException e1) {
                Log.e(TAG, "Unable to parse event end time, because " + e.getMessage(), e);
                return -2;
            }
        }
    }

    public String getDescription() {
        try {
            return json.getString("description");
        } catch (JSONException e) {
            Log.e(TAG, "Unable to parse event description, because " + e.getMessage(), e);
            return "Missing description";
        }
    }

    public String getName() {
        try {
            return json.getString("name");
        } catch (JSONException e) {
            Log.e(TAG, "Unable to parse event name, because " + e.getMessage(), e);
            return "Facebook event";
        }
    }

    public int getRsvp() {
        try {
            return fromFacebookStatus(json.getString("rsvp_status"));
        } catch (JSONException e) {
            Log.e(TAG, "Unable to parse event RSVP status, because " + e.getMessage(), e);
            // fallback to "invited" since we were able to get this event, but could not parse it
            return CalendarContract.Attendees.ATTENDEE_STATUS_INVITED;
        }
    }

    /**
     * <p>Attempts to convert the provided {@link String} to an event RSVP status, assuming that the argument is a valid Facebook status. </p>
     *
     * @param statusString
     * @return one of {@link android.provider.CalendarContract.Attendees} statuses
     * @see android.provider.CalendarContract.Attendees
     */
    public static int fromFacebookStatus(String statusString) {
        if (statusString.equals("attending")) {
            return CalendarContract.Attendees.ATTENDEE_STATUS_ACCEPTED;
        } else if (statusString.equals("unsure")) {
            return CalendarContract.Attendees.ATTENDEE_STATUS_TENTATIVE;
        } else if (statusString.equals("declined")) {
            return CalendarContract.Attendees.ATTENDEE_STATUS_DECLINED;
        } else {
            return CalendarContract.Attendees.ATTENDEE_STATUS_INVITED;
        }
    }
}
