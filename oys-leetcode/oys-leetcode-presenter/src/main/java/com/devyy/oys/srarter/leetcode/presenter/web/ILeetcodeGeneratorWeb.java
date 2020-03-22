package com.devyy.oys.srarter.leetcode.presenter.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/leetcode/generator")
@Api(tags = "Leetcode 生成器")
public interface ILeetcodeGeneratorWeb {

    @ApiOperation(value = "批量生成 Markdown 文件")
    @PostMapping("/step1")
    String step1();

    @ApiOperation(value = "批量生成 Sidebar 模块文件")
    @PostMapping("/step2")
    String step2();

    @ApiOperation(value = "批量生成 Git 提交脚本")
    @PostMapping("/step3")
    String step3();

    @ApiOperation(value = "批量生成 HTML 文件")
    @PostMapping("/step4")
    String step4();
}
