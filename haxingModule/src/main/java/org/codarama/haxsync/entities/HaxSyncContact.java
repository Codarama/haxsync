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

package org.codarama.haxsync.entities;

/**
 * <p>Facebook buddy</p>
 * <p>Contains all the publicly available information for a contact, that is synced</p>
 */
public interface HaxSyncContact {

    /**
     * @return The ID of this contact in the list of contacts
     */
    String getRemoteId();

    /**
     * @return the URL pointing to where the profile picture is stored at
     */
    String getBirthday();

    /**
     * @return the height of the profile picture as it is at the remote URL address
     */
    String getName();
}
