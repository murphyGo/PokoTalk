package com.murphy.pokotalk.service;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import android.util.SparseArray;

import com.murphy.pokotalk.content.ContentManager;
import com.murphy.pokotalk.content.ContentStream;
import com.murphy.pokotalk.content.ContentTransferManager;
import com.murphy.pokotalk.content.image.ImageDecoder;
import com.murphy.pokotalk.data.content.PokoBinaryFile;
import com.murphy.pokotalk.data.content.PokoImageFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Queue;

public class ContentService extends Service {
    public static final int CMD_LOCATE_CONTENT = 1;
    public static final int CMD_START_DOWNLOAD = 2;
    public static final int CMD_DOWNLOAD = 3;
    public static final int CMD_STORE_CONTENT = 4;
    public static final int CMD_UPLOAD = 5;

    // Temporary memory for binary data of contents to store in device
    private static HashMap<String, byte[]> imageBinary = null;
    private static HashMap<String, Uri> fileUri = null;

    // Threads for locating contents
    // Service code may run in UI thread so we pass processing to worker threads
    private static LocateContentThread locateContentThread = null;
    private static StartDownloadThread startDownloadThread = null;
    private static SparseArray<UploadJobThread> uploadThreads = new SparseArray<>();
    private static SparseArray<DownloadJobThread> downloadThreads = new SparseArray<>();
    private static ContentStoreThread contentStoreThread = null;

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
            case CMD_STORE_CONTENT: {
                processStoreContentCommand(intent);
                break;
            }
            case CMD_UPLOAD: {
                processUploadCommand(intent);
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
        synchronized (ContentService.class) {
            // Check if the thread is null
            if (locateContentThread == null) {
                // Create new thread
                locateContentThread = new LocateContentThread(this, 0);
                locateContentThread.start();
            }

            // Enqueue intent
            locateContentThread.enqueueIntent(intent);
        }
    }

    private void processStartDownloadCommand(Intent intent) {
        synchronized (ContentService.class) {
            // Check if the thread is null
            if (startDownloadThread == null) {
                // Create new thread
                startDownloadThread = new StartDownloadThread(this, 0);
                startDownloadThread.start();
            }

            // Enqueue intent
            startDownloadThread.enqueueIntent(intent);
        }
    }

    private void processDownloadCommand(Intent intent) {
        int downloadId = intent.getIntExtra("downloadId", -1);

        if (downloadId < 0) {
            return;
        }

        DownloadJobThread thread;

        synchronized (ContentService.class) {
            // Get download thread
            thread = downloadThreads.get(downloadId);

            // Check if the thread is null
            if (thread == null) {
                // Create new thread
                thread = new DownloadJobThread(this, downloadId);

                // Put in thread array
                downloadThreads.put(downloadId, thread);

                // Start thread
                thread.start();
            }

            // Enqueue intent
            thread.enqueueIntent(intent);
        }
    }

    private void processStoreContentCommand(Intent intent) {
        synchronized (ContentService.class) {
            // Check if the thread is null
            if (contentStoreThread == null) {
                // Create new thread
                contentStoreThread = new ContentStoreThread(this, 0);
                contentStoreThread.start();
            }

            // Enqueue intent
            contentStoreThread.enqueueIntent(intent);
        }
    }

    private void processUploadCommand(Intent intent) {
        int uploadId = intent.getIntExtra("uploadId", -1);

        if (uploadId < 0) {
            return;
        }

        UploadJobThread thread;

        synchronized (ContentService.class) {
            // Get download thread
            thread = uploadThreads.get(uploadId);

            // Check if the thread is null
            if (thread == null) {
                // Create new thread
                thread = new UploadJobThread(this, uploadId);

                // Put in thread array
                uploadThreads.put(uploadId, thread);

                // Start thread
                thread.start();
            }

            // Enqueue intent
            thread.enqueueIntent(intent);
        }
    }

    public static void putImageBinary(String contentName, byte[] bytes) {
        synchronized (ContentService.class) {
            if (imageBinary == null) {
                imageBinary = new HashMap<>();
            }

            imageBinary.put(contentName, bytes);
        }
    }

    public static void putFileUri(String contentName, Uri uri) {
        synchronized (ContentService.class) {
            if (fileUri == null) {
                fileUri = new HashMap<>();
            }

            fileUri.put(contentName, uri);
        }
    }

    public static byte[] getImageBinary(String contentName) {
        synchronized (ContentService.class) {
            byte[] bytes = imageBinary.remove(contentName);

            if (imageBinary.size() == 0) {
                imageBinary = null;
            }

            return bytes;
        }
    }

    public static Uri getFileUri(String contentName) {
        synchronized (ContentService.class) {
            Uri uri = fileUri.remove(contentName);

            if (fileUri.size() == 0) {
                fileUri = null;
            }

            return uri;
        }
    }

    private abstract static class IntentJobThread extends Thread {
        private int threadId;
        protected ContentService service;
        private boolean shouldStop = false;
        private Queue<Intent> intentQueue;
        private static final int WAIT_TIMEOUT = 12000;

        public IntentJobThread(ContentService service, int threadId) {
            this.service = service;
            this.threadId = threadId;
            intentQueue = new ArrayDeque<>();
        }

        @Override
        public void run() {
            Intent intent;

            while (true) {
                // Check if the thread should stop running
                if (shouldStop) {
                    // Stop thread loop and remove from list
                    if (service != null) {
                        try {
                            removeThread();
                        } catch (Exception e) {
                            // The thread should not be broken by any exception
                        }
                    }

                    // Break out of main loop
                    break;
                }

                synchronized (this) {
                    intent = intentQueue.poll();

                    if (intent == null) {
                        while (true) {
                            try {
                                // No intent, wait for intent to be enqueued
                                wait(WAIT_TIMEOUT);

                                // If the thread should stop, break loop
                                if (shouldStop) {
                                    break;
                                }

                                // Check for intent
                                intent = intentQueue.poll();

                                if (intent == null) {
                                    // Timeout, the thread should stop
                                    shouldStop = true;
                                }

                                // Break out of loop
                                break;
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();

                                // Interrupted, go back to loop
                                continue;
                            }
                        }
                    }
                }

                if (intent != null) {
                    try {
                        // Process intent
                        onIntent(intent);
                    } catch (Exception e) {
                        // The thread should not be broken by any exception
                    }
                }
            }
        }

        public void enqueueIntent(Intent intent) {
            synchronized (this) {
                // Enqueue intent
                intentQueue.add(intent);

                // Notify the thread
                notify();
            }
        }

        public void stopThread() {
            if (!shouldStop) {
                synchronized (this) {
                    shouldStop = true;

                    // Notify thread if it is waiting for intent
                    notify();
                }
            }
        }

        public int getThreadId() {
            return threadId;
        }

        protected abstract void onIntent(Intent intent);
        protected abstract void removeThread();
    }

    private static class DownloadJobThread extends IntentJobThread {
        private int downloadId;

        public DownloadJobThread(ContentService service, int downloadId) {
            super(service, downloadId);
            this.downloadId = downloadId;
        }

        @Override
        protected void onIntent(Intent intent) {
            ContentTransferManager transferManager = ContentTransferManager.getInstance();
            int downloadId = intent.getIntExtra("downloadId", -1);

            if (downloadId < 0 || downloadId != this.downloadId) {
                return;
            }

            // Copy downloaded data to buffer
            transferManager.writeBytesFromJobQueue(downloadId);

            // Check if download is done
            if (transferManager.hasDownloadEnded(downloadId)) {
                // Done, stop thread
                stopThread();
            }
        }

        @Override
        protected void removeThread() {
            synchronized (ContentService.class) {
                downloadThreads.remove(getThreadId());
                Log.v("POKO", "THREAD DOWNLOAD ID " + downloadId + " DONE");
            }
        }
    }

    private static class UploadJobThread extends IntentJobThread {
        private int uploadId;

        public UploadJobThread(ContentService service, int downloadId) {
            super(service, downloadId);
            this.uploadId = downloadId;
        }

        @Override
        protected void onIntent(Intent intent) {
            ContentTransferManager transferManager = ContentTransferManager.getInstance();
            int uploadId = intent.getIntExtra("uploadId", -1);

            if (uploadId < 0 || uploadId != this.uploadId) {
                return;
            }

            Log.v("POKO", "UPLOAD JOB INTENT " + this.uploadId + ", GIVEN " + uploadId);

            // Copy downloaded data to buffer
            transferManager.uploadJob(uploadId);

            Log.v("POKO", "Send every bytes");

            // Stop upload thread
            stopThread();
        }

        @Override
        protected void removeThread() {
            synchronized (ContentService.class) {
                uploadThreads.remove(getThreadId());
                Log.v("POKO", "THREAD UPLOAD ID " + uploadId + " DONE");
            }
        }
    }

    private static class LocateContentThread extends IntentJobThread {
        public LocateContentThread(ContentService service, int threadId) {
            super(service, threadId);
        }

        @Override
        protected void onIntent(Intent intent) {
            // Get content name
            final String contentName = intent.getStringExtra("contentName");

            // Get content type
            String contentType = intent.getStringExtra("contentType");

            // Get file name
            final String fileName = intent.getStringExtra("fileName");

            // Test validity of input
            if (contentName == null || contentType == null ||
                    (contentName.equals(ContentTransferManager.TYPE_BINARY) && fileName == null)) {
                return;
            }

            // Get content manager
            final ContentManager contentManager = ContentManager.getInstance();

            // Get transfer manager
            ContentTransferManager transferManager = ContentTransferManager.getInstance();

            switch(contentType) {
                // Image file load
                case ContentTransferManager.TYPE_IMAGE: {
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
                case ContentTransferManager.TYPE_BINARY: {
                    // Get job
                    final ContentManager.BinaryContentLocateJob job =
                            contentManager.getBinaryLoadJob(contentName);

                    // Job should exist
                    if (job == null) {
                        break;
                    }

                    // Create binary file
                    PokoBinaryFile file = new PokoBinaryFile();

                    // Set content name
                    file.setFileName(fileName);
                    file.setContentName(contentName);

                    // Read data
                    try {
                        // Test if the file exists
                        if (file.exists()) {
                            // Make uri for file
                            Uri uri = Uri.fromFile(new File(file.getFullFilePath()));

                            // Finish job
                            /** NOTE: Here we give null to binary argument because
                            /*       the file is already exists. So the process
                            /*       will skip file save phase.*/
                            if (processBinaryAndCallback(job, uri, null)) {
                                break;
                            }
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
                                    // Save file
                                    PokoBinaryFile file = new PokoBinaryFile();

                                    // Set file name
                                    file.setFileName(fileName);
                                    file.setContentName(contentName);

                                    try {
                                        // Make uri for file
                                        Uri uri = Uri.fromFile(new File(file.getFullFilePath()));

                                        // Finish binary file locate job
                                        if (processBinaryAndCallback(job, uri, bytes)) {
                                            // Binary processing failed
                                            contentManager.failBinaryLocateJob(contentName);
                                        }
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
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

        @Override
        protected void removeThread() {
            synchronized (ContentService.class) {
                ContentService.locateContentThread = null;
                Log.v("POKO", "LOCATE CONTENT THREAD CLOSES");
            }
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
                                                 Uri uri, byte[] binary) {
            if (uri != null) {
                // Set uri
                job.setUri(uri);

                // Set binary
                job.setBinary(binary);

                // Finish job
                job.finishJobAndStartCallbacks();

                return true;
            } else {
                return false;
            }
        }
    }

    private static class StartDownloadThread extends IntentJobThread {
        public StartDownloadThread(ContentService service, int threadId) {
            super(service, threadId);
        }

        @Override
        protected void onIntent(Intent intent) {
            int sendId = intent.getIntExtra("sendId", -1);
            int downloadId = intent.getIntExtra("downloadId", -1);
            int size = intent.getIntExtra("size", -1);
            int chunkId = intent.getIntExtra("chunkId", -1);

            if (chunkId > 0) {

            } else {
                if (sendId < 0 || downloadId < 0 || size < 0) {
                    return;
                }

                // Start receive data
                ContentTransferManager.getInstance().startDownloadJob(sendId, downloadId, size);
            }
        }

        @Override
        protected void removeThread() {
            synchronized (ContentService.class) {
                ContentService.startDownloadThread = null;
                Log.v("POKO", "START DOWNLOAD THREAD CLOSES");
            }
        }
    }

    private static class ContentStoreThread extends IntentJobThread {
        public ContentStoreThread(ContentService service, int threadId) {
            super(service, threadId);
        }

        @Override
        protected void onIntent(Intent intent) {
            // Get content name and type
            String fileName = intent.getStringExtra("fileName");
            String contentName = intent.getStringExtra("contentName");
            String contentType = intent.getStringExtra("contentType");

            // Content name and type should exist, file name should exists when binary type
            if (contentName == null || contentType == null ||
                    (contentType.equals(ContentTransferManager.TYPE_BINARY) && fileName == null)) {
                return;
            }

            // Create file and get binary data according to content type
            if (contentType.equals(ContentTransferManager.TYPE_IMAGE)) {
                // Make file
                PokoImageFile file = new PokoImageFile();

                // Get image binary
                byte[] bytes = ContentService.getImageBinary(contentName);

                // Binary data should exist
                if (bytes != null) {
                    try {
                        // Write to file
                        file.setFileName(contentName);
                        file.openWriter(false);
                        file.save(bytes);
                        file.closeWriter();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else if (contentType.equals(ContentTransferManager.TYPE_BINARY)) {
                ContentStream stream = null;

                // Make binary content file
                PokoBinaryFile file = new PokoBinaryFile();
                file.setContentName(contentName);
                file.setFileName(fileName);

                // Get file uri
                Uri uri = ContentService.getFileUri(contentName);

                try {
                    // Make content input stream
                    stream = new ContentStream(service, service.getContentResolver(), uri);

                    // Allocate byte buffer
                    byte[] buffer;

                    // Open file writer
                    file.openWriter(false);

                    // Read original file data
                    while((buffer = stream.getNextChunk()) != null) {
                        // Write to output stream
                        file.save(buffer);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    // Close stream
                    if (stream != null) {
                        stream.close();
                    }

                    // Close file
                    try {
                        file.closeWriter();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                // Invalid content type, just return
                stopThread();
                return;
            }

            // Done, stop thread
            stopThread();
        }

        @Override
        protected void removeThread() {
            synchronized (ContentService.class) {
                ContentService.contentStoreThread = null;
                Log.v("POKO", "CONTENT STORE THREAD CLOSES");
            }
        }
    }
}
