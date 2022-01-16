package com.devyy.oys.srarter.core.util;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

/**
 * 爬虫工具类
 *
 * @since 2019-12-01
 */
@Slf4j
public class SpiderUtil {
    private static boolean ioDownload2(String onlineUrl, String localUrl, int timeout) {
        try {
            FileUtils.copyURLToFile(new URL(onlineUrl), new File(localUrl), timeout, timeout);
            log.info("==>io下载成功 localUrl={}", localUrl);
            return true;
        } catch (Exception e) {
            if (!(e instanceof FileNotFoundException)) {
                log.warn("FileUtils.copyURLToFile failed={} e.message={}", onlineUrl, e.getMessage());
            }
            return false;
        }
    }

    /**
     * 带重试次数
     *
     * @param onlineUrl onlineUrl
     * @param localUrl  localUrl
     * @param times     重试次数
     * @return success
     */
    public static boolean ioDownload2Times(String onlineUrl, String localUrl, int times) {
        if (times < 0) {
            return false;
        } else {
            // timeout 递增
            int timeout = 10000;
            if (times < 1) {
                timeout = 30000;
            } else if (times < 2) {
                timeout = 20000;
            }
            boolean success = ioDownload2(onlineUrl, localUrl, timeout);
            if (!success) {
                return ioDownload2Times(onlineUrl, localUrl, times - 1);
            }
        }
        return true;
    }


    /**
     * 图片移动
     *
     * @param oldPath 原始路径
     * @param newPath 目标路径
     */
    public static void fileMove(String oldPath, String newPath) {
        File oldName = new File(oldPath);
        File newName = new File(newPath);
        oldName.renameTo(newName);
    }

    /**
     * 图片移动
     *
     * @param oldPath 原始路径
     * @param newPath 目标路径
     */
    public static void fileCopy(String oldPath, String newPath) {
        File oldName = new File(oldPath);
        File newName = new File(newPath);
        try {
            FileUtils.copyFile(oldName, newName);
            log.info("==>fileCopy success oldPath={} newPath={}", oldPath, newPath);
        } catch (IOException e) {
            log.warn("==>fileCopy failed oldPath={} newPath={}", oldPath, newPath);
        }
    }
}