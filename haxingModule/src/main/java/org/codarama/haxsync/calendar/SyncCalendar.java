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

package org.codarama.haxsync.calendar;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.text.format.Time;
import android.util.Log;

import org.codarama.haxsync.R;
import org.codarama.haxsync.SyncPreferences;
import org.codarama.haxsync.provider.facebook.Event;
import org.codarama.haxsync.provider.facebook.EventAttendee;

/**
 * <p>The application should use this class to manage it's own calendars, such as calendars for
 * events, birthdays, etc.</p>
 * <p>Besides creating and removing (in the case where a user no longer want so sync some specific
 * details) calendars this abstraction also takes care of
 * <ul>
 * <li>adding reminders</li>
 * <li>adding events</li>
 * <li> etc.</li>
 * </ul>
 * </p>
 */
public class SyncCalendar {
    private static final String TAG = "SyncCalendar";

    private final Context context;
    private final Account account;
    private final String calendarName;

    private long calendarID = -1;

    /**
     * The types of supported calendars
     */
    public static enum CALENDAR_TYPES {
        EVENTS, BIRTHDAYS
    }

    // We want to hide this from the world since we want to control the way this object is created,
    // possibly with the idea of making it a singleton, at some point.
    private SyncCalendar(Context context, Account account, String calendarName) {
        this.context = context;
        this.account = account;
        this.calendarName = calendarName;
    }

    /**
     * <p>Returns an instance of the {@link SyncCalendar}</p>
     * <p/>
     * <p>Note that this method <b>does not</b> actually create the calendar if it does not exist.
     * Instead any modification operation, such as adding events or reminders, would create it, if
     * it does not exist.</p>
     *
     * @param context the {@link Context} this calendar is created in
     * @param account the {@link Account} that is managing this calendar
     * @param type    the {@link org.codarama.haxsync.calendar.SyncCalendar.CALENDAR_TYPES} of this calendar
     * @return
     */
    public static final SyncCalendar getCalendar(Context context, Account account, CALENDAR_TYPES type) {
        String calendarName;

        switch (type) {
            case EVENTS:
                calendarName = context.getString(R.string.event_cal);
                break;
            case BIRTHDAYS:
                calendarName = context.getString(R.string.birthday_cal);
                break;
            default:
                throw new RuntimeException("Unsupported calendar type provided");
        }

        final SyncCalendar calendar = new SyncCalendar(context, account, calendarName);
        return calendar;
    }

    /**
     * Adds an event to the {@link SyncCalendar}
     *
     * @param event the {@link Event} to add
     * @return the unique identifier of this event
     */
    public long addEvent(Event event) {
        Log.i(TAG, "Adding event '" + event.getName() + "' in calendar " + calendarName);

        if (calendarID < 0) {
            initializeCalendar();
        }

        final ContentResolver resolver = context.getContentResolver();
        final String name = event.getName();
        final long start = event.getStartTime();
        final long end = event.getEndTime();
        final String location = event.getLocation();
        final String description = event.getDescription();
        final int rsvp = event.getRsvp();
        final long eid = event.getEventID();
        Uri insertUri = CalendarContract.Events.CONTENT_URI.buildUpon()
                .appendQueryParameter(android.provider.CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, account.name)
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, account.type)
                .build();

        if (eid != -2) {
            String where = CalendarContract.Events.CALENDAR_ID + " = " + calendarID + " AND " + CalendarContract.Events._SYNC_ID + " = " + eid;
            try (Cursor cursor = resolver.query(CalendarContract.Events.CONTENT_URI,
                    new String[]{CalendarContract.Events._ID, CalendarContract.Events.TITLE, CalendarContract.Events.DTSTART, CalendarContract.Events.DTEND,
                            CalendarContract.Events.SELF_ATTENDEE_STATUS, CalendarContract.Events.EVENT_LOCATION, CalendarContract.Events.DESCRIPTION}, where, null, null)) {
                int count = cursor.getCount();
                if (count == 0) {
                    cursor.close();
                    ContentValues values = new ContentValues();
                    values.put(CalendarContract.Events.DTSTART, start);
                    values.put(CalendarContract.Events.DTEND, end);
                    values.put(CalendarContract.Events.TITLE, name);
                    values.put(CalendarContract.Events.HAS_ATTENDEE_DATA, true);
                    values.put(CalendarContract.Events.SELF_ATTENDEE_STATUS, rsvp);
                    values.put(CalendarContract.Events._SYNC_ID, eid);
                    if (location != null) {
                        values.put(CalendarContract.Events.EVENT_LOCATION, location);
                    }
                    if (description != null)
                        values.put(CalendarContract.Events.DESCRIPTION, description);
                    if (rsvp != CalendarContract.Attendees.ATTENDEE_STATUS_ACCEPTED) {
                        values.put(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_FREE);
                    } else {
                        values.put(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY);
                    }
                    values.put(CalendarContract.Events.CALENDAR_ID, calendarID);
                    values.put(CalendarContract.Events.EVENT_TIMEZONE, Time.getCurrentTimezone());
                    return Long.valueOf(resolver.insert(insertUri, values).getLastPathSegment());
                } else {
                    cursor.moveToFirst();
                    long oldstart = cursor.getLong(cursor.getColumnIndex(CalendarContract.Events.DTSTART));
                    long id = cursor.getLong(cursor.getColumnIndex(CalendarContract.Events._ID));
                    long oldend = cursor.getLong(cursor.getColumnIndex(CalendarContract.Events.DTEND));
                    String oldlocation = cursor.getString(cursor.getColumnIndex(CalendarContract.Events.EVENT_LOCATION));
                    String oldDescription = cursor.getString(cursor.getColumnIndex(CalendarContract.Events.DESCRIPTION));
                    int oldrsvp = cursor.getInt(cursor.getColumnIndex(CalendarContract.Events.SELF_ATTENDEE_STATUS));
                    String oldname = cursor.getString(cursor.getColumnIndex(CalendarContract.Events.TITLE));
                    cursor.close();
                    ContentValues values = new ContentValues();
                    if (oldstart != start)
                        values.put(CalendarContract.Events.DTSTART, start);
                    if (oldend != end)
                        values.put(CalendarContract.Events.DTEND, end);
                    if (oldlocation != null && !oldlocation.equals(location))
                        values.put(CalendarContract.Events.EVENT_LOCATION, location);
                    if (oldDescription != null && !oldDescription.equals(description))
                        values.put(CalendarContract.Events.DESCRIPTION, description);
                    if (oldname != null && !oldname.equals(name))
                        values.put(CalendarContract.Events.TITLE, name);
                    if (values.size() != 0)
                        resolver.update(CalendarContract.Events.CONTENT_URI, values, CalendarContract.Events._ID + " = ?", new String[]{String.valueOf(id)});
                    return id;

                }
            }
        }
        return -1;
    }

    public void addAttendee(Context c, long eventID, EventAttendee attendee) {
        ContentValues cv = new ContentValues();
        cv.put(CalendarContract.Attendees.ATTENDEE_NAME, attendee.getName());
        cv.put(CalendarContract.Attendees.EVENT_ID, eventID);
        cv.put(CalendarContract.Attendees.ATTENDEE_STATUS, attendee.getAttendeeStatus());

        c.getContentResolver().insert(CalendarContract.Attendees.CONTENT_URI, cv);
    }

    public void removeAttendees(Context c, long eventID) {
        String where = CalendarContract.Attendees.EVENT_ID + " = '" + eventID + "'";
        c.getContentResolver().delete(CalendarContract.Attendees.CONTENT_URI, where, null);
    }

    /**
     * Add a reminder to the provided event
     *
     * @param eventID the unique identifier of the event
     * @param minutes the amount of minutes before the event that a reminder would pop up
     */
    public void addReminder(long eventID, long minutes) {
        Log.i(TAG, "Adding reminder for ID" + eventID + " in calendar " + calendarName);

        if (calendarID < 0) {
            initializeCalendar();
        }

        ContentResolver contentResolver = context.getContentResolver();

        //delete old reminder
        String where = CalendarContract.Reminders.EVENT_ID + " = " + eventID;
        contentResolver.delete(CalendarContract.Reminders.CONTENT_URI, where, null);

        ContentValues values = new ContentValues();
        values.put(CalendarContract.Reminders.EVENT_ID, eventID);
        values.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
        values.put(CalendarContract.Reminders.MINUTES, minutes);

        contentResolver.insert(CalendarContract.Reminders.CONTENT_URI, values);
    }

    /**
     * <p>Add a birtday to the specific calendar instance</p>
     * <p>Essentially does the same as adding an event only it formats the event in a specific way</p>
     *
     * @param name the name of the birthday boy
     * @param time the exact time in the space time continuum the person was born
     * @return the unique identifier of the event
     */
    public long addBirthday(String name, long time) {
        Log.i(TAG, "Adding birthday for " + name + " in calendar " + calendarName);

        if (calendarID < 0) {
            initializeCalendar();
        }

        ContentResolver resolver = context.getContentResolver();
        String where = CalendarContract.Events.CALENDAR_ID + " = " + calendarID + " AND " + CalendarContract.Events.TITLE + " = \"" + name + "\"";
        try (Cursor cursor = resolver.query(CalendarContract.Events.CONTENT_URI, new String[]{CalendarContract.Events._ID}, where, null, null)) {
            int count = cursor.getCount();
            if (count == 0) {
                cursor.close();
                ContentValues values = new ContentValues();
                values.put(CalendarContract.Events.DTSTART, time);
                values.put(CalendarContract.Events.TITLE, name);
                values.put(CalendarContract.Events.ALL_DAY, 1);
                values.put(CalendarContract.Events.RRULE, "FREQ=YEARLY");
                values.put(CalendarContract.Events.CALENDAR_ID, calendarID);
                values.put(CalendarContract.Events.DURATION, "P1D");
                values.put(CalendarContract.Events.EVENT_TIMEZONE, "utc");
                values.put(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_FREE);
                return Long.valueOf(resolver.insert(CalendarContract.Events.CONTENT_URI, values).getLastPathSegment());
            } else {
                cursor.moveToFirst();
                long id = cursor.getLong(cursor.getColumnIndex(CalendarContract.Events._ID));
                cursor.close();
                return id;
            }
        }
    }

    public void removeCalendar() {
        Log.i(TAG, "Deleting calendar " + calendarName);

        ContentResolver resolver = context.getContentResolver();

        long calID = getCalendarID(calendarName);
        if (calID == -2) {
            Log.w(TAG, "Attempted to remove non-existing calendar " + calendarName);
            return;
        }

        Uri calcUri = CalendarContract.Calendars.CONTENT_URI.buildUpon()
                .appendQueryParameter(android.provider.CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, account.name)
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, account.type)
                .build();
        resolver.delete(calcUri, CalendarContract.Calendars._ID + " = " + calID, null);
    }

    private void initializeCalendar() {
        SyncPreferences prefs = new SyncPreferences(context);

        long calendarID = getCalendarID(calendarName);
        if (calendarID == -2) {
            // lazy initialization - only create if it not already created
            int color = prefs.getEventCalendarColor();
            calendarID = createCalendar(calendarName, color);
        }

        this.calendarID = calendarID;
    }

    private long createCalendar(String name, int color) {
        ContentValues values = new ContentValues();
        ContentResolver resolver = context.getContentResolver();
        values.put(CalendarContract.Calendars.NAME, name);
        values.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, name);
        values.put(CalendarContract.Calendars.CALENDAR_COLOR, color);
        values.put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_OWNER);
        values.put(CalendarContract.Calendars.OWNER_ACCOUNT, account.name);
        values.put(CalendarContract.Calendars.ACCOUNT_NAME, account.name);
        values.put(CalendarContract.Calendars.ACCOUNT_TYPE, account.type);
        values.put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_READ);
        values.put(CalendarContract.Calendars.SYNC_EVENTS, 1);
        values.put(CalendarContract.Calendars.CALENDAR_TIME_ZONE, Time.getCurrentTimezone());
        Uri calSyncUri = CalendarContract.Calendars.CONTENT_URI.buildUpon()
                .appendQueryParameter(android.provider.CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, account.name)
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, account.type)
                .build();
        Uri calUri = resolver.insert(calSyncUri, values);
        long calId = ContentUris.parseId(calUri);
        return calId;
    }

    private long getCalendarID(String name) {
        String[] projection = new String[]{
                CalendarContract.Calendars._ID,
                CalendarContract.Calendars.ACCOUNT_NAME,
        };

        ContentResolver resolver = context.getContentResolver();
        String where = CalendarContract.Calendars.ACCOUNT_NAME + " = ? AND " + CalendarContract.Calendars.ACCOUNT_TYPE + " = '" + account.type
                + "' AND " + CalendarContract.Calendars.CALENDAR_DISPLAY_NAME + " = '" + name + "'";
        try (Cursor calendarCursor = resolver.query(CalendarContract.Calendars.CONTENT_URI, projection, where, new String[]{account.name}, null)) {
            Log.i(TAG, "Calendars found ('" + name + "'):" + calendarCursor.getCount());

            if (calendarCursor.getCount() <= 0) {
                calendarCursor.close();
                return -2;
            } else {
                calendarCursor.moveToFirst();
                long id = calendarCursor.getLong(calendarCursor.getColumnIndex(CalendarContract.Calendars._ID));
                calendarCursor.close();
                return id;
            }
        }
    }
}
