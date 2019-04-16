package com.murphy.pokotalk.server.content;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.IBinder;

import com.murphy.pokotalk.data.content.PokoBinaryFile;
import com.murphy.pokotalk.data.content.PokoImageFile;

import java.io.IOException;

public class ContentLoadService extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Get content name
        String contentName = intent.getStringExtra("contentName");

        // Get content type
        String contentType = intent.getStringExtra("contentType");

        if (contentName != null && contentType != null) {
            // Get content manager
            ContentManager contentManager = ContentManager.getInstance();

            // Get transfer manager
            ContentTransferManager transferManager = ContentTransferManager.getInstance();

            switch(contentType) {
                // Image file load
                case ContentManager.TYPE_IMAGE: {
                    // Get job
                    final ContentManager.ImageContentLocateJob job =
                            contentManager.getImageLoadJob(contentName);

                    // Job should exist
                    if (job == null) {
                        break;
                    }

                    // Create image file
                    PokoImageFile file = new PokoImageFile();

                    // Set content name
                    file.setFileName(contentName);

                    // Read data
                    try {
                        file.openReader();
                        byte[] buffer = file.read();
                        file.closeReader();

                        // Process image data and finish job
                        if (processImageAndCallback(job, buffer)) {
                            break;
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    // Content is not found, download from server
                    transferManager.addDownloadJob(contentName,
                            ContentTransferManager.TYPE_IMAGE,
                            new ContentTransferManager.DownloadJobCallback() {
                                @Override
                                public void run(String contentName, byte[] bytes) {
                                    if (processImageAndCallback(job, bytes)) {

                                    } else {
                                        // Image processing failed
                                    }
                                }
                            });
                    break;
                }
                // Binary file load
                case ContentManager.TYPE_BINARY: {
                    // Get job
                    final ContentManager.BinaryContentLocateJob job =
                            contentManager.getBinaryLoadJob(contentName);

                    // Job should exist
                    if (job == null) {
                        break;
                    }

                    // Create image file
                    PokoBinaryFile file = new PokoBinaryFile();

                    // Set content name
                    file.setFileName(contentName);

                    // Read data
                    try {
                        file.openReader();
                        byte[] buffer = file.read();
                        file.closeReader();

                        // Process binary data and finish job
                        if (processBinaryAndCallback(job, buffer)) {
                            break;
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    // Content is not found, download from server
                    transferManager.addDownloadJob(contentName,
                            ContentTransferManager.TYPE_BINARY,
                            new ContentTransferManager.DownloadJobCallback() {
                                @Override
                                public void run(String contentName, byte[] bytes) {
                                    if (processBinaryAndCallback(job, bytes)) {

                                    } else {
                                        // Image processing failed
                                    }
                                }
                            });
                    break;
                }
            }
        }

        // This service do not need to be sticky
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We do not allow binding service
        return null;
    }

    private boolean processImageAndCallback(ContentManager.ImageContentLocateJob job,
                                            byte[] buffer) {
        String contentName = job.getContentName();

        if (buffer == null) {
            return false;
        }

        // Decode image
        Bitmap bitmap = ImageDecoder.decodeImage(contentName, buffer);

        if (bitmap != null) {
            // Set image
            job.setImage(bitmap);

            // Finish job
            job.finishJobAndStartCallbacks();

            return true;
        } else {
            return false;
        }
    }

    private boolean processBinaryAndCallback(ContentManager.BinaryContentLocateJob job,
                                             byte[] buffer) {
        if (buffer != null) {
            // Set image
            job.setBinary(buffer);

            // Finish job
            job.finishJobAndStartCallbacks();

            return true;
        } else {
            return false;
        }
    }
}
