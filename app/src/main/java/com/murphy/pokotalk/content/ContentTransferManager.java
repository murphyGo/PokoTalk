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
    public static final int UPLOAD_WINDOW = 4 * 1024 * 1024;
    public static final int UPLOAD_ACK_TIMEOUT = 10000;

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
            // Indicating if loop is broken by end of file
            boolean eof = false;

            // Get stream
            ContentStream stream = job.getContentStream();

            // Send size
            long send = job.getSend();

            // Server ack
            long ack = job.getAck();

            // Chunk of data to send
            byte[] chunk = null;

            try {
                while (true) {
                    while (send < ack + ContentTransferManager.UPLOAD_WINDOW) {
                        if (chunk == null) {
                            // Read chunk
                            chunk = stream.getNextChunk();
                        }

                        // Check if we read all data
                        if (chunk == null) {
                            eof = true;
                            break;
                        }

                        // Send chunk
                        server.sendUpload(job.getId(), chunk);
                        send += chunk.length;
                        Log.v("Poko", "SEND " + send + " Bytes (" + stream.getSize() +")");

                        // Set chunk null
                        chunk = null;
                    }

                    // Test if the break is caused by sent all data
                    if (eof) {
                        break;
                    }

                    // Read next chunk early for performance
                    chunk = stream.getNextChunk();

                    // Test eof
                    if (chunk == null) {
                        break;
                    }

                    synchronized (job) {
                        // Get server ack
                        ack = job.getAck();

                        if (send >= ack + ContentTransferManager.UPLOAD_WINDOW) {
                            while (true) {
                                try {
                                    // Update send
                                    job.setSend(send);

                                    // Wait until server ack comes
                                    job.wait(UPLOAD_ACK_TIMEOUT);

                                    // Get out of loop
                                    break;
                                } catch (InterruptedException e) {
                                    // Interrupted, wait again
                                    Thread.currentThread().interrupt();
                                }
                            }

                            // Get server ack
                            ack = job.getAck();

                            if (send >= ack + ContentTransferManager.UPLOAD_WINDOW) {
                                // Timeout, fail job
                                // TODO: Fail the job
                                break;
                            }
                        }
                    }
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

        synchronized (job) {
            // Update ack
            if (job.getAck() < ack) {
                job.setAck(ack);
            }
        }

        // Check if upload is done
        if (job.getSize() <= job.getAck()) {
            UploadJobCallback callback = job.getCallback();

            // Call callback
            if (callback != null) {
                callback.onSuccess(job.getContentName());
            }
        } else {
            synchronized (job) {
                // Check if we can send more
                if (job.getSend() < ack + ContentTransferManager.UPLOAD_WINDOW) {
                    // Notify waiting thread
                    job.notifyAll();
                }
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

            if (downloadJob != null) {
                // Put bytes to download Job
                downloadJob.putBytesToQueue(part);
            }
        }
    }

    public void writeBytesFromJobQueue(int downloadId) {
        DownloadJob downloadJob;

        // Get server
        PokoServer server = PokoServer.getInstance();

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
                    synchronized (downloadJob) {
                        part = queue.poll();
                    }

                    // No more byte package, done
                    if (part == null) {
                        break;
                    }

                    // Copy bytes to job buffer
                    copyByteToJobBuffer(downloadJob, part);

                    // Calculate ack
                    int ack = downloadJob.getSize() - downloadJob.getLeft();

                    // Send ack to server
                    server.sendDownloadAck(downloadJob.getId(), ack);
                }
            } finally {
                downloadJob.getCopyLock().releaseWriteLock();
            }
        }
    }

    private void copyByteToJobBuffer(DownloadJob job, byte[] part) {
        int left = job.getLeft();
        int start = job.getSize() - left;
        int validSize = left < part.length ? left : part.length;

        left -= validSize;

        Log.v("POKO", "COPY " + part.length + " BYTES, LEFT " + left + " BYTES");

        // Set left
        job.setLeft(left);

        // Get buffer
        byte[] buffer = job.getBytes();

        // Copy data to buffer
        System.arraycopy(part, 0, buffer, start, validSize);

        // Check if download is done
        if (left == 0) {
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

        return downloadJob.isFinished();
    }

    public void failUploadJob(int jobId, boolean pending) {
        UploadJob job;

        Log.v("POKO", "FAILED TO UPLOAD");

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
        private Long send;
        private Long ack;
        private UploadJobCallback callback;

        private static final int ALL_IN_ONE_MODE = 0;
        private static final int STREAM_MODE = 1;

        public UploadJob(int mode) {
            ack = 0L;
            send = 0L;

            this.mode = mode;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        private byte[] getBinary() {
            return binary;
        }

        private void setBinary(byte[] binary) {
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

        private String getExtension() {
            return extension;
        }

        private void setExtension(String extension) {
            this.extension = extension;
        }

        public Long getAck() {
            return ack;
        }

        public void setAck(Long ack) {
            this.ack = ack;
        }

        public Long getSend() {
            return send;
        }

        public void setSend(Long send) {
            this.send = send;
        }

        public String getContentName() {
            return contentName;
        }

        public void setContentName(String contentName) {
            this.contentName = contentName;
        }

        private ContentStream getContentStream() {
            return contentStream;
        }

        private void setContentStream(ContentStream contentStream) {
            this.contentStream = contentStream;
        }

        private int getMode() {
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
        public int test = 0;

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

        private byte[] getBytes() {
            return bytes;
        }

        private void setBytes(byte[] bytes) {
            this.bytes = bytes;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        private int getLeft() {
            return left;
        }

        private void setLeft(int left) {
            this.left = left;
        }

        public DownloadJobCallback getCallback() {
            return callback;
        }

        public void setCallback(DownloadJobCallback callback) {
            this.callback = callback;
        }

        private synchronized void putBytesToQueue(byte[] part) {
            pendingBuffers.add(part);
        }

        private synchronized Queue<byte[]> getBytesQueue() {
            return pendingBuffers;
        }

        private PokoLock getCopyLock() {
            return copyLock;
        }

        private boolean isStarted() {
            return started;
        }

        private void setStarted(boolean started) {
            this.started = started;
        }

        private boolean isFinished() {
            return finished;
        }

        public boolean isSucceeded() {
            return !success;
        }

        public boolean isFailed() {
            return !success;
        }

        private void markJobSucceededAndStartCallback() {
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

        private void markJobFailedAndStartCallback() {
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
