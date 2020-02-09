package com.devyy.openyspider.integration.leetcode.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.devyy.openyspider.integration.leetcode.domain.LeetCodeProblemDO;
import com.devyy.openyspider.integration.leetcode.domain.LeetCodeProblemDetailDO;
import com.devyy.openyspider.integration.leetcode.enums.LeetcodeSideBarEnum;
import com.devyy.openyspider.integration.leetcode.mapper.ILeetCodeProblemDetailMapper;
import com.devyy.openyspider.integration.leetcode.mapper.ILeetCodeProblemMapper;
import com.devyy.openyspider.integration.leetcode.service.ILeetcodeGeneratorService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import javax.annotation.Resource;
import java.io.File;
import java.util.*;

/**
 * @since 2019-02-09
 */
@Slf4j
@Component
public class LeetcodeGeneratorService implements ILeetcodeGeneratorService {

//         private static final String FILE_FOLDER_NAME = "C:/Users/DEVYY/Documents/GitHub/翻译工程/Leetcode-Hub/docs";
//    private static final String FILE_FOLDER_NAME = "C:/Users/DEVYY/Documents/GitHub/翻译工程/Leetcode-Hub-beta/generator2";
private static final String FILE_FOLDER_NAME = "C:\\Users\\DEVYY\\Documents\\GitHub\\翻译工程\\Leetcode-Hub\\docs\\.vuepress\\dist";

    private static final int PAID_ONLY = 1;
    private static final int HAS_BUG = 1;

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
    public String doGeneratorMarkdownFiles() {
        QueryWrapper<LeetCodeProblemDO> problemDOQueryWrapper = new QueryWrapper<>();
        problemDOQueryWrapper.select()
//                .eq("paid_only", PAID_ONLY)
                .eq("has_bug", HAS_BUG)
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
            if (Objects.nonNull(detailDO)) {
                // fix 样式
                String htmlContent = detailDO.getHtmlContent()
                        .replaceAll("<pre>", "<pre class=\"language-text\">");
                ftlParams.put("htmlContent", htmlContent);
            }

            try {
                Template template = configuration.getTemplate(HAS_BUG == 0 ? "leetcodeMarkdown.ftl" : "leetcodeMarkdownFix.ftl");
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
                        fileNames.add(((File) file).getName().replace(".md",""));
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
            }
        });

        return "success";
    }

    @Override
    public String doGeneratorHtmlSrcFiles() {
        QueryWrapper<LeetCodeProblemDO> problemDOQueryWrapper = new QueryWrapper<>();
        problemDOQueryWrapper.select()
                .eq("has_bug", HAS_BUG)
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
                String fileFolder = FILE_FOLDER_NAME + "/htmlSrc/";
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
}
