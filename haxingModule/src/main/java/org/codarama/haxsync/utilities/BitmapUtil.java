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

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.provider.ContactsContract.DisplayPhoto;
import android.util.Log;

import java.io.ByteArrayOutputStream;

public class BitmapUtil {

    public static int getMaxSize(ContentResolver resolver) {
        // Note that this URI is safe to call on the UI thread.
        Cursor c = resolver.query(DisplayPhoto.CONTENT_MAX_DIMENSIONS_URI,
                new String[]{DisplayPhoto.DISPLAY_MAX_DIM}, null, null, null);
        try {
            c.moveToFirst();
            return c.getInt(0);
        } finally {
            c.close();
        }
    }

    private static Bitmap scale(Bitmap in, int size) {
        int width = in.getWidth();
        int height = in.getHeight();
        float scalew;
        float scaleh;
        Matrix matrix = new Matrix();
        //scale smaller axis to size (if necessary)
        if (width > height) {
            scaleh = (float) size / height;
            scalew = scaleh;
            if ((width * scalew) % 2 != 0) {
                scalew = ((width * scalew) + 1) / (float) width;
            }

        } else {
            scalew = (float) size / width;
            scaleh = scalew;
            if ((height * scaleh) % 2 != 0) {
                scaleh = ((height * scaleh) + 1) / (float) height;
            }
        }
        Log.i("RESIZING", "old width: " + width + "old height: " + height + "new size: " + size);
        matrix.postScale(scalew, scaleh);
        Bitmap scaledPic = Bitmap.createBitmap(in, 0, 0, width, height, matrix, true);
        return scaledPic;
    }

    private static PointF findFaceMid(Bitmap in) {
        PointF mid = new PointF();
        Bitmap bitmap565 = in.copy(Bitmap.Config.RGB_565, true);

        FaceDetector fd = new FaceDetector(in.getWidth(), in.getHeight(), 1);
        FaceDetector.Face[] faces = new FaceDetector.Face[1];
        fd.findFaces(bitmap565, faces);


        FaceDetector.Face face = faces[0];
        if (face != null) {
            try {
                face.getMidPoint(mid);
                return mid;
            } catch (NullPointerException n) {
                // FIXME please fix this horrible NPE catch block
                Log.e("Error", n.getLocalizedMessage());
            }
        }
        return null;

    }

    private static byte[] bitmapToBytes(Bitmap in) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        in.compress(CompressFormat.PNG, 0, bos);
        return bos.toByteArray();
    }


    public static byte[] resize(byte[] picArray, int size, boolean faceDetect) {
        if (picArray == null) {
            return null;
        }
        Bitmap pic = BitmapFactory.decodeByteArray(picArray, 0, picArray.length);
        if (pic == null) {
            return null;
        }

        size = Math.min(size, Math.min(pic.getHeight(), pic.getWidth()));
        if (size % 2 != 0)
            size--;
        Log.i("sizes", "old width:" + pic.getWidth() + " old height:" + pic.getHeight() + " new size:" + size);
        Bitmap scaledPic = scale(pic, size);

        int width = scaledPic.getWidth();
        int height = scaledPic.getHeight();

        //if pic is already square, we are done now
        if (width == height) {
            return bitmapToBytes(scaledPic);
        }

        PointF mid = null;
        int cropcenter;

        if (faceDetect)
            mid = findFaceMid(scaledPic);


        Bitmap out;
        if (width > height) {
            if (mid != null)
                cropcenter = Math.max(size / 2, Math.min((int) Math.floor(mid.y), width - size / 2));
            else
                cropcenter = width / 2;
            Log.i("CROPPING", "width:" + width + " center:" + cropcenter + " size:" + size + " left edge:" + (cropcenter - size / 2) + " right edge:" + (cropcenter + size / 2));
            out = Bitmap.createBitmap(scaledPic, cropcenter - size / 2, 0, size, size);
        } else {
            if (mid != null)
                cropcenter = Math.max(size / 2, Math.min((int) Math.floor(mid.x), height - size / 2));
            else
                cropcenter = height / 2;
            out = Bitmap.createBitmap(scaledPic, 0, 0, size, size);
        }

        return bitmapToBytes(out);
    }

}
