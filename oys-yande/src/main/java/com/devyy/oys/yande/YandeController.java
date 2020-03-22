package com.devyy.oys.yande;

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
@RequestMapping("/yande")
@Api(tags = "Y站爬虫")
public class YandeController {

    @Autowired
    private IYandeService yandeService;

    @ApiOperation(value = "扫描相册")
    @PostMapping("/step1")
    public String step1() {
        return yandeService.doScanImages();
    }

    @ApiOperation(value = "下载图片")
    @PostMapping("/step2")
    public String step2() {
        return yandeService.doDownload();
    }

}
