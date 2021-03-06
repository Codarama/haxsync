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
package org.codarama.haxsync.fragments;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.codarama.haxsync.R;
import org.codarama.haxsync.SyncPreferences;

/**
 * <p></>Home Screen Fragment</p>
 * <p>
 * <p>Displays information about the current synchronization state and some
 * statistics such as number of accounts synced, etc.<p/>
 */
public final class HomeFragment extends Fragment {

    private int totalContacts;

    private int syncedContacts;
    private int syncedBirthdays;
    private int syncedEvents;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.home_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        prepareData();
    }

    /**
     * First step - load data once when view is visible
     * Next step - bind a listener to monitor when sync finishes
     */
    private void prepareData() {
        ContentResolver resolver = getActivity().getApplicationContext().getContentResolver();
        try (Cursor cursor = resolver.query(ContactsContract.RawContacts.CONTENT_URI, null, null, null, null)) {
            totalContacts = cursor.getCount();
        }

        SyncPreferences preferences = new SyncPreferences(getActivity().getApplicationContext());
        this.syncedContacts = preferences.getHaxsyncContacts();
        this.syncedBirthdays = preferences.getHaxsyncBirthdays();
        this.syncedEvents = preferences.getHaxsyncEvents();


        TextView synced = (TextView) getActivity().findViewById(R.id.textContactsSynced);
        synced.setText(syncedContacts + " of " + totalContacts);

        TextView contacts = (TextView) getActivity().findViewById(R.id.textSyncedTotal);
        contacts.setText(Integer.toString(syncedContacts));
        TextView events = (TextView) getActivity().findViewById(R.id.textEventsTotal);
        events.setText(Integer.toString(syncedBirthdays));
        TextView birthdays = (TextView) getActivity().findViewById(R.id.textBirthdaysTotal);
        birthdays.setText(Integer.toString(syncedEvents));
    }
}
