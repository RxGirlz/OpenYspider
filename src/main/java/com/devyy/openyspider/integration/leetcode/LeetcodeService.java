package com.devyy.openyspider.integration.leetcode;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.devyy.openyspider.integration.leetcode.gson.GetQuestionTranslationGson;
import com.devyy.openyspider.integration.leetcode.gson.ProblemsAllGson;
import freemarker.template.Configuration;
import freemarker.template.Template;
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
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @since 2019-02-06
 */
@Slf4j
@Component
public class LeetcodeService implements ILeetcodeService {

    private static final String FILE_FOLDER_NAME = "C:/Users/DEVYY/Documents/GitHub/翻译工程/Leetcode-Hub/generator";

    @Value("classpath:json/problems-all.json")
    private org.springframework.core.io.Resource problemAllJson;

    @Value("classpath:json/getQuestionTranslation.json")
    private org.springframework.core.io.Resource getQuestionTranslationJson;

    @Autowired
    private Configuration configuration;

    @Resource
    private ILeetCodeProblemMapper leetCodeProblemMapper;
    @Resource
    private ILeetCodeProblemDetailMapper leetCodeProblemDetailMapper;

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
        problemDOQueryWrapper.select().eq("paid_only", 0);

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
    public String doGeneratorMarkdownFiles() {
        QueryWrapper<LeetCodeProblemDO> problemDOQueryWrapper = new QueryWrapper<>();
        problemDOQueryWrapper.select()
                .eq("paid_only", 0)
                .eq("has_bug", 0)
                .orderByAsc("question_id");
        leetCodeProblemMapper.selectList(problemDOQueryWrapper).forEach(leetCodeProblemDO -> {
            final Long questionId = leetCodeProblemDO.getQuestionId();
            final String feQuestionId = leetCodeProblemDO.getFeQuestionId();

            QueryWrapper<LeetCodeProblemDetailDO> detailDOQueryWrapper = new QueryWrapper<>();
            detailDOQueryWrapper.select().eq("question_id", questionId);
            LeetCodeProblemDetailDO detailDO = leetCodeProblemDetailMapper.selectOne(detailDOQueryWrapper);

            Map<String, String> ftlParams = new HashMap<>();
            ftlParams.put("feQuestionId", feQuestionId);
            ftlParams.put("titleCn", leetCodeProblemDO.getTitleCn());
            ftlParams.put("titleSlug", leetCodeProblemDO.getTitleSlug());
            if (Objects.nonNull(detailDO)) {
                // fix 样式
                String htmlContent = detailDO.getHtmlContent()
                        .replaceAll("<pre>", "<pre class=\"language-text\">");
                ftlParams.put("htmlContent", htmlContent);
            }

            try {
                Template template = configuration.getTemplate("leetcodeMarkdown.ftl");
                String mdContent = FreeMarkerTemplateUtils.processTemplateIntoString(template, ftlParams);
                // 文件名 eg. leetcode_1344_jump-game-v.md
                String fileName = "leetcode_" + feQuestionId + "_" + leetCodeProblemDO.getTitleSlug() + ".md";
                String fileFolder = FILE_FOLDER_NAME + getSidebarSliceByFeQuestionId(feQuestionId);
                String filePath = fileFolder + fileName;
                // 若文件夹路径不存在，则新建
                File file = new File(fileFolder);
                if (!file.exists()) {
                    if (!file.mkdirs()) {
                        log.error("==>localFolder={} 创建文件路径失败", fileFolder);
                    }
                }
                FileUtils.writeStringToFile(new File(filePath), mdContent, "UTF-8");
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        });
        return "success";
    }

    @Override
    public String doGeneratorSidebarFiles() {
        List<LeetcodeSideBarEnum> leetcodeSideBarEnums = LeetcodeSideBarEnum.getEnums();
        leetcodeSideBarEnums.forEach(enums -> {
            // 列出指定路径下全部文件名
            String filePath = FILE_FOLDER_NAME + enums.getSidebarSlice();
            Collection files = FileUtils.listFiles(new File(filePath), new String[]{"md"}, false);
            List<String> fileNames = new ArrayList<>();
            for (Object file : files) {
                if (file instanceof File) {
                    fileNames.add(((File) file).getName());
                }
            }
            // 按 windows 文件系统自然顺序排序
            fileNames.sort((o1, o2) -> {
                String[] o1Arr = o1.split("_");
                String[] o2Arr = o2.split("_");
                if (StringUtils.isNumeric(o1Arr[1]) && StringUtils.isNumeric(o2Arr[1])) {
                    int num1 = Integer.parseInt(o1Arr[1]);
                    int num2 = Integer.parseInt(o2Arr[1]);
                    return Integer.compare(num1, num2);
                } else {
                    return o1.compareTo(o2);
                }
            });

            Map<String, Object> ftlParams = new HashMap<>();
            ftlParams.put("sidebarSlice", enums.getSidebarSlice());
            ftlParams.put("fileNames", fileNames);

            try {
                Template template = configuration.getTemplate("leetcodeSidebarModule.ftl");
                String mdContent = FreeMarkerTemplateUtils.processTemplateIntoString(template, ftlParams);
                // 文件名 eg. sidebarOf001To100.js
                String fileName = enums.getJsModuleName() + ".js";
                String fileFolder = FILE_FOLDER_NAME + "/sidebar/";
                String filePath2 = fileFolder + fileName;
                // 若文件夹路径不存在，则新建
                File file = new File(fileFolder);
                if (!file.exists()) {
                    if (!file.mkdirs()) {
                        log.error("==>localFolder={} 创建文件路径失败", fileFolder);
                    }
                }
                FileUtils.writeStringToFile(new File(filePath2), mdContent, "UTF-8");
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        });
        return "success";
    }

    @Override
    public String doGeneratorGitCommitCmd() {
        List<LeetcodeSideBarEnum> leetcodeSideBarEnums = LeetcodeSideBarEnum.getEnums();
        leetcodeSideBarEnums.forEach(enums -> {
            // 列出指定路径下全部文件名
            String filePath = FILE_FOLDER_NAME + enums.getSidebarSlice();
            Collection files = FileUtils.listFiles(new File(filePath), new String[]{"md"}, false);
            List<String> fileNames = new ArrayList<>();
            for (Object file : files) {
                if (file instanceof File) {
                    fileNames.add(((File) file).getName());
                }
            }

            Map<String, Object> ftlParams = new HashMap<>();
            ftlParams.put("sidebarSlice", enums.getSidebarSlice());
            ftlParams.put("fileNames", fileNames);

            try {
                Template template = configuration.getTemplate("leetcodeGitCommand.ftl");
                String mdContent = FreeMarkerTemplateUtils.processTemplateIntoString(template, ftlParams);
                // 文件名 eg. sidebarOf001To100.js
                String fileName = enums.getJsModuleName() + ".sh";
                String filePath2 = FILE_FOLDER_NAME + "/" + fileName;

                FileUtils.writeStringToFile(new File(filePath2), mdContent, "UTF-8");
            } catch (Exception e) {
                log.error(e.getMessage());
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
                .eq("paid_only", 0)
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
}
