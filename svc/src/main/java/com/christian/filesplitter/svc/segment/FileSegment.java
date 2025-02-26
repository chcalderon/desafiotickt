package com.christian.filesplitter.svc.segment;

public class FileSegment {
    private String name;
    private String downloadUrl;

    public FileSegment(String name, String downloadUrl) {
        this.name = name;
        this.downloadUrl = downloadUrl;
    }

    public String getName() {
        return name;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }
}