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

package org.codarama.haxsync.utilities;

import android.content.ContentValues;
import android.content.Context;
import android.provider.CalendarContract.Attendees;
import android.util.Log;

import org.codarama.haxsync.provider.facebook.EventAttendee;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class CalendarUtil {
    private static SimpleDateFormat ISO8601DATEFORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

    public static void addAttendee(Context c, long eventID, EventAttendee attendee) {
        ContentValues cv = new ContentValues();
        cv.put(Attendees.ATTENDEE_NAME, attendee.getName());
        cv.put(Attendees.EVENT_ID, eventID);
        cv.put(Attendees.ATTENDEE_STATUS, attendee.getAttendeeStatus());

        c.getContentResolver().insert(Attendees.CONTENT_URI, cv);
    }

    public static void removeAttendees(Context c, long eventID) {
        String where = Attendees.EVENT_ID + " = '" + eventID + "'";
        c.getContentResolver().delete(Attendees.CONTENT_URI, where, null);
    }

    public static long ISOtoEpoch(String time) {
        try {
            Date d = ISO8601DATEFORMAT.parse(time);
            return d.getTime();
        } catch (ParseException e) {
            Log.e("Error", e.getLocalizedMessage());
            return -2;
        }

    }

    public static long convertTime(long time) {
        GregorianCalendar t1 = new GregorianCalendar(TimeZone.getTimeZone("America/Los_Angeles"));
        t1.setTimeInMillis(time);
        GregorianCalendar t2 = new GregorianCalendar();
        t2.set(t1.get(GregorianCalendar.YEAR), t1.get(GregorianCalendar.MONTH), t1.get(GregorianCalendar.DAY_OF_MONTH), t1.get(GregorianCalendar.HOUR_OF_DAY), t1.get(GregorianCalendar.MINUTE), t1.get(GregorianCalendar.SECOND));
        return t2.getTimeInMillis();
    }

}
