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

package org.codarama.haxsync.services;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.StreamItemPhotos;
import android.provider.ContactsContract.StreamItems;
import android.util.Log;

import org.codarama.haxsync.R;
import org.codarama.haxsync.provider.facebook.FacebookStatus;
import org.codarama.haxsync.provider.facebook.Status;
import org.codarama.haxsync.utilities.DeviceUtil;
import org.codarama.haxsync.utilities.FacebookUtil;
import org.codarama.haxsync.utilities.WebUtil;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service to handle view notifications. This allows the sample sync adapter to update the
 * information when the contact is being looked at
 */
@SuppressLint("NewApi")
public class NotifierService extends IntentService {
    private static final String TAG = "NotifierService";
    private static ContentResolver mContentResolver;

    public NotifierService() {
        super(TAG);
    }

    private long addContactStreamItem(long rawContactId, String uid, FacebookStatus status, Account account) {

        //get timestamp of latest saved streamItem
        long oldTimestamp = -2;
        try (Cursor c = mContentResolver.query(Uri.withAppendedPath(ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContactId),
                        RawContacts.StreamItems.CONTENT_DIRECTORY),
                new String[]{StreamItems.TIMESTAMP}, null, null, StreamItems.TIMESTAMP + " DESC")) {
            if (c.getCount() > 0) {
                c.moveToFirst();
                oldTimestamp = c.getLong(c.getColumnIndex("timestamp"));
            }
        }

        long timestamp = status.getTimestamp();
        //only add item if newer then latest saved one
        if (oldTimestamp >= timestamp) {
            return -2;
        }

        String message = status.getMessage();

        String picLink = findPicLink(message);
        message = message.replace(picLink, "");

        Youtube yt = findYoutube(message);
        if (yt != null)
            message = message.replace(yt.link, "");


        ContentValues values = new ContentValues();
        values.put(StreamItems.RAW_CONTACT_ID, rawContactId);
        values.put(StreamItems.RES_PACKAGE, "org.codarama.haxsync");
        values.put(StreamItems.RES_LABEL, R.string.app_name);
        values.put(StreamItems.TEXT, message);
        values.put(StreamItems.TIMESTAMP, timestamp);

        String commentString = status.getCommentHtml();
        if (!commentString.equals("")) {
            values.put(StreamItems.COMMENTS, commentString);
        }

        values.put(StreamItems.SYNC1, status.getID());
        values.put(StreamItems.SYNC2, uid);
        values.put(StreamItems.SYNC3, status.getPermalink());
        values.put(StreamItems.ACCOUNT_NAME, account.name);
        values.put(StreamItems.ACCOUNT_TYPE, account.type);
        Uri streamItemUri = mContentResolver.insert(StreamItems.CONTENT_URI, values);
        long streamItemId = ContentUris.parseId(streamItemUri);

        if (status.getType() == 247)
            addFBPhoto(streamItemId, account, status.getAppData());
        if (yt != null)
            addYoutubeThumb(streamItemId, account, yt);
        if (!picLink.equals(""))
            addPicThumb(streamItemId, account, picLink);

        return streamItemId;
    }

    private void addStreamPhoto(long itemID, byte[] photo, Account account, String type, String sync2) {
        ContentValues values = new ContentValues();
        values.put(StreamItemPhotos.STREAM_ITEM_ID, itemID);
        values.put(StreamItemPhotos.SORT_INDEX, 1);
        values.put(StreamItemPhotos.PHOTO, photo);
        values.put(StreamItems.ACCOUNT_NAME, account.name);
        values.put(StreamItems.ACCOUNT_TYPE, account.type);
        values.put(StreamItemPhotos.SYNC1, type);
        values.put(StreamItemPhotos.SYNC2, sync2);
        mContentResolver.insert(StreamItems.CONTENT_PHOTO_URI, values);
    }

    private String findPicLink(String message) {
        String picLink = "";
        Pattern picPattern = Pattern.compile("https?:\\/\\/[a-z0-9\\-\\.]+\\.[a-z]{2,3}/.+\\.(jpg|png|gif|bmp)", Pattern.CASE_INSENSITIVE);
        Matcher picMatcher = picPattern.matcher(message);

        while (picMatcher.find()) {
            picLink = picMatcher.group();
            //Log.i("picLink", picLink);
        }
        return picLink;
    }

    private Youtube findYoutube(String message) {
        Youtube yt = null;
        Pattern ytPattern = Pattern.compile("https?:\\/\\/(?:[0-9A-Z-]+\\.)?(?:youtu\\.be\\/|youtube\\.com\\S*[^\\w\\-\\s])([\\w\\-]{11})(?=[^\\w\\-]|$)(?![?=&+%\\w]*(?:['\"][^<>]*>|<\\/a>))[?=&+%\\w]*", Pattern.CASE_INSENSITIVE);
        Matcher ytMatcher = ytPattern.matcher(message);
        while (ytMatcher.find()) {
            yt = new Youtube();
            yt.link = ytMatcher.group();
            yt.ID = ytMatcher.group(1);
        }
        return yt;
    }

    private void addFBPhoto(long streamID, Account account, JSONObject appData) {
        long picID = 0;
        try {
            picID = appData.getJSONArray("photo_ids").getLong(0);
        } catch (Exception e) {
            Log.e("ERROR", e.toString());
        }
        if (picID != 0) {
            JSONObject picinfo = FacebookUtil.getPicInfo(picID);
            if (picinfo != null) {
                String src = null;
                try {
                    src = picinfo.getString("src_big");
                } catch (Exception e) {
                    Log.e("ERROR", e.toString());
                }
                if (src != null) {

                    byte[] pic = WebUtil.download(src);
                    if (pic != null) {
                        addStreamPhoto(streamID, pic, account, "fbphoto", String.valueOf(picID));
                    }
                }
            }
        }

    }

    private void addYoutubeThumb(long streamID, Account account, Youtube yt) {
        byte[] pic = WebUtil.download("http://img.youtube.com/vi/" + yt.ID + "/0.jpg");
        if (pic != null) {
            addStreamPhoto(streamID, pic, account, "youtube", yt.link);
        }
    }

    private void addPicThumb(long streamID, Account account, String picLink) {
        byte[] pic = WebUtil.download(picLink);
        if (pic != null) {
            addStreamPhoto(streamID, pic, account, "link", picLink);
        }
    }

    @SuppressWarnings("unused")
    @Override
    protected void onHandleIntent(Intent intent) {
        if (!FacebookUtil.RESPECT_FACEBOOK_POLICY && DeviceUtil.isOnline(this)) {
            Log.i(TAG, "is online");
            SharedPreferences prefs = this.getSharedPreferences(this.getPackageName() + "_preferences", MODE_MULTI_PROCESS);
            boolean sync = prefs.getBoolean("sync_status", true);
            boolean syncNew = prefs.getBoolean("status_new", true);
            boolean timelineAll = prefs.getBoolean("timeline_all", false);


            if (Build.VERSION.SDK_INT >= 15 && sync && syncNew) {
                mContentResolver = this.getContentResolver();

                AccountManager am = AccountManager.get(this);
                Account account = am.getAccountsByType("org.codarama.haxsync.account")[0];
                if (FacebookUtil.authorize(this, account)) {

                    String[] projection = new String[]{RawContacts._ID, RawContacts.SYNC1};
                    try (Cursor c = mContentResolver.query(intent.getData(), projection, null, null, null)) {
                        c.moveToFirst();
                        long id = c.getLong(c.getColumnIndex(RawContacts._ID));
                        String uid = c.getString(c.getColumnIndex(RawContacts.SYNC1));

                        ArrayList<Status> statuses = FacebookUtil.getStatuses(uid, timelineAll);

                        if (statuses != null) {
                            Log.i(TAG, statuses.toString());
                            for (Status status : statuses) {
                                FacebookStatus fbstatus = (FacebookStatus) status;
                                if (timelineAll || fbstatus.getActorID().equals(uid)) {
                                    if (fbstatus != null && !fbstatus.getMessage().equals(""))
                                        addContactStreamItem(id, uid, fbstatus, account);
                                }
                            }
                        }
                    }
                }
            }

        }

    }

    public static class Youtube {
        public String ID;
        public String link;
    }

}

    
