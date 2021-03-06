package com.devyy.oys.tujidao;

import com.devyy.oys.srarter.core.enums.StateTypeEnum;
import com.devyy.oys.srarter.core.util.ReptileUtil;
import com.devyy.oys.tujidao.dao.ITujidaoAlbumMapper;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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
 * @since 2019-12-01
 */
@Slf4j
@Service
public class TujidaoService implements ITujidaoService {
    @Value("${oys.tujidao.url.prefix}")
    private String TUJIDAO_URL_PREFIX;
    @Value("${oys.tujidao.img.url.prefix}")
    private String TUJIDAO_IMG_URL_PREFIX;

    @Value("${oys.tujidao.local.folder.prefix}")
    private String TUJIDAO_LOCAL_FOLDER_PREFIX;
    @Value("${oys.tujidao.local.preview.prefix}")
    private String TUJIDAO_LOCAL_PREVIEW_PREFIX;
    @Value("${oys.tujidao.local.cover.prefix}")
    private String TUJIDAO_LOCAL_COVER_PREFIX;

    @Value("${oys.tujidao.local.cover.num.start}")
    private Integer TUJIDAO_LOCAL_COVER_START;
    @Value("${oys.tujidao.local.cover.num.end}")
    private Integer TUJIDAO_LOCAL_COVER_END;

    @Value("${oys.tujidao.local.preview.num.start}")
    private Integer TUJIDAO_LOCAL_PREVIEW_START;
    @Value("${oys.tujidao.local.preview.num.end}")
    private Integer TUJIDAO_LOCAL_PREVIEW_END;

    /**
     * 文件不合法名正则
     */
    private static final Pattern FILE_PATTERN = Pattern.compile("[\\\\/:*?\"<>|]");

    @Autowired
    private ITujidaoAlbumMapper tujidaoAlbumMapper;

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

    @Override
    public String doScanAlbums() {
        // Cookies
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

        try {
            trustAllHttpsCertificates();
        } catch (Exception e) {
            log.error("==>TujidaoService#doScanAlbums failed e=", e);
        }
        HttpsURLConnection.setDefaultHostnameVerifier(hv);

        Document document = null;
        final int MIN_PAGE = 1;
        final int MAX_PAGE = 50;
        for (int i = MIN_PAGE; i <= MAX_PAGE; i++) {
            try {
                document = Jsoup.connect(TUJIDAO_URL_PREFIX + i).cookies(cookiesMap).get();
            } catch (IOException e) {
                log.error("==>url={} e={}", TUJIDAO_URL_PREFIX + i, e.getMessage());
            }
            if (Objects.nonNull(document)) {
                document.getElementsByClass("c1").first().getElementsByTag("a").forEach(e -> {
                    String eText = e.text();
                    String[] eTextArray = eText.split(" ");

                    TujidaoAlbumDO tujidaoDO = new TujidaoAlbumDO();
                    tujidaoDO.setAlbumId(Integer.parseInt(e.attr("href").replace("/a/?id=", "")));
                    tujidaoDO.setAlbumName(tujidaoDO.getAlbumId() + "-" + rmIllegalName(eText));
                    tujidaoDO.setTotal(Integer.parseInt(rmLeftRightP(eTextArray[0])));
                    tujidaoDO.setState(StateTypeEnum.STARTED.getSeq());

                    // 幂等，保证记录数唯一
                    Map<String, Object> queryMap = new HashMap<>(1);
                    queryMap.put("album_id", tujidaoDO.getAlbumId());
                    if (CollectionUtils.isEmpty(tujidaoAlbumMapper.selectByMap(queryMap))) {
                        tujidaoAlbumMapper.insert(tujidaoDO);
                        log.info("number={} 同步成功,total={},type={},title={}",
                                tujidaoDO.getAlbumId(), tujidaoDO.getTotal(), tujidaoDO.getType(), tujidaoDO.getAlbumName());
                    } else {
                        log.info("number={} 已存在", tujidaoDO.getAlbumId());
                    }
                });
            }
        }
        return "success";
    }

    @Override
    public String doScanImages() {
        return "success";
    }

    @Override
    public String doDownload() {
        return "success";
    }

    @Override
    public String doDownloadCover() {
        // 若文件夹路径不存在，则新建
        File file = new File(TUJIDAO_LOCAL_COVER_PREFIX);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                log.error("==>localFolder={} 创建文件路径失败", TUJIDAO_LOCAL_COVER_PREFIX);
                return "error";
            }
        }
        final int startInt = TUJIDAO_LOCAL_COVER_START;
        final int endInt = TUJIDAO_LOCAL_COVER_END;

        for (int i = startInt; i <= endInt; i++) {
            String onlinePath = String.format(Locale.CHINESE, "%s%d/1.jpg", TUJIDAO_LOCAL_PREVIEW_PREFIX, i);
            String localPath = String.format(Locale.CHINESE, "%s%d-1.jpg", TUJIDAO_LOCAL_COVER_PREFIX, i);

            // 幂等，若当前文件未下载，则进行下载
            File file2 = new File(localPath);
            if (!file2.exists()) {
                ReptileUtil.fileCopy(onlinePath, localPath);
            }
        }
        return "success";
    }

    @Override
    public String doPreDownload() {
        ExecutorService executors = Executors.newFixedThreadPool(8);
        final int startInt = TUJIDAO_LOCAL_PREVIEW_START;
        final int endInt = TUJIDAO_LOCAL_PREVIEW_END;
        for (int i = startInt; i <= endInt; i++) {
            final int finalI = i;
            executors.submit(() -> downloadByAlbumId(String.valueOf(finalI)));
        }
        return "success";
    }

    @Override
    public String doLocalMove() {
        Map<String, Object> queryMap = new HashMap<>(1);
        queryMap.put("state", StateTypeEnum.STARTED.getSeq());
        tujidaoAlbumMapper.selectByMap(queryMap).forEach(albumDO -> {
            int total = albumDO.getTotal();
            int albumId = albumDO.getAlbumId();
            String albumName = albumDO.getAlbumName();
            String localFolder = TUJIDAO_LOCAL_FOLDER_PREFIX + albumName;
            // 若文件夹路径不存在，则新建
            File file = new File(localFolder);
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    log.error("==>localFolder={} 创建文件路径失败", localFolder);
                    return;
                }
            }
            for (int i = 0; i <= total; i++) {
                String onlinePath = String.format(Locale.CHINESE, "%s%s/%d.jpg", TUJIDAO_LOCAL_PREVIEW_PREFIX, albumId, i);
                String localPath = String.format(Locale.CHINESE, "%s/%d.jpg", localFolder, i);

                // 幂等，若当前文件未下载，则进行下载
                File file2 = new File(localPath);
                if (!file2.exists()) {
                    ReptileUtil.fileMove(onlinePath, localPath);
                }
            }
            albumDO.setState(StateTypeEnum.ANALYSIS.getSeq());
            tujidaoAlbumMapper.updateById(albumDO);
            log.info("==>albumId={} 相册已本地迁移完成", albumId);
        });
        return "success";
    }

    private void downloadByAlbumId(String albumId) {
        String localFolder = TUJIDAO_LOCAL_PREVIEW_PREFIX + albumId + "/";
        // 若文件夹路径不存在，则新建
        File file = new File(localFolder);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                log.error("==>localFolder={} 创建文件路径失败", localFolder);
                return;
            }
        }
        for (int i = 0; ; i++) {
            String onlinePath = String.format(Locale.CHINESE, "%s%s/%d.jpg", TUJIDAO_IMG_URL_PREFIX, albumId, i);
            String localPath = String.format(Locale.CHINESE, "%s%d.jpg", localFolder, i);

            // 幂等，若当前文件未下载，则进行下载
            File file2 = new File(localPath);
            if (!file2.exists()) {
                if (!ReptileUtil.ioDownload2(onlinePath, localPath)) {
                    break;
                }
            }
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
     * 去除不合法文件名
     */
    private String rmIllegalName(String s) {
        return FILE_PATTERN.matcher(s).replaceAll("");
    }
}

