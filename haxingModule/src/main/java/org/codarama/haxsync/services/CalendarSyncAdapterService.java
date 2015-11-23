package org.codarama.haxsync.services;

import android.accounts.Account;
import android.accounts.OperationCanceledException;
import android.app.Service;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.CalendarContract;
import android.util.Log;

import org.codarama.haxsync.SyncPreferences;
import org.codarama.haxsync.calendar.SyncCalendar;
import org.codarama.haxsync.provider.facebook.BirthdayRequest;
import org.codarama.haxsync.provider.facebook.EventRequest;
import org.codarama.haxsync.provider.facebook.callbacks.BirthdayRequestCallback;
import org.codarama.haxsync.provider.facebook.callbacks.EventRequestCallback;
import org.codarama.haxsync.utilities.DeviceUtil;
import org.codarama.haxsync.utilities.FacebookUtil;

import java.util.HashSet;
import java.util.Set;

public class CalendarSyncAdapterService extends Service {
    private static final String TAG = "CalendarSyncService";
    private static SyncAdapterImpl sSyncAdapter = null;
    private static ContentResolver mContentResolver = null;

    public CalendarSyncAdapterService() {
        super();
    }

    private static long getCalendarID(Account account, String name) {
        String[] projection = new String[]{
                CalendarContract.Calendars._ID,
                CalendarContract.Calendars.ACCOUNT_NAME,
        };
        String where = CalendarContract.Calendars.ACCOUNT_NAME + " = ? AND " + CalendarContract.Calendars.ACCOUNT_TYPE + " = '" + account.type
                + "' AND " + CalendarContract.Calendars.CALENDAR_DISPLAY_NAME + " = '" + name + "'";
        Cursor calendarCursor = mContentResolver.query(CalendarContract.Calendars.CONTENT_URI, projection, where, new String[]{account.name}, null);
        Log.i("CALENDARS FOUND:", String.valueOf(calendarCursor.getCount()));
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

    private static void addReminder(long eventID, long minutes) {
        //delete old reminder
        String where = CalendarContract.Reminders.EVENT_ID + " = " + eventID;
        mContentResolver.delete(CalendarContract.Reminders.CONTENT_URI, where, null);

        ContentValues values = new ContentValues();
        values.put(CalendarContract.Reminders.EVENT_ID, eventID);
        values.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
        values.put(CalendarContract.Reminders.MINUTES, minutes);

        mContentResolver.insert(CalendarContract.Reminders.CONTENT_URI, values);

    }

    public static void removeCalendar(Context context, Account account, String name) {
        mContentResolver = context.getContentResolver();
        long calID = getCalendarID(account, name);
        if (calID == -2) {
            return;
        }
        Uri calcUri = CalendarContract.Calendars.CONTENT_URI.buildUpon()
                .appendQueryParameter(android.provider.CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, account.name)
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, account.type)
                .build();
        mContentResolver.delete(calcUri, CalendarContract.Calendars._ID + " = " + calID, null);
    }

    public static void setCalendarColor(Context context, Account account, String name, int color) {
        mContentResolver = context.getContentResolver();
        long calID = getCalendarID(account, name);
        if (calID == -2) {
            return;
        }
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Calendars.CALENDAR_COLOR, color);
        Uri calcUri = CalendarContract.Calendars.CONTENT_URI.buildUpon()
                .appendQueryParameter(android.provider.CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, account.name)
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, account.type)
                .build();

        mContentResolver.update(calcUri, values, CalendarContract.Calendars._ID + " = " + calID, null);
    }

    public static void removeReminders(Context context, Account account, String calendarName) {
        mContentResolver = context.getContentResolver();
        long calID = getCalendarID(account, calendarName);
        if (calID == -2) {
            return;
        }
        for (long id : getEvents(calID)) {
            String where = CalendarContract.Reminders.EVENT_ID + " = " + id;
            mContentResolver.delete(CalendarContract.Reminders.CONTENT_URI, where, null);
        }
    }

    public static void updateReminders(Context context, Account account, String calendarName, long minutes) {
        mContentResolver = context.getContentResolver();
        long calID = getCalendarID(account, calendarName);
        if (calID == -2) {
            return;
        }
        for (long id : getEvents(calID)) {
            addReminder(id, minutes);
        }

    }

    private static Set<Long> getEvents(long calendarID) {
        HashSet<Long> events = new HashSet<Long>();
        Cursor c1 = mContentResolver.query(CalendarContract.Events.CONTENT_URI, new String[]{CalendarContract.Events._ID}, CalendarContract.Events.CALENDAR_ID + " = " + calendarID, null, null);
        while (c1.moveToNext()) {
            events.add(c1.getLong(0));
        }
        c1.close();
        return events;
    }

    private static void performSync(Context context, Account account,
                                    Bundle extras, String authority, ContentProviderClient provider,
                                    SyncResult syncResult) throws OperationCanceledException {

        SyncPreferences prefs = new SyncPreferences(context);
        boolean wifiOnly = prefs.getWiFiOnly();
        boolean chargingOnly = prefs.getChargingOnly();

        if (!((wifiOnly && !DeviceUtil.isWifi(context)) || (chargingOnly && !DeviceUtil.isCharging(context)))) {
            Log.i(TAG, "Initiating event sync");

            FacebookUtil.refreshPermissions(context);
            boolean eventSync = prefs.shouldSyncEvents();
            boolean birthdaySync = prefs.shouldSyncBirthdays();

            if (FacebookUtil.authorize(context, account)) {

                // Part 1. Birthdays sync
                if (birthdaySync) {
                    BirthdayRequest request = new BirthdayRequest();
                    BirthdayRequestCallback callback = new BirthdayRequestCallback(context, account);
                    request.executeAsync(callback);
                } else {
                    SyncCalendar calendar = SyncCalendar.getCalendar(context, account, SyncCalendar.CALENDAR_TYPES.BIRTHDAYS);
                    calendar.removeCalendar();
                }

                // Part 2. Events sync
                if (eventSync) {
                    final boolean syncMaybe = prefs.shouldSyncMaybe();

                    EventRequest request = new EventRequest(syncMaybe);
                    EventRequestCallback callback = new EventRequestCallback(context, account);
                    request.executeAsync(callback);
                } else {
                    SyncCalendar calendar = SyncCalendar.getCalendar(context, account, SyncCalendar.CALENDAR_TYPES.EVENTS);
                    calendar.removeCalendar();
                }
            }
        } else {
            prefs.setMissedCalendarSync(true);
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        IBinder ret = getSyncAdapter().getSyncAdapterBinder();
        return ret;
    }

    private SyncAdapterImpl getSyncAdapter() {
        if (sSyncAdapter == null)
            sSyncAdapter = new SyncAdapterImpl(this);
        return sSyncAdapter;
    }

    private static class SyncAdapterImpl extends AbstractThreadedSyncAdapter {
        private Context mContext;

        public SyncAdapterImpl(Context context) {
            super(context, true);
            mContext = context;
        }

        @Override
        public void onPerformSync(Account account, Bundle extras,
                                  String authority, ContentProviderClient provider,
                                  SyncResult syncResult) {
            try {
                CalendarSyncAdapterService.performSync(mContext, account,
                        extras, authority, provider, syncResult);
            } catch (OperationCanceledException e) {
                Log.e("SyncAdapterImpl", e.getLocalizedMessage());
            }
        }
    }
}
