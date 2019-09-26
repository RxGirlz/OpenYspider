package com.devyy.openyspider.tujidao;

import com.devyy.openyspider.common.DownImageThread;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

/**
 * 图集岛-爬虫 HTTP 调用接口
 *
 * @author zhangyiyang
 * @since 2019-09-12
 */
@RestController
@RequestMapping("/tujidao")
public class TujidaoController {

    private static final Logger logger = LoggerFactory.getLogger(TujidaoController.class);

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
    private static final String TUJIDAO_LOCAL_PREFIX = "D:/图集岛爬虫（27865-）/";
    /**
     * 图集岛-图片真实路径前缀
     */
    private static final String TUJIDAO_IMG_URL_PREFIX = "https://ii.hywly.com/a/1/";
    /**
     * 文件不合法名正则
     */
    private static final Pattern FILE_PATTERN = Pattern.compile("[\\\\/:*?\"<>|]");

    @Autowired
    private TujidaoJpaDAO tujidaoJpaDAO;

    /**
     * Step1：解析 HTML 页面元素并持久化到数据库
     */
    @PostMapping("/step1")
    public String step1() {
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
        final int MAX_PAGE = 3;
        for (int i = MIN_PAGE; i <= MAX_PAGE; i++) {
            try {
                document = Jsoup.connect(TUJIDAO_URL_PREFIX + i).cookies(cookiesMap).get();
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
            if (Objects.nonNull(document)) {
                document.getElementsByClass("c1").first().getElementsByTag("a").forEach(e -> {
                    String eText = e.text();
                    String[] eTextArray = eText.split(" ");

                    TujidaoDO tujidaoDO = new TujidaoDO();
                    tujidaoDO.setNumber(Integer.parseInt(e.attr("href").replace("/a/?id=", "")));
                    tujidaoDO.setTitle(tujidaoDO.getNumber() + "-" + rmIllegalName(eText));
                    tujidaoDO.setTotal(Integer.parseInt(rmLeftRightP(eTextArray[0])));

                    // 幂等，保证记录数唯一
                    if (Objects.isNull(tujidaoJpaDAO.findByNumberEquals(tujidaoDO.getNumber()))) {
                        tujidaoJpaDAO.save(tujidaoDO);
                        logger.info("number={},total={},type={},title={}", tujidaoDO.getNumber(), tujidaoDO.getTotal(), tujidaoDO.getType(), tujidaoDO.getTitle());
                    } else {
                        logger.info("number={}已存在", tujidaoDO.getNumber());
                    }
                });
            }
        }
        return "success";
    }

    /**
     * Step2：区间下载相册
     */
    @PostMapping("/step2")
    public String step2() {
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        final int startInt = 27865;
        final int endInt = 30000;
        tujidaoJpaDAO.findAllByNumberBetweenOrderByNumberDesc(startInt, endInt).forEach(albumDO -> {
            int total = albumDO.getTotal();
            int num = albumDO.getNumber();
            String title = albumDO.getTitle();

            String localFolder = TUJIDAO_LOCAL_PREFIX + title;
            // 若文件夹路径不存在，则新建
            File file = new File(localFolder);
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    logger.error("==>localFolder={} 创建文件路径失败", localFolder);
                }
            }
            for (int i = 0; i <= total; i++) {
                String onlinePath = TUJIDAO_IMG_URL_PREFIX + num + "/" + i + ".jpg";
                String localPath = localFolder + "/" + i + ".jpg";

                // 幂等，若当前文件未下载，则进行下载
                File file2 = new File(localPath);
                if (!file2.exists()) {
                    executorService.execute(new DownImageThread(onlinePath, localPath));
                }
            }
        });
        return "success";
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
