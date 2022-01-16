package com.devyy.oys.tujidao;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST 接口层
 *
 * @since 2019-12-01
 */
@Slf4j
@RestController
@RequestMapping("/tujidao")
@Api(tags = "图集岛爬虫")
public class TuJiDaoController {
    @Autowired
    private TuJiDaoService tujidaoService;

    /**
     * 预下载
     *
     * @return "success"
     */
    @ApiOperation(value = "预下载")
    @PostMapping("/step1")
    public String step1() {
        return tujidaoService.doPreDownload();
    }

    /**
     * 生成封面
     *
     * @return "success"
     */
    @ApiOperation(value = "生成封面")
    @PostMapping("/step2")
    public String step2() {
        return tujidaoService.doGenerateCover();
    }

    /**
     * 同步更新记录
     *
     * @return "success"
     */
    @ApiOperation(value = "同步更新记录")
    @PostMapping("/step3")
    public String step3() {
        return tujidaoService.doSyncRecords();
    }

    /**
     * 本地迁移
     *
     * @return "success"
     */
    @ApiOperation(value = "本地迁移")
    @PostMapping("/step4")
    public String step4() {
        return tujidaoService.doLocalMigration();
    }
}
