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

package org.codarama.haxsync.provider.facebook;

import android.util.Log;

import org.codarama.haxsync.R;
import org.codarama.haxsync.utilities.FacebookUtil;
import org.json.JSONException;
import org.json.JSONObject;

public class FacebookStatus implements Status {
    private JSONObject json;

    public FacebookStatus(JSONObject json) {
        this.json = json;
    }

    @Override
    public String getMessage() {
        String message = null;
        try {
            message = json.getString("message");
        } catch (JSONException e) {
            Log.e("Error", e.getLocalizedMessage());
        }
        return message;
    }

    public String getCommentHtml() {
        String commentString = "";
        int comments = getCommentCount();
        int likes = getLikeCount();
        if (!getSourceID().equals(getActorID())) {
            commentString += "<b>" + FacebookUtil.getFriendName(getActorID()) + "</b>&nbsp;";
        }
        if (comments > 0) {
            commentString += "<img src=\"res://org.codarama.haxsync/" + R.drawable.comment + "\"/> " + comments;
        }
        if (likes > 0) {
            if (comments > 0) {
                commentString += "&nbsp;";
            }
            commentString += "<img src=\"res://org.codarama.haxsync/" + R.drawable.like + "\"/> " + likes;
        }
        return commentString;
    }

    @Override
    public long getTimestamp() {
        long time = 0;
        try {
            time = json.getInt("created_time") * 1000L;
        } catch (JSONException e) {
            Log.e("Error", e.getLocalizedMessage());
        }
        return time;
    }

    @Override
    public String getPermalink() {
        String link = null;
        try {
            link = json.getString("permalink");
        } catch (JSONException e) {
            Log.e("Error", e.getLocalizedMessage());
        }
        return link;
    }

    @Override
    public String getID() {
        String id = null;
        try {
            id = json.getString("post_id");
        } catch (JSONException e) {
            Log.e("Error", e.getLocalizedMessage());
        }
        return id;
    }

    private String getSourceID() {
        String id = "";
        try {
            id = json.getString("source_id");
        } catch (JSONException e) {
            Log.e("Error", e.getLocalizedMessage());
        }
        return id;
    }

    public int getType() {
        int type = 0;
        try {
            type = json.getInt("type");
        } catch (JSONException e) {
            Log.e("Error", e.getLocalizedMessage());
        }
        return type;
    }

    public int getCommentCount() {
        int comments = 0;
        try {
            comments = json.getJSONObject("comments").getInt("count");
        } catch (JSONException e) {
            Log.e("Error", e.getLocalizedMessage());
        }
        return comments;
    }

    public JSONObject getAppData() {
        JSONObject appData = null;
        try {
            appData = json.getJSONObject("app_data");
        } catch (JSONException e) {
            Log.e("Error", e.getLocalizedMessage());
        }
        return appData;
    }

    public String getActorID() {
        String id = null;
        try {
            id = json.getString("actor_id");
        } catch (JSONException e) {
            Log.e("Error", e.getLocalizedMessage());
        }
        return id;
    }

    public int getLikeCount() {
        int likes = 0;
        try {
            likes = json.getJSONObject("likes").getInt("count");
        } catch (JSONException e) {
            Log.e("Error", e.getLocalizedMessage());
        }
        return likes;
    }

    @Override
    public String toString() {
        String msg = getMessage();
        if (msg == null) {
            return "empty status";
        }
        return msg;
    }

}
