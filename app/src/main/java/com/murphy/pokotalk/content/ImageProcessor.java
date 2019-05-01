package com.murphy.pokotalk.content;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;

public class ImageProcessor {
    /** Adjusts orientation of bitmap image to correct orientation */
    public static Bitmap adjustOrientation(Context context, ContentResolver contentResolver,
                                           Bitmap bitmap, Uri uri) {
        int angle = getRotationInDegrees(context, contentResolver, uri);

        if (angle == 0) {
            return bitmap;
        }

        return rotate(bitmap, angle);
    }

    private static int getRotationInDegrees(Context context, ContentResolver contentResolver, Uri uri) {
        return ImageOrientationUtil
                .getExifRotation(ImageOrientationUtil
                        .getFromMediaUri(
                                context,
                                contentResolver,
                                uri));
    }

    public static Bitmap rotate(Bitmap bitmap, float degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public static Bitmap flip(Bitmap bitmap, boolean horizontal, boolean vertical) {
        Matrix matrix = new Matrix();
        matrix.preScale(horizontal ? -1 : 1, vertical ? -1 : 1);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }
}
