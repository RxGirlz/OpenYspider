package com.devyy.oys.leetcode.presenter.cloud.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/leetcode")
@Api(tags = "Leetcode 爬虫")
public interface ILeetcodeController {

    @ApiOperation(value = "扫描问题集")
    @PostMapping("/step1")
    String step1();

    @ApiOperation(value = "匹配中文标题")
    @PostMapping("/step2")
    String step2();

    @ApiOperation(value = "扫描问题题目")
    @PostMapping("/step3")
    String step3();

    @ApiOperation(value = "测试 Vuepress 渲染 Bug")
    @PostMapping("/step4")
    String step4();

    @ApiOperation(value = "扫描图片资源")
    @PostMapping("/step5")
    String step5();

    @ApiOperation(value = "下载图片")
    @PostMapping("/step6")
    String step6();

    @ApiOperation(value = "扫描 Text 文本内容")
    @PostMapping("/step7")
    String step7();
}
