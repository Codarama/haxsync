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

package org.codarama.haxsync.utilities.intents;

import android.content.Intent;

public interface IntentBuilder {

    /**
     * @param postID    the id of the post
     * @param uid       the uid of the poster
     * @param permalink a permalink to the post
     * @return an intent that displays the post
     */
    Intent getPostIntent(String postID, String uid, String permalink);


    /**
     * @param objectID the object_id of a photo object
     * @return an intent that displays the photo
     */
    Intent getPhotoIntent(String objectID);


    /**
     * @param uid the uid of the user in question
     * @return an intent that displays the user's profile page
     */
    Intent getProfileIntent(String uid);

}
