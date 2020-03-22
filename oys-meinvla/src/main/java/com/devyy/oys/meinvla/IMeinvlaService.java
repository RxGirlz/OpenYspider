package com.devyy.oys.meinvla;


import com.devyy.oys.srarter.core.base.IBaseService;

/**
 * @since 2019-12-01
 */
public interface IMeinvlaService extends IBaseService {
    /**
     * 扫描视频
     *
     * @return success
     */
    String doScanVideo();

    /**
     * 下载视频
     *
     * @return success
     */
    String doDownloadVideo();
}
