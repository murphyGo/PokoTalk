package com.murphy.pokotalk.data.content;

import android.os.Environment;

import com.murphy.pokotalk.Constants;

import java.io.File;

public class PokoImageFile extends ContentFile {
    private static String rootPath = null;

    @Override
    public String getRootPath() {
        if (rootPath == null) {
            File sdcardFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            String sdcardPath = sdcardFolder.getAbsolutePath();
            rootPath = sdcardPath + File.separator + Constants.contentRootDirectory;
        }

        return rootPath;
    }

    @Override
    public String getRestPath() {
        return Constants.imageContentDirectory;
    }
}
