package com.devyy.oys.tujidao;

import com.devyy.oys.srarter.core.enums.StateTypeEnum;
import com.devyy.oys.srarter.core.util.SpiderUtil;
import com.devyy.oys.tujidao.dao.TuJiDaoAlbumMapper;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.FileUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

/**
 * Service 层实现
 *
 * @since 2019-12-01
 */
@Slf4j
@Service
public class TuJiDaoServiceImpl implements TuJiDaoService {
    /**
     * 源站网页 url
     */
    @Value("${oys.tujidao.url.prefix}")
    private String tjdWebUrlPrefix;

    /**
     * 源站图片 cdn
     */
    @Value("${oys.tujidao.img.url.prefix}")
    private String tjdImgUrlPrefix;

    /**
     * 本地归档目录
     */
    @Value("${oys.tujidao.local.folder.prefix}")
    private String tjdLocalFolderPrefix;

    /**
     * 本地 Preview 目录
     */
    @Value("${oys.tujidao.local.preview.prefix}")
    private String tjdLocalPreviewPrefix;

    /**
     * 本地封面目录
     */
    @Value("${oys.tujidao.local.cover.prefix}")
    private String tjdLocalCoverPrefix;

    /**
     * 封面范围 [start, end]
     */
    @Value("${oys.tujidao.local.cover.num.start}")
    private Integer tjdCoverStart;
    @Value("${oys.tujidao.local.cover.num.end}")
    private Integer tjdLocalCoverEnd;

    /**
     * Preview 范围 [start, end]
     */
    @Value("${oys.tujidao.local.preview.num.start}")
    private Integer tjdPreviewStart;
    @Value("${oys.tujidao.local.preview.num.end}")
    private Integer tjdPreviewEnd;

    @Autowired
    private TuJiDaoAlbumMapper tuJiDaoAlbumMapper;

    @Override
    public String doPreDownload() {
        ExecutorService executors = Executors.newFixedThreadPool(8);
        for (int i = tjdPreviewStart; i <= tjdPreviewEnd; i++) {
            final int finalI = i;
            executors.submit(() -> downloadByAlbumId(String.valueOf(finalI)));
        }
        return "success";
    }

    private void downloadByAlbumId(String albumId) {
        String localFolder = tjdLocalPreviewPrefix + albumId + "/";
        // 若文件夹路径不存在，则新建
        File file = new File(localFolder);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                log.error("==>localFolder={} 创建文件路径失败", localFolder);
                return;
            }
        }
        // 顺序扫描直到 404
        for (int i = 0; ; i++) {
            String onlinePath = String.format(Locale.CHINESE, "%s%s/%d.jpg", tjdImgUrlPrefix, albumId, i);
            String localPath = String.format(Locale.CHINESE, "%s%d.jpg", localFolder, i);

            // 幂等，若当前文件未下载，则进行下载
            File file2 = new File(localPath);
            if (!file2.exists()) {
                if (!SpiderUtil.ioDownload2Times(onlinePath, localPath, 3)) {
                    break;
                }
            }
        }
    }

    @Override
    public String doGenerateCover() {
        // 若文件夹路径不存在，则新建
        File file = new File(tjdLocalCoverPrefix);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                log.error("==>localFolder={} 创建文件路径失败", tjdLocalCoverPrefix);
                return "error";
            }
        }

        // 选取 1.jpg 作为封面
        for (int i = tjdCoverStart; i <= tjdLocalCoverEnd; i++) {
            String onlinePath = String.format(Locale.CHINESE, "%s%d/1.jpg", tjdLocalPreviewPrefix, i);
            String localPath = String.format(Locale.CHINESE, "%s%d-1.jpg", tjdLocalCoverPrefix, i);

            // 幂等，若当前文件未下载，则进行下载
            File file2 = new File(localPath);
            if (!file2.exists()) {
                SpiderUtil.fileCopy(onlinePath, localPath);
            }
        }
        return "success";
    }

    @Override
    public String doSyncRecords() {
        // 解决 cookie 和 https 问题
        Map<String, String> cookiesMap = buildCookies();
        try {
            trustAllHttpsCertificates();
        } catch (Exception e) {
            log.error("==>TujidaoService#doScanAlbums failed e=", e);
        }
        HttpsURLConnection.setDefaultHostnameVerifier(hv);

        Document document = null;
        // 2021-01-16 源站限制最大页数 50 页
        for (int i = 1; i <= 50; i++) {
            try {
                document = Jsoup.connect(tjdWebUrlPrefix + i).cookies(cookiesMap).get();
            } catch (IOException e) {
                log.error("==>url={} e={}", tjdWebUrlPrefix + i, e.getMessage());
            }
            if (Objects.isNull(document)) {
                continue;
            }
            Element c1Element = document.getElementsByClass("c1").first();
            if (Objects.isNull(c1Element)) {
                continue;
            }
            c1Element.getElementsByTag("a").forEach(aElement -> {
                String text = aElement.text();

                TuJiDaoAlbumDO tjdDO = new TuJiDaoAlbumDO();
                tjdDO.setAlbumId(Integer.parseInt(aElement.attr("href").replace("/a/?id=", "")));
                tjdDO.setAlbumName(tjdDO.getAlbumId() + "-" + rmIllegalName(text));
                // 2022-01-16 源站已经隐去 图片数 信息
                tjdDO.setTotal(-1);
                tjdDO.setState(StateTypeEnum.STARTED.getSeq());

                // 幂等，保证记录数唯一
                Map<String, Object> queryMap = new HashMap<>(1);
                queryMap.put("album_id", tjdDO.getAlbumId());
                if (CollectionUtils.isEmpty(tuJiDaoAlbumMapper.selectByMap(queryMap))) {
                    tuJiDaoAlbumMapper.insert(tjdDO);
                    log.info("number={} 同步成功,total={},type={},title={}",
                            tjdDO.getAlbumId(), tjdDO.getTotal(), tjdDO.getType(), tjdDO.getAlbumName());
                } else {
                    log.info("number={} 已存在", tjdDO.getAlbumId());
                }
            });
        }
        return "success";
    }

    @Override
    public String doLocalMigration() {
        Map<String, Object> queryMap = new HashMap<>(1);
        queryMap.put("state", StateTypeEnum.STARTED.getSeq());
        tuJiDaoAlbumMapper.selectByMap(queryMap).forEach(albumDO -> {
            int total = albumDO.getTotal();
            int albumId = albumDO.getAlbumId();
            String albumName = albumDO.getAlbumName();
            String localFolder = tjdLocalFolderPrefix + albumName;

            // 若文件夹路径不存在，则新建
            File file = new File(localFolder);
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    log.error("==>localFolder={} 创建文件路径失败", localFolder);
                    return;
                }
            }

            // 2022-01-16 针对源站已经隐去 图片数 信息 fix
            if (total == -1) {
                String previewDir = tjdLocalPreviewPrefix + albumId;
                total = FileUtils.listFiles(new File(previewDir), new String[]{"jpg"}, false).size() - 1;
            }

            for (int i = 0; i <= total; i++) {
                String onlinePath = String.format(Locale.CHINESE, "%s%s/%d.jpg", tjdLocalPreviewPrefix, albumId, i);
                String localPath = String.format(Locale.CHINESE, "%s/%d.jpg", localFolder, i);

                // 幂等，若当前文件未下载，则进行下载
                File file2 = new File(localPath);
                if (!file2.exists()) {
                    SpiderUtil.fileMove(onlinePath, localPath);
                }
            }
            albumDO.setState(StateTypeEnum.ANALYSIS.getSeq());
            tuJiDaoAlbumMapper.updateById(albumDO);
            log.info("==>albumId={} 相册已本地迁移完成", albumId);
        });
        return "success";
    }

    private Map<String, String> buildCookies() {
        Map<String, String> cookiesMap = new HashMap<>();
        cookiesMap.put("TujidaoService%5Fid", "411999177-1588406642-%7C1600614708");
        cookiesMap.put("PHPSESSID", "uulfluqmbunauqqjnj4mf8i8dr");
        cookiesMap.put("UM_distinctid", "171d493d83c1c-074736cab8c8d9-670103b-1fa400-171d493d83d151");
        cookiesMap.put("atpsida", "7a72b6965366155670da7487_1600614730_4");
        cookiesMap.put("cna", "MkUPF0HprU0CAXWIT4ixum+C");
        cookiesMap.put("leixing", "0");
        cookiesMap.put("name", "rxgirlz");
        cookiesMap.put("sca", "5cafd77a");
        cookiesMap.put("uid", "157108");
        return cookiesMap;
    }

    private final HostnameVerifier hv = (s, sslSession) -> true;

    private void trustAllHttpsCertificates() throws Exception {
        javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];
        javax.net.ssl.TrustManager tm = new miTM();
        trustAllCerts[0] = tm;
        javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, null);
        javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    }

    private static class miTM implements javax.net.ssl.TrustManager, javax.net.ssl.X509TrustManager {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public boolean isServerTrusted(java.security.cert.X509Certificate[] certs) {
            return true;
        }

        public boolean isClientTrusted(java.security.cert.X509Certificate[] certs) {
            return true;
        }

        public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType)
                throws java.security.cert.CertificateException {
            return;
        }

        public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType)
                throws java.security.cert.CertificateException {
            return;
        }
    }

    /**
     * 去除 '[' 和 ']'
     */
    private String rmLeftRight(String s) {
        return s.replace("[", "").replace("]", "");
    }

    /**
     * 去除 '[' 和 ']' 和 'p'
     */
    private String rmLeftRightP(String s) {
        return rmLeftRight(s).replace("p", "");
    }

    /**
     * 文件不合法名正则
     */
    private static final Pattern FILE_PATTERN = Pattern.compile("[\\\\/:*?\"<>|]");

    /**
     * 去除不合法文件名
     */
    private String rmIllegalName(String s) {
        return FILE_PATTERN.matcher(s).replaceAll("");
    }
}

