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

package org.codarama.haxsync.contacts;


import org.codarama.haxsync.entities.HaxSyncContact;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>This class is used to store calendar entries, parsed from an iCal birthday calendar</p>
 * <p>Part of it's purpose is to properly discover the names of each entry</p>
 */
public class CalendarEntries {

    private List<HaxSyncContact> contacts = new ArrayList<>();
    private String commonPart;


    /**
     * @return a {@link List} of {@link HaxSyncContact} entries, with proper names
     */
    public List<HaxSyncContact> getContacts() {
        if (contacts.size() < 2) {
            // unfortunately if there are less than 2 contacts with birthdays then we can't really
            // determine the right name - to avoid showing "Birthday of ..." we just assume we are
            // fucked and throw an exception.
            throw new IllegalStateException("Not enough contacts to discover exact names");
        } else {
            processThyContacts();
        }

        return new ArrayList<>(contacts);
    }

    private void processThyContacts() {
        commonPart = contacts.get(0).getName();

        for (HaxSyncContact contact : contacts) {
            String current = contact.getName();
            for (int i = 0; i < current.length() - 1 && i < commonPart.length() - 1; i++) {
                if (current.charAt(i) != commonPart.charAt(i)) {
                    commonPart = commonPart.substring(0, i);
                }
            }
        }

        List<HaxSyncContact> newList = new ArrayList<>();

        for (HaxSyncContact contact : contacts) {
            String currentName = contact.getName();
            final String processedName = currentName.replace(commonPart, "");

            final String birthday = contact.getBirthday();
            final String id = contact.getRemoteId();


            newList.add(new HaxSyncContact() {
                @Override
                public String getRemoteId() {
                    return id;
                }

                @Override
                public String getBirthday() {
                    return birthday;
                }

                @Override
                public String getName() {
                    return processedName;
                }
            });
        }

        this.contacts = newList;
    }

    public void add(final String personUID, final String birthday, final String summary) {

        contacts.add(new HaxSyncContact() {
            @Override
            public String getRemoteId() {
                return personUID;
            }

            @Override
            public String getBirthday() {
                return birthday;
            }

            @Override
            public String getName() {
                return summary;
            }
        });
    }
}

