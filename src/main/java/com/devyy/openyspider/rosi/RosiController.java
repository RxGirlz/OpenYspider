package com.devyy.openyspider.rosi;

import com.devyy.openyspider.common.DownImageThread;
import com.devyy.openyspider.rosi.dao.RosiImgJpaDAO;
import com.devyy.openyspider.rosi.dao.RosiAlbumJpaDAO;
import com.devyy.openyspider.rosi.model.RosiAlbumDO;
import com.devyy.openyspider.rosi.model.RosiImgDO;
import com.devyy.openyspider.rosi.model.RosiTypeEnum;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * ROSI-爬虫 HTTP 调用接口
 *
 * @author zhangyiyang
 * @since 2019-09-13
 */
@RestController
@RequestMapping("/rosi")
public class RosiController {

    private static final Logger logger = LoggerFactory.getLogger(RosiController.class);

    @Autowired
    private RosiAlbumJpaDAO rosiAlbumJpaDAO;
    @Autowired
    private RosiImgJpaDAO rosiImgJpaDAO;

    /**
     * ROSI写真-离线 HTML 目录
     */
    private static final String OFFLINE_HTML_PREFIXS = "D:/reptile4ROSI/html_dump/";
    /**
     * ROSI写真-本地存储路径前缀（根据情况自定义）
     */
    private static final String ROSIMM_LOCAL_PREFIX = "D:/ROSI爬虫/";

    /**
     * Step1：解析 HTML 文件到 tbl_rosi_album
     * <p>
     * columns：3704
     */
    @PostMapping("/step1")
    public String step1() {
        // 离线 HTML 需手动预处理
        // 1. ROSI写真
        // RosiTypeEnum curType = RosiTypeEnum.ROSIMM;
        // String offlineHtml = OFFLINE_HTML_PREFIXS + "rosimm/1-20.html";
        // String offlineHtml = OFFLINE_HTML_PREFIXS + "rosikz/21-48.html";

        // 2. ROSI口罩
        // RosiTypeEnum curType = RosiTypeEnum.ROSIKZ;
        // String offlineHtml = OFFLINE_HTML_PREFIXS + "rosikz/1-14.html";

        // 3. 情趣系列
        RosiTypeEnum curType = RosiTypeEnum.ROSISEX;
        String offlineHtml = OFFLINE_HTML_PREFIXS + "rosisex/1.html";
        logger.info("==>offlineHtml={}", offlineHtml);

        Document document = null;
        try {
            File htmlFile = new File(offlineHtml);
            document = Jsoup.parse(htmlFile, "UTF-8");
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        if (Objects.nonNull(document)) {
            document.getElementById("sliding").children().forEach(slidingElement -> {
                Element aElement = slidingElement.getElementsByClass("p-title").first();
                String albumName = aElement.text();
                String albumUrl = aElement.getElementsByTag("a").first().attr("href");
                String picNum = slidingElement.getElementsByClass("picnum").first().text();
                Integer albumNum = Integer.parseInt(picNum.replace("P", ""));
                String dataIdStr = slidingElement.getElementsByTag("li").attr("data-id");
                Integer dataId = Integer.parseInt(dataIdStr);

                RosiAlbumDO rosiDO = new RosiAlbumDO();
                rosiDO.setAlbumName(albumName);
                rosiDO.setAlbumUrl(albumUrl);
                rosiDO.setAlbumNum(albumNum);
                rosiDO.setDataId(dataId);
                rosiDO.setAlbumType(curType.getSeq());

                // 幂等，保证记录数唯一
                if (Objects.isNull(rosiAlbumJpaDAO.findByDataIdEquals(rosiDO.getDataId()))) {
                    rosiAlbumJpaDAO.save(rosiDO);
                    logger.info("albumName={},albumUrl={},albumNum={},dataId={}", albumName, albumUrl, albumNum, dataId);
                } else {
                    logger.info("dataId={}已存在", rosiDO.getDataId());
                }
            });
        }
        return "success";
    }

    /**
     * Step2：解析图片真实路径到 tbl_rosiimg_album
     * <p>
     * 使用工具：ChromeDriver、selenium、登录网站会员（￥6.00）
     */
    @PostMapping("/step2")
    public String step2() {
        // 配置 chromedriver.exe 路径
        System.setProperty("webdriver.chrome.driver", "C:/Users/DEVYY/Documents/chromedriver_win32/chromedriver.exe");

        // 用于断点续传
//        final int minDataId = 1;
//        final int maxDataId = 619;
//        List<RosiAlbumDO> rosiDOList = rosiAlbumJpaDAO.findAllByDataIdBetweenOrderByDataIdDesc(minDataId, maxDataId);


        // 启动一个 chrome 实例
        WebDriver webDriver = new ChromeDriver();
        // 设置超时时间为 10s
        webDriver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
        webDriver.get("https://www.rosmm33.com/rosimm");

        // wait 35s 输入账号密码
        this.waitSeconds(35);

        rosiAlbumJpaDAO.findAllByAlbumTypeEquals(RosiTypeEnum.ROSISEX.getSeq()).forEach(rosiDO -> {
            Integer dataId = rosiDO.getDataId();
            // 访问网址
            String url = "https://www.rosmm33.com" + rosiDO.getAlbumUrl();
            try {
                webDriver.get(url);
            }
            // 此处捕获所有 Throwable 因为并不需要关心，还会中断程序
            catch (Throwable e) {
                logger.warn(e.getMessage().substring(0, 30));
            }

            // wait 2s 加载动态页面
            this.waitSeconds(2);

            Document document = Jsoup.parse(webDriver.getPageSource());
            Elements imgStringElements = document.getElementById("imgString").children();
            for (int i = 0, j = 1; i < imgStringElements.size(); i++, j++) {
                Element e = imgStringElements.get(i);
                String onlinePath = e.attr("src");

                RosiImgDO rosiImgDO = new RosiImgDO();
                rosiImgDO.setDataId(dataId);
                rosiImgDO.setImgName(j + ".jpg");
                rosiImgDO.setImgUrl(onlinePath);

                // 幂等，保证记录数唯一
                if (rosiImgJpaDAO.findByImgUrlEquals(rosiImgDO.getImgUrl()) == null) {
                    rosiImgJpaDAO.save(rosiImgDO);
                    logger.info("dataId={},imgName={},imgUrl={}", rosiImgDO.getDataId(), rosiImgDO.getImgName(), rosiImgDO.getImgUrl());
                } else {
                    logger.info("imgUrl={}已存在", rosiImgDO.getImgUrl());
                }
            }
        });
        // wait 60s 不要浏览器
        this.waitSeconds(60);

        return "success";
    }

    /**
     * Step3：因为 Step2 可能会触发网站反爬机制。为避免漏爬数据，需反查两表数据
     * <p>
     * Table：tbl_rosi_album & tbl_rosiimg_album
     */
    @PostMapping("/step3")
    public String step3() {
        logger.info("==>step3() 开始反查···");
        List<RosiAlbumDO> list = rosiAlbumJpaDAO.findAllByAlbumTypeEquals(RosiTypeEnum.ROSISEX.getSeq());
        for (RosiAlbumDO rosiDO : list) {
            int num = rosiDO.getAlbumNum();
            Integer dataId = rosiDO.getDataId();

            List<RosiImgDO> rosiImgDOList = rosiImgJpaDAO.findAllByDataIdEquals(dataId);
            int size = rosiImgDOList.size();
            if (size != num) {
                logger.warn("==>dataId={} 数量不符，需要复查 RosiImg表={} Rosi表={}", dataId, size, num);
            }
        }
        return "success";
    }

    /**
     * Step4：获取果实，愉快地下载图片
     * <p>
     * columns：134803
     */
    @PostMapping("/step4")
    public String step4() {
        ExecutorService service = Executors.newFixedThreadPool(8);

        rosiAlbumJpaDAO.findAll().forEach(rosiAlbumDO -> {
            Integer dataId = rosiAlbumDO.getDataId();
            logger.info("==>dataId={}", dataId);

            int albumType = rosiAlbumDO.getAlbumType();
            // 文件夹名
            String localFolder = getFolderStr(albumType) + rosiAlbumDO.getAlbumName() + "/";
            // 若文件夹路径不存在，则新建
            File file = new File(localFolder);
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    logger.error("==>dataId={},localFolder={} 创建文件路径失败", dataId, localFolder);
                }
            }

            rosiImgJpaDAO.findAllByDataIdEquals(dataId).forEach(rosiImgDO -> {
                String onlinePath = rosiImgDO.getImgUrl();
                String localPath = localFolder + rosiImgDO.getImgName();

                // 幂等，若当前文件未下载，则进行下载
                File file2 = new File(localPath);
                if (!file2.exists()) {
                    service.execute(new DownImageThread(onlinePath, localPath));
                }
            });
        });

        return "success";
    }

    /**
     * 获取文件夹名
     */
    private String getFolderStr(int albumType) {
        RosiTypeEnum typeEnum = RosiTypeEnum.getEnumBySeq(albumType);
        return ROSIMM_LOCAL_PREFIX + typeEnum.getDesc() + "/";

    }

    /**
     * 线程睡眠
     *
     * @param seconds 秒
     */
    private void waitSeconds(int seconds) {
        try {
            logger.info("==>waitSeconds {}s", seconds);
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
        }
    }
}
