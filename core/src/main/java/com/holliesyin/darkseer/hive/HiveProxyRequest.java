package com.holliesyin.darkseer.hive;

/**
 * Created by Hollies Yin on 2017-12-11.
 */
public class HiveProxyRequest {
    private String appId;
    private String msgId;
    private String content;

    public HiveProxyRequest() {
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "HiveProxyRequest{" +
                "appId='" + appId + '\'' +
                ", msgId='" + msgId + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}