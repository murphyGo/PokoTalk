package com.murphy.pokotalk.server.content;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;

import com.murphy.pokotalk.data.content.PokoBinaryFile;
import com.murphy.pokotalk.data.content.PokoImageFile;

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

    public static final String TYPE_IMAGE = "image";
    public static final String TYPE_BINARY = "binary";

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

    public void locateImage(Context context, String contentName,
                            final ImageContentLoadCallback callback) {
        final ContentManager manager = this;
        Bitmap bitmap;

        Log.v("POKO", "LOCATE IMAGE " + contentName);
        // First, check cache.
        synchronized (manager) {
            bitmap = imageCache.get(contentName);
        }

        // Check cache hit
        if (bitmap != null) {
            Log.v("POKO", "CACHE HIT " + contentName);
            callback.onLoadImage(bitmap);
            return;
        }

        // Second, check device storage
        synchronized (manager) {
            // Check if job for same content is processing
            ImageContentLocateJob job = getImageLoadJob(contentName);

            if (job != null && job.addCallback(callback)) {
                // Job exists and added callback, done

                Log.v("POKO", "JOB EXISTS, DONE");
                return;
            }
            // Create image locate job
            ImageContentLocateJob newJob = new ImageContentLocateJob();
            newJob.setContentName(contentName);
            newJob.addCallback(callback);

            // Add to job list
            imageLocateJobs.put(contentName, newJob);
        }

        // Request service for file reading in asynchronous mode
        Intent intent = new Intent(context, ContentLoadService.class);

        // Put information
        intent.putExtra("command", ContentLoadService.CMD_LOCATE_CONTENT);
        intent.putExtra("contentName", contentName);
        intent.putExtra("contentType", TYPE_IMAGE);

        // Start service
        context.startService(intent);
    }

    public void locateFile(Context context, String contentName,
                           final BinaryContentLoadCallback callback) {
        final ContentManager manager = this;
        byte[] binary;

        // First, check cache.
        synchronized (manager) {
            binary = binaryCache.get(contentName);
        }

        // Check cache hit
        if (binary != null) {
            callback.onLoadBinary(binary);
            return;
        }

        // Second, check device storage
        synchronized (manager) {
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
        Intent intent = new Intent(context, ContentLoadService.class);

        // Put information
        intent.putExtra("command", ContentLoadService.CMD_LOCATE_CONTENT);
        intent.putExtra("contentName", contentName);
        intent.putExtra("contentType", TYPE_BINARY);

        // Start service
        context.startService(intent);
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


    public interface ContentLoadCallback {
        void onError();
    }

    public static abstract class ImageContentLoadCallback
            implements ContentLoadCallback {
        public abstract void onLoadImage(Bitmap image);
    }

    public static abstract class BinaryContentLoadCallback
            implements ContentLoadCallback {
        public abstract void onLoadBinary(byte[] bytes);
    }

    public abstract class ContentLocateJob {
        protected String contentName;
        protected byte[] binary;
        protected List<ContentLoadCallback> callbacks = new ArrayList<>();
        protected boolean finished = false;
        protected boolean isError = false;

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

            // Store content as file
            saveContent();

            // Add content to cache of content manager
            addToCache(contentManager);

            // Remove job from job list
            removeJob(contentManager);

            synchronized (this) {
                // Finish job
                finished = true;

                try {
                    // Start all callback function
                    for (int i = 0; i < callbacks.size(); i++) {
                        startCallback(callbacks.get(i));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public void failJobAndStartCallbacks() {
            // Get content manager
            ContentManager contentManager = ContentManager.getInstance();

            // Set error
            setError(true);

            // Remove job from job list
            removeJob(contentManager);

            synchronized (this) {
                // Finish job
                finished = true;

                try {
                    // Start all callback function
                    for (int i = 0; i < callbacks.size(); i++) {
                        startCallback(callbacks.get(i));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public byte[] getBinary() {
            return binary;
        }

        public void setBinary(byte[] binary) {
            this.binary = binary;
        }

        public boolean isError() {
            return isError;
        }

        public void setError(boolean error) {
            isError = error;
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
                if (isError()) {
                    imageCallback.onError();
                } else {
                    imageCallback.onLoadImage(image);
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
                if (isError()) {
                    binaryCallback.onError();
                } else {
                    binaryCallback.onLoadBinary(binary);
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
