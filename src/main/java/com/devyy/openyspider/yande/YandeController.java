package com.devyy.openyspider.yande;

import com.devyy.openyspider.common.DownImageThread;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * https://yande.re/post
 *
 * @author zhangyiyang
 * @since 2019-09-24
 */
@RestController
@RequestMapping("/yande")
public class YandeController {

    private static final Logger logger = LoggerFactory.getLogger(YandeController.class);

    @Autowired
    private YandeJpaDAO yandeJpaDAO;

    private static final String YANDE_REQUEST_URL = "https://yande.re/post?page=";

    /**
     * Step1: 解析并持久化到 tbl_mzsock_album2
     * <p>
     * columns：461338
     */
    @PostMapping("/step1")
    public String step1() {
        for (int i = 1; i <= 30; i++) {
            String url = YANDE_REQUEST_URL + i;
            logger.info("==>url={}", url);
            Document document = null;
            try {
                document = Jsoup.connect(url).get();
            } catch (IOException e) {
                logger.error("Jsoup Error: {}", e.getMessage());
            }

            // <li style="width: 160px;" id="p572390" class=" javascript-hide creator-id-175701">
            //   <div class="inner" style="width: 150px; height: 150px;">
            //     <a class="thumb" href="/post/show/572390"
            //       ><img
            //         src="https://assets.yande.re/data/preview/cf/15/cf1581b36e2b5ea5b223cfa358907189.jpg"
            //         style="margin-left: 0px;"
            //         alt="Rating: Safe Score: 2 Tags: high_school_fleet katsuta_satoko megane noma_machiko seifuku tagme uchida_mayumi uda_megumi yagi_tsugumi yamashita_hideko User: saemonnokami"
            //         class="preview"
            //         title="Rating: Safe Score: 2 Tags: high_school_fleet katsuta_satoko megane noma_machiko seifuku tagme uchida_mayumi uda_megumi yagi_tsugumi yamashita_hideko User: saemonnokami"
            //         width="113"
            //         height="150"
            //       /><span class="plid">#pl https://yande.re/post/show/572390</span></a
            //     >
            //   </div>
            //   <a
            //     class="directlink largeimg"
            //     href="https://files.yande.re/image/cf1581b36e2b5ea5b223cfa358907189/yande.re%20572390%20high_school_fleet%20katsuta_satoko%20megane%20noma_machiko%20seifuku%20tagme%20uchida_mayumi%20uda_megumi%20yagi_tsugumi%20yamashita_hideko.jpg"
            //     ><span class="directlink-info"
            //       ><img
            //         class="directlink-icon directlink-icon-large"
            //         src="https://assets.yande.re/assets/ddl_large-b6a70700b689a23392964bd25e0993d9229c3ffec4c7a4ef5dbe10a516c862d9.gif"
            //         alt=""/><img
            //         class="directlink-icon directlink-icon-small"
            //         src="https://assets.yande.re/assets/ddl-ee9c03fa35a341df36dbc9d247ee4cd820e3534fa21e9f6127fc9dfcd980658e.gif"
            //         alt=""/><img
            //         class="parent-display"
            //         src="https://assets.yande.re/assets/post-star-parent-d285dd41318c1fe1c500726fc01b57e9e600c88f1f2d039eee54015d0e85e499.gif"
            //         alt=""/><img
            //         class="child-display"
            //         src="https://assets.yande.re/assets/post-star-child-e9a0fc526fbf75a32a2870e376fe953d9edd83d503a0a5d64f74b725ba2a874c.gif"
            //         alt=""/><img
            //         class="flagged-display"
            //         src="https://assets.yande.re/assets/post-star-flagged-32d544649ddc8d22df6a0c695637c0aba9d25efefd40c753a9e95f63d883f2d5.gif"
            //         alt=""/><img
            //         class="pending-display"
            //         src="https://assets.yande.re/assets/post-star-pending-3c474adbd4112537d620711311acde4af1d549cd795c8bdea14853205cba56ca.gif"
            //         alt=""/></span
            //     ><span class="directlink-res">1496 x 1978</span></a
            //   >
            // </li>
            if (Objects.nonNull(document)) {
                document.getElementById("post-list-posts").children().forEach(element -> {
                    // p572390
                    String pid = element.getElementsByTag("li").get(0).attr("id");
                    // https://files.yande.re/image/cf1581b36e2b5ea5b223cfa358907189/yande.re%20572390%20high_school_fleet%20katsuta_satoko%20megane%20noma_machiko%20seifuku%20tagme%20uchida_mayumi%20uda_megumi%20yagi_tsugumi%20yamashita_hideko.jpg
                    String imgUrl = element.getElementsByClass("directlink").get(0).attr("href");

                    YandeDO yandeDO = new YandeDO();
                    yandeDO.setImgName(pid);
                    yandeDO.setImgUrl(imgUrl);

                    // 幂等，保证记录数唯一
                    if (Objects.isNull(yandeJpaDAO.findByImgNameEquals(yandeDO.getImgName()))) {
                        yandeJpaDAO.save(yandeDO);
                        logger.info("pid={} imgUrl={}", pid, imgUrl);
                    } else {
                        logger.warn("记录已存在 pid={} imgUrl={}", pid, imgUrl);
                    }
                });
            }
        }
        return "success";
    }

    /**
     * Step2: 分文件夹下载到本地
     */
    @PostMapping("/step2")
    public String step2() {
        ExecutorService service = Executors.newFixedThreadPool(8);
        // 分页查询 500000 张  5000 * 100
        for (int page = 0; page < 100; page++) {
            PageRequest pageRequest = PageRequest.of(page, 5000);
            yandeJpaDAO.findAll(pageRequest).forEach(bean -> {
                String onlinePath = bean.getImgUrl();
                // D:/Yande爬虫/p572/p572406.jpg
                String localFolder = null;
                try {
                    localFolder = "D:/Yande爬虫/" + bean.getImgName().substring(0, 4);
                } catch (StringIndexOutOfBoundsException e) {
                    logger.warn("StringIndexOutOfBoundsException bean.getImgName()={}", bean.getImgName());
                }
                if (Objects.nonNull(localFolder)) {
                    // 若文件夹路径不存在，则新建
                    File file = new File(localFolder);
                    if (!file.exists()) {
                        if (!file.mkdirs()) {
                            logger.error("==>localFolder={} 创建文件路径失败", localFolder);
                        }
                    }
                    String localPath = localFolder + "/" + bean.getImgName() + ".jpg";
                    // 幂等，若当前文件未下载，则进行下载
                    File file2 = new File(localPath);
                    if (!file2.exists()) {
                        service.execute(new DownImageThread(onlinePath, localPath));
                    }
                }
            });
        }
        return "success";
    }
}
