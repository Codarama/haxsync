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

import org.codarama.haxsync.entities.LegacyFriend;
import org.codarama.haxsync.utilities.FacebookUtil;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Deprecated
public class FacebookFQLFriend implements LegacyFriend {
    private JSONObject json;
    private List<String> defaultURLs = Arrays.asList("https://fbcdn-profile-a.akamaihd.net/static-ak/rsrc.php/v2/yL/r/HsTZSDw4avx.gif", "https://fbcdn-profile-a.akamaihd.net/static-ak/rsrc.php/v2/yp/r/yDnr5YfbJCH.gif");

    public FacebookFQLFriend(JSONObject json) {
        this.json = json;
    }

    @Override
    public String toString() {
        return "Name: " + this.getName(true) + ", FB-ID: " + this.getUserName();
    }

    @Override
    public String getName(boolean ignoreMiddleNames) {
        String name = null;
        try {
            name = json.getString("name");
        } catch (JSONException e) {
            Log.e("Error", e.getLocalizedMessage());
        }
        return name;
    }

    //returns lame @facebook.com email because the API doesn't allow anything else
    public String getEmail() {
        try {
            return json.getString("username") + "@facebook.com";
        } catch (JSONException e) {
            return null;
        }
    }

    @Override
    public String getUserName() {
        String uid = null;
        try {
            uid = json.getString("uid");
        } catch (JSONException e) {
            Log.e("Error", e.getLocalizedMessage());
        }
        return uid;
    }

    @Override
    public String getPicURL() {
        String url = null;
        try {
            url = defaultURLs.contains(json.getString("pic_big")) ? null : json.getString("pic_big");
        } catch (JSONException e) {
            Log.e("Error", e.getLocalizedMessage());
        }
        return url;
    }

    @Override
    public long getPicTimestamp() {
        long timestamp = 0;
        try {
            timestamp = json.getLong("pic_modified");
        } catch (JSONException e) {
            Log.e("Error", e.getLocalizedMessage());
        }

        return timestamp;
    }

    public String getCountry() {
        String country = null;
        try {
            country = json.getJSONObject("current_location").getString("country");
        } catch (JSONException e) {
            Log.e("Error", e.getLocalizedMessage());
        }
        return country;
    }

    public String getState() {
        String state = null;
        try {
            state = json.getJSONObject("current_location").getString("state");
        } catch (JSONException e) {
            Log.e("Error", e.getLocalizedMessage());
        }
        return state;
    }

    public String getCity() {
        String city = null;
        try {
            city = json.getJSONObject("current_location").getString("city");
        } catch (JSONException e) {
            Log.e("Error", e.getLocalizedMessage());
        }
        return city;
    }

    public String getBirthday() {
        try {
            String birthday = json.getString("birthday_date");
            String[] birthdayArray = birthday.split("/");
            if (birthdayArray.length == 3) {
                return birthdayArray[2] + "-" + birthdayArray[0] + "-" + birthdayArray[1];
            } else if (birthdayArray.length == 2) {
                return "--" + birthdayArray[0] + "-" + birthdayArray[1];
            }

        } catch (JSONException e) {
            Log.e("Error", e.getLocalizedMessage());
        }
        return null;
    }

    @Override
    public ArrayList<Status> getStatuses() {
        JSONObject status = null;
        ArrayList<Status> statuses = new ArrayList<Status>();
        try {
            status = json.getJSONObject("status");
        } catch (JSONException e) {
            Log.e("Error", e.getLocalizedMessage());
        }
        if (status != null) {
            statuses.add(new FacebookStatus(status));
            return statuses;
        } else {
            String uid = this.getUserName();
            if (uid != null) {
                return FacebookUtil.getStatuses(uid, false);
            }

        }
        return statuses;
    }

}
