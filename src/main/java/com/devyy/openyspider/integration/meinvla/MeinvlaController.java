package com.devyy.openyspider.integration.meinvla;

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
@Api(tags = "美女啦爬虫")
@Slf4j
@RestController
@RequestMapping("/meinvla")
public class MeinvlaController {

    @Autowired
    private IMeinvlaService meinvlaService;

    @ApiOperation(value = "扫描相册")
    @PostMapping("/step1")
    public String step1() {
        return meinvlaService.doScanAlbums();
    }

    @ApiOperation(value = "扫描图片")
    @PostMapping("/step2")
    public String step2() {
        return meinvlaService.doScanImages();
    }

    @ApiOperation(value = "数据清洗")
    @PostMapping("/step3")
    public String step3() {
        return meinvlaService.doDataClean();
    }

    @ApiOperation(value = "下载图片")
    @PostMapping("/step4")
    public String step4() {
        return meinvlaService.doDownload();
    }

    @ApiOperation(value = "扫描视频")
    @PostMapping("/step5")
    public String step5() {
        return meinvlaService.doScanVideo();
    }

    @ApiOperation(value = "下载视频")
    @PostMapping("/step6")
    public String step6() {
        return meinvlaService.doDownloadVideo();
    }
}
