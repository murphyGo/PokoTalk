package com.murphy.pokotalk.server.content;

import android.util.SparseArray;

import com.murphy.pokotalk.server.PokoServer;

public class ContentTransferManager {
    private static ContentTransferManager instance = null;
    private SparseArray<UploadJob> pendingUploadJobs;
    private SparseArray<DownloadJob> pendingDownloadJobs;
    private SparseArray<UploadJob> uploadJobs;
    private SparseArray<DownloadJob> downloadJobs;

    public static final String TYPE_IMAGE = "image";
    public static final String TYPE_BINARY = "binary";

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

    public synchronized int addUploadJob(byte[] binary,
                                         String extension, UploadJobCallback callback) {
        // Make temporary id
        int pendingId = pendingUploadJobId++;

        // Create pending job
        UploadJob job = new UploadJob();
        job.setId(pendingId);
        job.setBinary(binary);
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

    public synchronized void uploadJob(int id) {
        // Get job
        UploadJob job = uploadJobs.get(id);

        // Get server
        PokoServer server = PokoServer.getInstance();

        // Send data
        server.sendUpload(job.getId(), job.getBinary());
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
        server.sendStartDownload(contentName, type);
    }

    public synchronized void startDownloadJob(int sendId, int downloadId, int size) {
        // Get pending download job
        DownloadJob job = pendingDownloadJobs.get(sendId);

        if (job != null) {
            // Update id to download id
            job.setId(downloadId);

            // Set size
            job.setSize(size);

            // Set bytes left
            job.setLeft(size);

            // Allocate buffer
            job.setBytes(new byte[size]);
        }
    }

    public void downloadJob(int downloadId, byte[] part) {
        DownloadJob job;
        synchronized (this) {
            // Get download job
             job = downloadJobs.get(downloadId);
        }

        if (job != null) {
            int left = job.getLeft();
            int start = job.getSize() - left;
            int validSize = left < part.length ? left : part.length;

            left -= validSize;

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
                synchronized (this) {
                    // Remove job
                    downloadJobs.remove(job.getId());
                }

                // Get callback
                DownloadJobCallback callback = job.getCallback();

                // Call callback
                if (callback != null) {
                    callback.run(job.getContentName(), buffer);
                }
            }
        }
    }

    private class UploadJob {
        private Integer id;
        private String extension;
        private byte[] binary;
        private Long size;
        private UploadJobCallback callback;

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
    }

    private class DownloadJob {
        private int id;
        private String contentName;
        private String type;
        private byte[] bytes;
        private int size;
        private int left;
        private DownloadJobCallback callback;

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
    }

    public static abstract class UploadJobCallback {
        public abstract void run(String contentName);
    }

    public static abstract class DownloadJobCallback {
        public abstract void run(String contentName, byte[] bytes);
    }
}
