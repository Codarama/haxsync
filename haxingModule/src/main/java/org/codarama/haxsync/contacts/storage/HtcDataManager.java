package org.codarama.haxsync.contacts.storage;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

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
public class HtcDataManager {
    private static final String TAG = "HtcDataManager";

    private static final String HTC_DATA_ELEMENT = "<HTCData>";
    private static final String HTC_DATA_ELEMENT_NAME = "HTCData";
    private static final String FACEBOOK_PATTERN = "<Facebook>id:(.*)/friendof.*</Facebook>";

    private ContentResolver resolver;

    public HtcDataManager(ContentResolver resolver) {
        this.resolver = resolver;
    }

    /**
     * <p>Reads the HTCData tags in the notes of each of the Google contacts to identify the
     * respective Facebook ID. Returns a {@link Map} of Facebook ID/Google ID pairs.</p>
     * <p/>
     * <p>Facebook IDs are in string format and are the keys.</p>
     *
     * @return a mappinjg between Facebook ID and Google ID
     */
    public Map<String, Long> fetchFriends() {

        Map<String, Long> contacts = new HashMap<String, Long>();
        String noteWhere = ContactsContract.Data.MIMETYPE + " = ?";
        String[] noteWhereParams = new String[]{ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE};
        String[] columns = new String[]{ContactsContract.Data.RAW_CONTACT_ID, ContactsContract.CommonDataKinds.Note.NOTE};

        try (Cursor cursor = resolver.query(ContactsContract.Data.CONTENT_URI, columns, noteWhere, noteWhereParams, null)) {
            while (cursor.moveToNext()) {
                try {
                    String note = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Note.NOTE));
                    Long rawID = cursor.getLong(cursor.getColumnIndex(ContactsContract.Data.RAW_CONTACT_ID));
                    String uid = parseNote(note);
                    contacts.put(uid, rawID);
                } catch (IllegalStateException e) {
                    Log.e(TAG, "Issue while loading HTCData field");
                    // originally the code would break here - not sure why, it would make more sense to continue with
                    // the next contact? Still leaving this note here for future consideration
                }
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

        return "";
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
}
