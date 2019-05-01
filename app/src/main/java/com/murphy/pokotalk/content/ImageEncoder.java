package com.murphy.pokotalk.content;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;

public class ImageEncoder {
    public static byte[] encodeToJPEG(Bitmap bitmap) {
        // Compress to jpeg format
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }
}
