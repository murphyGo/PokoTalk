package com.murphy.pokotalk.server.content;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

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

        // First, check cache.
        synchronized (manager) {
            bitmap = imageCache.get(contentName);
        }

        // Check cache hit
        if (bitmap != null) {
            callback.onLoadImage(bitmap);
        }

        // Second, check device storage
        synchronized (manager) {
            // Check if job for same content is processing
            ImageContentLocateJob job = getImageLoadJob(contentName);

            if (job != null && job.addCallback(callback)) {
                // Job exists and added callback, done
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

    public interface ContentLoadCallback {

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
        private String contentName;
        private List<ContentLoadCallback> callbacks = new ArrayList<>();
        private boolean finished = false;

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

        protected abstract void startCallback(ContentLoadCallback callback);
        protected abstract void removeJob(ContentManager contentManager);
        protected abstract void addToCache(ContentManager contentManager);
    }

    public class ImageContentLocateJob extends ContentLocateJob {
        private Bitmap image;

        @Override
        protected void startCallback(ContentLoadCallback callback) {
            if (callback instanceof ImageContentLoadCallback) {
                ((ImageContentLoadCallback) callback).onLoadImage(image);
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

        public Bitmap getImage() {
            return image;
        }

        public void setImage(Bitmap image) {
            this.image = image;
        }
    }

    public class BinaryContentLocateJob extends ContentLocateJob {
        private byte[] binary;

        @Override
        protected void startCallback(ContentLoadCallback callback) {
            if (callback instanceof BinaryContentLoadCallback) {
                ((BinaryContentLoadCallback) callback).onLoadBinary(binary);
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

        public byte[] getBinary() {
            return binary;
        }

        public void setBinary(byte[] binary) {
            this.binary = binary;
        }
    }
}
