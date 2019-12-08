package com.devyy.openyspider.integration.tujidao;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @since 2019-12-01
 */
@Slf4j
@RestController
@RequestMapping("/tujidao")
@Api(tags = "图集岛爬虫")
public class TujidaoController {
    @Autowired
    private ITujidaoService tujidaoService;

    @ApiOperation(value = "扫描相册")
    @PostMapping("/step1")
    public String step1() {
        return tujidaoService.doScanAlbums();
    }

    @ApiOperation(value = "扫描下载封面")
    @PostMapping("/step2")
    public String step2() {
        return tujidaoService.doDownloadCover();
    }

    @ApiOperation(value = "预下载")
    @PostMapping("/step3")
    public String step3() {
        return tujidaoService.doPreDownload();
    }

    @ApiOperation(value = "本地下载")
    @PostMapping("/step4")
    public String step4() {
        return tujidaoService.doLocalMove();
    }
}
