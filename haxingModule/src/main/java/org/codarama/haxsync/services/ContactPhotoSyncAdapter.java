package org.codarama.haxsync.services;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;

import org.codarama.haxsync.SyncPreferences;
import org.codarama.haxsync.contacts.storage.HtcDataManager;
import org.codarama.haxsync.provider.facebook.FacebookGraphFriend;
import org.codarama.haxsync.utilities.BitmapUtil;
import org.codarama.haxsync.utilities.ContactUtil;
import org.codarama.haxsync.utilities.DeviceUtil;
import org.codarama.haxsync.utilities.RootUtil;
import org.codarama.haxsync.utilities.WebUtil;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * This implementation of the {@link AbstractThreadedSyncAdapter} takes care of syncing the
 * contact photographs.
 */
public class ContactPhotoSyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String TAG = "ContactPhotoSyncAdapter";

    private Context context;

    /**
     * Set up the sync adapter
     */
    public ContactPhotoSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        /*
         * If your app uses a content resolver, get an instance of it
         * from the incoming Context
         */
        this.context = context;
    }

    /**
     * Set up the sync adapter. This form of the
     * constructor maintains compatibility with Android 3.0
     * and later platform versions
     */
    public ContactPhotoSyncAdapter(
            Context context,
            boolean autoInitialize,
            boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        this.context = context;
    }

    @Override
    public void onPerformSync(
            Account account,
            Bundle extras,
            String authority,
            ContentProviderClient provider,
            SyncResult syncResult) {

        // step 1. configure sync options
        SyncPreferences prefs = new SyncPreferences(context);
        boolean force = prefs.getForceSync();
        boolean root = prefs.getRootEnabled();
        boolean google = prefs.shouldUpdateGooglePhotos();
        boolean primary = prefs.shouldBePrimaryImage();

        // step 2. extract list of contacts to sync
        ContentResolver resolver = context.getContentResolver();

        HtcDataManager manager = new HtcDataManager(resolver);
        Map<String, Long> uids = manager.fetchFriends();

        // step 3. perform sync per contact
        for (String facebookId : uids.keySet()) {
            try {
                FacebookGraphFriend friend = null; // FIXME contact facebook for their precious information
                updateContactPhoto(uids.get(facebookId), friend.getPicTimestamp(), friend.getPicURL(), force, root, google, primary);

                ++syncResult.stats.numUpdates;
            } catch (Exception e) {
                // FIXME should probably think of a better exception handling here
                ++syncResult.stats.numIoExceptions;
            }
        }

    }

    private void updateContactPhoto(long rawContactId, long timestamp, String imgUrl, boolean force, boolean root, boolean google, boolean primary) {
        ContentResolver resolver = context.getContentResolver();

        if (imgUrl == null) {
            Log.w(TAG, "Update contact photo was initiated with empty photo");
            return;
        }

        if (!force && isSamePhoto(resolver, rawContactId, imgUrl)) {
            // only update photos that are new, unless force is selected, then update them all
            force = true;
        }

        if (force) {
            Log.i(TAG, "Getting new image at " + imgUrl);

            byte[] photo = WebUtil.download(imgUrl);
            byte[] origPhoto = photo;

            ContactUtil.Photo photoi = new ContactUtil.Photo();
            photoi.data = photo;
            photoi.timestamp = timestamp;
            photoi.url = imgUrl;

            ContactUtil.updateContactPhoto(resolver, rawContactId, photoi, primary);

            if (root) {
                processImage(resolver, rawContactId, photo, origPhoto);
            }

            if (google) {
                Log.i(TAG, "Performing Google photo push");
                for (long raw : ContactUtil.getRawContacts(resolver, rawContactId, "com.google")) {
                    ContactUtil.updateContactPhoto(resolver, raw, photoi, false);
                }
            }
        }
    }

    private void processImage(ContentResolver resolver, long rawContactId, byte[] photo, byte[] origPhoto) {

        // I am going to out of limb here and speculate that this advanced piece of code
        // attempts to modify the image by croping it and detecting the face on the photo

        // uses root privileges which I do not like

        // should be considered worth restoring to functionality only if we find a way to
        // avoid using root shell

        File cacheDir = context.getCacheDir();
        boolean faceDetect = true;
        int rootsize = 512;

        String where = ContactsContract.Data.RAW_CONTACT_ID + " = '" + rawContactId
                + "' AND " + ContactsContract.Data.MIMETYPE + " = '" + ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE + "'";

        try (Cursor c1 = resolver.query(ContactsContract.Data.CONTENT_URI, new String[]{ContactsContract.CommonDataKinds.Photo.PHOTO_FILE_ID}, where, null, null)) {
            if (c1.getCount() > 0) {
                c1.moveToLast();
                String photoID = c1.getString(c1.getColumnIndex(ContactsContract.CommonDataKinds.Photo.PHOTO_FILE_ID));

                if (photoID != null) {
                    photo = BitmapUtil.resize(origPhoto, rootsize, faceDetect);
                    String picpath = DeviceUtil.saveBytes(photo, cacheDir);
                    try {
                        String newpath = RootUtil.movePic(picpath, photoID);
                        RootUtil.changeOwner(newpath);
                    } catch (Exception e) {
                        Log.e(TAG, "Issue while doing something I am not sure what", e);
                    }
                }
            }
        }
    }

    private boolean isSamePhoto(ContentResolver resolver, long rawContactId, String newUrl) {
        String oldUrl = "";
        String[] columns = new String[]{ContactsContract.Data.SYNC3};
        String where = ContactsContract.Data.RAW_CONTACT_ID + " = '" + rawContactId
                + "' AND " + ContactsContract.Data.MIMETYPE + " = '" + ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE + "'";

        try (Cursor c1 = resolver.query(ContactsContract.Data.CONTENT_URI, columns, where, null, null)) {
            if (c1.getCount() > 0) {
                c1.moveToLast();

                if (!c1.isNull(c1.getColumnIndex(ContactsContract.Data.SYNC3))) {
                    oldUrl = c1.getString(c1.getColumnIndex(ContactsContract.Data.SYNC3));
                }
            }
        }

        return !oldUrl.equals(newUrl);
    }
}
