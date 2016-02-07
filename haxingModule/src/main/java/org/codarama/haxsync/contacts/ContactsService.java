package org.codarama.haxsync.contacts;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.util.Log;

import org.codarama.haxsync.model.ProfilePicture;
import org.codarama.haxsync.utilities.BitmapUtil;
import org.codarama.haxsync.utilities.ContactUtil;
import org.codarama.haxsync.utilities.DeviceUtil;
import org.codarama.haxsync.utilities.RootUtil;
import org.codarama.haxsync.utilities.WebUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Manages HTCData records - storing facebook IDs against Google contacts and reading this information.
 */
public class ContactsService {
    private static final String TAG = "ContactsService";

    private static final String HTC_DATA_ELEMENT = "<HTCData>";
    private static final String HTC_DATA_ELEMENT_NAME = "HTCData";
    private static final String FACEBOOK_PATTERN = "<Facebook>id:(.*)/friendof.*</Facebook>";
    private static final String INVALID_VALUE = "<INVALID_VALUE>";

    private static final String USERNAME_COLUMN = ContactsContract.RawContacts.SYNC1;
    private static final String TIMESTAMP_COLUMN = ContactsContract.RawContacts.SYNC2;

    private ContentResolver resolver;

    public ContactsService(ContentResolver resolver) {
        this.resolver = resolver;
    }

    /**
     * <p>Reads the HTCData tags in the notes of each of the Google contacts to identify the
     * respective Facebook ID. Returns a {@link Map} of Facebook IDs (keys) and Google IDs (values) pairs.</p>
     * <p/>
     *
     * @return a mapping between Facebook ID and Google ID
     */
    public Map<String, Long> fetchFriends() {
        Map<String, Long> contacts = new HashMap<String, Long>();
        String noteWhere = ContactsContract.Data.MIMETYPE + " = ?";
        String[] noteWhereParams = new String[]{ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE};
        String[] columns = new String[]{ContactsContract.Data.RAW_CONTACT_ID, ContactsContract.CommonDataKinds.Note.NOTE};

        try (Cursor cursor = resolver.query(ContactsContract.Data.CONTENT_URI, columns, noteWhere, noteWhereParams, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    try {
                        String note = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Note.NOTE));
                        Long googleId = cursor.getLong(cursor.getColumnIndex(ContactsContract.Data.RAW_CONTACT_ID));
                        String value = parseNote(note);
                        if (!value.equals(INVALID_VALUE)) {
                            contacts.put(value, googleId);
                        }
                    } catch (IllegalStateException | NumberFormatException e) {
                        Log.e(TAG, "Issue while loading HTCData field", e);
                        // originally the code would break here - not sure why, it would make more sense to continue with
                        // the next contact? Still leaving this note here for future consideration
                    }
                } while (cursor.moveToNext());
            }
        }
        return contacts;
    }

    // TODO Add unit test
    String parseNote(String note) {
        if (note != null && note.startsWith(HTC_DATA_ELEMENT)) {
            Pattern pattern = Pattern.compile(FACEBOOK_PATTERN, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(note);

            while (matcher.find()) {
                String uid = matcher.group(1);
                Log.d(TAG, "Loaded HTCData field for UID: " + uid);
                return uid;
            }
        }

        return INVALID_VALUE;
    }

    public void writeHTCData(long rawContactID, String fbID, String friendID) {
        String note = "";

        String noteWhere = ContactsContract.Data.MIMETYPE + " = ? AND " + ContactsContract.Data.RAW_CONTACT_ID + "= ?";
        String[] noteWhereParams = new String[]{ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE, String.valueOf(rawContactID)};
        String[] columns = new String[]{ContactsContract.CommonDataKinds.Note.NOTE};

        try (Cursor cursor = resolver.query(ContactsContract.Data.CONTENT_URI, columns, noteWhere, noteWhereParams, null)) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                note = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Note.NOTE));
            }
        }

        Document doc = null;
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = null;
        try {
            docBuilder = docFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            Log.e(TAG, "Error creating parsing factory", e);
            return;
        }

        if (note.startsWith(HTC_DATA_ELEMENT)) {
            try (StringReader reader = new StringReader(note)) {
                InputSource inputSource = new InputSource(reader);
                doc = docBuilder.parse(inputSource);
            } catch (Exception e) {
                Log.e(TAG, "Error parsing notes record", e);
            }
        }

        if (doc == null) {
            doc = docBuilder.newDocument();
            Node htcdata = doc.createElement(HTC_DATA_ELEMENT_NAME);
            doc.appendChild(htcdata);
        }


        Node fb = doc.createElement("Facebook");
        fb.setTextContent("id:" + friendID + "/friendof:" + fbID);
        Node htc = doc.getFirstChild();

        NodeList oldRecord = doc.getElementsByTagName("Facebook");
        if (oldRecord.getLength() > 0) {
            htc.replaceChild(fb, oldRecord.item(0));
        } else {
            htc.appendChild(fb);
        }

        String xmlString;
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

            StreamResult result = new StreamResult(new StringWriter());
            DOMSource source = new DOMSource(doc);
            transformer.transform(source, result);
            xmlString = result.getWriter().toString();
        } catch (TransformerException e) {
            Log.e(TAG, "Error building HTCData", e);
            return;
        }

        if (xmlString != null) {
            resolver.delete(ContactsContract.Data.CONTENT_URI, noteWhere, noteWhereParams);
            ContentValues contentValues = new ContentValues();
            contentValues.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE);
            contentValues.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactID);
            contentValues.put(ContactsContract.CommonDataKinds.Note.NOTE, xmlString);
            resolver.insert(ContactsContract.Data.CONTENT_URI, contentValues);
        }
    }

    public void updateContactPhoto(ProfilePicture picture, boolean force, boolean root, boolean google, boolean primary, File cacheDir) {

        if (picture.getURL() == null) {
            Log.w(TAG, "Update contact photo was initiated with empty photo");
            return;
        }

        if (!force && isSamePhoto(resolver, picture.getGoogleId(), picture.getURL())) {
            // only update photos that are new, unless force is selected, then update them all
            force = true;
        }

        if (force) {
            Log.i(TAG, "Getting new image at " + picture.getURL());

            byte[] photo = WebUtil.download(picture.getURL());
            byte[] origPhoto = photo;

            ContactUtil.Photo photoi = new ContactUtil.Photo();
            photoi.data = photo;
            photoi.timestamp = -1;
            photoi.url = picture.getURL();

            ContactUtil.updateContactPhoto(resolver, picture.getGoogleId(), photoi, primary);

            if (root) {
                processImage(resolver, picture.getGoogleId(), photo, origPhoto, cacheDir);
            }

            if (google) {
                Log.i(TAG, "Performing Google photo push");
                for (long raw : ContactUtil.getRawContacts(resolver, picture.getGoogleId(), "com.google")) {
                    ContactUtil.updateContactPhoto(resolver, raw, photoi, false);
                }
            }
        }
    }

    public Map<String, Long> getLocalContacts(Account account) {
        Uri rawContactUri = ContactsContract.RawContacts.CONTENT_URI.buildUpon()
                .appendQueryParameter(ContactsContract.RawContacts.ACCOUNT_NAME, account.name)
                .appendQueryParameter(ContactsContract.RawContacts.ACCOUNT_TYPE, account.type)
                .build();
        HashMap<String, Long> localContacts = new HashMap<>();
        try (Cursor c1 = resolver.query(rawContactUri, new String[]{BaseColumns._ID, USERNAME_COLUMN, TIMESTAMP_COLUMN}, null, null, null)) {
            while (c1.moveToNext()) {
                long entry = c1.getLong(c1.getColumnIndex(BaseColumns._ID));
                localContacts.put(c1.getString(1), Long.valueOf(entry));
            }
        }
        return localContacts;
    }

    public Map<String, Long> loadPhoneContacts() {

        HashMap<String, Long> contacts = new HashMap<String, Long>();
        try (Cursor cursor = resolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.RAW_CONTACT_ID},
                null,
                null,
                null)) {
            while (cursor.moveToNext()) {
                contacts.put(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)), cursor.getLong(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.RAW_CONTACT_ID)));
            }
        }
        return contacts;
    }

    private void processImage(ContentResolver resolver, long rawContactId, byte[] photo, byte[] origPhoto, File cacheDir) {

        // I am going to out of limb here and speculate that this advanced piece of code
        // attempts to modify the image by croping it and detecting the face on the photo

        // uses root privileges which I do not like

        // should be considered worth restoring to functionality only if we find a way to
        // avoid using root shell

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
