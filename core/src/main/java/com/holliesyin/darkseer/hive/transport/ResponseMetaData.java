package com.holliesyin.darkseer.hive.transport;

/**
 * Created by Hollies Yin on 2017-12-11.
 */
public class ResponseMetaData {
    private int contentLength;
    private int chunkSize;
    private int chunkCount;

    public ResponseMetaData() {
    }

    public ResponseMetaData(int contentLength, int chunkSize, int chunkCount) {
        this.contentLength = contentLength;
        this.chunkSize = chunkSize;
        this.chunkCount = chunkCount;
    }

    public int getContentLength() {
        return contentLength;
    }

    public void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    public int getChunkCount() {
        return chunkCount;
    }

    public void setChunkCount(int chunkCount) {
        this.chunkCount = chunkCount;
    }

    @Override
    public String toString() {
        return "ResponseMetaData{" +
                "contentLength=" + contentLength +
                ", chunkSize=" + chunkSize +
                ", chunkCount=" + chunkCount +
                '}';
    }
}