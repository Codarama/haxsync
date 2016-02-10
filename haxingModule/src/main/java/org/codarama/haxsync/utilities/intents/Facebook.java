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
import android.net.Uri;

public class Facebook implements IntentBuilder {

    @Override
    public Intent getPostIntent(String postID, String uid, String permalink) {
        return new Intent(Intent.ACTION_VIEW, Uri.parse("fb://post/" + postID + "?owner=" + uid));
    }

    @Override
    public Intent getPhotoIntent(String pid) {
        return new Intent(Intent.ACTION_VIEW, Uri.parse("fb://photo/" + pid));
    }

    @Override
    public Intent getProfileIntent(String uid) {
        return new Intent(Intent.ACTION_VIEW, Uri.parse("fb://profile/" + uid));
    }

    public String getPackageName() {
        return "com.facebook.katana";
    }

}
