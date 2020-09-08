package com.devyy.oys.srarter.core.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

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

    public static boolean ioDownload2(String onlineUrl, String localUrl) {
        try {
            FileUtils.copyURLToFile(new URL(onlineUrl), new File(localUrl));
            log.info("==>io下载成功 localUrl={}", localUrl);
            return true;
        } catch (Exception e) {
            if (!(e instanceof FileNotFoundException)) {
                log.error("onlineUrl={} e={}", onlineUrl, e.getMessage(), e);
            }
            return false;
        }
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
    public static boolean fileMove(String oldPath, String newPath) {
        File oldName = new File(oldPath);
        File newName = new File(newPath);
        return oldName.renameTo(newName);
    }

    /**
     * 图片移动
     *
     * @param oldPath 原始路径
     * @param newPath 目标路径
     */
    public static boolean fileCopy(String oldPath, String newPath) {
        File oldName = new File(oldPath);
        File newName = new File(newPath);
        try {
            FileUtils.copyFile(oldName, newName);
            log.info("==>fileCopy success oldPath={} newPath={}", oldPath, newPath);
            return true;
        } catch (IOException e) {
            log.warn("==>fileCopy failed oldPath={} newPath={}", oldPath, newPath);
            return false;
        }
    }

}