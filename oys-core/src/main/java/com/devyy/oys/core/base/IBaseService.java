package com.devyy.oys.core.base;

/**
 * 基础接口
 *
 * @since 2019-11-26
 */
public interface IBaseService {
    /**
     * 扫描相册&入库
     *
     * @return success
     */
    String doScanAlbums();

    /**
     * 扫描图片&入库
     *
     * @return success
     */
    String doScanImages();

    /**
     * 下载资源到本地
     *
     * @return success
     */
    String doDownload();

    /**
     * 数据清洗
     *
     * @return success
     */
    default String doDataClean() {
        return "success";
    }
}

