package org.codarama.haxsync.utilities;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class WebUtil {

    private static final String TAG = "WebUtil";

    public static byte[] download(String urlString) {
        ByteArrayOutputStream bais = new ByteArrayOutputStream();
        InputStream is = null;
        URL url;
        try {
            url = new URL(urlString);
            is = url.openStream();
            byte[] byteChunk = new byte[4096];
            int n;

            while ((n = is.read(byteChunk)) > 0) {
                bais.write(byteChunk, 0, n);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error downloading URL" + urlString, e);
            if (urlString.contains("fbcdn_sphotos_")) {
                String alt = urlString;
                urlString = urlString.replace("fbcdn_sphotos_", "fbcdn-sphotos-");
                alt = alt.replace("fbcdn_sphotos_a-a.akamaihd.net", "a1.sphotos.ak.fbcdn.net");
                byte[] res = download(urlString);
                return (res == null) ? download(alt) : res;
            }
            return null;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    // nothing to do about it
                    Log.e("Error", e.getLocalizedMessage());
                }
            }
        }

        return bais.toByteArray();
    }

}
