package com.devyy.openyspider.integration.leetcode;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @since 2019-02-06
 */
@Slf4j
@RestController
@RequestMapping("/json")
@Api(tags = "Leetcode 爬虫")
public class LeetcodeController {
    @Resource
    private ILeetcodeService leetcodeService;

    @ApiOperation(value = "扫描问题集")
    @PostMapping("/step1")
    public String step1() {
        return leetcodeService.doScanProblems();
    }

    @ApiOperation(value = "匹配中文标题")
    @PostMapping("/step2")
    public String step2() {
        return leetcodeService.doScanTiTleCn();
    }

    @ApiOperation(value = "扫描问题题目")
    @PostMapping("/step3")
    public String step3() {
        return leetcodeService.doScanProblemsDetail();
    }

    @ApiOperation(value = "批量生成 Markdown 文件")
    @PostMapping("/step4")
    public String step4() {
        return leetcodeService.doGeneratorMarkdownFiles();
    }

    @ApiOperation(value = "批量生成 Sidebar 模块文件")
    @PostMapping("/step5")
    public String step5() {
        return leetcodeService.doGeneratorSidebarFiles();
    }

    @ApiOperation(value = "批量生成 Git 提交脚本")
    @PostMapping("/step6")
    public String step6() {
        return leetcodeService.doGeneratorGitCommitCmd();
    }

    @ApiOperation(value = "测试 Vuepress 渲染 Bug")
    @PostMapping("/step7")
    public String step7() {
        return leetcodeService.doTestVuePressBugs();
    }
}
