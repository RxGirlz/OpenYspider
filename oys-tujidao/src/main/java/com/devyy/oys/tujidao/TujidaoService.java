package com.devyy.oys.tujidao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.devyy.oys.srarter.core.enums.StateTypeEnum;
import com.devyy.oys.srarter.core.util.ReptileUtil;
import com.devyy.oys.tujidao.dao.ITujidaoAlbumMapper;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
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

    /**
     * 图集岛-相册目录路径前缀
     */
    private static final String TUJIDAO_URL_PREFIX = "http://www.tujidao.com/u/?action=gengxin&page=";
    /**
     * 图集岛-本地存储路径前缀（根据情况自定义）
     */
    // private static final String TUJIDAO_LOCAL_PREFIX = "D:/图集岛爬虫（00001-10000）/";
    // private static final String TUJIDAO_LOCAL_PREFIX = "D:/图集岛爬虫（10001-20000）/";
    // private static final String TUJIDAO_LOCAL_PREFIX = "D:/图集岛爬虫（20001-27864）/";
//    private static final String TUJIDAO_LOCAL_PREFIX = "D:/图集岛爬虫（27865-）/";
    private static final String TUJIDAO_LOCAL_PREFIX = "D:/图集岛爬虫（30001-）/";

    /**
     * 图集岛-图片真实路径前缀
     */
    private static final String TUJIDAO_IMG_URL_PREFIX = "https://ii.hywly.com/a/1/";
    /**
     * 文件不合法名正则
     */
    private static final Pattern FILE_PATTERN = Pattern.compile("[\\\\/:*?\"<>|]");

    @Autowired
    private ITujidaoAlbumMapper tujidaoAlbumMapper;

    @Override
    public String doScanAlbums() {
        // Cookies
        Map<String, String> cookiesMap = new HashMap<>();
        cookiesMap.put("7Dw1Tw3Bh2Mvu%5Fid", "157108");
        cookiesMap.put("7Dw1Tw3Bh2Mvu%5Fleixing", "0");
        cookiesMap.put("7Dw1Tw3Bh2Mvu%5Fpw", "c85f71f0fccab6ec");
        cookiesMap.put("7Dw1Tw3Bh2Mvu%5Fusername", "rxgirlz");
        cookiesMap.put("ASPSESSIONIDAARTABDD", "CPFDHMNAFPLGGCKNALKFFFDE");
        cookiesMap.put("CNZZDATA1257039673", "1450844396-1568287597-%7C1568292998");
        cookiesMap.put("UM_distinctid", "16d258579f52e1-0321e74b65393f-5373e62-1fa400-16d258579f75aa");

        Document document = null;
        final int MIN_PAGE = 1;
        final int MAX_PAGE = 20;
        for (int i = MIN_PAGE; i <= MAX_PAGE; i++) {
            try {
                document = Jsoup.connect(TUJIDAO_URL_PREFIX + i).cookies(cookiesMap).get();
            } catch (IOException e) {
                log.error(e.getMessage());
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
                        log.info("number={},total={},type={},title={}", tujidaoDO.getAlbumId(), tujidaoDO.getTotal(), tujidaoDO.getType(), tujidaoDO.getAlbumName());
                    } else {
                        log.info("number={}已存在", tujidaoDO.getAlbumId());
                    }
                });
            }
        }
        return "success";
    }

    @Override
    public String doScanImages() {
        return null;
    }

    @Deprecated
    @Override
    public String doDownload() {
        ExecutorService executorService = Executors.newFixedThreadPool(8);
        final int startInt = 30001;
        final int endInt = 40000;
        QueryWrapper<TujidaoAlbumDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().select().between(TujidaoAlbumDO::getAlbumId, startInt, endInt);
        tujidaoAlbumMapper.selectList(queryWrapper).forEach(albumDO -> {
            int total = albumDO.getTotal();
            int num = albumDO.getAlbumId();
            String title = albumDO.getAlbumName();

            String localFolder = TUJIDAO_LOCAL_PREFIX + title;
            // 若文件夹路径不存在，则新建
            File file = new File(localFolder);
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    log.error("==>localFolder={} 创建文件路径失败", localFolder);
                }
            }
            for (int i = 0; i <= total; i++) {
                String onlinePath = TUJIDAO_IMG_URL_PREFIX + num + "/" + i + ".jpg";
                String localPath = localFolder + "/" + i + ".jpg";

                // 幂等，若当前文件未下载，则进行下载
                File file2 = new File(localPath);
                if (!file2.exists()) {
//                    executorService.execute(() -> {
                    ReptileUtil.ioDownload(localPath, onlinePath);
//                    });
                }
            }
//            albumDO.setState(StateTypeEnum.ANALYSIS.getSeq());
//            tujidaoAlbumMapper.updateById(albumDO);
//            log.info("==>albumId={} 已下载完成", num);
        });
        return "success";
    }

    @Override
    public String doDownloadCover() {
        String localFolder = "D:/图集岛爬虫封面/";
        // 若文件夹路径不存在，则新建
        File file = new File(localFolder);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                log.error("==>localFolder={} 创建文件路径失败", localFolder);
            }
        }
        final int startInt = 32181;
        final int endInt = 33073;

        for (int i = startInt; i <= endInt; i++) {

            String onlinePath = TUJIDAO_IMG_URL_PREFIX + i + "/0.jpg";
            String localPath = localFolder + "/" + i + "-0.jpg";

            // 幂等，若当前文件未下载，则进行下载
            File file2 = new File(localPath);
            if (!file2.exists()) {
                ReptileUtil.ioDownload(onlinePath, localPath);
            }
        }
        return "success";
    }

    @Override
    public String doPreDownload() {
        final int startInt = 32181;
        final int endInt = 33073;
        for (int i = startInt; i <= endInt; i++) {
            this.downloadByAlbumId(String.valueOf(i));
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
            String localFolder = TUJIDAO_LOCAL_PREFIX + albumName;
            // 若文件夹路径不存在，则新建
            File file = new File(localFolder);
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    log.error("==>localFolder={} 创建文件路径失败", localFolder);
                }
            }
            for (int i = 0; i <= total; i++) {
                String onlinePath = "D:/图集岛爬虫Preview/" + albumId + "/" + i + ".jpg";
                String localPath = localFolder + "/" + i + ".jpg";

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
        String localFolder = "D:/图集岛爬虫Preview/" + albumId + "/";
        // 若文件夹路径不存在，则新建
        File file = new File(localFolder);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                log.error("==>localFolder={} 创建文件路径失败", localFolder);
            }
        }

        for (int i = 0; ; i++) {
            String onlinePath = TUJIDAO_IMG_URL_PREFIX + albumId + "/" + i + ".jpg";
            String localPath = localFolder + i + ".jpg";

            // 幂等，若当前文件未下载，则进行下载
            File file2 = new File(localPath);

            if (!file2.exists()) {
                if (!ReptileUtil.ioDownload(onlinePath, localPath)) {
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
