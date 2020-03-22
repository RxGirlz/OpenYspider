package com.devyy.oys.yande;

import com.devyy.oys.core.enums.StateTypeEnum;
import com.devyy.oys.core.util.ReptileUtil;
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

/**
 * @since 2019-12-01
 */
@Slf4j
@Service
public class YandeService implements IYandeService {

    private static final String YANDE_REQUEST_URL = "https://yande.re/post?page=";

    @Autowired
    private IYandeImageMapper yandeImageMapper;

    @Override
    public String doScanAlbums() {
        return null;
    }

    @Override
    public String doScanImages() {
        for (int i = 1; i <= 12280; i++) {
            String url = YANDE_REQUEST_URL + i;
            log.info("==>url={}", url);
            Document document = null;
            try {
                document = Jsoup.connect(url).get();
            } catch (IOException e) {
                log.error("Jsoup Error: {}", e.getMessage());
            }

            if (Objects.nonNull(document)) {
                document.getElementById("post-list-posts").children().forEach(element -> {
                    // p572390
                    String pid = element.getElementsByTag("li").get(0).attr("id");
                    // https://files.yande.re/image/cf1581b36e2b5ea5b223cfa358907189/yande.re%20572390%20high_school_fleet%20katsuta_satoko%20megane%20noma_machiko%20seifuku%20tagme%20uchida_mayumi%20uda_megumi%20yagi_tsugumi%20yamashita_hideko.jpg
                    String imgUrl = element.getElementsByClass("directlink").get(0).attr("href");

                    YandeImageDO yandeDO = new YandeImageDO();
                    yandeDO.setImgName(pid);
                    yandeDO.setImgUrl(imgUrl);
                    yandeDO.setState(StateTypeEnum.STARTED.getSeq());

                    // 幂等，保证记录数唯一
                    Map<String, Object> queryMap = new HashMap<>(1);
                    queryMap.put("img_name", pid);
                    if (CollectionUtils.isEmpty(yandeImageMapper.selectByMap(queryMap))) {
                        yandeImageMapper.insert(yandeDO);
                        log.info("pid={} imgUrl={}", pid, imgUrl);
                    } else {
                        log.warn("记录已存在 pid={} imgUrl={}", pid, imgUrl);
                    }
                });
            }
        }
        return "success";
    }

    @Override
    public String doDownload() {
        ExecutorService service = Executors.newFixedThreadPool(12);

        Map<String, Object> queryMap = new HashMap<>(1);
        queryMap.put("state", StateTypeEnum.STARTED.getSeq());
        yandeImageMapper.selectByMap(queryMap).forEach(bean -> {
            String onlinePath = bean.getImgUrl();
            // D:/Yande爬虫/p572/p572406.jpg
            String localFolder;
            if (bean.getImgName().length() >= 4) {
                localFolder = "D:/Yande爬虫/" + bean.getImgName().substring(0, 4);
            } else {
                localFolder = "D:/Yande爬虫/p000";
            }

            // 若文件夹路径不存在，则新建
            File file = new File(localFolder);
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    log.error("==>localFolder={} 创建文件路径失败", localFolder);
                }
            }
            String localPath = localFolder + "/" + bean.getImgName() + ".jpg";
            // 幂等，若当前文件未下载，则进行下载
            File file2 = new File(localPath);
            if (!file2.exists()) {
                service.execute(() -> {
                    // 下载中-便于线程宕掉后回溯
                    bean.setState(StateTypeEnum.DOWNLOADING.getSeq());
                    yandeImageMapper.updateById(bean);
                    // 下载
                    if (ReptileUtil.ioDownload(onlinePath, localPath)) {
                        bean.setState(StateTypeEnum.DONE.getSeq());
                    } else {
                        bean.setState(StateTypeEnum.STARTED.getSeq());
                    }
                    yandeImageMapper.updateById(bean);
                });
            }
        });
        return "success";
    }
}
