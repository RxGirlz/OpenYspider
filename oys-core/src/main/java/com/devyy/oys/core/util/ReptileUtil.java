package com.devyy.oys.core.util;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Pattern;

/**
 * @since 2019-12-01
 */
@Slf4j
public class ReptileUtil {

    /**
     * 文件不合法名正则
     */
    private static final Pattern FILE_PATTERN = Pattern.compile("[\\\\/:*?\"<>|]");

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

}