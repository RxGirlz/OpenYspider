package com.devyy.openyspider.integration.tujidao;

import com.devyy.openyspider.base.IBaseService;

/**
 * @since 2019-12-01
 */
public interface ITujidaoService extends IBaseService {
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
