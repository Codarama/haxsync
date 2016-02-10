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

public class Stream implements IntentBuilder {
    private String prefix;

    public Stream(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public Intent getPostIntent(String postID, String uid, String permalink) {
        Intent intent = new Intent(prefix + ".action.SHOW_POST");
        intent.putExtra(prefix + "id", postID);
        intent.putExtra(prefix + "from", "");
        intent.putExtra(prefix + "to", "");
        intent.putExtra(prefix + "fromid", "");
        intent.putExtra(prefix + "message", "");
        intent.putExtra(prefix + "haallegato", false);
        intent.putExtra(prefix + "picture", "");
        intent.putExtra(prefix + "link", "");
        intent.putExtra(prefix + "object_id", "");
        intent.putExtra(prefix + "source", "");
        intent.putExtra(prefix + "name", "");
        intent.putExtra(prefix + "caption", "");
        intent.putExtra(prefix + "description", "");
        intent.putExtra(prefix + "data", "");
        intent.putExtra(prefix + "like", "");
        intent.putExtra(prefix + "comm", "");
        intent.putExtra(prefix + "type", "");
        intent.putExtra(prefix + "icon", "");
        intent.putExtra(prefix + "story", "");
        return intent;
    }

    @Override
    public Intent getPhotoIntent(String pid) {
        Intent intent = new Intent(prefix + ".action.SHOW_FOTO");
        intent.putExtra(prefix + "id", pid);
        return intent;
    }

    @Override
    public Intent getProfileIntent(String uid) {
        Intent intent = new Intent(prefix + ".action.SHOW_PROFILE");
        intent.putExtra(prefix + "id", uid);
        return intent;
    }

}
