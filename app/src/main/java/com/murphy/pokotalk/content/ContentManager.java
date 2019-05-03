package com.murphy.pokotalk.content;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.murphy.pokotalk.data.content.PokoBinaryFile;
import com.murphy.pokotalk.data.content.PokoImageFile;
import com.murphy.pokotalk.service.ContentService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/** Content manager is responsible for locating contents like images, files,
 * and returning it to user. Contents can be located by cache, reading local storage or
 * downloading from server.
 * Locating method priority
 * 1. Cache
 * 2. Local storage
 * 3. Download from server
 */
public class ContentManager {
    private static ContentManager instance = null;
    private HashMap<String, Bitmap> imageCache;
    private HashMap<String, byte[]> binaryCache;
    private HashMap<String, ImageContentLocateJob> imageLocateJobs;
    private HashMap<String, BinaryContentLocateJob> binaryLocateJobs;

    public static final String EXT_JPG = "jpg";

    public static final int CACHE_MAX = 32;

    public static ContentManager getInstance() {
        if (instance == null) {
            synchronized (ContentManager.class) {
                instance = instance == null ? new ContentManager() : instance;
            }
        }

        return instance;
    }

    public static void clear() {
        synchronized (ContentManager.class) {
            instance = null;
        }
    }

    public ContentManager() {
        imageCache = new HashMap<>();
        binaryCache = new HashMap<>();
        imageLocateJobs = new HashMap<>();
        binaryLocateJobs = new HashMap<>();
    }

    public static String getExtension(String contentName) {
        int dotIndex = contentName.lastIndexOf('.');

        if (dotIndex < 1) {
            return null;
        } else {
            return contentName.substring(dotIndex + 1);
        }
    }

    public void locateThumbnailImage(Context context, String contentName,
                                     final ImageContentLoadCallback callback) {
        // Get last dot character index
        int dotIndex = contentName.lastIndexOf('.');

        if (dotIndex < 1) {
            // Invalid content name, fail job
            callback.onError();
        } else {
            // Get image name and extension
            String ext = contentName.substring(dotIndex + 1);
            String name = contentName.substring(0, dotIndex);

            // Make thumbnail image name
            String thumbnailName = name + "_thumbnail." + ext;

            // Locate thumbnail image
            locateImage(context, thumbnailName, callback);
        }
    }

    public void locateImage(Context context, String contentName,
                            final ImageContentLoadCallback callback) {
        Bitmap bitmap;

        Log.v("POKO", "LOCATE TYPE_IMAGE " + contentName);

        // First, check cache.
        synchronized (this) {
            bitmap = imageCache.get(contentName);
        }

        // Check cache hit
        if (bitmap != null) {
            callback.onLoadImage(bitmap);
            return;
        }

        ImageContentLocateJob job;

        // Second, check device storage
        synchronized (this) {
            // Check if job for same content is processing
            job = getImageLoadJob(contentName);

            if (job != null && job.addCallback(callback)) {
                // Job exists and added callback, done
                return;
            }
            // Create image locate job
            job = new ImageContentLocateJob();
            job.setContentName(contentName);
            job.addCallback(callback);

            // Add to job list
            imageLocateJobs.put(contentName, job);
        }

        // Request service for file reading in asynchronous mode
        Intent intent = new Intent(context, ContentService.class);

        // Put information
        intent.putExtra("command", ContentService.CMD_LOCATE_CONTENT);
        intent.putExtra("contentName", contentName);
        intent.putExtra("contentType", ContentTransferManager.TYPE_IMAGE);

        // Start service
        context.startService(intent);
    }

    public void locateBinary(Context context, String contentName,
                           final BinaryContentLoadCallback callback) {
        byte[] binary;

        Log.v("POKO", "LOCATE TYPE_BINARY " + contentName);

        // First, check cache.
        synchronized (this) {
            binary = binaryCache.get(contentName);
        }

        // Check cache hit
        if (binary != null) {
            callback.onLoadBinary(binary);
            return;
        }

        // Second, check device storage
        synchronized (this) {
            // Check if job for same content is processing
            BinaryContentLocateJob job = getBinaryLoadJob(contentName);

            if (job != null && job.addCallback(callback)) {
                // Job exists and added callback, done
                return;
            }

            // Create image locate job
            BinaryContentLocateJob newJob = new BinaryContentLocateJob();
            newJob.setContentName(contentName);
            newJob.addCallback(callback);

            // Add to job list
            binaryLocateJobs.put(contentName, newJob);
        }

        // Request service for file reading in asynchronous mode
        Intent intent = new Intent(context, ContentService.class);

        // Put information
        intent.putExtra("command", ContentService.CMD_LOCATE_CONTENT);
        intent.putExtra("contentName", contentName);
        intent.putExtra("contentType", ContentTransferManager.TYPE_BINARY);

        // Start service
        context.startService(intent);
    }

    public Bitmap locateImageFromCache(String contentName) {
        // Read image from cache
        synchronized (this) {
            return imageCache.get(contentName);
        }
    }

    public byte[] locateBinaryFromCache(String contentName) {
        // Read image from cache
        synchronized (this) {
            return binaryCache.get(contentName);
        }
    }

    // Puts image content to cache
    public void putImageContentInCache(String contentName, Bitmap bitmap) {
        synchronized (this) {
            checkCacheSize(imageCache);

            imageCache.put(contentName, bitmap);
        }
    }

    // Puts binary content to cache
    public void putBinaryContentInCache(String contentName, byte[] binary) {
        synchronized (this) {
            checkCacheSize(binaryCache);

            binaryCache.put(contentName, binary);
        }
    }

    protected void checkCacheSize(HashMap cache) {
        // Check if cache is too big
        if (cache.size() > CACHE_MAX) {
            // Remove half of cache data
            ArrayList<String> keys = new ArrayList<>(cache.keySet());

            // Remove cache data
            for (int i = 0; i < keys.size() / 2; i++) {
                cache.remove(keys.get(i));
            }
        }
    }

    public ImageContentLocateJob getImageLoadJob(String contentName) {
        synchronized (this) {
            return imageLocateJobs.get(contentName);
        }
    }

    public BinaryContentLocateJob getBinaryLoadJob(String contentName) {
        synchronized (this) {
            return binaryLocateJobs.get(contentName);
        }
    }

    public void removeImageJob(String contentName) {
        synchronized (this) {
            imageLocateJobs.remove(contentName);
        }
    }

    public void removeBinaryJob(String contentName) {
        synchronized (this) {
            binaryLocateJobs.remove(contentName);
        }
    }

    public void failImageLocateJob(String contentName) {
        ImageContentLocateJob job;

        synchronized (this) {
            job = imageLocateJobs.get(contentName);
        }

        if (job != null) {
            job.failJobAndStartCallbacks();
        }
    }

    public void failBinaryLocateJob(String contentName) {
        BinaryContentLocateJob job;

        synchronized (this) {
            job = binaryLocateJobs.get(contentName);
        }

        if (job != null) {
            job.failJobAndStartCallbacks();
        }
    }


    public static abstract class ContentLoadCallback {
        private Handler handler;
        private Runnable errorRunnable;
        private Runnable successRunnable;
        private boolean canceled = false;
        private boolean started = false;

        private ContentLoadCallback() {
            final ContentLoadCallback callback = this;

            // Callback runs in main UI thread
            handler = new Handler(Looper.getMainLooper());

            // Make error runnable to post to handler
            errorRunnable = new Runnable() {
                @Override
                public void run() {
                    // Remove runnable
                    synchronized (callback) {
                        errorRunnable = null;
                    }

                    onError();
                }
            };

            // Make success runnable to post to handler
            successRunnable = new Runnable() {
                @Override
                public void run() {
                    // Remove runnable
                    synchronized (callback) {
                        successRunnable = null;
                    }

                    onSuccess();
                }
            };
        }

        public abstract void onError();
        abstract void onSuccess();

        // Starts callback, if isSuccess, success callback is called.
        // If not, error callback is called.
        // This methods does nothing if the callback is canceled.
        protected synchronized void run(boolean isSuccess) {
            // Check if the callback is canceled
            if (canceled || started) {
                return;
            }

            // Mark started so that callback does not get called twice
            started = true;

            if (isSuccess) {
                // Post success callback
                handler.post(successRunnable);
            } else {
                // Post error callback
                handler.post(errorRunnable);
            }
        }

        // Cancels callback to run if this callback has not started yet
        public synchronized void cancel() {
            // Test if the callback is already canceled
            if (canceled) {
                return;
            }

            // Set canceled true
            canceled = true;

            if (successRunnable != null) {
                // Remove callback if posted
                handler.removeCallbacks(successRunnable);

                // Remove callback
                successRunnable = null;
            }

            if (errorRunnable != null) {
                // Remove callback if posted
                handler.removeCallbacks(errorRunnable);

                // Remove callback
                errorRunnable = null;
            }
        }
    }

    public static abstract class ImageContentLoadCallback
            extends ContentLoadCallback {
        private Bitmap image;

        private void setImage(Bitmap image) {
            this.image = image;
        }

        @Override
        void onSuccess() {
            onLoadImage(image);
        }

        public abstract void onLoadImage(Bitmap image);
    }

    public static abstract class BinaryContentLoadCallback
            extends ContentLoadCallback {
        private byte[] bytes;

        private void setBytes(byte[] bytes) {
            this.bytes = bytes;
        }

        @Override
        void onSuccess() {
            onLoadBinary(bytes);
        }

        public abstract void onLoadBinary(byte[] bytes);
    }

    public abstract class ContentLocateJob {
        protected String contentName;
        protected byte[] binary;
        protected List<ContentLoadCallback> callbacks = new ArrayList<>();
        protected boolean finished = false;
        protected boolean isSuccess = false;

        public String getContentName() {
            return contentName;
        }

        public void setContentName(String contentName) {
            this.contentName = contentName;
        }

        public boolean addCallback(ContentLoadCallback callback) {
            synchronized (this) {
                // Check if the job finished
                if (finished) {
                    return false;
                }

                // Add callback
                callbacks.add(callback);

                return true;
            }
        }

        // Finishes job and start callbacks.
        public void finishJobAndStartCallbacks() {
            // Get content manager
            ContentManager contentManager = ContentManager.getInstance();

            // Set success
            setSuccess(true);

            // Store content as file
            saveContent();

            // Add content to cache of content manager
            addToCache(contentManager);

            // Remove job from job list
            removeJob(contentManager);

            Log.v("POKO", "LOCATING " + contentName + " DONE");

            synchronized (this) {
                // Finish job
                finished = true;

                // Start all callback function
                for (int i = 0; i < callbacks.size(); i++) {
                    try {
                        startCallback(callbacks.get(i));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        public void failJobAndStartCallbacks() {
            // Get content manager
            ContentManager contentManager = ContentManager.getInstance();

            // Set error
            setSuccess(false);

            // Remove job from job list
            removeJob(contentManager);

            Log.v("POKO", "LOCATING " + contentName + " FAILED");

            synchronized (this) {
                // Finish job
                finished = true;

                // Start all callback function
                for (int i = 0; i < callbacks.size(); i++) {
                    try {
                        startCallback(callbacks.get(i));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        public byte[] getBinary() {
            return binary;
        }

        public void setBinary(byte[] binary) {
            this.binary = binary;
        }

        public boolean isSuccess() {
            return isSuccess;
        }

        public void setSuccess(boolean success) {
            isSuccess = success;
        }

        protected abstract void startCallback(ContentLoadCallback callback);
        protected abstract void removeJob(ContentManager contentManager);
        protected abstract void addToCache(ContentManager contentManager);
        protected abstract void saveContent();
    }

    public class ImageContentLocateJob extends ContentLocateJob {
        private Bitmap image;

        @Override
        protected void startCallback(ContentLoadCallback callback) {
            if (callback instanceof ImageContentLoadCallback) {
                ImageContentLoadCallback imageCallback =
                        (ImageContentLoadCallback) callback;
                if (isSuccess()) {
                    imageCallback.setImage(image);
                    imageCallback.run(true);
                } else {
                    imageCallback.run(false);
                }
            }
        }

        @Override
        protected void removeJob(ContentManager contentManager) {
            contentManager.removeImageJob(getContentName());
        }

        @Override
        protected void addToCache(ContentManager contentManager) {
            contentManager.putImageContentInCache(getContentName(), getImage());
        }

        @Override
        protected void saveContent() {
            if (binary == null) {
                return;
            }

            // Create image file
            PokoImageFile file = new PokoImageFile();

            try {
                // Write to file
                file.setFileName(getContentName());
                file.openWriter(false);
                file.save(binary);
                file.closeWriter();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public Bitmap getImage() {
            return image;
        }

        public void setImage(Bitmap image) {
            this.image = image;
        }
    }

    public class BinaryContentLocateJob extends ContentLocateJob {
        @Override
        protected void startCallback(ContentLoadCallback callback) {
            if (callback instanceof BinaryContentLoadCallback) {
                BinaryContentLoadCallback binaryCallback =
                        (BinaryContentLoadCallback) callback;
                if (isSuccess()) {
                    binaryCallback.setBytes(binary);
                    binaryCallback.run(true);

                } else {
                    binaryCallback.run(false);
                }
            }
        }

        @Override
        protected void removeJob(ContentManager contentManager) {
            contentManager.removeBinaryJob(getContentName());
        }

        @Override
        protected void addToCache(ContentManager contentManager) {
            contentManager.putBinaryContentInCache(getContentName(), getBinary());
        }

        @Override
        protected void saveContent() {
            if (binary == null) {
                return;
            }

            // Create image file
            PokoBinaryFile file = new PokoBinaryFile();

            try {
                // Write to file
                file.setFileName(getContentName());
                file.openWriter(false);
                file.save(binary);
                file.closeWriter();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
