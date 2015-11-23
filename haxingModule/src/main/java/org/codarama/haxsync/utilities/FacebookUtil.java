package org.codarama.haxsync.utilities;

import android.accounts.Account;
import android.app.Activity;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.CalendarContract.Attendees;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;

import org.codarama.haxsync.R;
import org.codarama.haxsync.provider.facebook.Event;
import org.codarama.haxsync.provider.facebook.EventAttendee;
import org.codarama.haxsync.provider.facebook.FacebookFQLFriend;
import org.codarama.haxsync.provider.facebook.FacebookGraphFriend;
import org.codarama.haxsync.provider.facebook.FacebookStatus;
import org.codarama.haxsync.provider.facebook.Status;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.TimeZone;

public class FacebookUtil extends Application {
    public static final int PERMISSION_LEVEL = 1;
    public static final boolean RESPECT_FACEBOOK_POLICY = true;
    public static final String[] PERMISSIONS = {"user_events"/*calendar*/, "user_friends" /*contacts*/};
    public static Activity activity;

    public static boolean isExtendingToken = false;
    private static final String TAG = "FacebookUtil";

    // Used when the real name could not be fetched
    private static final String DEFAULT_FRIEND_NAME = "John Doe";
    private static final int FQL_LIMIT = 20;
    private static HashMap<String, Long> birthdays = new HashMap<String, Long>();

    public static boolean authorize(Context context, Account account) {

        // make sure the SDK is initialized
        FacebookSdk.sdkInitialize(context);

        if (isExtendingToken || !DeviceUtil.isOnline(context)) {
            return true; // false
        }

        // TODO Evaluate if removing the workaround code would have any adverse effects on the code
//        DeviceUtil.showJellyBeanNotification(context);

        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken == null || accessToken.isExpired()) {
            notifyToken(context);
            return true; // false
        }

        return true;
    }

    private static void notifyToken(Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setClassName("org.codarama.haxsync", "org.codarama.haxsync.activities.AuthorizationActivity");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        isExtendingToken = true;

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Resources res = context.getResources();
        Notification.Builder builder = new Notification.Builder(context);

        builder.setContentIntent(contentIntent)
                .setSmallIcon(android.R.drawable.stat_notify_error)
                .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.icon))
                .setTicker(res.getString(R.string.token_notification_ticker))
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setContentTitle(res.getString(R.string.token_notification_title))
                .setContentText(res.getString(R.string.token_notification_description));
        Notification n = builder.getNotification();

        nm.notify(0, n);
    }

    public static void refreshPermissions(Context c) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);

        int permissionLevel = prefs.getInt("permission_level", 0);
        Log.i("Permission Level", String.valueOf(permissionLevel));

        if (permissionLevel < FacebookUtil.PERMISSION_LEVEL) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setClassName("org.codarama.haxsync", "org.codarama.haxsync.activities.AuthorizationActivity");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            c.startActivity(intent);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("permission_level", FacebookUtil.PERMISSION_LEVEL);
            editor.commit();
        }
    }

    // TODO break this up, use async requests
    public static List<FacebookGraphFriend> getFriends(int maxsize, boolean addMeToFriends) {
        final List<FacebookGraphFriend> friends = new ArrayList<FacebookGraphFriend>();
        final List<String> ids = new ArrayList<String>();
        AccessToken accessToken = AccessToken.getCurrentAccessToken();

        // Request personal details (if set up)
        GraphRequest request = GraphRequest.newMeRequest(
                accessToken,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(
                            JSONObject object,
                            GraphResponse response) {
                        Log.i(TAG, "id response " + response);
                        try {
                            Log.i(TAG, "add Me to friends:" + object.getString("id"));
                            ids.add(object.getString("id"));
                        } catch (JSONException e) {
                            Log.e(TAG, "Failed to parse result from Facebook", e);
                        }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id");
        request.setParameters(parameters);
        if (addMeToFriends) {
            request.executeAndWait();
        }

        // Request friend IDs
        request = GraphRequest.newMyFriendsRequest(
                accessToken,
                new GraphRequest.GraphJSONArrayCallback() {
                    @Override
                    public void onCompleted(JSONArray jsonArray, GraphResponse graphResponse) {
                        Log.i(TAG, "Facebook friends received : " + jsonArray);
                        try {
                            for (int i = 0; i < jsonArray.length(); i++) {
                                ids.add(jsonArray.getJSONObject(i).getString("id"));
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "Failed to parse result from Facebook", e);
                        }
                    }
                });
        parameters = new Bundle();
        parameters.putString("fields", "id");
        request.setParameters(parameters);
        request.executeAndWait();

        // Request friends details
        for (String id : ids) {
            request = GraphRequest.newGraphPathRequest(
                    accessToken,
                    id,
                    new GraphRequest.Callback() {
                        @Override
                        public void onCompleted(GraphResponse graphResponse) {
                            Log.i(TAG, "Facebook friend details received : ");
                            Log.i(TAG, graphResponse.getJSONObject().toString());
                            JSONObject response = graphResponse.getJSONObject();
                            FacebookGraphFriend friend = new FacebookGraphFriend(response);
                            friends.add(friend);
                        }
                    });
            parameters = new Bundle();
            parameters.putString("fields", "picture.width(" + maxsize + ").height(" + maxsize + "), name, first_name, last_name, username, birthday, location, updated_time");
            request.setParameters(parameters);
            request.executeAndWait();
        }

        return friends;

    }

    @Deprecated
    // TODO see if this needs to be removed, since it is unused (check ContactsSyncAdapterService)
    public static ArrayList<FacebookFQLFriend> getFriendInfo(boolean status) {
        //me/friends?fields=picture.height(720).width(720),name,username,birthday,location

        //old fql way
        JSONArray friendarray = new JSONArray();
        JSONArray timestamps = new JSONArray();
        int fetched = FQL_LIMIT;
        int callNo = 0;
        try {
            while (fetched == FQL_LIMIT) {
                JSONObject jsonFQL = new JSONObject();
                String query1;
                if (status) {
                    query1 = "select name,  username, status, uid, profile_update_time, birthday_date, pic_big, current_location from user where uid in (select uid2 from friend where uid1=me()) order by name limit " + FQL_LIMIT + " offset " + callNo * FQL_LIMIT;
                } else {
                    query1 = "select name,  username, uid, profile_update_time, birthday_date, pic_big, current_location from user where uid in (select uid2 from friend where uid1=me()) order by name limit " + FQL_LIMIT + " offset " + callNo * FQL_LIMIT;
                }
                //Log.i("query1", query1);
                jsonFQL.put("query1", query1);
                jsonFQL.put("query2", "SELECT modified, src_big, owner FROM photo WHERE pid IN (SELECT cover_pid FROM album WHERE owner IN (SELECT uid FROM #query1) AND type = 'profile')");
                Bundle params = new Bundle();
                params.putString("method", "fql.multiquery");
                params.putString("queries", jsonFQL.toString());
                String friendstring = ""; //facebook.request(params);
                JSONArray friendInfo = new JSONArray(friendstring);
                JSONArray newFriends = friendInfo.getJSONObject(0).getJSONArray("fql_result_set");
                Log.i("friendstring", friendstring);
                fetched = newFriends.length();
                Log.i("no of friends fetched", String.valueOf(fetched));
                friendarray = concatArray(friendarray, newFriends);
                timestamps = concatArray(timestamps, friendInfo.getJSONObject(1).getJSONArray("fql_result_set"));
                callNo++;
            }
        } catch (Exception e) {
            Log.e(TAG, "ERROR:", e);
        }
        //Log.i("friendarray", friendarray.toString());
        Log.i(TAG, "total number of friends fetched" + String.valueOf(friendarray.length()));
        Log.i(TAG, "timestamps" + timestamps.toString());
        return mergeArrays(friendarray, timestamps);

    }

    public static PicInfo getProfilePicInfo(String uid) {
        String query = "SELECT modified, src_big, owner FROM photo WHERE pid IN (SELECT cover_pid FROM album WHERE owner = " + uid + " AND type = 'profile')";

        PicInfo pic = new PicInfo();
        try {
            JSONArray hires = queryJSONArray(query);
            if (hires.length() >= 1) {
                pic.url = hires.getJSONObject(0).getString("src_big");
                pic.timestamp = hires.getJSONObject(0).getLong("modified");
            } else {
                query = "select pic_big from user where uid =" + uid;
                pic.url = queryJSONArray(query).getJSONObject(0).getString("pic_big");
            }
        } catch (JSONException e) {
            Log.e(TAG, "Failed to get profile picture because " + e.getLocalizedMessage(), e);
        }
        return pic;
    }

    @Deprecated
    private static JSONArray queryJSONArray(String query) {
        final Stack<JSONArray> response = new Stack<JSONArray>();
        final AccessToken accessToken = AccessToken.getCurrentAccessToken();

        Log.d(TAG, "Executing FQL request :");
        Log.d(TAG, query);

        GraphRequest request = GraphRequest.newGraphPathRequest(
                accessToken,
                "/fql",
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse graphResponse) {
                        Log.i(TAG, "Received Facebook FQL response : ");

                        if (graphResponse.getError() != null) {
                            Log.e(TAG, "Unfortunately Facebook says that " + graphResponse.getError().getErrorMessage(), graphResponse.getError().getException());
                        }

                        if (graphResponse.getJSONArray() != null) {
                            Log.i(TAG, graphResponse.getJSONArray().toString());
                            response.push(graphResponse.getJSONArray());
                        } else {
                            Log.w(TAG, "Empty result received from Facebook");
                            response.push(new JSONArray());
                        }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("q", query);
        request.setParameters(parameters);
        request.executeAndWait();

        return response.pop();
    }

    private static JSONObject queryJSONObjectNew(String edge, String fields) {
        final Stack<JSONObject> response = new Stack<JSONObject>();
        final AccessToken accessToken = AccessToken.getCurrentAccessToken();

        Log.d(TAG, "Executing Graph API request :");
        Log.d(TAG, "Edge :" + edge);
        Log.d(TAG, "Fields :" + fields);

        GraphRequest request = GraphRequest.newGraphPathRequest(
                accessToken,
                "/" + edge,
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse graphResponse) {
                        Log.i(TAG, "Received Facebook FQL response : ");
                        Log.d(TAG, graphResponse.getRawResponse());

                        if (graphResponse.getError() != null) {
                            Log.e(TAG, "Unfortunately Facebook says that " + graphResponse.getError().getErrorMessage(), graphResponse.getError().getException());
                        }

                        if (graphResponse.getJSONObject() != null) {
                            Log.i(TAG, graphResponse.getJSONObject().toString());
                            response.push(graphResponse.getJSONObject());
                        } else {
                            Log.w(TAG, "Empty result received from Facebook");
                            response.push(new JSONObject());
                        }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", fields);
        request.setParameters(parameters);
        request.executeAndWait();

        return response.pop();
    }

    @Deprecated
    private static JSONObject queryJSONObject(String query) {
        final Stack<JSONObject> response = new Stack<JSONObject>();
        final AccessToken accessToken = AccessToken.getCurrentAccessToken();

        Log.d(TAG, "Executing FQL request :");
        Log.d(TAG, query);

        GraphRequest request = GraphRequest.newGraphPathRequest(
                accessToken,
                "/fql",
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse graphResponse) {
                        Log.i(TAG, "Received Facebook FQL response : ");

                        if (graphResponse.getError() != null) {
                            Log.e(TAG, "Unfortunately Facebook says that " + graphResponse.getError().getErrorMessage(), graphResponse.getError().getException());
                        }

                        if (graphResponse.getJSONObject() != null) {
                            Log.i(TAG, graphResponse.getJSONArray().toString());
                            response.push(graphResponse.getJSONObject());
                        } else {
                            Log.w(TAG, "Empty result received from Facebook");
                            response.push(new JSONObject());
                        }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("q", query);
        request.setParameters(parameters);
        request.executeAndWait();

        return response.pop();
    }

    private static JSONArray concatArray(JSONArray old, JSONArray add) throws JSONException {
        for (int i = 0; i < add.length(); i++) {
            old.put(add.get(i));
        }
        return old;
    }

    public static ArrayList<Status> getStatuses(String uid, boolean fullTimeline) {
        JSONArray statusarray = getStatusJSON(uid, fullTimeline);
        ArrayList<Status> statuses = new ArrayList<Status>();
        if (statusarray != null) {
            for (int j = statusarray.length() - 1; j >= 0; j--) {
                JSONObject statusjson = null;
                try {
                    statusjson = statusarray.getJSONObject(j);
                } catch (JSONException e) {
                    Log.e("Error", e.getLocalizedMessage());
                }
                if (statusjson != null) {
                    statuses.add(new FacebookStatus(statusjson));
                }
            }
        }
        return statuses;

    }

    private static JSONArray getStatusJSON(String uid, boolean fullTimeline) {
        String query = "SELECT message, post_id, type, created_time, actor_id, app_data, source_id, likes, comments, permalink FROM stream WHERE source_id=" + uid + "  ORDER BY created_time DESC LIMIT 50";
        if (!fullTimeline) {
            query = "SELECT message, post_id, type, created_time, actor_id, app_data, source_id, likes, comments, permalink FROM stream WHERE source_id=" + uid + " AND actor_id=" + uid + " ORDER BY created_time DESC LIMIT 50";
        }
        JSONArray result = queryJSONArray(query);
        return result;
    }

    public static List<Event> getEvents(String status) {
        ArrayList<Event> events = new ArrayList<Event>();
        try {
            String statusString = "";
            String[] statusArray = status.split("\\|");
            for (int i = 0; i < statusArray.length; i++) {
                statusString += "'" + statusArray[i] + "'";
                if (i != statusArray.length - 1)
                    statusString += ", ";
            }

            String request = "SELECT name, eid, start_time, end_time, location, description FROM event WHERE eid IN ( SELECT eid FROM event_member WHERE uid =  me() AND rsvp_status in (" + statusString + "))";
            JSONArray rawEvents = queryJSONArray(request);

            for (int i = 0; i < rawEvents.length(); i++) {
                events.add(new Event(rawEvents.getJSONObject(i)));
            }
        } catch (JSONException e) {
            Log.e(TAG, "Failed to fetch events", e);
        }
        return events;
    }

    private static void addBirthday(String name, String date) {
        int month = Integer.valueOf(date.split("/")[0]);
        int day = Integer.valueOf(date.split("/")[1]);
        GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.set(Calendar.getInstance().get(Calendar.YEAR), month - 1, day, 0, 0, 0);
        long millis = cal.getTimeInMillis();
        birthdays.put(name, millis);
    }

    public static HashMap<String, Long> getBirthdays() {
        if (birthdays.size() > 0) {
            return birthdays;
        }
        String query = "select name, birthday_date from user where uid in (select uid2 from friend where uid1=me()) order by name";
        JSONArray result = queryJSONArray(query);
        for (int i = 0; i < result.length(); i++) {
            try {
                JSONObject friend = result.getJSONObject(i);
                if (friend.getString("birthday_date") != null) {
                    addBirthday(friend.getString("name"), friend.getString("birthday_date"));
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to fetch birthdays", e);
            }
        }
        return birthdays;
    }

    private static ArrayList<FacebookFQLFriend> mergeArrays(JSONArray friendarray, JSONArray timestamps) {
        ArrayList<FacebookFQLFriend> friends = new ArrayList<FacebookFQLFriend>();
        try {
            for (int i = 0; i < friendarray.length(); i++) {
                JSONObject friend = friendarray.getJSONObject(i);
                String uid = friend.getString("uid");

                //write birthday hashmap
                String birthdayDate = friend.getString("birthday_date");
                if (birthdayDate != null && !birthdayDate.equals("") && !birthdayDate.equals("null")) {
                    addBirthday(friend.getString("name"), friend.getString("birthday_date"));
                }

                long modified = 0;
                for (int j = 0; j < timestamps.length(); j++) {
                    JSONObject timestamp = timestamps.getJSONObject(j);
                    if (timestamp.getString("owner").equals(uid)) {
                        modified = timestamp.getLong("modified");
                        String src = timestamp.getString("src_big");
                        if (!src.equalsIgnoreCase("")) {
                            friend.put("pic_big", src);
                        }
                        break;
                    }
                }
                friend.put("pic_modified", modified);
                friends.add(new FacebookFQLFriend(friend));
            }
        } catch (JSONException e) {
            Log.e(TAG, "Failed to merge friends arrays", e);
        }

        return friends;
    }

    public static JSONObject getPicInfo(long picID) {
        String query = "SELECT owner, src_big, pid, aid FROM photo WHERE object_id=" + picID;
        JSONObject result = null;
        try {
            result = queryJSONArray(query).getJSONObject(0);
        } catch (JSONException e) {
            Log.e(TAG, "Failed to get picture info for " + picID, e);
            return null;
        }
        return result;
    }

    public static JSONObject getProfilePic(String uid) {
        String query = "SELECT modified, src_big FROM photo WHERE pid IN (SELECT cover_pid FROM album WHERE owner=" + uid + " AND type = 'profile')";
        JSONObject result = null;
        try {
            result = queryJSONArray(query).getJSONObject(0);
        } catch (JSONException e) {
            Log.e(TAG, "Failed to fetch profile pic", e);
        }

        return result;
    }

    public static String getSelfID() {
        FacebookGraphFriend selfInfo = getSelfInfo();
        return selfInfo.getUserName();
    }

    public static FacebookGraphFriend getSelfInfo() {
        final Stack<JSONObject> json = new Stack<JSONObject>();
        final AccessToken accessToken = AccessToken.getCurrentAccessToken();

        GraphRequest request = GraphRequest.newMeRequest(
                accessToken,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(
                            JSONObject object,
                            GraphResponse response) {
                        Log.i(TAG, "Received Facebook response");
                        Log.i(TAG, object.toString());
                        json.push(object);
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "picture.width(720).height(720), name, username, birthday, location, updated_time");
        request.setParameters(parameters);
        request.executeAndWait();

        FacebookGraphFriend friend = new FacebookGraphFriend(json.peek());
        return friend;
    }

    public static JSONObject getSelfInfoAsync() {
        final Stack<JSONObject> json = new Stack<JSONObject>();
        final AccessToken accessToken = AccessToken.getCurrentAccessToken();

        GraphRequest request = GraphRequest.newMeRequest(
                accessToken,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(
                            JSONObject object,
                            GraphResponse response) {
                        Log.i(TAG, "Received Facebook response");
                        Log.i(TAG, object.toString());
                        json.push(object);
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "name, picture");
        request.setParameters(parameters);
        request.executeAndWait();

        return json.peek();
    }

    public static String getFacebookName() throws JSONException {
        return getSelfInfoAsync().getString("name");
    }

    public static String getFriendName(String uid) {
        String name = DEFAULT_FRIEND_NAME;
        try {
            String query = "select name from user where uid = " + uid;
            name = queryJSONArray(query).getJSONObject(0).getString("name");
        } catch (JSONException e) {
            Log.e(TAG, "Failed to fetch friend name", e);
        }
        return name;
    }

    public static JSONArray getFriendNames() {
        String query = "select name from user where uid in (select uid2 from friend where uid1=me()) order by name";
        JSONArray friendInfo = queryJSONArray(query);
        return friendInfo;
    }

    public static int convertStatus(String statusString) {
        if (statusString.equals("attending")) {
            return Attendees.ATTENDEE_STATUS_ACCEPTED;
        } else if (statusString.equals("unsure")) {
            return Attendees.ATTENDEE_STATUS_TENTATIVE;
        } else if (statusString.equals("declined")) {
            return Attendees.ATTENDEE_STATUS_DECLINED;
        } else {
            return Attendees.ATTENDEE_STATUS_INVITED;
        }
    }

    public static List<EventAttendee> getEventAttendees(long eid) {
        String query1 = "SELECT uid, rsvp_status FROM event_member WHERE eid = " + eid + " AND uid IN (select uid2 from friend where uid1=me())";
        String query2 = "SELECT name, username, uid FROM user WHERE uid IN (SELECT uid FROM #query1)";

        JSONArray statuses = queryJSONArray(query1);
        JSONArray possibleAttendees = queryJSONArray(query2);

        List<EventAttendee> attendees = mergeAttendees(statuses, possibleAttendees);
        return attendees;
    }

    public static String getSelfAttendance(long eid) {
        try {
            String query = "SELECT rsvp_status FROM event_member WHERE eid = " + eid + " AND uid = me()";
            return queryJSONArray(query).getJSONObject(0).getString("rsvp_status");
        } catch (JSONException e) {
            Log.e(TAG, "Failed to get self attendance", e);
            return "";
        }
    }

    private static List<EventAttendee> mergeAttendees(JSONArray statuses, JSONArray attendees) {
        ArrayList<EventAttendee> attendeeList = new ArrayList<EventAttendee>();
        try {
            for (int i = 0; i < attendees.length(); i++) {
                JSONObject friend = attendees.getJSONObject(i);
                String uid = friend.getString("uid");

                //write birthday hashmap

                long modified = 0;
                for (int j = 0; j < statuses.length(); j++) {
                    JSONObject status = statuses.getJSONObject(j);
                    if (status.getString("uid").equals(uid)) {
                        friend.put("rsvp_status", status.getString("rsvp_status"));
                        break;
                    }
                }
                attendeeList.add(new EventAttendee(friend));
            }
        } catch (JSONException e) {
            Log.e(TAG, "Failed to merge attendees", e);
        }
        return attendeeList;
    }

    public static class PicInfo {
        public String url;
        public long timestamp;
    }
}
