package com.devyy.oys.tujidao;

/**
 * Service 层
 *
 * @since 2019-12-01
 */
public interface TuJiDaoService {
    /**
     * 预下载
     *
     * @return success
     * @since 2019-12-08
     */
    String doPreDownload();

    /**
     * 生成封面
     *
     * @return success
     * @since 2019-12-08
     */
    String doGenerateCover();

    /**
     * 同步更新记录
     *
     * @return success
     */
    String doSyncRecords();

    /**
     * 本地迁移
     *
     * @return success
     * @since 2019-12-08
     */
    String doLocalMigration();
}
