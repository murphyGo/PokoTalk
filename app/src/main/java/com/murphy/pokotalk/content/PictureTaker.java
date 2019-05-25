package com.murphy.pokotalk.content;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;

import com.murphy.pokotalk.Constants;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PictureTaker {
    private Context context;
    private Activity activity;
    private Fragment fragment;
    private String fullPath;

    public PictureTaker(Context context) {
        this.context = context;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public void setFragment(Fragment fragment) {
        this.fragment = fragment;
    }

    private File createPictureFile() throws IOException {
        // Make timestamp string and file name
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Constants.locale)
                .format(new Date());
        String fileName = "Poko_" + timestamp;

        // Get directory
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        // Create temporary picture file
        File file = File.createTempFile(fileName,
                ".jpg",
                dir);

        // Save absolute file path
        fullPath = file.getAbsolutePath();

        return file;

    }

    public String getAbsolutePath() {
        return fullPath;
    }

    public void scanPictureFile() {
        // Media store scans the picture file so that the picture is shown in gallery app
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File file = new File(getAbsolutePath());
        Uri uri = Uri.fromFile(file);
        intent.setData(uri);
        context.sendBroadcast(intent);
    }

    public int startCameraIntent(int requestCode) throws IOException {
        // Make intent to start camera
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Check if there is camera app that can resolve this intent
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            File photoFile = createPictureFile();

            if (photoFile != null) {
                // Make picture file uri
                Uri photoURI = FileProvider.getUriForFile(context,
                        context.getApplicationContext()
                                .getPackageName() + ".provider",
                        photoFile);

                // Put extra that picture data will be written
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                // Start activity
                if (activity != null) {
                    activity.startActivityForResult(intent, requestCode);
                } else if (fragment != null) {
                    fragment.startActivityForResult(intent, requestCode);
                } else {
                    return -1;
                }

                return 1;
            }
        }

        return -1;
    }
}
