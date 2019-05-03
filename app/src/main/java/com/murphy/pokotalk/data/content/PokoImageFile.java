package com.murphy.pokotalk.data.content;

import android.os.Environment;

import java.io.File;

public class PokoImageFile extends ContentFile {
    private static String rootPath = null;

    @Override
    public String getRootPath() {
        if (rootPath == null) {
            // Get sdcard folder
            File sdcardFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

            // Set root path
            rootPath = sdcardFolder.getAbsolutePath();
        }

        return rootPath;
    }
}
