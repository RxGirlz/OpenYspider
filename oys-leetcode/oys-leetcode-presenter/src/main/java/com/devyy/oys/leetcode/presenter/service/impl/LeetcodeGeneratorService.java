package com.devyy.oys.leetcode.presenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.devyy.oys.leetcode.core.domain.LeetCodeProblemDO;
import com.devyy.oys.leetcode.core.domain.LeetCodeProblemDetailDO;
import com.devyy.oys.leetcode.core.enums.LeetcodeDifficultyTypeEnum;
import com.devyy.oys.leetcode.core.enums.LeetcodeSideBarEnum;
import com.devyy.oys.leetcode.data.dao.ILeetCodeProblemDetailMapper;
import com.devyy.oys.leetcode.data.dao.ILeetCodeProblemMapper;
import com.devyy.oys.leetcode.presenter.service.ILeetcodeGeneratorService;
import com.devyy.oys.leetcode.presenter.service.LeetcodeHelper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.File;
import java.util.*;

/**
 * @since 2019-02-09
 */
@Slf4j
@Component
public class LeetcodeGeneratorService implements ILeetcodeGeneratorService {

    private static final int PAID_ONLY = 1;
    private static final int HAS_BUG = 1;

    @Value("classpath:json/problems-all.json")
    private org.springframework.core.io.Resource problemAllJson;

    @Value("classpath:json/getQuestionTranslation.json")
    private org.springframework.core.io.Resource getQuestionTranslationJson;

    @Autowired
    private Configuration configuration;

    @Autowired
    private ILeetCodeProblemMapper leetCodeProblemMapper;
    @Autowired
    private ILeetCodeProblemDetailMapper leetCodeProblemDetailMapper;

    @Override
    public String doGeneratorMarkdownFiles() {
        QueryWrapper<LeetCodeProblemDO> problemDOQueryWrapper = new QueryWrapper<>();
        problemDOQueryWrapper.select()
//                .eq("paid_only", PAID_ONLY)
                .eq("has_bug", HAS_BUG)
//                .isNull("has_bug")
                .in("fe_question_id", "1360","1361","1362","1363","1352","1355")
                // SELECT * FROM dev.tbl_leetcode_problem where  question_id > 1420 order by question_id asc;
                .gt("question_id", 1457)
                .orderByAsc("question_id");
        leetCodeProblemMapper.selectList(problemDOQueryWrapper).forEach(leetCodeProblemDO -> {
            final Long questionId = leetCodeProblemDO.getQuestionId();
            final String feQuestionId = leetCodeProblemDO.getFeQuestionId();

            QueryWrapper<LeetCodeProblemDetailDO> detailDOQueryWrapper = new QueryWrapper<>();
            detailDOQueryWrapper.select().eq("question_id", questionId);
            LeetCodeProblemDetailDO detailDO = leetCodeProblemDetailMapper.selectOne(detailDOQueryWrapper);

            Map<String, Object> ftlParams = new HashMap<>();
            ftlParams.put("paidOnly", leetCodeProblemDO.getPaidOnly());
            ftlParams.put("feQuestionId", feQuestionId);
            ftlParams.put("titleCn", leetCodeProblemDO.getTitleCn());
            ftlParams.put("titleSlug", leetCodeProblemDO.getTitleSlug());
            ftlParams.put("hasBug", Objects.equals(leetCodeProblemDO.getHasBug(), 1));
            if (Objects.nonNull(detailDO)) {
                // fix 样式
                String htmlContent = detailDO.getHtmlContent()
                        .replaceAll("<pre>", "<pre class=\"language-text\">")
                        .replaceAll(LeetcodeHelper.URL1, "")
                        .replaceAll(LeetcodeHelper.URL2, "")
                        .replaceAll(LeetcodeHelper.URL3, "")
                        .replaceAll(LeetcodeHelper.URL4, "")
                        .replaceAll(LeetcodeHelper.URL5, "")
                        .replaceAll(LeetcodeHelper.URL6, "");
                ftlParams.put("htmlContent", htmlContent);
                String txtContent = detailDO.getTxtContent();
                ftlParams.put("txtContent", txtContent);
                if (Objects.nonNull(txtContent) && txtContent.contains("class")) {
                    ftlParams.put("type", "java");
                } else if (Objects.nonNull(txtContent) && txtContent.contains("MySQL")) {
                    ftlParams.put("type", "sql");
                } else {
                    ftlParams.put("type", "");
                }
                ftlParams.put("difficulty", LeetcodeDifficultyTypeEnum.getEnumBySeq(leetCodeProblemDO.getDifficulty()).getDesc());
            }

            try {
                Template template = configuration.getTemplate("leetcodeMarkdown.ftl");
                String mdContent = FreeMarkerTemplateUtils.processTemplateIntoString(template, ftlParams);
                // 文件名 eg. leetcode_1344_jump-game-v.md
                String fileName = "leetcode_" + feQuestionId + "_" + leetCodeProblemDO.getTitleSlug() + ".md";
                String fileFolder = LeetcodeHelper.FILE_FOLDER_NAME + LeetcodeHelper.getSidebarSliceByFeQuestionId(feQuestionId);
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
            String filePath = LeetcodeHelper.FILE_FOLDER_NAME + enums.getSidebarSlice();
            log.info("==>filePath={}", filePath);
            Collection files = null;
            try {
                files = FileUtils.listFiles(new File(filePath), new String[]{"md"}, false);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
            if (Objects.nonNull(files)) {
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
                    String fileFolder = LeetcodeHelper.FILE_FOLDER_NAME + "/sidebar/";
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
            }
        });
        return "success";
    }

    @Override
    public String doGeneratorGitCommitCmd() {
        List<LeetcodeSideBarEnum> leetcodeSideBarEnums = LeetcodeSideBarEnum.getEnums();
        leetcodeSideBarEnums.forEach(enums -> {
            // 列出指定路径下全部文件名
            String filePath = LeetcodeHelper.FILE_FOLDER_NAME + enums.getSidebarSlice();
            log.info("==>filePath={}", filePath);
            Collection files = null;
            try {
                files = FileUtils.listFiles(new File(filePath), new String[]{"md"}, false);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
            if (Objects.nonNull(files)) {
                List<String> fileNames = new ArrayList<>();
                for (Object file : files) {
                    if (file instanceof File) {
                        fileNames.add(((File) file).getName().replace(".md", ""));
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
                    String filePath2 = LeetcodeHelper.FILE_FOLDER_NAME + "/" + fileName;

                    FileUtils.writeStringToFile(new File(filePath2), mdContent, "UTF-8");
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
            }
        });

        return "success";
    }

    @Override
    public String doGeneratorHtmlSrcFiles() {
        QueryWrapper<LeetCodeProblemDO> problemDOQueryWrapper = new QueryWrapper<>();
        problemDOQueryWrapper.select()
                .eq("has_bug", HAS_BUG)
                .in("fe_question_id", "1360","1361","1362","1363","1352","1355")
                .gt("question_id", 1420)
                .orderByAsc("question_id");
        leetCodeProblemMapper.selectList(problemDOQueryWrapper).forEach(leetCodeProblemDO -> {
            final Long questionId = leetCodeProblemDO.getQuestionId();
            final String feQuestionId = leetCodeProblemDO.getFeQuestionId();

            QueryWrapper<LeetCodeProblemDetailDO> detailDOQueryWrapper = new QueryWrapper<>();
            detailDOQueryWrapper.select().eq("question_id", questionId);
            LeetCodeProblemDetailDO detailDO = leetCodeProblemDetailMapper.selectOne(detailDOQueryWrapper);

            Map<String, Object> ftlParams = new HashMap<>();
            ftlParams.put("feQuestionId", feQuestionId);
            ftlParams.put("titleSlug", leetCodeProblemDO.getTitleSlug());
            if (Objects.nonNull(detailDO)) {
                // fix 样式
                String htmlContent = detailDO.getHtmlContent()
                        .replaceAll("<pre>", "<pre class=\"language-text\">");
                ftlParams.put("htmlContent", htmlContent);
            }

            try {
                Template template = configuration.getTemplate("leetcodeHtml.ftl");
                String mdContent = FreeMarkerTemplateUtils.processTemplateIntoString(template, ftlParams);
                // 文件名 eg. leetcode_1344_jump-game-v.md
                String fileName = "leetcode_" + feQuestionId + "_" + leetCodeProblemDO.getTitleSlug() + ".html";
                String fileFolder = LeetcodeHelper.FILE_FOLDER_NAME + "/htmlSrc/";
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


}
