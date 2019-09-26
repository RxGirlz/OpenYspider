package com.devyy.openyspider.common;


public class DownImageThread implements Runnable {

    private String onlinePath;
    private String localPath;


    public DownImageThread(String onlinePath, String localPath) {
        this.onlinePath = onlinePath;
        this.localPath = localPath;
    }

    @Override
    public void run() {
        ReptileUtil.syncDownload(onlinePath, localPath);
    }
}
