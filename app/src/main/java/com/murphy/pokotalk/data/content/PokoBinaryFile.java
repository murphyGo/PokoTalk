package com.murphy.pokotalk.data.content;

import android.os.Environment;
import android.webkit.MimeTypeMap;

import com.murphy.pokotalk.content.ContentManager;

import java.io.File;
import java.io.FileNotFoundException;

public class PokoBinaryFile extends ContentFile {
    private static String rootPath = null;
    private String contentName;

    @Override
    public String getFullFilePath() throws FileNotFoundException {
        if (fullFilePath == null) {
            fullFilePath = getFullDirectoryPath() + File.separator + getRealFileName();
        }
        return fullFilePath;
    }

    @Override
    public String getRootPath() throws FileNotFoundException {
        if (rootPath == null) {
            // Get file name
            String fileName = getFileName();

            // Get extension of file name
            String extension = ContentManager.getExtension(fileName);

            if (extension == null) {
                throw new FileNotFoundException("No extension");
            }

            // Get MIME type of file
            String MIMEType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);

            if (MIMEType == null) {
                throw new FileNotFoundException("Invalid MIME type");
            }

            // Get MIME prefix
            String MIMEPrefix = MIMEType.substring(MIMEType.indexOf('/'));

            // Sdcard folder
            File sdcardFolder;

            // Get root directory for each type of file
            switch (MIMEPrefix) {
                case "audio": {
                    sdcardFolder = Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_MUSIC);
                    break;
                }
                case "image": {
                    sdcardFolder = Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_PICTURES);
                    break;
                }
                case "text": {
                    sdcardFolder = Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DOCUMENTS);
                    break;
                }
                case "video": {
                    sdcardFolder = Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DCIM);
                    break;
                }
                case "application":
                default: {
                    sdcardFolder = Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DOWNLOADS);
                    break;
                }
            }

            // Set root path
            rootPath = sdcardFolder.getAbsolutePath();
        }

        return rootPath;
    }

    public String getContentName() {
        return contentName;
    }

    public void setContentName(String contentName) {
        this.contentName = contentName;
    }

    public String getRealFileName() throws FileNotFoundException {
        // Get file name and extension and content name
        String extension = ContentManager.getExtension(fileName);
        String name = ContentManager.getFileNameWithoutExtension(fileName);
        String contentName = getContentName();

        // All three string should exist
        if (extension == null || name == null || contentName == null) {
            throw new FileNotFoundException("No file name");
        }

        // Combine three components
        return name + '_' + getContentName() + '.' + extension;
    }
}
