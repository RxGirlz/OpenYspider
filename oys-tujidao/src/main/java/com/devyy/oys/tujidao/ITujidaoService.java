package com.devyy.oys.tujidao;

import com.devyy.oys.core.base.IBaseService;

/**
 * @since 2019-12-01
 */
public interface ITujidaoService extends IBaseService {
    /**
     * feature: 封面扫描
     *
     * @return success
     * @since 2019-12-08
     */
    String doDownloadCover();

    /**
     * feature: 预下载功能
     *
     * @return success
     * @since 2019-12-08
     */
    String doPreDownload();

    /**
     * feature: 本地下载 即不走网络，走本地传输
     *
     * @return success
     * @since 2019-12-08
     */
    String doLocalMove();
}
