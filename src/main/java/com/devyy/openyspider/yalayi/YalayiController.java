package com.devyy.openyspider.yalayi;

import com.devyy.openyspider.common.ReptileUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

/**
 * 雅拉伊-爬虫 HTTP 调用接口
 *
 * @author zhangyiyang
 * @since 2019-09-13
 */
@RestController
@RequestMapping("/yalayi")
public class YalayiController {

    private static final Logger logger = LoggerFactory.getLogger(YalayiController.class);

    @Autowired
    private YalayiJpaDAO yalayiJpaDAO;

    /**
     * 雅拉伊-相册目录路径前缀
     */
    private static final String[] YALAYI_PREFIXS = {
//            "https://www.yalayi.com/gallery",
//            "https://www.yalayi.com/gallery/index_2.html",
//            "https://www.yalayi.com/gallery/index_3.html",
//            "https://www.yalayi.com/gallery/index_4.html",
//            "https://www.yalayi.com/gallery/index_5.html",
//            "https://www.yalayi.com/gallery/index_6.html",
//            "https://www.yalayi.com/gallery/index_7.html",
//            "https://www.yalayi.com/gallery/index_8.html",
//            "https://www.yalayi.com/gallery/index_9.html",

//            "https://www.yalayi.com/selected",
//            "https://www.yalayi.com/selected/index_2.html",
//            "https://www.yalayi.com/selected/index_3.html",
//            "https://www.yalayi.com/selected/index_4.html",
//            "https://www.yalayi.com/selected/index_5.html",
//            "https://www.yalayi.com/selected/index_6.html",
//            "https://www.yalayi.com/selected/index_7.html",

            "https://www.yalayi.com/free/",
    };

    /**
     * 雅拉伊-本地存储路径前缀（根据情况自定义）
     */
    private static final String YALAYI_LOCAL_PREFIX = "D:/雅拉伊爬虫/FREE/";

    /**
     * Step1：解析 HTML 页面元素并持久化到数据库
     */
    @PostMapping("/step1")
    public String step1() {
        Document document = null;
        for (String url : YALAYI_PREFIXS) {
            try {
                document = Jsoup.connect(url).get();
            } catch (IOException e) {
                logger.error("Jsoup Error: {}", e.getMessage());
            }
            if (Objects.nonNull(document)) {
                document.getElementsByClass("img-box").forEach(imgBoxElement -> {
                    String modelName = imgBoxElement.getElementsByClass("name").first().text();
                    String albumName = imgBoxElement.getElementsByClass("sub").first().text();
                    String albumUrl = imgBoxElement.getElementsByClass("sub").first()
                            .getElementsByTag("a").first().attr("href");
                    String albumUpdate = imgBoxElement.getElementsByClass("right").first().text();
                    String albumInfo = imgBoxElement.getElementsByClass("left").first().text();
                    String albumNumS = imgBoxElement.getElementsByClass("num").first().text();
                    Integer albumNum = Integer.parseInt(albumNumS.replace("p", ""));

                    YalayiDO yalayiDO = new YalayiDO();
                    yalayiDO.setModelName(modelName);
                    yalayiDO.setAlbumName(albumName);
                    yalayiDO.setAlbumUrl(albumUrl);
                    yalayiDO.setAlbumUpdate(albumUpdate);
                    yalayiDO.setAlbumInfo(albumInfo);
                    yalayiDO.setAlbumNum(albumNum);
                    yalayiDO.setAlbumType(YalayiTypeEnum.FREE.getSeq());

                    // 幂等，保证记录数唯一
                    if (Objects.isNull(yalayiJpaDAO.findByAlbumInfoEquals(yalayiDO.getAlbumInfo()))) {
                        yalayiJpaDAO.save(yalayiDO);
                        logger.info("modelName={},albumName={},albumUrl={},albumUpdate={},albumInfo={},albumNum={}",
                                modelName, albumName, albumUrl, albumUpdate, albumInfo, albumNum);
                    } else {
                        logger.info("albumInfo={}已存在", yalayiDO.getAlbumInfo());
                    }
                });
            }
        }
        return "success";
    }

    /**
     * Step2：解析并下载图片
     */
    @PostMapping("/step2")
    public String step2() {
        // 是否使用离线 HTML 文件
        final boolean isOffline = false;

        yalayiJpaDAO.findByAlbumTypeEquals(YalayiTypeEnum.FREE.getSeq()).forEach(yalayiDO -> {
            String onlineUrl = yalayiDO.getAlbumUrl();

            // 文件夹名
            String folderName = yalayiDO.getAlbumInfo() + "-" + yalayiDO.getAlbumName() + "-" + yalayiDO.getModelName() + "-" + yalayiDO.getAlbumNum() + "p/";
            String localFolder = YALAYI_LOCAL_PREFIX + folderName;

            // 若文件夹路径不存在，则新建
            File file = new File(localFolder);
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    logger.error("==>localFolder={} 创建文件路径失败", localFolder);
                }
            }

            Document document = null;
            try {
                if (isOffline) {
                    // 离线 HTML 文本
                    String offlineUrl = onlineUrl.replace("https://www.yalayi.com/gallery", "D:/雅拉伊爬虫/offlineHTML");
                    File offlineHtml = new File(offlineUrl);
                    document = Jsoup.parse(offlineHtml, "UTF-8", "");
                } else {
                    document = Jsoup.connect(onlineUrl).get();
                }
            } catch (IOException e) {
                logger.error("Jsoup Error: {}", e.getMessage());
            }
            if (Objects.nonNull(document)) {
                document.getElementsByClass("lazy").forEach(lazyElment -> {
                    String onlinePath = lazyElment.attr("data-src");

                    // 跳过最后一张广告图
                    if (!Objects.equals(onlinePath, "https://yly.hywly.com/ad/end.jpg")) {
                        String filePath = localFolder + onlinePath
                                .replace("https://yly.hywly.com/img/gallery/", "")
                                .replace("/", "-");

                        // 幂等，若当前文件未下载，则进行下载
                        File file2 = new File(filePath);
                        if (!file2.exists()) {
                            ReptileUtil.syncDownload(onlinePath, filePath);
                        }
                    }
                });
            }
        });
        return "success";
    }
}
