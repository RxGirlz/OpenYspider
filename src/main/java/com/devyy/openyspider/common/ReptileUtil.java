package com.devyy.openyspider.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

/**
 *
 */
public class ReptileUtil {

    private static final Logger logger = LoggerFactory.getLogger(ReptileUtil.class);
    /**
     * 线程池
     */
    private static final ExecutorService service = Executors.newFixedThreadPool(8);

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
    public static void syncDownload(String onlinePath, String localPath) {
        try (DataInputStream dataInputStream = new DataInputStream(new URL(onlinePath).openStream());
             FileOutputStream fileOutputStream = new FileOutputStream(new File(localPath))) {

            ByteArrayOutputStream output = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024];
            int length;

            while ((length = dataInputStream.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }
            fileOutputStream.write(output.toByteArray());

            logger.info("==>下载成功 localPath={}", localPath);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * 异步图片下载
     *
     * @param onlinePath 线上图片路径
     * @param localPath  本地图片路径
     */
    public static void asyncDownload(String onlinePath, String localPath) {
        service.execute(new DownImageThread(onlinePath, localPath));
    }

    /**
     * 去除不合法文件名
     */
    public static String rmIllegalName(String s) {
        return FILE_PATTERN.matcher(s).replaceAll("");
    }


}


