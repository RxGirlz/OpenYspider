package com.devyy.openyspider.integration.leetcode.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.devyy.openyspider.base.StateTypeEnum;
import com.devyy.openyspider.common.ReptileUtil;
import com.devyy.openyspider.integration.leetcode.domain.LeetCodeProblemDO;
import com.devyy.openyspider.integration.leetcode.domain.LeetCodeProblemDetailDO;
import com.devyy.openyspider.integration.leetcode.domain.LeetcodeImageDO;
import com.devyy.openyspider.integration.leetcode.enums.LeetcodeSideBarEnum;
import com.devyy.openyspider.integration.leetcode.gson.GetQuestionTranslationGson;
import com.devyy.openyspider.integration.leetcode.gson.ProblemsAllGson;
import com.devyy.openyspider.integration.leetcode.mapper.ILeetCodeProblemDetailMapper;
import com.devyy.openyspider.integration.leetcode.mapper.ILeetCodeProblemMapper;
import com.devyy.openyspider.integration.leetcode.mapper.ILeetcodeImageMapper;
import com.devyy.openyspider.integration.leetcode.service.ILeetcodeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @since 2019-02-06
 */
@Slf4j
@Component
public class LeetcodeService implements ILeetcodeService {
    private static final String FILE_FOLDER_NAME = "C:/Users/DEVYY/Documents/GitHub/翻译工程/Leetcode-Hub-beta/generator2";
    private static final String URL_PATTERN = ("[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]");

    private static final int PAID_ONLY = 1;

    @Value("classpath:json/problems-all.json")
    private org.springframework.core.io.Resource problemAllJson;

    @Value("classpath:json/getQuestionTranslation.json")
    private org.springframework.core.io.Resource getQuestionTranslationJson;

    @Resource
    private ILeetCodeProblemMapper leetCodeProblemMapper;
    @Resource
    private ILeetCodeProblemDetailMapper leetCodeProblemDetailMapper;
    @Resource
    private ILeetcodeImageMapper leetcodeImageMapper;

    @Override
    public String doScanProblems() {
        String problemAllStr = null;
        try {
            problemAllStr = IOUtils.toString(problemAllJson.getInputStream(), "UTF-8");
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        ProblemsAllGson problemsAllGson = JSONObject.parseObject(problemAllStr, ProblemsAllGson.class);

        if (Objects.nonNull(problemsAllGson)) {
            // 1345
            problemsAllGson.getStat_status_pairs().forEach(statStatusPairsBean -> {
                ProblemsAllGson.StatStatusPairsBean.StatBean statBean = statStatusPairsBean.getStat();
                ProblemsAllGson.StatStatusPairsBean.DifficultyBean difficultyBean = statStatusPairsBean.getDifficulty();

                LeetCodeProblemDO leetCodeProblemDO = LeetCodeProblemDO.builder()
                        .title(statBean.getQuestion__title())
                        .titleSlug(statBean.getQuestion__title_slug())
                        .paidOnly(statStatusPairsBean.isPaid_only())
                        .questionId((long) statBean.getQuestion_id())
                        .feQuestionId(statBean.getFrontend_question_id())
                        .difficulty(difficultyBean.getLevel())
                        .build();

                leetCodeProblemMapper.insert(leetCodeProblemDO);
            });
        }
        return "success";
    }

    @Override
    public String doScanTiTleCn() {
        String getQuestionTranslationStr = null;
        try {
            getQuestionTranslationStr = IOUtils.toString(getQuestionTranslationJson.getInputStream(), "UTF-8");
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        GetQuestionTranslationGson getQuestionTranslationGson
                = JSONObject.parseObject(getQuestionTranslationStr, GetQuestionTranslationGson.class);

        if (Objects.nonNull(getQuestionTranslationStr)) {
            getQuestionTranslationGson.getData().getTranslations().forEach(translationsBean -> {
                log.info("==>prblemId={} title={}", translationsBean.getQuestionId(), translationsBean.getTitle());
                QueryWrapper<LeetCodeProblemDO> codeProblemDOQueryWrapper = new QueryWrapper<>();
                codeProblemDOQueryWrapper.select()
                        .eq("question_id", translationsBean.getQuestionId())
                        .isNull("title_cn");

                LeetCodeProblemDO leetCodeProblemDO = leetCodeProblemMapper.selectOne(codeProblemDOQueryWrapper);
                if (Objects.nonNull(leetCodeProblemDO)) {
                    leetCodeProblemDO.setTitleCn(translationsBean.getTitle());
                    leetCodeProblemMapper.updateById(leetCodeProblemDO);
                }
            });
        }
        return "success";
    }

    @Override
    public String doScanProblemsDetail() {
        System.setProperty("webdriver.chrome.driver", "C:/Users/DEVYY/Documents/chromedriver_win32/chromedriver.exe");
        WebDriver webDriver = new ChromeDriver();
        // 设置超时时间为 10 s
        webDriver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
        webDriver.get("https://leetcode-cn.com/problems/two-sum/");
        // wait 30s 输入账号密码
        this.waitSeconds(30);

        QueryWrapper<LeetCodeProblemDO> problemDOQueryWrapper = new QueryWrapper<>();
        problemDOQueryWrapper.select().eq("paid_only", PAID_ONLY);

        leetCodeProblemMapper.selectList(problemDOQueryWrapper).forEach(leetCodeProblemDO -> {
            String url = "https://leetcode-cn.com/problems/" + leetCodeProblemDO.getTitleSlug();
            try {
                log.info("==>url {}", url);
                webDriver.get(url);
            }
            // 此处捕获所有 Throwable 因为并不需要关心，还会中断程序
            catch (Throwable e) {
                log.warn(e.getMessage().substring(0, 30));
            }

            Document document = Jsoup.parse(webDriver.getPageSource());
            if (Objects.nonNull(document)) {
                String htmlContent = document.getElementsByClass("content__1Y2H").outerHtml();
                log.info("==>htmlContent={}", htmlContent);

                LeetCodeProblemDetailDO leetCodeProblemDetailDO = new LeetCodeProblemDetailDO();
                leetCodeProblemDetailDO.setHtmlContent(htmlContent);
                leetCodeProblemDetailDO.setQuestionId(leetCodeProblemDO.getQuestionId());

                leetCodeProblemDetailMapper.insert(leetCodeProblemDetailDO);
            }
        });
        return "success";
    }

    @Override
    public String doTestVuePressBugs() {
        System.setProperty("webdriver.chrome.driver", "C:/Users/DEVYY/Documents/chromedriver_win32/chromedriver.exe");
        WebDriver webDriver = new ChromeDriver();
        // 设置超时时间为 10 s
        webDriver.manage().timeouts().pageLoadTimeout(3, TimeUnit.SECONDS);
        webDriver.get("http://localhost:8080/doc-leetcode/001-100/leetcode_1_two-sum.html");

        QueryWrapper<LeetCodeProblemDO> problemDOQueryWrapper = new QueryWrapper<>();
        problemDOQueryWrapper.select()
                .eq("paid_only", PAID_ONLY)
                .isNull("has_bug")
                .orderByAsc("question_id");
        leetCodeProblemMapper.selectList(problemDOQueryWrapper).forEach(leetCodeProblemDO -> {
            final String feQuestionId = leetCodeProblemDO.getFeQuestionId();
            String url = "http://localhost:8080/doc-leetcode" + getSidebarSliceByFeQuestionId(feQuestionId)
                    + "leetcode_" + feQuestionId + "_" + leetCodeProblemDO.getTitleSlug() + ".html";
            try {
                log.info("==>url {}", url);
                webDriver.get(url);
            }
            // 此处捕获所有 Throwable 因为并不需要关心，还会中断程序
            catch (Throwable e) {
                log.warn(e.getMessage().substring(0, 30));
            }

            Document document = Jsoup.parse(webDriver.getPageSource());
            if (Objects.nonNull(document)) {

                log.info("==>class theme-default-content content__default={}", document.getElementsByClass("theme-default-content content__default").outerHtml());
                int hasBug = 0;
                if (StringUtils.isEmpty(document.getElementsByClass("theme-default-content content__default").outerHtml())) {
                    hasBug = 1;
                    log.info("==>feQuestionId={} has Bug", feQuestionId);
                }
                leetCodeProblemDO.setHasBug(hasBug);

                leetCodeProblemMapper.updateById(leetCodeProblemDO);
            }
        });
        return "success";
    }

    /**
     * 根据 feQuestionId 获取 SidebarSlice
     *
     * @param feQuestionId feQuestionId
     * @return SidebarSlice
     */
    private String getSidebarSliceByFeQuestionId(String feQuestionId) {
        List<LeetcodeSideBarEnum> leetcodeSideBarEnums = LeetcodeSideBarEnum.getEnums();
        if (StringUtils.isNumeric(feQuestionId)) {
            int feQuestionIdNum = Integer.parseInt(feQuestionId);
            for (LeetcodeSideBarEnum leetcodeSideBarEnum : leetcodeSideBarEnums) {
                if (leetcodeSideBarEnum.getStartSeq() <= feQuestionIdNum
                        && feQuestionIdNum <= leetcodeSideBarEnum.getEndSeq()) {
                    return leetcodeSideBarEnum.getSidebarSlice();
                }
            }
        }
        return LeetcodeSideBarEnum.SIDEBAR_1301_1400.getSidebarSlice();
    }

    /**
     * 线程睡眠
     *
     * @param seconds 秒
     */
    private void waitSeconds(int seconds) {
        try {
            log.info("==>waitSeconds {}s", seconds);
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public String doScanAlbums() {
        return null;
    }

    private static final String URL1 = "https://s3-lc-upload.s3.amazonaws.com";
    private static final String URL2 = "https://assets.leetcode-cn.com";
    private static final String URL3 = "https://assets.leetcode.com";
    private static final String URL4 = "https://aliyun-lc-upload.oss-cn-hangzhou.aliyuncs.com";
    private static final String URL5 = "https://upload.wikimedia.org";
    private static final String URL6 = "http://upload.wikimedia.org";
    private static final Pattern URL_PATTERN1 = Pattern.compile(URL1 + URL_PATTERN);
    private static final Pattern URL_PATTERN2 = Pattern.compile(URL2 + URL_PATTERN);
    private static final Pattern URL_PATTERN3 = Pattern.compile(URL3 + URL_PATTERN);
    private static final Pattern URL_PATTERN4 = Pattern.compile(URL4 + URL_PATTERN);
    private static final Pattern URL_PATTERN5 = Pattern.compile(URL5 + URL_PATTERN);
    private static final Pattern URL_PATTERN6 = Pattern.compile(URL6 + URL_PATTERN);


    @Override
    public String doScanImages() {
        Pattern[] patterns = new Pattern[]{URL_PATTERN1, URL_PATTERN2, URL_PATTERN3, URL_PATTERN4, URL_PATTERN5, URL_PATTERN6};

        Set<String> originUrls = new HashSet<>();
        Set<String> remoteImgUrls = new HashSet<>();
        Set<String> localImgNames = new HashSet<>();
        leetCodeProblemDetailMapper.selectByMap(null).forEach(leetCodeProblemDetailDO -> {
            String htmlContent = leetCodeProblemDetailDO.getHtmlContent();
            for (Pattern pattern : patterns) {
                Matcher matcher = pattern.matcher(htmlContent);
                if (matcher.find()) {
                    String originUrl = matcher.group();
                    originUrls.add(originUrl);
                    log.info("==>questionId={} url={}", leetCodeProblemDetailDO.getQuestionId(), originUrl);
                    if (originUrl.endsWith(".PNG") || originUrl.endsWith(".png")
                            || originUrl.endsWith(".JPG") || originUrl.endsWith(".jpg") || originUrl.endsWith(".JPEG") || originUrl.endsWith(".jpeg")
                            || originUrl.endsWith(".GIF") || originUrl.endsWith(".gif")) {
                        remoteImgUrls.add(originUrl);

                        LeetcodeImageDO leetcodeImageDO = new LeetcodeImageDO();
                        leetcodeImageDO.setQuestionId(leetCodeProblemDetailDO.getQuestionId());
                        leetcodeImageDO.setImgUrl(originUrl);

                        String localImgName = originUrl.replace(URL1, "")
                                .replace(URL2, "")
                                .replace(URL3, "")
                                .replace(URL4, "")
                                .replace(URL5, "")
                                .replace(URL6, "");
                        log.info(localImgName);
                        localImgNames.add(localImgName);

                        leetcodeImageDO.setImgName(localImgName);

                        QueryWrapper<LeetcodeImageDO> queryWrapper = new QueryWrapper<>();
                        queryWrapper.select().eq("img_url", originUrl);
                        if (Objects.isNull(leetcodeImageMapper.selectOne(queryWrapper))) {
                            leetcodeImageMapper.insert(leetcodeImageDO);
                        }
                    }
                }
            }
        });
        log.info("==>originUrls.size()={}", originUrls.size());
        log.info("==>remoteImgUrls.size()={}", remoteImgUrls.size());
        log.info("==>localImgNames.size()={}", localImgNames.size());
        return "success";
    }

    @Override
    public String doDownload() {
        ExecutorService service = Executors.newFixedThreadPool(4);
        QueryWrapper<LeetcodeImageDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.select().eq("state", StateTypeEnum.STARTED.getSeq());
        leetcodeImageMapper.selectList(queryWrapper).forEach(leetcodeImageDO -> {
            String onlinePath = leetcodeImageDO.getImgUrl();
            String imgName = leetcodeImageDO.getImgName();
            String[] imgNameArr = imgName.split("/");
            String imageName = imgNameArr[imgNameArr.length - 1];
            String localFolder = FILE_FOLDER_NAME + imgName.replace(imageName, "");
            String localPath = FILE_FOLDER_NAME + imgName;

            // 若文件夹路径不存在，则新建
            File file = new File(localFolder);
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    log.error("==>localFolder={} 创建文件路径失败", localFolder);
                }
            }
            service.execute(() -> {
                // 下载中-便于线程宕掉后回溯
                leetcodeImageDO.setState(StateTypeEnum.DOWNLOADING.getSeq());
                leetcodeImageMapper.updateById(leetcodeImageDO);
                // 下载
                if (ReptileUtil.ioDownload(onlinePath, localPath)) {
                    leetcodeImageDO.setState(StateTypeEnum.DONE.getSeq());
                } else {
                    leetcodeImageDO.setState(StateTypeEnum.STARTED.getSeq());
                }
                leetcodeImageMapper.updateById(leetcodeImageDO);
            });
//            if (ReptileUtil.ioDownload(onlinePath, localPath)) {
//                leetcodeImageDO.setState(StateTypeEnum.DONE.getSeq());
//            }
//            leetcodeImageMapper.updateById(leetcodeImageDO);
        });
        return "success";
    }
}
