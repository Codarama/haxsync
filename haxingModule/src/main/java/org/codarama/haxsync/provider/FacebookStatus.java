package org.codarama.haxsync.provider;

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
