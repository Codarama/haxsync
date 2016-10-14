package org.codarama.haxsync.provider.facebook.callbacks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.facebook.FacebookRequestError;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;

import org.codarama.haxsync.SyncPreferences;
import org.codarama.haxsync.contacts.ContactsService;
import org.codarama.haxsync.entities.ProfilePicture;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ContactDetailsCallback implements GraphRequest.Callback {

    private static final String TAG = "ContactDetailsCallback";
    private final Context context;
    private final Map<String, Long> friends = new HashMap<>();

    public ContactDetailsCallback(Context context, Map<String, Long> friendsMap) {
        super();
        this.context = context;
        this.friends.putAll(friendsMap);
    }

    @Override
    public void onCompleted(GraphResponse graphResponse) {
        if (graphResponse == null) {
            Log.i(TAG, "Facebook just returned null graph response. Yoo should panic!");
            return;
        }

        // handle errors, should probably think of some more elaborate solution here
        if (graphResponse.getError() != null) {
            FacebookRequestError error = graphResponse.getError();
            Log.e(TAG, "Unfortunately Facebook says that " + error.getErrorMessage(), error.getException());
            return;
        }

        Log.i(TAG, "Received Facebook response : ");
        Log.d(TAG, graphResponse.getRawResponse());

        // attempt to parse the data returned by Facebook
        if (graphResponse.getJSONObject() != null) {
            handleRespose(graphResponse.getJSONObject());
        }

        // visit any remaining pages from the response
        GraphRequest nextPageRequest = graphResponse.getRequestForPagedResults(GraphResponse.PagingDirection.NEXT);
        if (nextPageRequest != null) {
            nextPageRequest.setCallback(this);
            nextPageRequest.executeAsync();
        }
    }

    private void handleRespose(JSONObject jsonObject) {

        JSONArray array = jsonObject.names();

        // step 1. configure sync options
        SyncPreferences prefs = new SyncPreferences(context);
        final boolean force = prefs.getForceSync();
        final boolean root = prefs.getRootEnabled();
        final boolean google = prefs.shouldUpdateGooglePhotos();
        final boolean primary = prefs.shouldBePrimaryImage();
        final File cacheDir = context.getCacheDir();

        // step 2. read result from JSON response
        for (int i = 0; i < array.length(); i++) {
            try {
                final String facebookId = array.getString(i);
                final JSONObject object = jsonObject.getJSONObject(facebookId);
                final JSONObject data = object.getJSONObject("data");

                final String url = data.getString("url");
                final boolean isSilhouette = data.getBoolean("is_silhouette");

                // oddly some pictures lack the height or width attribute
                final int height = data.has("height") ? data.getInt("height") : 0;
                final int width = data.has("width") ? data.getInt("width") : 0;

                final ProfilePicture friend = new ProfilePicture() {
                    @Override
                    public Long getGoogleId() {
                        return friends.get(facebookId);
                    }

                    @Override
                    public String getURL() {
                        return url;
                    }

                    @Override
                    public long getHeight() {
                        return height;
                    }

                    @Override
                    public long getWidth() {
                        return width;
                    }
                };

                // step 3. sync the information with the local contact
                new AsyncTask<String, Void, String>() {
                    @Override
                    protected String doInBackground(String... params) {
                        ContactsService manager = new ContactsService(context.getContentResolver());
                        manager.updateContactPhoto(friend, force, root, google, primary, cacheDir);

                        return "Success";
                    }
                }.execute();
            } catch (JSONException e) {
                Log.e(TAG, "Failed while parsing facebook contact data", e);
                Log.e(TAG, "Parsed data was : " + array);
            }
        }

        if (force) {
            // after we have executed a force sync, restore setting back to no force sync
            prefs.setForceSync(false);
        }

    }
}
