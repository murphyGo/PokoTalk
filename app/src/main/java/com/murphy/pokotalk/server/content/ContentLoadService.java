package com.murphy.pokotalk.server.content;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.util.Log;

import com.murphy.pokotalk.data.content.PokoBinaryFile;
import com.murphy.pokotalk.data.content.PokoImageFile;

import java.io.FileNotFoundException;
import java.io.IOException;

public class ContentLoadService extends Service {
    public static final int CMD_LOCATE_CONTENT = 1;
    public static final int CMD_START_DOWNLOAD = 2;
    public static final int CMD_DOWNLOAD = 3;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Get command
        int command = intent.getIntExtra("command", -1);

        // Do appropriate job according to the command
        switch (command) {
            case CMD_LOCATE_CONTENT: {
                processLocateContentCommand(intent);
                break;
            }
            case CMD_START_DOWNLOAD: {
                processStartDownloadCommand(intent);
                break;
            }
            case CMD_DOWNLOAD: {
                processDownloadCommand(intent);
                break;
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

    private void processLocateContentCommand(Intent intent) {
        // Get content name
        final String contentName = intent.getStringExtra("contentName");

        // Get content type
        String contentType = intent.getStringExtra("contentType");

        if (contentName == null || contentType == null) {
            return;
        }

        // Get content manager
        final ContentManager contentManager = ContentManager.getInstance();

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
                    Log.v("POKO", "FILE HIT");
                    // Process image data and finish job
                    if (processImageAndCallback(job, buffer)) {
                        break;
                    }

                } catch (FileNotFoundException e) {

                } catch (IOException e) {
                    e.printStackTrace();
                }

                Log.v("POKO","FILE NOT FOUND, REQUEST TO SERVER");

                // Content is not found, download from server
                transferManager.addDownloadJob(contentName,
                        ContentTransferManager.TYPE_IMAGE,
                        new ContentTransferManager.DownloadJobCallback() {
                            @Override
                            public void onSuccess(String contentName, byte[] bytes) {
                                if (!processImageAndCallback(job, bytes)) {
                                    // Image processing failed
                                    contentManager.failImageLocateJob(contentName);
                                }
                            }

                            @Override
                            public void onError() {
                                // Fail the job
                                contentManager.failImageLocateJob(contentName);
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

                } catch (FileNotFoundException e) {

                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Content is not found, download from server
                transferManager.addDownloadJob(contentName,
                        ContentTransferManager.TYPE_BINARY,
                        new ContentTransferManager.DownloadJobCallback() {
                            @Override
                            public void onSuccess(String contentName, byte[] bytes) {
                                if (processBinaryAndCallback(job, bytes)) {
                                    // Binary processing failed
                                    contentManager.failBinaryLocateJob(contentName);
                                }
                            }

                            @Override
                            public void onError() {
                                // Fail the job
                                contentManager.failBinaryLocateJob(contentName);
                            }
                        });
                break;
            }
        }
    }

    private void processStartDownloadCommand(Intent intent) {
        int sendId = intent.getIntExtra("sendId", -1);
        int downloadId = intent.getIntExtra("downloadId", -1);
        int size = intent.getIntExtra("size", -1);

        if (sendId < 0 || downloadId < 0 || size < 0) {
            return;
        }

        // Start receive data
        ContentTransferManager.getInstance().startDownloadJob(sendId, downloadId, size);
    }

    private void processDownloadCommand(Intent intent) {
        int downloadId = intent.getIntExtra("downloadId", -1);

        if (downloadId < 0) {
            return;
        }

        // Copy downloaded data to buffer
        ContentTransferManager.getInstance().writeBytesFromJobQueue(downloadId);
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
            // Set image binary
            job.setBinary(buffer);

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
