package com.murphy.pokotalk.content;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ImageDecoder {
    public static Bitmap decodeImage(String contentName, byte[] binary) {
        // Split content name by dot
        String[] split = contentName.split("\\.");

        if (split.length > 0) {
            // Get extension of content name
            String ext = split[split.length - 1].toLowerCase();

            switch (ext) {
                case "jpg":
                case "jpeg": {
                    return ImageDecoder.decodeJpeg(binary);
                }
            }
        }

        return null;
    }

    public static Bitmap decodeJpeg(byte[] bytes) {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
