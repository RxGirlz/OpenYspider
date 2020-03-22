package com.devyy.oys.leetcode.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.devyy.oys.core.enums.StateTypeEnum;
import com.devyy.oys.core.util.ReptileUtil;
import com.devyy.oys.leetcode.domain.LeetCodeProblemDO;
import com.devyy.oys.leetcode.domain.LeetCodeProblemDetailDO;
import com.devyy.oys.leetcode.domain.LeetcodeImageDO;
import com.devyy.oys.leetcode.gson.GetQuestionTranslationGson;
import com.devyy.oys.leetcode.gson.ProblemsAllGson;
import com.devyy.oys.leetcode.mapper.ILeetCodeProblemDetailMapper;
import com.devyy.oys.leetcode.mapper.ILeetCodeProblemMapper;
import com.devyy.oys.leetcode.mapper.ILeetcodeImageMapper;
import com.devyy.oys.leetcode.service.ILeetcodeScannerService;
import com.devyy.oys.leetcode.service.LeetcodeHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
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
public class LeetcodeScannerService implements ILeetcodeScannerService {
    private static final String URL_PATTERN = ("[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]");

    @Value("classpath:json/problems-all.json")
    private org.springframework.core.io.Resource problemAllJson;

    @Value("classpath:json/getQuestionTranslation.json")
    private org.springframework.core.io.Resource getQuestionTranslationJson;

    @Autowired
    private ILeetCodeProblemMapper leetCodeProblemMapper;
    @Autowired
    private ILeetCodeProblemDetailMapper leetCodeProblemDetailMapper;
    @Autowired
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
            // 1557
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

                QueryWrapper<LeetCodeProblemDO> queryWrapper = new QueryWrapper<>();
                queryWrapper.select().eq("question_id", statBean.getQuestion_id());
                // 如果不存在则插入
                if (Objects.isNull(leetCodeProblemMapper.selectOne(queryWrapper))) {
                    leetCodeProblemMapper.insert(leetCodeProblemDO);
                    log.info("==>新增 LeetCodeProblemDO={}", JSON.toJSONString(leetCodeProblemDO));
                }
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
            // bug: 会存在没有中文名称的情况
            getQuestionTranslationGson.getData().getTranslations().forEach(translationsBean -> {

                QueryWrapper<LeetCodeProblemDO> codeProblemDOQueryWrapper = new QueryWrapper<>();
                codeProblemDOQueryWrapper.select()
                        .eq("question_id", translationsBean.getQuestionId())
                        .isNull("title_cn");

                LeetCodeProblemDO leetCodeProblemDO = leetCodeProblemMapper.selectOne(codeProblemDOQueryWrapper);
                if (Objects.nonNull(leetCodeProblemDO)) {
                    leetCodeProblemDO.setTitleCn(translationsBean.getTitle());
                    leetCodeProblemMapper.updateById(leetCodeProblemDO);
                    log.info("==>更新 prblemId={} title={}", translationsBean.getQuestionId(), translationsBean.getTitle());
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
        webDriver.manage().timeouts().pageLoadTimeout(20, TimeUnit.SECONDS);
        webDriver.get("https://leetcode-cn.com/problems/two-sum/");
        // wait 30s 输入账号密码
        this.waitSeconds(30);

        // 不存在 problemDetail 的 questionId Set
        Set<Long> problemDetailQuestionIds = new HashSet<>();
        leetCodeProblemDetailMapper.selectList(null).forEach(leetCodeProblemDetailDO -> {
            problemDetailQuestionIds.add(leetCodeProblemDetailDO.getQuestionId());
        });

        leetCodeProblemMapper.selectList(null).forEach(leetCodeProblemDO -> {
            Long questionId = leetCodeProblemDO.getQuestionId();

            if (!problemDetailQuestionIds.contains(questionId)) {
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

                    LeetCodeProblemDetailDO leetCodeProblemDetailDO = new LeetCodeProblemDetailDO();
                    leetCodeProblemDetailDO.setHtmlContent(htmlContent);
                    leetCodeProblemDetailDO.setQuestionId(leetCodeProblemDO.getQuestionId());

                    leetCodeProblemDetailMapper.insert(leetCodeProblemDetailDO);
                    log.info("==>新增 htmlContent={}", htmlContent);
                }
            }
        });
        return "success";
    }

    @Override
    public String doScanTextContents() {
        String localPath = "C:/Users/DEVYY/Documents/GitHub/翻译工程/Leetcode-Txt/problem-code/";
        for (int i = 1; i <= 1349; i++) {
            String fileName = localPath + "J (" + i + ").txt";
            try {
                String fileContent = FileUtils.readFileToString(new File(fileName), "UTF-8");

                QueryWrapper<LeetCodeProblemDO> queryWrapper = new QueryWrapper<>();
                queryWrapper.select().eq("fe_question_id", i);
                LeetCodeProblemDO leetCodeProblemDO = leetCodeProblemMapper.selectOne(queryWrapper);
                if (Objects.nonNull(leetCodeProblemDO)) {
                    QueryWrapper<LeetCodeProblemDetailDO> queryWrapper2 = new QueryWrapper<>();
                    queryWrapper2.select().eq("question_id", leetCodeProblemDO.getQuestionId()).isNull("txt_content");
                    LeetCodeProblemDetailDO leetCodeProblemDetailDO = leetCodeProblemDetailMapper.selectOne(queryWrapper2);
                    if (Objects.nonNull(leetCodeProblemDetailDO)) {
                        leetCodeProblemDetailDO.setTxtContent(fileContent);
                        leetCodeProblemDetailMapper.updateById(leetCodeProblemDetailDO);
                        log.info("==>txtContent={} insert", fileContent);
                    }
                }
            } catch (Exception e) {
                log.error("==>i={} e={}", i, e.getMessage());
            }
        }
        return null;
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
                .isNull("has_bug")
                .orderByAsc("question_id");
        leetCodeProblemMapper.selectList(problemDOQueryWrapper).forEach(leetCodeProblemDO -> {
            final String feQuestionId = leetCodeProblemDO.getFeQuestionId();
            String url = "http://localhost:8080/doc4-leetcode" + LeetcodeHelper.getSidebarSliceByFeQuestionId(feQuestionId)
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


    private static final Pattern URL_PATTERN1 = Pattern.compile(LeetcodeHelper.URL1 + URL_PATTERN);
    private static final Pattern URL_PATTERN2 = Pattern.compile(LeetcodeHelper.URL2 + URL_PATTERN);
    private static final Pattern URL_PATTERN3 = Pattern.compile(LeetcodeHelper.URL3 + URL_PATTERN);
    private static final Pattern URL_PATTERN4 = Pattern.compile(LeetcodeHelper.URL4 + URL_PATTERN);
    private static final Pattern URL_PATTERN5 = Pattern.compile(LeetcodeHelper.URL5 + URL_PATTERN);
    private static final Pattern URL_PATTERN6 = Pattern.compile(LeetcodeHelper.URL6 + URL_PATTERN);


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
                // while 游标自动后移
                while (matcher.find()) {
                    String originUrl = matcher.group();
                    originUrls.add(originUrl);

                    if (originUrl.endsWith(".PNG") || originUrl.endsWith(".png")
                            || originUrl.endsWith(".JPG") || originUrl.endsWith(".jpg")
                            || originUrl.endsWith(".JPEG") || originUrl.endsWith(".jpeg")
                            || originUrl.endsWith(".GIF") || originUrl.endsWith(".gif")) {
                        remoteImgUrls.add(originUrl);

                        LeetcodeImageDO leetcodeImageDO = new LeetcodeImageDO();
                        leetcodeImageDO.setQuestionId(leetCodeProblemDetailDO.getQuestionId());
                        leetcodeImageDO.setImgUrl(originUrl);

                        String localImgName = originUrl.replace(LeetcodeHelper.URL1, "")
                                .replace(LeetcodeHelper.URL2, "")
                                .replace(LeetcodeHelper.URL3, "")
                                .replace(LeetcodeHelper.URL4, "")
                                .replace(LeetcodeHelper.URL5, "")
                                .replace(LeetcodeHelper.URL6, "");
                        log.info(localImgName);
                        localImgNames.add(localImgName);

                        leetcodeImageDO.setImgName(localImgName);

                        QueryWrapper<LeetcodeImageDO> queryWrapper = new QueryWrapper<>();
                        queryWrapper.select().eq("img_url", originUrl);
                        if (Objects.isNull(leetcodeImageMapper.selectOne(queryWrapper))) {
                            leetcodeImageMapper.insert(leetcodeImageDO);
                            log.info("==>新增 questionId={} url={}", leetCodeProblemDetailDO.getQuestionId(), originUrl);
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
        queryWrapper.select()
//                .eq("state", StateTypeEnum.STARTED.getSeq())
        .isNull("state")
        ;
        leetcodeImageMapper.selectList(queryWrapper).forEach(leetcodeImageDO -> {
            String onlinePath = leetcodeImageDO.getImgUrl();
            String imgName = leetcodeImageDO.getImgName();
            String[] imgNameArr = imgName.split("/");
            String imageName = imgNameArr[imgNameArr.length - 1];
            String localFolder = LeetcodeHelper.FILE_FOLDER_NAME + imgName.replace(imageName, "");
            String localPath = LeetcodeHelper.FILE_FOLDER_NAME + imgName;

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
        });
        return "success";
    }
}
