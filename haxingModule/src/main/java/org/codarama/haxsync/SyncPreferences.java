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

package org.codarama.haxsync;

import android.content.Context;
import android.content.SharedPreferences;

public class SyncPreferences {

    private static final String EVENT_COLOR = "event_color";
    private static final String EVENT_REMINDER_MINUTES = "event_reminder_minutes";
    private static final String EVENT_REMINDERS = "event_reminders";
    private static final String WIFI_ONLY = "wifi_only";
    private static final String CHARGING_ONLY = "charging_only";
    private static final String MISSED_CALENDAR_SYNC = "missed_calendar_sync";
    private static final String SYNC_EVENTS = "sync_events";
    private static final String SYNC_BIRTHDAYS = "sync_birthdays";
    private static final String BIRTHDAY_REMINDERS = "birthday_reminders";
    private static final String BIRTHDAY_REMINDER_MINUTES = "birthday_reminder_minutes";
    private static final String EVENT_STATUS = "event_status";

    private final Context context;
    private final SharedPreferences prefs;

    public SyncPreferences(Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(context.getPackageName() + "_preferences", Context.MODE_MULTI_PROCESS);
    }

    public long getEventReminderMinutes() {
        return prefs.getLong(EVENT_REMINDER_MINUTES, 30);
    }

    public boolean shouldRemindForEvents() {
        return prefs.getBoolean(EVENT_REMINDERS, false);
    }

    public int getEventCalendarColor() {
        return prefs.getInt(EVENT_COLOR, 0xff2525);
    }

    public boolean getWiFiOnly() {
        return prefs.getBoolean(WIFI_ONLY, false);
    }

    public boolean getChargingOnly() {
        return prefs.getBoolean(CHARGING_ONLY, false);
    }

    public void setMissedCalendarSync(boolean missedCallendarSync) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(MISSED_CALENDAR_SYNC, true);
        editor.commit();
    }

    public boolean shouldSyncEvents() {
        return prefs.getBoolean(SYNC_EVENTS, true);
    }

    public boolean shouldSyncBirthdays() {
        return prefs.getBoolean(SYNC_BIRTHDAYS, false);
    }

    public boolean shouldSyncMaybe() {
        String eventStatuses = prefs.getString(EVENT_STATUS, "attending|unsure");
        return eventStatuses.contains("unsure");
    }

    public boolean shouldRemindForBirthdays() {
        return prefs.getBoolean(BIRTHDAY_REMINDERS, false);
    }

    public long getBirthdayReminderMinutes() {
        return prefs.getLong(BIRTHDAY_REMINDER_MINUTES, 1440);
    }

    public int getBirthdayCalendarColor() {
        return prefs.getInt("birthday_color", 0xff1212);
    }

    public boolean syncPhoneContectsOnly() {
        return prefs.getBoolean("phone_only_cal", false);
    }
}
