package org.codarama.haxsync.services;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.util.Log;

import org.apache.commons.lang3.StringUtils;
import org.codarama.haxsync.SyncPreferences;
import org.codarama.haxsync.contacts.ContactsService;
import org.codarama.haxsync.model.Friend;
import org.codarama.haxsync.utilities.ContactUtil;
import org.codarama.haxsync.utilities.DeviceUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This implementation of the {@link AbstractThreadedSyncAdapter} takes care of syncing the
 * contact list.
 */
public class ContactsSyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String TAG = "ContactsSyncAdapter";

    private Context context;

    /**
     * Set up the sync adapter
     */
    public ContactsSyncAdapter(Context context, boolean autoInitialize) {
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
    public ContactsSyncAdapter(
            Context context,
            boolean autoInitialize,
            boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        this.context = context;
    }

    @Override
    public void onPerformSync(
            final Account account,
            final Bundle extras,
            final String authority,
            final ContentProviderClient provider,
            final SyncResult syncResult) {
        Log.i(TAG, "Initiating facebook contacts sync");

        ContentResolver resolver = context.getContentResolver();
        ContactsService service = new ContactsService(resolver);

        Map<String, Long> localContacts = service.getLocalContacts(account);
        Map<String, Long> names = service.loadPhoneContacts();
        Map<String, Long> uids = service.fetchFriends();

        SyncPreferences prefs = new SyncPreferences(context);
        boolean wifiOnly = prefs.getWiFiOnly();
        boolean chargingOnly = prefs.getChargingOnly();

        // step 1. check prerequisites - WiFi, battery, etc.
        if (wifiOnly && !DeviceUtil.isWifi(context)) {
            prefs.setMissedCalendarSync(true);
            return;
        }

        if (chargingOnly && !DeviceUtil.isCharging(context)) {
            prefs.setMissedCalendarSync(true);
            return;
        }

        boolean phoneOnly = prefs.syncPhoneContectsOnly();
        boolean ignoreMiddleaNames = prefs.shouldIgnoreMiddleNames();
        int fuzziness = prefs.fuzzinessLevel();
        Set<String> addFriends = prefs.getAddFriends();

        List<Friend> friends = null; // TODO extract using pure magic

        for (Friend friend : friends) {
            String uid = friend.getUserName();
            String friendName = friend.getName(ignoreMiddleaNames);

            if (friendName == null || uid == null) {
                Log.w(TAG, "Skipping friend due to lack of name of id");
                continue;
            }

            String match = matches(names.keySet(), friendName, fuzziness);

            if (!(phoneOnly && (match == null) && !uids.containsKey(uid)) || addFriends.contains(friendName)) {
                // STEP 1. Add contact - if the contact is not part of the HTCData records and does not match any
                // of the fuzziness names we add them to the list of contacts with the intention to make them available
                // for manual merge (I guess)
                if (localContacts.get(uid) == null) {
                    //String name = friend.getString("name");
                    //Log.i(TAG, name + " already on phone: " + Boolean.toString(names.contains(name)));

                    addContact(account, friendName, uid);

                    Uri rawContactUr = ContactsContract.RawContacts.CONTENT_URI.buildUpon()
                            .appendQueryParameter(ContactsContract.RawContacts.ACCOUNT_NAME, account.name)
                            .appendQueryParameter(ContactsContract.RawContacts.ACCOUNT_TYPE, account.type)
                            .appendQueryParameter(ContactsContract.RawContacts.Data.DATA1, uid)
                            .build();
                    try (Cursor c = resolver.query(rawContactUr, new String[]{BaseColumns._ID}, null, null, null)) {
                        c.moveToLast();
                        Long entry = c.getLong(c.getColumnIndex(BaseColumns._ID));
                        localContacts.put(uid, entry);
                        if (uids.containsKey(uid)) {
                            ContactUtil.merge(context, uids.get(uid), entry);
                        } else if (names.containsKey(match)) {
                            ContactUtil.merge(context, names.get(match), entry);
                        }
                    }
                }
            }
        }
    }

    private void addContact(Account account, String name, String username) {
        ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();

        ContentProviderOperation.Builder builder = ContentProviderOperation
                .newInsert(ContactsContract.RawContacts.CONTENT_URI);
        builder.withValue(ContactsContract.RawContacts.ACCOUNT_NAME, account.name);
        builder.withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, account.type);
        builder.withValue(ContactsContract.RawContacts.SYNC1, username);
        operationList.add(builder.build());

        builder = ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI);
        builder.withValueBackReference(
                ContactsContract.CommonDataKinds.StructuredName.RAW_CONTACT_ID,
                0);
        builder.withValue(
                ContactsContract.Data.MIMETYPE,
                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
        builder.withValue(
                ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                name);
        operationList.add(builder.build());

        builder = ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI);
        builder.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0);
        builder.withValue(ContactsContract.Data.MIMETYPE,
                "vnd.android.cursor.item/vnd.org.codarama.haxsync.profile");
        builder.withValue(ContactsContract.Data.DATA1, username);
        builder.withValue(ContactsContract.Data.DATA2, "Facebook Profile");
        builder.withValue(ContactsContract.Data.DATA3, "View profile");
        operationList.add(builder.build());

        try {
            ContentResolver resolver = context.getContentResolver();
            resolver.applyBatch(ContactsContract.AUTHORITY,                    operationList);
        } catch (Exception e) {
            Log.e("Error", e.getLocalizedMessage());
        }
    }

    private String matches(Set<String> phoneContacts, String fbContact, int maxdistance) {
        if (maxdistance == 0) {
            if (phoneContacts.contains(fbContact)) {
                return fbContact;
            }
            return null;
        }
        int bestDistance = maxdistance;
        String bestMatch = null;
        for (String contact : phoneContacts) {
            int distance = StringUtils.getLevenshteinDistance(contact != null ? contact.toLowerCase() : "", fbContact != null ? fbContact.toLowerCase() : "");
            if (distance <= bestDistance) {
                //Log.i("FOUND MATCH", "Phone Contact: " + contact +" FB Contact: " + fbContact +" distance: " + distance + "max distance: " +maxdistance);
                bestMatch = contact;
                bestDistance = distance;
            }
        }
        return bestMatch;
    }
}
