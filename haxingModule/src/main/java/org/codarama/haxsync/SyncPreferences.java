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

package org.codarama.haxsync;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

/**
 * <p>Contains all the settings that control how HaxSync works.</p>
 * <p>Currently supported (and their defaults):</p>
 * <ul>
 * <li>EVENT_COLOR (0xff2525)</li>
 * <li>EVENT_REMINDER_MINUTES (30)</li>
 * <li>EVENT_REMINDERS (false)</li>
 * <li>WIFI_ONLY (false)</li>
 * <li>CHARGING_ONLY (false)</li>
 * <li>MISSED_CALENDAR_SYNC (true)</li>
 * <li>SYNC_EVENTS (true)</li>
 * <li>SYNC_BIRTHDAYS (true)</li>
 * <li>BIRTHDAY_REMINDERS (false)</li>
 * <li>BIRTHDAY_REMINDER_MINUTES (1440)</li>
 * <li>EVENT_STATUS (attending|unsure)</li>
 * <li>FORCE_SYNC (false)</li>
 * <li>HAXSYNC_CONTACTS (0)</li>
 * <li>HAXSYNC_EVENTS (0)</li>
 * <li>HAXSYNC_BIRTHDAYS (0)</li>
 * </ul>
 */
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
    private static final String FORCE_SYNC = "force_dl";

    private static final String HAXSYNC_CONTACTS = "haxsynx_contacts";
    private static final String HAXSYNC_EVENTS = "haxsynx_events";
    private static final String HAXSYNC_BIRTHDAYS = "haxsynx_birthdays";

    private final Context context;
    private final SharedPreferences prefs;

    public SyncPreferences(Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(context.getPackageName() + "_preferences", Context.MODE_MULTI_PROCESS);
    }

    public int getHaxsyncContacts() {
        return prefs.getInt(HAXSYNC_CONTACTS, 0);
    }
    public void setHaxsyncContacts(int newValue) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(HAXSYNC_CONTACTS, newValue);
        editor.commit();
    }


    public int getHaxsyncEvents() {
        return prefs.getInt(HAXSYNC_EVENTS, 0);
    }
    public void setHaxsyncEvents(long newValue) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(HAXSYNC_EVENTS, newValue);
        editor.commit();
    }


    public int getHaxsyncBirthdays() {
        return prefs.getInt(HAXSYNC_BIRTHDAYS, 0);
    }
    public void setHaxsyncBirthdays(int newValue) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(HAXSYNC_BIRTHDAYS, newValue);
        editor.commit();
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

    public boolean getForceSync() {
        return prefs.getBoolean(FORCE_SYNC, false);
    }

    public void setForceSync(boolean forceSync) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(FORCE_SYNC, true);
        editor.commit();
    }

    public boolean getRootEnabled() {
        return prefs.getBoolean("root_enabled", false);
    }

    public boolean shouldUpdateGooglePhotos() {
        return prefs.getBoolean("update_google_photos", false);
    }

    public boolean shouldBePrimaryImage() {
        return prefs.getBoolean("image_primary", true);
    }

    public boolean shouldIgnoreMiddleNames() {
        return prefs.getBoolean("ignore_middle_names", false);
    }

    public int fuzzinessLevel() {
        return Integer.parseInt(prefs.getString("fuzziness", "2"));
    }

    public Set<String> getAddFriends() {
        return prefs.getStringSet("add_friends", new HashSet<String>());
    }

    public String getBirthdayCalendarAddress() {
        return "webcal://www.facebook.com/ical/b.php?uid=587066728&key=AQAm5hw4OpDsR_8b";
    }
}
