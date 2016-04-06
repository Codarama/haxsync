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

    /**
     * <p>Calendar</p>
     *
     * @param json the {@link JSONObject} containing data about this event
     */
    public Event(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the ID of the event in Facebook
     */
    public long getEventID() {
        try {
            return json.getLong("id");
        } catch (JSONException e) {
            Log.e(TAG, "Unable to parse event ID, because " + e.getMessage(), e);
            return -2;
        }
    }

    /**
     * @return the location where this event is taking place in
     */
    public String getLocation() {
        try {
            return json.getJSONObject("place").getString("name");
        } catch (JSONException e) {
            Log.e(TAG, "Unable to parse event location, because " + e.getMessage(), e);
            return "No location data available";
        }
    }

    /**
     * @return the start time of this event
     */
    public long getStartTime() {
        try {
            String timeString = json.getString("start_time");
            return CalendarUtil.ISOtoEpoch(timeString);
        } catch (JSONException e) {
            Log.e(TAG, "Unable to parse event start time, because " + e.getMessage(), e);
            return -2;
        }
    }

    /**
     * @return the end time of this event, or start time if no end time is returned
     */
    public long getEndTime() {
        try {
            String timeString = json.getString("end_time");
            return CalendarUtil.ISOtoEpoch(timeString);
        } catch (JSONException e) {
            Log.e(TAG, "Unable to parse event end time, because " + e.getMessage(), e);
            Log.i(TAG, "Falling back to start time.");
            return getStartTime();
        }
    }

    /**
     * @return the description of this event
     */
    public String getDescription() {
        try {
            return json.getString("description");
        } catch (JSONException e) {
            Log.e(TAG, "Unable to parse event description, because " + e.getMessage(), e);
            return "Missing description";
        }
    }

    /**
     * @return the name of this event
     */
    public String getName() {
        try {
            return json.getString("name");
        } catch (JSONException e) {
            Log.e(TAG, "Unable to parse event name, because " + e.getMessage(), e);
            return "Facebook event";
        }
    }

    /**
     * @return the RSVP status for this event
     * @see android.provider.CalendarContract.Attendees
     */
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
