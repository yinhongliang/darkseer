package com.holliesyin.darkseer.hive;

/**
 * Created by Hollies Yin on 2017-12-11.
 */
public class HiveProxyResponse {
    private String appId;
    private String msgId;
    private String statusCode;
    private String result;

    public HiveProxyResponse() {
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

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "HiveProxyResponse{" +
                "appId='" + appId + '\'' +
                ", msgId='" + msgId + '\'' +
                ", statusCode='" + statusCode + '\'' +
                ", result='" + result + '\'' +
                '}';
    }

    public interface StatusCode{
        String SUCCESS = "CODE_SUCCESS";
        String FAIL = "CODE_FAIL";
    }
}
