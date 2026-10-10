package com.mbu.dslibrary.model;

/** Pointer to a file saved in MongoDB GridFS (embedded inside other documents). */
public class StoredFile {
    private String fileId;
    private String filename;
    private String title;
    private long size;
    private java.time.Instant uploadedAt;

    public StoredFile() {}

    public StoredFile(String fileId, String filename, String title, long size) {
        this.fileId = fileId;
        this.filename = filename;
        this.title = title;
        this.size = size;
        this.uploadedAt = java.time.Instant.now();
    }

    public java.time.Instant getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(java.time.Instant uploadedAt) { this.uploadedAt = uploadedAt; }

    public String getFileId() { return fileId; }
    public void setFileId(String fileId) { this.fileId = fileId; }
    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }
}
