package com.murphy.pokotalk.server.content;

import android.util.Log;
import android.util.SparseArray;

import com.murphy.pokotalk.data.PokoLock;
import com.murphy.pokotalk.server.PokoServer;

import java.util.ArrayDeque;
import java.util.Queue;

public class ContentTransferManager {
    private static ContentTransferManager instance = null;
    private SparseArray<UploadJob> pendingUploadJobs;
    private SparseArray<DownloadJob> pendingDownloadJobs;
    private SparseArray<UploadJob> uploadJobs;
    private SparseArray<DownloadJob> downloadJobs;

    public static final String TYPE_IMAGE = "image";
    public static final String TYPE_BINARY = "binary";

    public static final int DOWNLOAD_START_WAIT = 10000;

    private int pendingUploadJobId = 1;
    private int pendingDownloadJobId = 1;

    private ContentTransferManager() {
        pendingUploadJobs = new SparseArray<>();
        pendingDownloadJobs = new SparseArray<>();
        uploadJobs = new SparseArray<>();
        downloadJobs = new SparseArray<>();
    }

    public static ContentTransferManager getInstance() {
        if (instance == null) {
            synchronized (ContentTransferManager.class) {
                instance = instance == null ? new ContentTransferManager() : instance;
            }
        }

        return instance;
    }

    public synchronized int addUploadJob(byte[] binary, String extension,
                                         UploadJobCallback callback) {
        // Make temporary id
        int pendingId = pendingUploadJobId++;

        // Create pending job
        UploadJob job = new UploadJob();
        job.setId(pendingId);
        job.setBinary(binary);
        job.setSize((long) binary.length);
        job.setCallback(callback);
        job.setExtension(extension);

        // Add to pending list
        pendingUploadJobs.put(pendingId, job);

        return pendingId;
    }

    public synchronized void startUploadJob(int pendingId, int uploadId) {
        // Get pending job
        UploadJob job = pendingUploadJobs.get(pendingId);

        if (job != null) {
            // Remove pending job
            pendingUploadJobs.remove(pendingId);

            // Update job id to upload id
            job.setId(uploadId);

            // Add to active jobs
            uploadJobs.put(uploadId, job);

            // Get server
            PokoServer server = PokoServer.getInstance();

            // Send start upload
            server.sendStartUpload(job.getId(), job.getBinary().length, job.getExtension());
        }
    }

    public void uploadJob(int uploadId, String contentName) {
        UploadJob job;

        synchronized (this) {
            // Get job
            job = uploadJobs.get(uploadId);

            // Set content name
            job.setContentName(contentName);
        }

        // Get server
        PokoServer server = PokoServer.getInstance();

        // Send data
        server.sendUpload(job.getId(), job.getBinary());
    }

    public void uploadAck(int jobId, long ack) {
        UploadJob job;

        synchronized (this) {
            // Get job
            job = uploadJobs.get(jobId);
        }

        // Update ack
        if (job.getAck() < ack) {
            job.setAck(ack);
        }

        // Check if upload is done
        if (job.getSize() <= job.getAck()) {
            UploadJobCallback callback = job.getCallback();

            // Call callback
            if (callback != null) {
                callback.onSuccess(job.getContentName());
            }
        }
    }

    public synchronized void addDownloadJob(String contentName,
                                            String type, DownloadJobCallback callback) {
        // Make pending job id
        int pendingId = pendingDownloadJobId++;

        // Create download job
        DownloadJob job = new DownloadJob();
        job.setId(pendingId);
        job.setContentName(contentName);
        job.setType(type);
        job.setCallback(callback);

        // Add to download jobs
        pendingDownloadJobs.put(pendingId, job);

        // Get server
        PokoServer server = PokoServer.getInstance();

        // Start download
        server.sendStartDownload(contentName, type, pendingId);
    }

    public synchronized void startDownloadJob(int sendId, int downloadId, int size) {
        // Get pending download job
        DownloadJob job = pendingDownloadJobs.get(sendId);
        Log.v("POKO", "START DOWNLOAD JOB");
        if (job != null) {
            synchronized (job) {
                // Update id to download id
                job.setId(downloadId);
                Log.v("POKO", "CHANGE ID FROM  "+ sendId + " TO " + downloadId);
                // Set size
                job.setSize(size);

                // Set bytes left
                job.setLeft(size);

                // Allocate buffer
                job.setBytes(new byte[size]);

                // Set started
                job.setStarted(true);

                // Awake waiting threads to write in buffer
                job.notifyAll();
            }

            // Add to download jobs
            downloadJobs.put(downloadId, job);
        }
    }

    public void downloadJob(int downloadId, byte[] part) {
        DownloadJob job;
        synchronized (this) {
            // Get download job
             job = downloadJobs.get(downloadId);
        }

        if (job != null) {
            Log.v("POKO", "DOWNLOAD JOB");
            int left = job.getLeft();
            int start = job.getSize() - left;
            int validSize = left < part.length ? left : part.length;

            left -= validSize;

            Log.v("POKO", "LEFT " + left + "BYTES");

            // Set left
            job.setLeft(left);

            // Get buffer
            byte[] buffer = job.getBytes();

            // Copy data to buffer
            for (int i = 0; i < validSize; i++) {
                buffer[start + i] = part[i];
            }

            // Check if download is done
            if (left == 0) {
                Log.v("POKO", "DOWNLOAD DONE");
                synchronized (this) {
                    // Remove job
                    downloadJobs.remove(job.getId());
                }

                // Get callback
                DownloadJobCallback callback = job.getCallback();

                // Call callback
                if (callback != null) {
                    callback.onSuccess(job.getContentName(), buffer);
                    Log.v("POKO", "DOWNLOAD CALLBACK");
                }
            }
        }
    }

    public void putBytesToJobQueue(int downloadId, byte[] part) {
        DownloadJob downloadJob;

        synchronized (this) {
            downloadJob = downloadJobs.get(downloadId);
        }

        if (downloadJob != null) {
            downloadJob.putBytesToQueue(part);
        }
    }

    public void writeBytesFromJobQueue(int downloadId) {
        DownloadJob downloadJob;

        synchronized (this) {
            downloadJob = downloadJobs.get(downloadId);
        }

        if (downloadJob != null) {
            synchronized (downloadJob) {
                // Check if the job is started
                if (!downloadJob.isStarted()) {
                    try {
                        // Wait until the job is started
                        downloadJob.wait(DOWNLOAD_START_WAIT);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                    if (!downloadJob.isStarted()) {
                        // Timeout, the job failed...
                        //TODO: clear job and call error callback
                        return;
                    }
                }
            }

            while(true) {
                try {
                    // Only one thread can copy for this job at a time
                    downloadJob.getCopyLock().acquireWriteLock();

                    break;
                } catch (InterruptedException e) {
                    // Interrupted, go back and wait again
                    continue;
                }
            }

            try {
                // Get byte package queue
                Queue<byte[]> queue = downloadJob.getBytesQueue();

                byte[] part;

                while (true) {
                    // Get a byte package from queue
                    synchronized (queue) {
                        part = queue.poll();
                    }

                    // No more byte package, done
                    if (part == null) {
                        break;
                    }

                    // Copy bytes to job buffer
                    copyByteToJobBuffer(downloadJob, part);
                }
            } finally {
                downloadJob.getCopyLock().releaseWriteLock();
            }
        }
    }

    protected void copyByteToJobBuffer(DownloadJob job, byte[] part) {
        int left = job.getLeft();
        int start = job.getSize() - left;
        int validSize = left < part.length ? left : part.length;

        left -= validSize;

        Log.v("POKO", "LEFT " + left + "BYTES");

        // Set left
        job.setLeft(left);

        // Get buffer
        byte[] buffer = job.getBytes();

        // Copy data to buffer
        for (int i = 0; i < validSize; i++) {
            buffer[start + i] = part[i];
        }

        // Check if download is done
        if (left == 0) {
            Log.v("POKO", "DOWNLOAD DONE");
            synchronized (this) {
                // Remove job
                downloadJobs.remove(job.getId());
            }

            // Get callback
            DownloadJobCallback callback = job.getCallback();

            // Call callback
            if (callback != null) {
                callback.onSuccess(job.getContentName(), buffer);
                Log.v("POKO", "DOWNLOAD CALLBACK");
            }
        }
    }

    public void failUploadJob(int jobId, boolean pending) {
        UploadJob job;

        Log.v("POKO", "FAILED UPLOAD");
        synchronized (this) {
            // Get job
            if (pending) {
                job = pendingUploadJobs.get(jobId);
            } else {
                job = uploadJobs.get(jobId);
            }

            // Check if job exists
            if (job != null) {
                // Remove job
                if (pending) {
                    pendingUploadJobs.remove(jobId);
                } else {
                    uploadJobs.remove(jobId);
                }
            }
        }

        if (job != null) {
            // Get job callback
            UploadJobCallback callback = job.getCallback();

            // Call callback
            if (callback != null) {
                callback.onError();
            }
        }
    }

    public void failDownloadJob(int jobId, boolean pending) {
        DownloadJob job;

        Log.v("POKO", "FAILED DOWNLOAD");
        synchronized (this) {
            // Get job
            if (pending) {
                job = pendingDownloadJobs.get(jobId);
            } else {
                job = downloadJobs.get(jobId);
            }

            // Check if job exists
            if (job != null) {
                // Remove job
                if (pending) {
                    pendingDownloadJobs.remove(jobId);
                } else {
                    downloadJobs.remove(jobId);
                }
            }
        }

        if (job != null) {
            // Get job callback
            DownloadJobCallback callback = job.getCallback();

            // Call callback
            if (callback != null) {
                callback.onError();
            }
        }
    }

    private class UploadJob {
        private Integer id;
        private String contentName;
        private String extension;
        private byte[] binary;
        private Long size;
        private Long ack;
        private UploadJobCallback callback;

        public UploadJob() {
            ack = 0L;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public byte[] getBinary() {
            return binary;
        }

        public void setBinary(byte[] binary) {
            this.binary = binary;
        }

        public Long getSize() {
            return size;
        }

        public void setSize(Long size) {
            this.size = size;
        }

        public UploadJobCallback getCallback() {
            return callback;
        }

        public void setCallback(UploadJobCallback callback) {
            this.callback = callback;
        }

        public String getExtension() {
            return extension;
        }

        public void setExtension(String extension) {
            this.extension = extension;
        }

        public Long getAck() {
            return ack;
        }

        public void setAck(Long ack) {
            this.ack = ack;
        }

        public String getContentName() {
            return contentName;
        }

        public void setContentName(String contentName) {
            this.contentName = contentName;
        }
    }

    private class DownloadJob {
        private int id;
        private String contentName;
        private String type;
        private byte[] bytes;
        private int size;
        private int left;
        private DownloadJobCallback callback;
        private Queue<byte[]> pendingBuffers;
        private PokoLock copyLock;
        private boolean started;

        public DownloadJob() {
            pendingBuffers = new ArrayDeque<>();
            copyLock = new PokoLock();
            started = false;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getContentName() {
            return contentName;
        }

        public void setContentName(String contentName) {
            this.contentName = contentName;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public byte[] getBytes() {
            return bytes;
        }

        public void setBytes(byte[] bytes) {
            this.bytes = bytes;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public int getLeft() {
            return left;
        }

        public void setLeft(int left) {
            this.left = left;
        }

        public DownloadJobCallback getCallback() {
            return callback;
        }

        public void setCallback(DownloadJobCallback callback) {
            this.callback = callback;
        }

        public synchronized void putBytesToQueue(byte[] part) {
            pendingBuffers.add(part);
        }

        public synchronized Queue<byte[]> getBytesQueue() {
            return pendingBuffers;
        }

        public PokoLock getCopyLock() {
            return copyLock;
        }

        public boolean isStarted() {
            return started;
        }

        public void setStarted(boolean started) {
            this.started = started;
        }
    }

    public static abstract class UploadJobCallback {
        public abstract void onSuccess(String contentName);
        public abstract void onError();
    }

    public static abstract class DownloadJobCallback {
        public abstract void onSuccess(String contentName, byte[] bytes);
        public abstract void onError();
    }
}
