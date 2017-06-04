package com.company;

import java.nio.file.Path;
import java.nio.file.Paths;

public class SimpleFile {
    private Path path;
    private byte[] content;
    private long lastModified;

    public SimpleFile(String path, byte[] content, long lastModified) {
        this.path = Paths.get(path);
        this.content = content;
        this.lastModified = lastModified;
    }

    public Path getPath() {
        return path;
    }

    public byte[] getContent() {
        return content;
    }

    public long getLastModified() {
        return lastModified;
    }
}
