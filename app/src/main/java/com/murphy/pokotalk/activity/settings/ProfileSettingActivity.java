package com.murphy.pokotalk.activity.settings;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.R;
import com.murphy.pokotalk.content.ContentManager;
import com.murphy.pokotalk.content.ContentTransferManager;
import com.murphy.pokotalk.content.ImageEncoder;
import com.murphy.pokotalk.content.ImageProcessor;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.service.ContentService;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class ProfileSettingActivity extends AppCompatActivity
        implements CameraPermissionDialog.Listener {
    private Button backspaceButton;
    private Button galleryButton;
    private Button cameraButton;
    private AppCompatActivity activity;
    private String[] cameraPermissionString = {Manifest.permission.CAMERA};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_setting_layout);

        // This activity
        activity = this;

        // Get views
        backspaceButton = findViewById(R.id.backspaceButton);
        galleryButton = findViewById(R.id.profile_gallery_button);
        cameraButton = findViewById(R.id.profile_camera_button);

        // Add listeners
        backspaceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        galleryButton.setOnClickListener(galleryButtonClickListener);
        cameraButton.setOnClickListener(cameraButtonClickListener);
    }

    private View.OnClickListener galleryButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Start gallery intent to select profile image
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, Constants.RequestCode.PROFILE_GALLERY.value);
        }
    };

    private View.OnClickListener cameraButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Test if camera permission is granted
            boolean cameraPermission = ContextCompat.checkSelfPermission(
                    getApplicationContext(),
                    Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED;

            if (cameraPermission) {
                // Start camera intent to capture profile image
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, Constants.RequestCode.PROFILE_CAMERA.value);
                } else {
                    // No camera application available, show message
                    Toast.makeText(getApplicationContext(), R.string.no_camera_app_message,
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                // Request for camera permission
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        activity,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    // Show permission rationale dialog
                    CameraPermissionDialog dialog = new CameraPermissionDialog();
                    dialog.show(getSupportFragmentManager(), "camera permission");
                } else {
                    // Request for permission
                    ActivityCompat.requestPermissions(activity,
                            cameraPermissionString, Constants.CAMERA_PERMISSION);
                }
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == Constants.RequestCode.PROFILE_GALLERY.value) {
            if (data == null) {
                return;
            }

            try {
                // Get image uri
                final Uri imageUri = data.getData();
                if (imageUri != null) {
                    // Get content resolver
                    ContentResolver resolver = getContentResolver();

                    // Get input stream for image
                    InputStream imageStream = resolver.openInputStream(imageUri);

                    // Decode image data and generate bitmap
                    Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);

                    // Get bitmap with adjusted orientation
                    Bitmap adjustedImage = ImageProcessor.adjustOrientation(
                            this, resolver, selectedImage, imageUri);

                    // Start image edit activity
                    Intent intent = new Intent(getApplicationContext(), ImageEditionActivity.class);
                    ImageEditionActivity.image =  adjustedImage;
                    startActivityForResult(intent, Constants.RequestCode.IMAGE_EDITION.value);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else if (requestCode == Constants.RequestCode.PROFILE_CAMERA.value) {
            if (data == null) {
                return;
            }

            // Get extras
            Bundle extras = data.getExtras();
            if (extras != null) {
                // Get bitmap image
                Bitmap image = (Bitmap) extras.get("data");

                // Start image edit activity
                Intent intent = new Intent(getApplicationContext(), ImageEditionActivity.class);
                ImageEditionActivity.image = image;
                startActivityForResult(intent, Constants.RequestCode.IMAGE_EDITION.value);
            }
        } else if (requestCode == Constants.RequestCode.IMAGE_EDITION.value) {
            if (resultCode == RESULT_OK && data != null) {
                // Get image
                final Bitmap image = ImageEditionActivity.image;

                if (image != null) {
                    // Encode to jpeg format
                    final byte[] binary = ImageEncoder.encodeToJPEG(image);

                    // Add upload job
                    int id = ContentTransferManager.getInstance().addUploadJob(
                            binary,
                            ContentManager.EXT_JPG,
                            new ContentTransferManager.UploadJobCallback() {
                                @Override
                                public void onSuccess(final String contentName) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            // Uploaded profile image
                                            Toast.makeText(getApplicationContext(),
                                                    contentName,
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    });

                                    // Put content in cache
                                    ContentManager.getInstance().putImageContentInCache(contentName, image);

                                    // Start service to save content as a file
                                    ContentService.putImageBinary(contentName, binary);
                                    Intent intent = new Intent(getApplicationContext(), ContentService.class);
                                    intent.putExtra("command", ContentService.CMD_STORE_CONTENT);
                                    intent.putExtra("contentName", contentName);
                                    intent.putExtra("contentType", ContentTransferManager.TYPE_IMAGE);
                                    startService(intent);

                                    // Finish activity
                                    setResult(RESULT_OK);
                                    finish();
                                }

                                @Override
                                public void onError() {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            // Show error message
                                            Toast.makeText(getApplicationContext(),
                                                    R.string.profile_upload_error,
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                            });

                    // Get server
                    PokoServer server = PokoServer.getInstance();

                    // Send update profile image request
                    server.sendUpdateProfileImage(id);
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == Constants.CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (permissions.length > 0 && permissions[0] == Manifest.permission.CAMERA) {
                    // Camera permission granted, start camera
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(intent, Constants.RequestCode.PROFILE_CAMERA.value);
                    } else {
                        // No camera application available, show message
                        Toast.makeText(this, R.string.no_camera_app_message,
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    // Camera permission dialog listener
    @Override
    public void onPermissionRationaleAction(int which) {
        switch (which) {
            case CameraPermissionDialog.GOT_IT: {
                // Request for permission
                ActivityCompat.requestPermissions(activity,
                        cameraPermissionString, Constants.CAMERA_PERMISSION);
                break;
            }
        }
    }
}
