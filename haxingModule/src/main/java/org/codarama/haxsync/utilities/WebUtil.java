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
