package com.murphy.pokotalk.activity.chat;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.Toast;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.R;
import com.murphy.pokotalk.activity.settings.CameraPermissionDialog;
import com.murphy.pokotalk.content.ContentReader;
import com.murphy.pokotalk.content.ContentStream;
import com.murphy.pokotalk.content.PictureTaker;
import com.murphy.pokotalk.content.image.ImageProcessor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class ChatAttachOptionFragment extends Fragment {
    private Listener listener;
    private Button galleryOptionButton;
    private Button cameraOptionButton;
    private Button fileOptionButton;
    private String[] cameraPermissionString = {Manifest.permission.CAMERA};
    private PictureTaker pictureTaker;

    public interface Listener {
        void onImageAttachedToMessage(Bitmap bitmap);
        void onBinaryAttachedToMessage(Uri fileUri, String fileName, ContentStream contentStream);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (Listener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    " should implement listener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chat_attach_option_fragment, null, false);

        // Find views
        galleryOptionButton = view.findViewById(R.id.chatGalleryAttachButton);
        cameraOptionButton = view.findViewById(R.id.chatCameraAttachButton);
        fileOptionButton = view.findViewById(R.id.chatFileAttachButton);

        // Add listeners
        galleryOptionButton.setOnClickListener(galleryOptionClickListener);
        cameraOptionButton.setOnClickListener(cameraOptionClickListener);
        fileOptionButton.setOnClickListener(fileOptionClickListener);

        return view;
    }

    private View.OnClickListener galleryOptionClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Show gallery to user to select a picture
            Intent intent = new Intent(Intent.ACTION_PICK);

            // Target only images
            intent.setType("image/*");

            // Start app to select image
            startActivityForResult(intent, Constants.RequestCode.ATTACH_IMAGE.value);
        }
    };

    private View.OnClickListener cameraOptionClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Context context = getContext();
            FragmentActivity activity = getActivity();

            if (context == null || activity == null) {
                return;
            }

            // Test if camera permission is granted
            boolean cameraPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED;

            if (cameraPermission) {
                // Start camera intent to capture profile image
                startTakingPicture();
            } else {
                // Request for camera permission
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        activity,
                        Manifest.permission.CAMERA)) {
                    // Show permission rationale dialog
                    CameraPermissionDialog dialog = new CameraPermissionDialog();
                    dialog.show(activity.getSupportFragmentManager(), "camera permission");
                } else {
                    // Request for permission
                    ActivityCompat.requestPermissions(activity,
                            cameraPermissionString, Constants.CAMERA_PERMISSION);
                }
            }
        }
    };

    private View.OnClickListener fileOptionClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Get context
            Context context = getContext();

            // Context should exist
            if (context == null) {
                return;
            }

            // Make intent to pick attachment
            Intent target = new Intent(Intent.ACTION_OPEN_DOCUMENT);

            // Set universal type so that the user can pick any type of content
            target.setType("*/*");

            // Add openable category
            target.addCategory(Intent.CATEGORY_OPENABLE);

            // We do not have permissions yet so do not grant permission
//            target.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
//            target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            target.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            // Get choose menu title
            String title = getString(R.string.select_attach_file_title);

            // Make chooser
            Intent chooser = Intent.createChooser(target, title);

            // Start choice
            startActivityForResult(chooser, Constants.RequestCode.ATTACH_FILE.value);
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Context context = getContext();

        if (resultCode == Activity.RESULT_OK && context != null) {
            if (requestCode == Constants.RequestCode.ATTACH_IMAGE.value) {
                try {
                    // Get image uri
                    final Uri imageUri = data.getData();
                    if (imageUri != null && listener != null) {
                        // Get content resolver
                        ContentResolver resolver = context.getContentResolver();

                        // Get input stream for image
                        InputStream imageStream = resolver.openInputStream(imageUri);

                        // Decode image data and generate bitmap
                        Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);

                        // Get bitmap with adjusted orientation
                        Bitmap adjustedImage = ImageProcessor.adjustOrientation(
                                context, resolver, selectedImage, imageUri);

                        // Start listener callback
                        listener.onImageAttachedToMessage(adjustedImage);
                    }
                } catch (FileNotFoundException e) {
                    Toast.makeText(context, R.string.chat_attach_image_not_found, Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == Constants.RequestCode.ATTACH_CAMERA_PICTURE.value) {
                if (listener == null || pictureTaker == null) {
                    return;
                }

                // Add image to gallery
                pictureTaker.scanPictureFile();

                // Get content resolver
                ContentResolver resolver = context.getContentResolver();

                // Make file and uri
                File file = new File (pictureTaker.getAbsolutePath());
                Uri uri = Uri.fromFile(file);

                // Get image
                Bitmap image = BitmapFactory.decodeFile(file.getAbsolutePath());

                // Get bitmap with adjusted orientation
                Bitmap adjustedImage = ImageProcessor.adjustOrientation(
                        context, resolver, image, uri);
                
                // Start listener callback
                listener.onImageAttachedToMessage(adjustedImage);
            } else if (requestCode == Constants.RequestCode.ATTACH_FILE.value) {
                // Get file uri
                final Uri fileUri = data.getData();

                if (fileUri == null) {
                    return;
                }

                // Take persistable permission
                context.getContentResolver().takePersistableUriPermission(fileUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION |
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                // Get content resolver
                ContentResolver resolver = context.getContentResolver();

                // Get file name
                String fileName = ContentReader.getFileName(resolver, fileUri);

                // Get file extension
                ContentResolver cR = context.getContentResolver();
                MimeTypeMap mime = MimeTypeMap.getSingleton();
                String extension = mime.getExtensionFromMimeType(cR.getType(fileUri));

                // Check if extension is in file
                if (fileName != null) {
                    // Get last index of dot
                    int lastIndex = fileName.lastIndexOf('.');

                    // Check if file name has no extension or suffix after dot does not
                    // equals to file extension
                    if (lastIndex < 0 || (fileName.length() > lastIndex + 1
                            && !fileName.substring(lastIndex + 1).equals(extension))) {
                        // Add extension to file name
                        fileName += '.' + extension;
                    }
                }

                Log.v("POKO", "FILE NAME " + fileName);
                Log.v("POKO", "FILE SIZE " + ContentReader.getFileSize(resolver, fileUri));

                ContentStream contentStream = null;

                Log.v("POKO", "START READ CHUNKS");
                try {
                    // Get input stream for image
                    contentStream = new ContentStream(context, resolver, fileUri);

                    // Stat listener callback
                    listener.onBinaryAttachedToMessage(fileUri, fileName, contentStream);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            } else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void startTakingPicture() {
        Context context = getContext();
        Activity activity = getActivity();

        if (context == null || activity == null) {
            return;
        }

        // Take picture
        try {
            pictureTaker = new PictureTaker(context);
            pictureTaker.setFragment(this);

            int result = pictureTaker.startCameraIntent(
                    Constants.RequestCode.ATTACH_CAMERA_PICTURE.value);

            if (result < 0) {
                // No camera application available, show message
                Toast.makeText(context, R.string.no_camera_app_message,
                        Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            // Failed
            Toast.makeText(context, R.string.camera_failed,
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == Constants.CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startTakingPicture();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}

