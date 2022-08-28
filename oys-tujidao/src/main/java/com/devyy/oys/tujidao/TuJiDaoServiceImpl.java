package com.devyy.oys.tujidao;

import com.devyy.oys.srarter.core.enums.StateTypeEnum;
import com.devyy.oys.srarter.core.util.SpiderUtil;
import com.devyy.oys.tujidao.dao.TuJiDaoAlbumMapper;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;

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
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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

    static Map<Integer, Integer> map = new HashMap<>();

    static {
        map.put(119, 50);
        map.put(1562, 86);
        map.put(293, 129);
        map.put(313, 100);
        map.put(331, 116);
        map.put(338, 80);
        map.put(37, 80);
        map.put(3849, 100);
        map.put(543, 16);
        map.put(714, 61);
        map.put(716, 68);
        map.put(727, 69);

        map.put(16471, 24);
        map.put(16681, 46);
        map.put(16968, 75);
        map.put(17058, 100);

        map.put(32793, 158);
        map.put(34456, 44);
        map.put(35381, 33);
        map.put(35527, 150);
        map.put(35534, 58);
        map.put(35576, 21);
        map.put(35697, 39);
        map.put(35854, 25);
        map.put(36655, 60);
        map.put(36656, 52);
        map.put(36658, 68);
        map.put(36659, 73);
        map.put(36697, 76);
        map.put(36698, 52);
        map.put(36700, 80);
        map.put(36701, 92);
        map.put(37180, 45);
        map.put(38013, 41);
        map.put(38313, 42);
        map.put(38432, 35);
        map.put(39740, 48);

        map.put(44264, 17);
        map.put(45681, 42);
        map.put(45697, 19);
        map.put(46207, 16);
        map.put(48080, 62);
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
        // 55624-54484
        for (int i = 1; i <= 100; i++) {
            try {
                document = Jsoup.connect(tjdWebUrlPrefix + i).cookies(cookiesMap).get();
            } catch (IOException e) {
                log.error("==>url={} e={}", tjdWebUrlPrefix + i, e.getMessage());
            }
            if (Objects.isNull(document)) {
                continue;
            }
            Element heziElement = document.getElementsByClass("hezi").first();
            if (Objects.isNull(heziElement)) {
                continue;
            }

            heziElement.getElementsByTag("li").forEach(liElement -> {
                Element biaotiElement = liElement.getElementsByClass("biaoti").first();
                Element shuliangElement = liElement.getElementsByClass("shuliang").first();

                if (biaotiElement == null || shuliangElement == null) {
                    return;
                }

                int albumId = Integer.parseInt(liElement.attr("id"));
                int total = Integer.parseInt(shuliangElement.text().replace("P", ""));
                String albumName = MessageFormat.format("{0}-[{1}P] {2}",
                        String.valueOf(albumId), total, rmIllegalName(biaotiElement.text()));

                TuJiDaoAlbumDO tjdDO = new TuJiDaoAlbumDO();
                tjdDO.setAlbumId(albumId);
                tjdDO.setAlbumName(albumName);
                tjdDO.setTotal(total);
                tjdDO.setState(StateTypeEnum.STARTED.getSeq());

                // 幂等，保证记录数唯一
                Map<String, Object> queryMap = new HashMap<>(1);
                queryMap.put("album_id", tjdDO.getAlbumId());
                if (CollectionUtils.isEmpty(tuJiDaoAlbumMapper.selectByMap(queryMap))) {
                    tuJiDaoAlbumMapper.insert(tjdDO);
                    log.info("album_id={} 同步成功,total={},title={}",
                            tjdDO.getAlbumId(), tjdDO.getTotal(), tjdDO.getAlbumName());
                } else {
                    log.warn("album_id={} 已存在", tjdDO.getAlbumId());
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
     * 文件不合法名正则
     */
    private static final Pattern FILE_PATTERN = Pattern.compile("[\\\\/:*?\"<>|]");

    /**
     * 去除不合法文件名
     */
    private String rmIllegalName(String s) {
        return FILE_PATTERN.matcher(s).replaceAll("");
    }

    static String[] jpgs = {
            "D:/图集岛爬虫Preview/49322/20.jpg",
            "D:/图集岛爬虫Preview/49323/13.jpg",
            "D:/图集岛爬虫Preview/49324/34.jpg",
            "D:/图集岛爬虫Preview/49331/49.jpg",
            "D:/图集岛爬虫Preview/49332/22.jpg",
            "D:/图集岛爬虫Preview/49340/19.jpg",
            "D:/图集岛爬虫Preview/49340/83.jpg",
            "D:/图集岛爬虫Preview/49349/38.jpg",
            "D:/图集岛爬虫Preview/49355/56.jpg",
            "D:/图集岛爬虫Preview/49357/83.jpg",
            "D:/图集岛爬虫Preview/49360/42.jpg",
            "D:/图集岛爬虫Preview/49366/1.jpg",
            "D:/图集岛爬虫Preview/49367/13.jpg",
            "D:/图集岛爬虫Preview/49369/20.jpg",
            "D:/图集岛爬虫Preview/49372/1.jpg",
            "D:/图集岛爬虫Preview/49374/61.jpg",
            "D:/图集岛爬虫Preview/49380/27.jpg",
            "D:/图集岛爬虫Preview/49382/23.jpg",
            "D:/图集岛爬虫Preview/49386/28.jpg",
            "D:/图集岛爬虫Preview/49387/20.jpg",
            "D:/图集岛爬虫Preview/49387/21.jpg",
            "D:/图集岛爬虫Preview/49388/88.jpg",
            "D:/图集岛爬虫Preview/49390/8.jpg",
            "D:/图集岛爬虫Preview/49392/23.jpg",
            "D:/图集岛爬虫Preview/49393/26.jpg",
            "D:/图集岛爬虫Preview/49397/82.jpg",
            "D:/图集岛爬虫Preview/49408/8.jpg",
            "D:/图集岛爬虫Preview/49410/91.jpg",
            "D:/图集岛爬虫Preview/49412/30.jpg",
            "D:/图集岛爬虫Preview/49414/21.jpg",
            "D:/图集岛爬虫Preview/49418/5.jpg",
            "D:/图集岛爬虫Preview/49420/8.jpg",
            "D:/图集岛爬虫Preview/49421/22.jpg",
            "D:/图集岛爬虫Preview/49421/42.jpg",
            "D:/图集岛爬虫Preview/49424/9.jpg",
            "D:/图集岛爬虫Preview/49425/19.jpg",
            "D:/图集岛爬虫Preview/49426/68.jpg",
            "D:/图集岛爬虫Preview/49426/82.jpg",
            "D:/图集岛爬虫Preview/49432/28.jpg",
            "D:/图集岛爬虫Preview/49501/34.jpg",
            "D:/图集岛爬虫Preview/49501/52.jpg",
            "D:/图集岛爬虫Preview/49503/19.jpg",
            "D:/图集岛爬虫Preview/49503/40.jpg",
            "D:/图集岛爬虫Preview/49508/14.jpg",
            "D:/图集岛爬虫Preview/49512/33.jpg",
            "D:/图集岛爬虫Preview/49513/1.jpg",
            "D:/图集岛爬虫Preview/49521/34.jpg",
            "D:/图集岛爬虫Preview/49522/31.jpg",
            "D:/图集岛爬虫Preview/49537/26.jpg",
            "D:/图集岛爬虫Preview/49544/1.jpg",
            "D:/图集岛爬虫Preview/49544/11.jpg",
            "D:/图集岛爬虫Preview/49544/4.jpg",
            "D:/图集岛爬虫Preview/49547/43.jpg",
            "D:/图集岛爬虫Preview/49550/5.jpg",
            "D:/图集岛爬虫Preview/49551/16.jpg",
            "D:/图集岛爬虫Preview/49551/65.jpg",
            "D:/图集岛爬虫Preview/49559/6.jpg",
            "D:/图集岛爬虫Preview/49562/75.jpg",
            "D:/图集岛爬虫Preview/49563/42.jpg",
            "D:/图集岛爬虫Preview/49563/55.jpg",
            "D:/图集岛爬虫Preview/49563/9.jpg",
            "D:/图集岛爬虫Preview/49565/17.jpg",
            "D:/图集岛爬虫Preview/49574/1.jpg",
            "D:/图集岛爬虫Preview/49574/4.jpg",
            "D:/图集岛爬虫Preview/49578/12.jpg",
            "D:/图集岛爬虫Preview/49578/15.jpg",
            "D:/图集岛爬虫Preview/49578/27.jpg",
            "D:/图集岛爬虫Preview/49583/28.jpg",
            "D:/图集岛爬虫Preview/49591/64.jpg",
            "D:/图集岛爬虫Preview/49598/10.jpg",
            "D:/图集岛爬虫Preview/49599/38.jpg",
            "D:/图集岛爬虫Preview/49601/42.jpg",
            "D:/图集岛爬虫Preview/49602/64.jpg",
            "D:/图集岛爬虫Preview/49602/75.jpg",
            "D:/图集岛爬虫Preview/49605/31.jpg",
            "D:/图集岛爬虫Preview/49606/40.jpg",
            "D:/图集岛爬虫Preview/49608/15.jpg",
            "D:/图集岛爬虫Preview/49611/27.jpg",
            "D:/图集岛爬虫Preview/49611/45.jpg",
            "D:/图集岛爬虫Preview/49614/36.jpg",
            "D:/图集岛爬虫Preview/49627/28.jpg",
            "D:/图集岛爬虫Preview/49627/7.jpg",
            "D:/图集岛爬虫Preview/49628/51.jpg",
            "D:/图集岛爬虫Preview/49628/76.jpg",
            "D:/图集岛爬虫Preview/49629/47.jpg",
            "D:/图集岛爬虫Preview/49633/11.jpg",
            "D:/图集岛爬虫Preview/49637/5.jpg",
            "D:/图集岛爬虫Preview/49639/24.jpg",
            "D:/图集岛爬虫Preview/49640/12.jpg",
            "D:/图集岛爬虫Preview/49645/21.jpg",
            "D:/图集岛爬虫Preview/49646/41.jpg",
            "D:/图集岛爬虫Preview/49653/12.jpg",
            "D:/图集岛爬虫Preview/49658/11.jpg",
            "D:/图集岛爬虫Preview/49665/2.jpg",
            "D:/图集岛爬虫Preview/49666/72.jpg",
            "D:/图集岛爬虫Preview/49671/4.jpg",
            "D:/图集岛爬虫Preview/49672/22.jpg",
            "D:/图集岛爬虫Preview/49677/25.jpg",
            "D:/图集岛爬虫Preview/49677/27.jpg",
            "D:/图集岛爬虫Preview/49691/23.jpg",
            "D:/图集岛爬虫Preview/49702/1.jpg",
            "D:/图集岛爬虫Preview/49704/9.jpg",
            "D:/图集岛爬虫Preview/49706/48.jpg",
            "D:/图集岛爬虫Preview/49713/5.jpg",
            "D:/图集岛爬虫Preview/49716/10.jpg",
            "D:/图集岛爬虫Preview/49721/18.jpg",
            "D:/图集岛爬虫Preview/49722/0.jpg",
            "D:/图集岛爬虫Preview/49722/28.jpg",
            "D:/图集岛爬虫Preview/49722/3.jpg",
            "D:/图集岛爬虫Preview/49722/31.jpg",
            "D:/图集岛爬虫Preview/49724/8.jpg",
            "D:/图集岛爬虫Preview/49725/2.jpg",
            "D:/图集岛爬虫Preview/49742/27.jpg",
            "D:/图集岛爬虫Preview/49750/5.jpg",
            "D:/图集岛爬虫Preview/49755/1.jpg",
            "D:/图集岛爬虫Preview/49763/52.jpg",
            "D:/图集岛爬虫Preview/49763/63.jpg",
            "D:/图集岛爬虫Preview/49770/38.jpg",
            "D:/图集岛爬虫Preview/49771/49.jpg",
            "D:/图集岛爬虫Preview/49774/38.jpg",
            "D:/图集岛爬虫Preview/49778/41.jpg",
            "D:/图集岛爬虫Preview/49779/11.jpg",
            "D:/图集岛爬虫Preview/49779/3.jpg",
            "D:/图集岛爬虫Preview/49780/21.jpg",
            "D:/图集岛爬虫Preview/49784/46.jpg",
            "D:/图集岛爬虫Preview/49789/25.jpg",
            "D:/图集岛爬虫Preview/49789/36.jpg",
            "D:/图集岛爬虫Preview/49791/47.jpg",
            "D:/图集岛爬虫Preview/49792/13.jpg",
            "D:/图集岛爬虫Preview/49804/15.jpg",
            "D:/图集岛爬虫Preview/49805/14.jpg",
            "D:/图集岛爬虫Preview/49805/24.jpg",
            "D:/图集岛爬虫Preview/49805/4.jpg",
            "D:/图集岛爬虫Preview/49805/7.jpg",
            "D:/图集岛爬虫Preview/49815/49.jpg",
            "D:/图集岛爬虫Preview/49816/75.jpg",
            "D:/图集岛爬虫Preview/49818/6.jpg",
            "D:/图集岛爬虫Preview/49819/26.jpg",
            "D:/图集岛爬虫Preview/49822/13.jpg",
            "D:/图集岛爬虫Preview/49822/18.jpg",
            "D:/图集岛爬虫Preview/49823/11.jpg",
            "D:/图集岛爬虫Preview/49826/15.jpg",
            "D:/图集岛爬虫Preview/49830/62.jpg",
            "D:/图集岛爬虫Preview/49836/2.jpg",
            "D:/图集岛爬虫Preview/49842/20.jpg",
            "D:/图集岛爬虫Preview/49842/35.jpg",
            "D:/图集岛爬虫Preview/49858/45.jpg",
            "D:/图集岛爬虫Preview/49859/1.jpg",
            "D:/图集岛爬虫Preview/49863/46.jpg",
            "D:/图集岛爬虫Preview/49863/5.jpg",
            "D:/图集岛爬虫Preview/49870/17.jpg",
            "D:/图集岛爬虫Preview/49874/6.jpg",
            "D:/图集岛爬虫Preview/49878/46.jpg",
            "D:/图集岛爬虫Preview/49879/15.jpg",
            "D:/图集岛爬虫Preview/49885/20.jpg",
            "D:/图集岛爬虫Preview/49891/13.jpg",
            "D:/图集岛爬虫Preview/49891/68.jpg",
            "D:/图集岛爬虫Preview/49895/69.jpg",
            "D:/图集岛爬虫Preview/49900/18.jpg",
            "D:/图集岛爬虫Preview/49900/33.jpg",
            "D:/图集岛爬虫Preview/49901/14.jpg",
            "D:/图集岛爬虫Preview/49905/71.jpg",
            "D:/图集岛爬虫Preview/49909/5.jpg",
            "D:/图集岛爬虫Preview/49910/32.jpg",
            "D:/图集岛爬虫Preview/49916/39.jpg",
            "D:/图集岛爬虫Preview/49933/29.jpg",
            "D:/图集岛爬虫Preview/49933/41.jpg",
            "D:/图集岛爬虫Preview/49936/32.jpg",
            "D:/图集岛爬虫Preview/49942/46.jpg",
            "D:/图集岛爬虫Preview/49954/79.jpg",
            "D:/图集岛爬虫Preview/49960/54.jpg",
            "D:/图集岛爬虫Preview/49962/21.jpg",
            "D:/图集岛爬虫Preview/49962/34.jpg",
            "D:/图集岛爬虫Preview/49964/7.jpg",
            "D:/图集岛爬虫Preview/49967/9.jpg",
            "D:/图集岛爬虫Preview/49969/1.jpg",
            "D:/图集岛爬虫Preview/49969/55.jpg",
            "D:/图集岛爬虫Preview/49969/85.jpg",
            "D:/图集岛爬虫Preview/49975/40.jpg",
            "D:/图集岛爬虫Preview/49976/3.jpg",
            "D:/图集岛爬虫Preview/49977/19.jpg",
            "D:/图集岛爬虫Preview/49978/25.jpg",
            "D:/图集岛爬虫Preview/49978/38.jpg",
            "D:/图集岛爬虫Preview/49978/51.jpg",
            "D:/图集岛爬虫Preview/49979/18.jpg",
            "D:/图集岛爬虫Preview/49980/4.jpg",
            "D:/图集岛爬虫Preview/49984/16.jpg",
            "D:/图集岛爬虫Preview/49984/7.jpg",
            "D:/图集岛爬虫Preview/49986/48.jpg",
            "D:/图集岛爬虫Preview/49988/24.jpg",
            "D:/图集岛爬虫Preview/49999/48.jpg",
            "D:/图集岛爬虫Preview/49999/53.jpg",
            "D:/图集岛爬虫Preview/49999/57.jpg",
            "D:/图集岛爬虫Preview/49999/64.jpg",
            "D:/图集岛爬虫Preview/49999/8.jpg",
            "D:/图集岛爬虫Preview/50000/16.jpg",
            "D:/图集岛爬虫Preview/50000/17.jpg",
            "D:/图集岛爬虫Preview/50002/38.jpg",
            "D:/图集岛爬虫Preview/50003/20.jpg",
            "D:/图集岛爬虫Preview/50004/20.jpg",
            "D:/图集岛爬虫Preview/50004/7.jpg",
            "D:/图集岛爬虫Preview/50006/42.jpg",
            "D:/图集岛爬虫Preview/50007/9.jpg",
            "D:/图集岛爬虫Preview/50012/75.jpg",
            "D:/图集岛爬虫Preview/50021/25.jpg",
            "D:/图集岛爬虫Preview/50022/46.jpg",
            "D:/图集岛爬虫Preview/50026/4.jpg",
            "D:/图集岛爬虫Preview/50028/1.jpg",
            "D:/图集岛爬虫Preview/50028/4.jpg",
            "D:/图集岛爬虫Preview/50030/19.jpg",
            "D:/图集岛爬虫Preview/50031/6.jpg",
            "D:/图集岛爬虫Preview/50032/1.jpg",
            "D:/图集岛爬虫Preview/50035/38.jpg",
            "D:/图集岛爬虫Preview/50038/18.jpg",
            "D:/图集岛爬虫Preview/50038/2.jpg",
            "D:/图集岛爬虫Preview/50038/34.jpg",
            "D:/图集岛爬虫Preview/50042/28.jpg",
            "D:/图集岛爬虫Preview/50042/38.jpg",
            "D:/图集岛爬虫Preview/50043/6.jpg",
            "D:/图集岛爬虫Preview/50048/10.jpg",
            "D:/图集岛爬虫Preview/50048/13.jpg",
            "D:/图集岛爬虫Preview/50048/18.jpg",
            "D:/图集岛爬虫Preview/50048/3.jpg",
            "D:/图集岛爬虫Preview/50048/35.jpg",
            "D:/图集岛爬虫Preview/50048/4.jpg",
            "D:/图集岛爬虫Preview/50053/10.jpg",
            "D:/图集岛爬虫Preview/50053/19.jpg",
            "D:/图集岛爬虫Preview/50053/39.jpg",
            "D:/图集岛爬虫Preview/50053/8.jpg",
            "D:/图集岛爬虫Preview/50053/9.jpg",
            "D:/图集岛爬虫Preview/50066/39.jpg",
            "D:/图集岛爬虫Preview/50068/2.jpg",
            "D:/图集岛爬虫Preview/50069/63.jpg",
            "D:/图集岛爬虫Preview/50069/64.jpg",
            "D:/图集岛爬虫Preview/50071/43.jpg",
            "D:/图集岛爬虫Preview/50077/41.jpg",
            "D:/图集岛爬虫Preview/50083/3.jpg",
            "D:/图集岛爬虫Preview/50088/19.jpg",
            "D:/图集岛爬虫Preview/50089/1.jpg",
            "D:/图集岛爬虫Preview/50091/15.jpg",
            "D:/图集岛爬虫Preview/50091/19.jpg",
            "D:/图集岛爬虫Preview/50100/36.jpg",
            "D:/图集岛爬虫Preview/50101/18.jpg",
            "D:/图集岛爬虫Preview/50101/2.jpg",
            "D:/图集岛爬虫Preview/50102/8.jpg",
            "D:/图集岛爬虫Preview/50103/24.jpg",
            "D:/图集岛爬虫Preview/50106/61.jpg",
            "D:/图集岛爬虫Preview/50109/22.jpg",
            "D:/图集岛爬虫Preview/50110/11.jpg",
            "D:/图集岛爬虫Preview/50113/53.jpg",
            "D:/图集岛爬虫Preview/50114/23.jpg",
            "D:/图集岛爬虫Preview/50114/4.jpg",
            "D:/图集岛爬虫Preview/50117/1.jpg",
            "D:/图集岛爬虫Preview/50120/13.jpg",
            "D:/图集岛爬虫Preview/50121/4.jpg",
            "D:/图集岛爬虫Preview/50121/6.jpg",
            "D:/图集岛爬虫Preview/50125/0.jpg",
            "D:/图集岛爬虫Preview/50126/39.jpg",
            "D:/图集岛爬虫Preview/50131/30.jpg",
            "D:/图集岛爬虫Preview/50140/12.jpg",
            "D:/图集岛爬虫Preview/50142/9.jpg",
            "D:/图集岛爬虫Preview/50149/12.jpg",
            "D:/图集岛爬虫Preview/50151/57.jpg",
            "D:/图集岛爬虫Preview/50154/54.jpg",
            "D:/图集岛爬虫Preview/50160/19.jpg",
            "D:/图集岛爬虫Preview/50160/21.jpg",
            "D:/图集岛爬虫Preview/50167/33.jpg",
            "D:/图集岛爬虫Preview/50170/16.jpg",
            "D:/图集岛爬虫Preview/50170/2.jpg",
            "D:/图集岛爬虫Preview/50174/22.jpg",
            "D:/图集岛爬虫Preview/50176/4.jpg",
            "D:/图集岛爬虫Preview/50177/19.jpg",
            "D:/图集岛爬虫Preview/50179/7.jpg",
            "D:/图集岛爬虫Preview/50181/1.jpg",
            "D:/图集岛爬虫Preview/50181/8.jpg",
            "D:/图集岛爬虫Preview/50183/4.jpg",
            "D:/图集岛爬虫Preview/50189/16.jpg",
            "D:/图集岛爬虫Preview/50192/17.jpg",
            "D:/图集岛爬虫Preview/50192/24.jpg",
            "D:/图集岛爬虫Preview/50192/36.jpg",
            "D:/图集岛爬虫Preview/50193/26.jpg",
            "D:/图集岛爬虫Preview/50194/8.jpg",
            "D:/图集岛爬虫Preview/50319/22.jpg"
    };

    private static void reDownload() {
        String directoryStr = "F:/图集岛爬虫Preview/";
        for (String jpg : jpgs) {
            String tmp = jpg.replace(directoryStr, "");

            String albumId = tmp.split("-")[0];
            String fileName = tmp.split("/")[1];

            String onlinePath = "https://tjg.gzhuibei.com/a/1/" + albumId + "/" + fileName;

            SpiderUtil.ioDownload2Times(onlinePath, jpg, 3);
        }
    }

    private static void delSize0() {
        String part = "图集岛爬虫Preview";
        String directoryStr = "F:/" + part + "/";
        File directory = new File(directoryStr);

        File[] folders = directory.listFiles((FilenameFilter) DirectoryFileFilter.DIRECTORY);
        if (folders == null) {
            return;
        }
        for (File folder : folders) {
            String folderName = folder.getName();
            String subDirectoryStr = directoryStr + folderName;

            File subDirectory = new File(subDirectoryStr);
            Collection<File> jpgFiles = FileUtils.listFiles(subDirectory, new String[]{"jpg"}, false);
            for (File jpg : jpgFiles) {
                if (jpg.length() == 0) {

//                    jpg.delete();
                    System.out.println("size0 jpg=" + subDirectoryStr + "/" + jpg.getName());
                }
            }
        }
    }

    public static void main(String[] args) {
//        reDownload();

//        delSize0();

        Set<String> zipFileNames = new HashSet<>();
        Set<String> fileNames = new HashSet<>();
        File zipDirectory = new File("F:/zip图集岛爬虫（40001-50000）/");
        File directory = new File("F:/图集岛爬虫（40001-50000）/");

        Collection<File> zipFiles = FileUtils.listFiles(zipDirectory, new String[]{"zip"}, false);
        for (File folder : zipFiles) {
            String folderName = folder.getName();
            zipFileNames.add(folderName.replace(".zip", ""));
        }

        File[] folders = directory.listFiles((FilenameFilter) DirectoryFileFilter.DIRECTORY);
        if (folders == null) {
            return;
        }
        for (File folder : folders) {
            String folderName = folder.getName();
            fileNames.add(folderName);
        }

        fileNames.removeAll(zipFileNames);
        System.out.println(fileNames);
    }
}
