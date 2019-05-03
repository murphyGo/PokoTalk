package com.murphy.pokotalk.content;

import android.util.Log;
import android.util.SparseArray;

import com.murphy.pokotalk.data.PokoLock;
import com.murphy.pokotalk.server.PokoServer;

import java.io.IOException;
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
        // Make job
        UploadJob job = addUploadJob(UploadJob.ALL_IN_ONE_MODE, extension, callback);

        // Set binary buffer and size
        job.setBinary(binary);
        job.setSize((long) binary.length);

        return job.getId();
    }

    public synchronized int addUploadJob(ContentStream contentStream, String extension,
                                         UploadJobCallback callback) {
        // Make job
        UploadJob job = addUploadJob(UploadJob.STREAM_MODE, extension, callback);

        // Set content stream
        job.setContentStream(contentStream);

        // Set file size
        job.setSize((long) contentStream.getSize());

        return job.getId();
    }

    private synchronized UploadJob addUploadJob(int mode, String extension,
                                                UploadJobCallback callback) {
        // Make temporary id
        int pendingId = pendingUploadJobId++;

        // Create pending job
        UploadJob job = new UploadJob(mode);
        job.setId(pendingId);
        job.setExtension(extension);
        job.setCallback(callback);

        // Add to pending list
        pendingUploadJobs.put(pendingId, job);

        return job;
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

            // Get mode
            int mode = job.getMode();

            // Send start upload properly according to mode
            if (mode == UploadJob.ALL_IN_ONE_MODE) {
                server.sendStartUpload(job.getId(), job.getBinary().length, job.getExtension());
            } else if (mode == UploadJob.STREAM_MODE) {
                server.sendStartUpload(job.getId(), job.getContentStream().getSize(), job.getExtension());
            }
        }
    }

    public void setUploadJobContentName(int uploadId, String contentName) {
        UploadJob job;

        synchronized (this) {
            // Get job
            job = uploadJobs.get(uploadId);

            if (job == null) {
                return;
            }

            // Set content name
            job.setContentName(contentName);
        }
    }

    public void uploadJob(int uploadId) {
        UploadJob job;

        synchronized (this) {
            // Get job
            job = uploadJobs.get(uploadId);

            if (job == null) {
                return;
            }
        }

        // Get server
        PokoServer server = PokoServer.getInstance();

        if (job.getMode() == UploadJob.ALL_IN_ONE_MODE) {
            // Send all data and finish
            server.sendUpload(job.getId(), job.getBinary());
        } else if (job.getMode() == UploadJob.STREAM_MODE) {
            // Get stream
            ContentStream stream = job.getContentStream();

            byte[] chunk;

            try {
                // Read chunk data till end of file
                while ((chunk = stream.getNextChunk()) != null) {
                    // Send chunk
                    server.sendUpload(job.getId(), chunk);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                stream.close();
            }
        }
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

    public void startDownloadJob(int sendId, int downloadId, int size) {
        DownloadJob job;

        synchronized (this) {
            // Get pending download job
            job = pendingDownloadJobs.get(sendId);
        }

        if (job != null) {
            synchronized (job) {
                // Update id to download id
                job.setId(downloadId);

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

            synchronized (this) {
                // Remove from pending download job
                pendingDownloadJobs.remove(sendId);

                // Add to download jobs
                downloadJobs.put(downloadId, job);
            }
        }
    }

    public void putBytesToJobQueue(int downloadId, int sendId, byte[] part) {
        DownloadJob downloadJob;

        synchronized (this) {
            // Get job from download jobs
            downloadJob = downloadJobs.get(downloadId);

            if (downloadJob == null) {
                // Get job from pending jobs
                downloadJob = pendingDownloadJobs.get(sendId);
            }
        }

        if (downloadJob != null) {
            // Put bytes to download Job
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
                // Check if the job failed
                if (downloadJob.isFinished()) {
                    return;
                }

                // Check if the job is started
                if (!downloadJob.isStarted()) {
                    try {
                        // Wait until the job is started
                        downloadJob.wait(DOWNLOAD_START_WAIT);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                    if (!downloadJob.isStarted()) {
                        // Timeout, get rid of the job
                        synchronized (this) {
                            // Remove job
                            downloadJobs.remove(downloadJob.getId());
                        }

                        // Mark the job failed and start callback
                        downloadJob.markJobFailedAndStartCallback();

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

            // Mark the job succeeded and start callback
            job.markJobSucceededAndStartCallback();
        }
    }

    public boolean hasDownloadEnded(int downloadId) {
        DownloadJob downloadJob;

        synchronized (this) {
            downloadJob = downloadJobs.get(downloadId);
        }

        if (downloadJob == null) {
            return true;
        }

        if (downloadJob.isFinished()) {
            return true;
        }

        return false;
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
            // Fail the job and start callback
            job.markJobFailedAndStartCallback();
        }
    }

    private class UploadJob {
        private Integer id;
        private String contentName;
        private String extension;
        private int mode;
        private ContentStream contentStream;
        private byte[] binary;
        private Long size;
        private Long ack;
        private UploadJobCallback callback;

        public static final int ALL_IN_ONE_MODE = 0;
        public static final int STREAM_MODE = 1;

        public UploadJob(int mode) {
            ack = 0L;

            this.mode = mode;
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

        public ContentStream getContentStream() {
            return contentStream;
        }

        public void setContentStream(ContentStream contentStream) {
            this.contentStream = contentStream;
        }

        public int getMode() {
            return mode;
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
        private boolean finished;
        private boolean success;

        public DownloadJob() {
            pendingBuffers = new ArrayDeque<>();
            copyLock = new PokoLock();
            started = false;
            finished = false;
            success = false;
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

        public boolean isFinished() {
            return finished;
        }

        public boolean isSucceeded() {
            return !success;
        }

        public boolean isFailed() {
            return !success;
        }

        public void markJobSucceededAndStartCallback() {
            if (finished) {
                return;
            }

            synchronized (this) {
                if (finished) {
                    return;
                }

                finished = true;
                success = true;
            }

            if (callback != null) {
                callback.onSuccess(contentName, bytes);
            }
        }

        public void markJobFailedAndStartCallback() {
            if (finished) {
                return;
            }

            synchronized (this) {
                if (finished) {
                    return;
                }

                finished = true;
                success = false;
            }

            if (callback != null) {
                callback.onError();
            }
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
