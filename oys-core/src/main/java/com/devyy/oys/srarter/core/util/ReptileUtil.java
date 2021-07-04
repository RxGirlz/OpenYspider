package com.devyy.oys.srarter.core.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @since 2019-12-01
 */
@Slf4j
public class ReptileUtil {
    /**
     * 同步图片下载
     *
     * @param onlinePath 线上图片路径
     * @param localPath  本地图片路径
     */
    public static boolean ioDownload(String onlinePath, String localPath) {
        try (FileOutputStream fileOutputStream = new FileOutputStream(new File(localPath))) {
            URL imgUrl = new URL(onlinePath);
            URLConnection con = imgUrl.openConnection();
            con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.108 Safari/537.36");
            // 5s
            con.setConnectTimeout(5 * 1000);
            // 5min
            con.setReadTimeout(5 * 60 * 1000);
//            con.setReadTimeout(30 * 1000);
            DataInputStream dataInputStream = new DataInputStream(con.getInputStream());

            ByteArrayOutputStream output = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024];
            int length;

            while ((length = dataInputStream.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }
            fileOutputStream.write(output.toByteArray());

            log.info("==>下载成功 localPath={}", localPath);
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

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

    public static boolean nioDownload(String onlineUrl, String localUrl) {
        try (InputStream ins = new URL(onlineUrl).openStream()) {
            Path target = Paths.get(localUrl);
            Files.createDirectories(target.getParent());
            Files.copy(ins, target, StandardCopyOption.REPLACE_EXISTING);
            log.info("==>nio下载成功 localUrl={}", localUrl);
            return true;
        } catch (Exception e) {
            if (!(e instanceof FileNotFoundException)) {
                log.error(e.getMessage(), e);
            }
            return false;
        }
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