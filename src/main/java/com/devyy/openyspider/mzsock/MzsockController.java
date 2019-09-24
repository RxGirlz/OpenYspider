package com.devyy.openyspider.mzsock;

import com.devyy.openyspider.common.ReptileUtil;
import com.devyy.openyspider.mzsock.dao.MzsockAlbumJpaDAO;
import com.devyy.openyspider.mzsock.dao.MzsockImgJpaDAO;
import com.devyy.openyspider.mzsock.model.MzsockAlbumDO;
import com.devyy.openyspider.mzsock.model.MzsockImgDO;
import com.devyy.openyspider.mzsock.model.MzsockTypeEnum;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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
 * http://mzsock.com/
 *
 * @since 2019-09-13
 */
@RestController
@RequestMapping("/mzsock")
public class MzsockController {

    private static final Logger logger = LoggerFactory.getLogger(MzsockController.class);

    @Autowired
    private MzsockAlbumJpaDAO mzsockJpaDAO;
    @Autowired
    private MzsockImgJpaDAO subMzsockJpaDAO;

    private static final String[] urls = {
            "http://mzsock.com/mv/",
            "http://mzsock.com/mv/page/2/",
            "http://mzsock.com/mv/page/3/",
            "http://mzsock.com/mv/page/4/",
            "http://mzsock.com/cy/",
            "http://mzsock.com/cy/page/2/",
            "http://mzsock.com/cy/page/3/",
            "http://mzsock.com/sw/",
            "http://mzsock.com/sw/page/2/",
            "http://mzsock.com/lz/",
            "http://mzsock.com/lz/page/2/",
            "http://mzsock.com/fbx/",
            "http://mzsock.com/fbx/page/2/",
            "http://mzsock.com/fbx/page/3/",
            "http://mzsock.com/fbx/page/4/",
            "http://mzsock.com/ydx/",
            "http://mzsock.com/ydx/page/2/",
            "http://mzsock.com/rzt/",
            "http://mzsock.com/cwzp/",
            "http://mzsock.com/cwzp/page/2/",
            "http://mzsock.com/cwzp/page/3/",
            "http://mzsock.com/cwzp/page/4/",
            "http://mzsock.com/cwzp/page/5/",
    };

    /**
     * Step1：解析并持久化到 tbl_mzsock_album2
     * <p>
     * columns：242
     */
    @PostMapping("/step1")
    public String step1() {
        for (String url : urls) {
            Integer curTypeInt = getEnumByUrl(url);
            Document document = null;
            try {
                document = Jsoup.connect(url).get();
            } catch (IOException e) {
                logger.error("Jsoup Error: {}", e.getMessage());
            }
            if (Objects.nonNull(document)) {
                document.getElementsByClass("post-thumbnail").forEach(ele -> {
                    String albumUrl = ele.getElementsByTag("a").attr("href");
                    String originAlbumTitle = ele.getElementsByTag("a").attr("title");
                    String albumTitle = ReptileUtil.rmIllegalName(Objects.equals(originAlbumTitle, "") ? "未命名" : originAlbumTitle);
                    Integer albumNum = Integer.parseInt(ele.getElementsByTag("span").text());

                    MzsockAlbumDO mzsockDO = new MzsockAlbumDO();
                    mzsockDO.setAlbumName(albumTitle);
                    mzsockDO.setAlbumNum(albumNum);
                    mzsockDO.setAlbumUrl(albumUrl);
                    mzsockDO.setAlbumType(curTypeInt);

                    // 幂等，保证记录数唯一
                    if (Objects.isNull(mzsockJpaDAO.findByAlbumUrlEquals(mzsockDO.getAlbumUrl()))) {
                        mzsockJpaDAO.save(mzsockDO);
                        logger.info("alnumUrl={} albumTitle={} albumNum={} type={}", albumUrl, albumTitle, albumNum, curTypeInt);
                    } else {
                        logger.warn("记录已存在 alnumUrl={} albumTitle={} albumNum={} type={}", albumUrl, albumTitle, albumNum, curTypeInt);
                    }
                });
            }
        }
        return "success";
    }

    /**
     * Step2：解析并持久化到 tbl_sub_mzsock_album2
     * <p>
     * columns：19988
     */
    @PostMapping("/step2")
    public String step2() {
        mzsockJpaDAO.findAll().forEach(mzsockDO -> {
            String originUrl = mzsockDO.getAlbumUrl();
            int ids = mzsockDO.getId().intValue();
            int num = mzsockDO.getAlbumNum();
            int pages = getPageNum(num);
            String url = originUrl;
            for (int i = 1, j = 1; i <= pages; i++) {
                if (i != 1) {
                    url = originUrl.replace(".html", "_") + i + ".html";
                }
                Document document = null;
                try {
                    document = Jsoup.connect(url).get();
                } catch (IOException e) {
                    logger.warn("Jsoup Error: url={}", url);
                }
                if (document != null) {
                    Elements elements = document.getElementsByClass("img_jz");

                    for (Element e : elements) {
                        String imgUrl = e.getElementsByTag("img").attr("src");
                        String imgName = j + ".jpg";
                        j++;

                        MzsockImgDO subMzsockDO = new MzsockImgDO();
                        subMzsockDO.setImgName(imgName);
                        subMzsockDO.setImgUrl(imgUrl);
                        subMzsockDO.setAlbumId(ids);

                        // 幂等，保证记录数唯一
                        if (subMzsockJpaDAO.findByImgUrlEquals(subMzsockDO.getImgUrl()) == null) {
                            subMzsockJpaDAO.save(subMzsockDO);
                            logger.info("albumId={} imgName={} imgUrl={}", ids, subMzsockDO.getImgName(), subMzsockDO.getImgUrl());
                        } else {
                            logger.info("albumId={} imgUrl={}已存在", ids, subMzsockDO.getImgUrl());
                        }
                    }
                }

            }
        });
        return "success";
    }

    /**
     * Step3：下载
     */
    @PostMapping("/step3")
    public String step3() {
        subMzsockJpaDAO.findAll().forEach(subMzsockDO -> {
            MzsockAlbumDO mzsockDO = mzsockJpaDAO.findById(subMzsockDO.getAlbumId().longValue()).get();
            // 文件夹名
            String localFolder = "D:/Mzsock爬虫/" + mzsockDO.getAlbumName() + "-[" + mzsockDO.getAlbumNum() + "P]/";
            // 若文件夹路径不存在，则新建
            File file = new File(localFolder);
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    logger.error("==>Id={},localFolder={} 创建文件路径失败", mzsockDO.getId(), localFolder);
                }
            }

            String onlinePath = subMzsockDO.getImgUrl();
            String localPath = localFolder + subMzsockDO.getImgName();

            // 幂等，若当前文件未下载，则进行下载
            File file2 = new File(localPath);
            if (!file2.exists()) {
                ReptileUtil.asyncDownload(onlinePath, localPath);
            }
        });

        return "success";
    }

    // 页数
    private int getPageNum(int n) {
        int ret = n / 5;
        if (ret * 5 < n) {
            ret += 1;
        }
        return ret;
    }

    private Integer getEnumByUrl(String url) {
        MzsockTypeEnum[] enums = MzsockTypeEnum.values();
        for (MzsockTypeEnum typeEnum : enums) {
            if (url.startsWith(typeEnum.getUrl())) {
                return typeEnum.getSeq();
            }
        }
        return null;
    }
}
