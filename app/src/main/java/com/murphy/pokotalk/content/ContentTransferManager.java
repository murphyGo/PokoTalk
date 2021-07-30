package com.murphy.pokotalk.content;

import android.os.Handler;
import android.os.Looper;
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

    // Add upload job which uses all in one mode
    public synchronized int addUploadJob(byte[] binary, String extension,
                                         UploadJobCallback callback) {
        // Make job
        UploadJob job = addUploadJob(UploadJob.ALL_IN_ONE_MODE, extension, callback);

        // Set binary buffer and size
        job.setBinary(binary);

        // Set file size
        job.setSize((long) binary.length);

        return job.getId();
    }

    // Add upload job which uses chunk mode
    public synchronized int addUploadJob(ContentStream contentStream, String extension,
                                         UploadJobCallback callback) {
        // Make job
        UploadJob job = addUploadJob(UploadJob.CHUNK_MODE, extension, callback);

        // Set content stream
        job.setContentStream(contentStream);

        // Set file size
        job.setSize((long) contentStream.getSize());

        return job.getId();
    }

    // Common routine for adding upload job
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

        // Start timeout
        job.startTimeout();

        return job;
    }

    public synchronized void startUploadJob(int pendingId, int uploadId) {
        // Get pending job
        UploadJob job = pendingUploadJobs.get(pendingId);

        if (job != null && !job.isFinished()) {
            synchronized (job) {
                // Remove pending job
                pendingUploadJobs.remove(pendingId);

                // Add to active jobs
                uploadJobs.put(uploadId, job);

                // Reset timeout
                job.resetTimeout();

                // Start job
                job.start(uploadId);
            }

            // Get server
            PokoServer server = PokoServer.getInstance();

            // Get mode
            int mode = job.getMode();

            // Send start upload message properly according to mode
            if (mode == UploadJob.ALL_IN_ONE_MODE) {
                server.sendStartUpload(job.getId(), job.getBinary().length, job.getExtension());
            } else if (mode == UploadJob.CHUNK_MODE) {
                server.sendStartUpload(job.getId(), job.getContentStream().getSize(), job.getExtension());
            }
        }
    }

    public void setUploadJobContentName(int uploadId, String contentName) {
        UploadJob job;

        synchronized (this) {
            // Get job
            job = uploadJobs.get(uploadId);

            if (job == null || job.isFinished()) {
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

            if (job == null || job.isFinished()) {
                return;
            }
        }

        // Get server
        PokoServer server = PokoServer.getInstance();

        if (job.getMode() == UploadJob.ALL_IN_ONE_MODE) {
            // Send all data and finish
            server.sendUpload(job.getId(), job.getBinary());
        } else if (job.getMode() == UploadJob.CHUNK_MODE) {
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

                    // Test eof or the job is finished
                    if (chunk == null || job.isFinished()) {
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

                            // Break upload if the job finished
                            if (job.isFinished()) {
                                break;
                            }

                            if (send >= ack + ContentTransferManager.UPLOAD_WINDOW) {
                                // Timeout, fail job
                                job.failJobAndStartCallback();
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

            if (job == null || job.isFinished()) {
                return;
            }
        }

        synchronized (job) {
            // Update ack
            if (job.getAck() < ack) {
                job.setAck(ack);
            }

            // Reset timeout
            job.resetTimeout();
        }

        // Check if upload is done
        if (job.getSize() <= job.getAck()) {
            // Mark success and start callback
            job.finishJobAndStartCallback();
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

        // Start timeout
        job.startTimeout();

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

        if (job != null && !job.isFinished()) {
            synchronized (this) {
                // Remove from pending download job
                pendingDownloadJobs.remove(sendId);

                // Add to download jobs
                downloadJobs.put(downloadId, job);

                synchronized (job) {
                    // Set size
                    job.setSize(size);

                    // Set bytes left
                    job.setLeft(size);

                    // Allocate buffer
                    job.setBytes(new byte[size]);

                    // Reset timeout
                    job.resetTimeout();

                    // Start job
                    job.start(downloadId);
                }
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

            if (downloadJob != null && !downloadJob.isFinished()) {
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

        if (downloadJob != null && !downloadJob.isFinished()) {
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

                    // Check for timeout or finished
                    if (!downloadJob.isStarted() || downloadJob.isFinished()) {
                        // Mark the job failed and start callback
                        downloadJob.failJobAndStartCallback();

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
                    // Check if the job has finished
                    if (downloadJob.isFinished()) {
                        break;
                    }

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

        // Reset timeout
        job.resetTimeout();

        // Set left
        job.setLeft(left);

        // Get buffer
        byte[] buffer = job.getBytes();

        // Copy data to buffer
        System.arraycopy(part, 0, buffer, start, validSize);

        // Check if download is done
        if (left == 0) {
            // Mark the job succeeded and start callback
            job.finishJobAndStartCallback();
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

        synchronized (this) {
            // Get job
            if (pending) {
                job = pendingUploadJobs.get(jobId);
            } else {
                job = uploadJobs.get(jobId);
            }

            // Check if job exists
            if (job != null) {
                // Fail the job and start callback
                job.failJobAndStartCallback();
            }
        }
    }

    public void failDownloadJob(int jobId, boolean pending) {
        DownloadJob job;

        synchronized (this) {
            // Get job
            if (pending) {
                job = pendingDownloadJobs.get(jobId);
            } else {
                job = downloadJobs.get(jobId);
            }

            // Check if job exists
            if (job != null) {
                // Fail the job and start callback
                job.failJobAndStartCallback();
            }
        }
    }

    abstract private class TransferJob {
        private int id;
        private String contentName;
        private Handler handler;
        private Runnable timeoutJob;
        private Runnable scheduledJob;

        private boolean started;
        private boolean finished;
        private boolean success;

        private static final int TIMEOUT = 10000;

        public TransferJob() {
            handler = new Handler(Looper.getMainLooper());
            started = false;
            finished = false;
            success = false;
            scheduledJob = null;

            // Make timeout job
            timeoutJob = new Runnable() {
                @Override
                public void run() {
                    // Remove job
                    scheduledJob = null;

                    // Timeout, fail the job
                    failJobAndStartCallback();
                }
            };
        }

        public synchronized void startTimeout() {
            if (scheduledJob == null && !isFinished()) {
                // Post timeout callback
                handler.postDelayed(timeoutJob, TIMEOUT);
                scheduledJob = timeoutJob;
            }
        }

        public synchronized void resetTimeout() {
            if (scheduledJob != null && !isFinished()) {
                // Cancel timeout callback
                handler.removeCallbacks(scheduledJob);
            }

            // Post timeout callback
            handler.postDelayed(timeoutJob, TIMEOUT);
            scheduledJob = timeoutJob;
        }

        public synchronized void cancelTimeout() {
            if (scheduledJob != null) {
                handler.removeCallbacks(scheduledJob);
                scheduledJob = null;
            }
        }

        public synchronized void start(int jobId) {
            if (isStarted()) {
                return;
            }

            // Set started
            started = true;

            // Update job id
            setId(jobId);

            // Notify all threads that are waiting for the job to be started
            notifyAll();
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getContentName() {
            return contentName;
        }

        public void setContentName(String contentName) {
            this.contentName = contentName;
        }

        public boolean isStarted() {
            return started;
        }

        public boolean isFinished() {
            return finished;
        }

        public void setFinished(boolean finished) {
            this.finished = finished;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public boolean isFailed() {
            return !success;
        }

        public void finishJobAndStartCallback() {
            // If finished already, just return
            if (isFinished()) {
                return;
            }

            synchronized (this) {
                // If finished already, just return
                if (isFinished()) {
                    return;
                }

                // Cancel timeout
                cancelTimeout();

                // Set finished set succeeded
                setFinished(true);
                setSuccess(true);
            }

            // Remove the job from list
            removeJobFromList();

            // Start callback
            startCallback(true);
        }

        public void failJobAndStartCallback() {
            // If finished already, just return
            if (isFinished()) {
                return;
            }

            synchronized (this) {
                // If finished already, just return
                if (isFinished()) {
                    return;
                }

                // Cancel timeout
                cancelTimeout();

                // Set finished and failed
                setFinished(true);
                setSuccess(false);
            }

            // Remove the job from list
            removeJobFromList();

            // Start callback
            startCallback(false);
        }

        abstract void startCallback(boolean success);
        abstract void removeJobFromList();
    }

    private class UploadJob extends TransferJob {
        private String extension;
        private int mode;
        private ContentStream contentStream;
        private byte[] binary;
        private Long size;
        private Long send;
        private Long ack;
        private UploadJobCallback callback;

        private static final int ALL_IN_ONE_MODE = 0;   // It sends all data with one message
        private static final int CHUNK_MODE = 1;        // It sends by dividing into small chunks

        public UploadJob(int mode) {
            super();

            ack = 0L;
            send = 0L;

            this.mode = mode;
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

        private ContentStream getContentStream() {
            return contentStream;
        }

        private void setContentStream(ContentStream contentStream) {
            this.contentStream = contentStream;
        }

        private int getMode() {
            return mode;
        }

        @Override
        void startCallback(boolean success) {
            if (callback != null) {
                if (success) {
                    callback.onSuccess(getContentName());
                } else {
                    callback.onError();
                }
            }
        }

        @Override
        void removeJobFromList() {
            synchronized (ContentTransferManager.getInstance()) {
                if (isStarted()) {
                    pendingUploadJobs.remove(getId());
                } else {
                    uploadJobs.remove(getId());
                }
            }
        }
    }

    private class DownloadJob extends TransferJob {
        private String type;
        private byte[] bytes;
        private int size;
        private int left;
        private DownloadJobCallback callback;
        private Queue<byte[]> pendingBuffers;
        private PokoLock copyLock;

        public DownloadJob() {
            super();

            pendingBuffers = new ArrayDeque<>();
            copyLock = new PokoLock();
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

        @Override
        void startCallback(boolean success) {
            if (callback != null) {
                if (success) {
                    callback.onSuccess(getContentName(), bytes);
                } else {
                    callback.onError();
                }
            }
        }

        @Override
        void removeJobFromList() {
            synchronized (ContentTransferManager.getInstance()) {
                if (isStarted()) {
                    pendingDownloadJobs.remove(getId());
                } else {
                    downloadJobs.remove(getId());
                }
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
