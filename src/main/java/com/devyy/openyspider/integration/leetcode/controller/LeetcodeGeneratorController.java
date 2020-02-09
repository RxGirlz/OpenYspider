package com.devyy.openyspider.integration.leetcode.controller;

import com.devyy.openyspider.integration.leetcode.service.ILeetcodeGeneratorService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @since 2019-02-09
 */
@Slf4j
@RestController
@RequestMapping("/leetcode/generator")
@Api(tags = "Leetcode 生成器")
public class LeetcodeGeneratorController {
    @Resource
    private ILeetcodeGeneratorService leetcodeGeneratorService;

    @ApiOperation(value = "批量生成 Markdown 文件")
    @PostMapping("/step1")
    public String step1() {
        return leetcodeGeneratorService.doGeneratorMarkdownFiles();
    }

    @ApiOperation(value = "批量生成 Sidebar 模块文件")
    @PostMapping("/step2")
    public String step2() {
        return leetcodeGeneratorService.doGeneratorSidebarFiles();
    }

    @ApiOperation(value = "批量生成 Git 提交脚本")
    @PostMapping("/step3")
    public String step3() {
        return leetcodeGeneratorService.doGeneratorGitCommitCmd();
    }

    @ApiOperation(value = "批量生成 HTML 文件")
    @PostMapping("/step4")
    public String step4() {
        return leetcodeGeneratorService.doGeneratorHtmlSrcFiles();
    }
}
