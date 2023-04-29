package com.chbcraft.net.handlers.inbound.websocket.pojo;

import java.io.File;

public class FileInfo {
    private String fileName;
    private File originalFile;
    private long fileSize;
    private String fileModifier;
    private String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public File getOriginalFile() {
        return originalFile;
    }

    public void setOriginalFile(File originalFile) {
        this.originalFile = originalFile;
    }

    public String getFileModifier() {
        return fileModifier;
    }

    public void setFileModifier(String fileModifier) {
        this.fileModifier = fileModifier;
    }
}
