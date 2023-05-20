package com.chbcraft.net.handlers.inbound.websocket.pojo;

import java.io.File;

public class FileInfo {
    private String fileName;
    private File tarFile;
    private File tempFile;
    private long fileSize;
    private long FileOffSet = 0;
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

    public File getTarFile() {
        return tarFile;
    }

    public void setTarFile(File tarFile) {
        this.tarFile = tarFile;
    }

    public File getTempFile() {
        return tempFile;
    }

    public void setTempFile(File tempFile) {
        this.tempFile = tempFile;
    }

    public String getFileModifier() {
        return fileModifier;
    }

    public void setFileModifier(String fileModifier) {
        this.fileModifier = fileModifier;
    }

    public long getFileOffSet() {
        return FileOffSet;
    }

    public void setFileOffSet(long fileOffSet) {
        FileOffSet = fileOffSet;
    }
}
