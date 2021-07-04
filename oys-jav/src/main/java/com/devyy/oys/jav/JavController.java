package com.devyy.oys.jav;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.devyy.oys.jav.dao.IJavMapper;
import com.devyy.oys.jav.domain.JavEntityDO;
import com.devyy.oys.srarter.core.enums.StateTypeEnum;
import com.devyy.oys.srarter.core.util.ReptileUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@RestController
@RequestMapping("/jav")
@Api(tags = "Jav爬虫")
public class JavController {

    private static final String JAV_HOST = "https://lib.javbuff.work";

    private static final String JAV_LIST_PAGE_FORMAT = "%s/cn/search/%s/p%s.html";

    @Autowired
    private IJavMapper javMapper;

    @PostMapping("/step1")
    @ApiOperation(value = "扫描实体")
    public void doScanEntities() {
        // SSNI
        final String avPrefix = "IPX";
//        final String[] retryPages = null;
        final String[] retryPages = {"20"};

        if (Objects.isNull(retryPages)) {
            // 获取到最大页数
            String endPageURL = String.format(Locale.ENGLISH, JAV_LIST_PAGE_FORMAT, JAV_HOST, avPrefix, 1000);
            Document endPageHtml = null;
            try {
                endPageHtml = Jsoup.connect(endPageURL).get();
            } catch (IOException e) {
                log.error("==>JavController#step1 endPagURL={} e=", endPageURL, e);
            }
            int endPage = 1;
            if (Objects.nonNull(endPageHtml)) {
                String endPageStr = endPageHtml.getElementsByClass("current").get(0).text();
                endPage = Integer.parseInt(endPageStr);
                log.info("==>endPage={}", endPage);
            }
            // 从第1页开始
            doScanByRange(avPrefix, 1, endPage);
        } else {
            for (String page : retryPages) {
                IdWorker.getId();
                doScanByRange(avPrefix, Integer.parseInt(page), Integer.parseInt(page));
            }
        }
    }

    private void doScanByRange(String idPrefix, int start, int end) {
        for (int i = start; i <= end; i++) {
            String currentUrl = String.format(Locale.ENGLISH, JAV_LIST_PAGE_FORMAT, JAV_HOST, idPrefix, i);
            log.info("==>currentUrl={}", currentUrl);
            Document document = null;
            try {
                document = Jsoup.connect(currentUrl).get();
            } catch (IOException e) {
                log.error("==>JavController#step1 currentUrl={} e=", currentUrl, e);
            }
            if (Objects.nonNull(document)) {
                Elements elements = document.getElementsByClass("col-md-2 col-sm-2 col-xs-4 text-center ff-vod-img-new ff-col");
                for (Element element : elements) {
                    Element h4 = element.getElementsByTag("h4").get(0).getElementsByTag("a").get(0);
                    String href = h4.attr("href");
                    String pageUrl = JAV_HOST + href;
                    String id = h4.text();
                    String title = h4.attr("title");
                    // 构造实体
                    JavEntityDO javEntityDO = new JavEntityDO();
                    javEntityDO.setId(id);
                    javEntityDO.setPageUrl(pageUrl);
                    javEntityDO.setTitle(title);
                    javEntityDO.setState(StateTypeEnum.STARTED.getSeq());

                    // 幂等，保证记录数唯一
                    if (Objects.isNull(javMapper.selectById(id))) {
                        javMapper.insert(javEntityDO);
                        log.info("id={} 同步成功,pageUrl={}", id, javEntityDO.getPageUrl());
                    } else {
                        log.info("id={} 已存在", id);
                    }
                }
            }
        }
    }

    @PostMapping("/step2")
    @ApiOperation(value = "获取详情")
    public void doGetDetails() {
        ExecutorService service = Executors.newFixedThreadPool(8);
        QueryWrapper<JavEntityDO> wrapper = new QueryWrapper<>();
        wrapper.select().eq("state", StateTypeEnum.STARTED.getSeq());
        javMapper.selectList(wrapper).forEach(javEntityDO -> {
            String pageUrl = javEntityDO.getPageUrl();
//        String pageUrl = "https://lib.javbuff.work/cn/JavBuff/7sz3/";
//        JavEntityDO javEntityDO = new JavEntityDO();
            service.submit(() -> {
                doGetDetail(javEntityDO, pageUrl);
            });
        });
    }

    private void doGetDetail(JavEntityDO javEntityDO, String pageUrl) {
        Document document = null;
        try {
            document = Jsoup.connect(pageUrl).get();
        } catch (IOException e) {
            log.error("==>JavController#step2 pageUrl={} e=", pageUrl, e);
        }
        if (Objects.nonNull(document)) {
            // 封面 URL
            Element coverElement = document.getElementsByClass("img-responsive img-thumbnail ff-img").get(0);
            String coverUrl = coverElement.attr("data-original");
            javEntityDO.setCoverUrl(coverUrl);

            // 识别码 出版商 类型 系列 发售日 时长 剧情
            Element element = document.getElementsByClass("dl-horizontal").get(0);
            Elements dt = element.getElementsByTag("dt");
            Elements dd = element.getElementsByTag("dd");
            for (int i = 0; i < dt.size(); i++) {
                String key = dt.get(i).text();
                String value = dd.get(i).text();
                if (key.startsWith("出版商")) {
                    javEntityDO.setStudio(value);
                } else if (key.startsWith("类型")) {
                    javEntityDO.setGenre(value);
                } else if (key.startsWith("系列")) {
                    javEntityDO.setLabel(value);
                } else if (key.startsWith("发售日")) {
                    javEntityDO.setReleaseDate(value);
                } else if (key.startsWith("时长")) {
                    javEntityDO.setLength(value);
                }
            }
            // 演员
            Elements starsElements = document.getElementsByClass("col-md-22 col-sm-2 col-xs-4 text-center ff-col ff-vod-img-hot1a");
            if (Objects.nonNull(starsElements)) {
                javEntityDO.setStars(starsElements.text());
            }
            // 剧情
            Element storyElement = document.getElementsByClass("lead vod-content").get(0);
            String story = storyElement.text();
            javEntityDO.setStory(story);

            // 更新表
            javEntityDO.setState(StateTypeEnum.ANALYSIS.getSeq());
            javMapper.updateById(javEntityDO);
            log.info("updateById success javEntityDO={}", JSON.toJSONString(javEntityDO));
        }
    }

    @PostMapping("/step3")
    @ApiOperation(value = "下载封面")
    public void doDownCovers() {
        // 若文件夹路径不存在，则新建
        File file = new File("D:/Jav");
        if (!file.exists()) {
            if (!file.mkdirs()) {
                log.error("==>localFolder={} 创建文件路径失败", "D:/Jav");
            }
        }
        ExecutorService service = Executors.newFixedThreadPool(8);
        QueryWrapper<JavEntityDO> wrapper = new QueryWrapper<>();
        wrapper.select().eq("state", StateTypeEnum.ANALYSIS.getSeq());

        javMapper.selectList(wrapper).forEach(javEntityDO -> {
            String id = javEntityDO.getId();
            String coverUrl = javEntityDO.getCoverUrl();
            service.submit(() -> {
                String localPath = String.format(Locale.ENGLISH,
                        "D:/Jav/%s/%s.jpg", id.split("-")[0], id);
                // 幂等，若当前文件未下载，则进行下载
                File file2 = new File(localPath);
                if (!file2.exists()) {
                    ReptileUtil.ioDownload(coverUrl, localPath);
                    javEntityDO.setState(StateTypeEnum.DONE.getSeq());
                    javMapper.updateById(javEntityDO);
                }
            });
        });
    }
}
