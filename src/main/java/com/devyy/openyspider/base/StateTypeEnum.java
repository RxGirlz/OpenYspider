package com.devyy.openyspider.base;

import lombok.Getter;

/**
 * 资源状态
 *
 * @since 2019-12-01
 */
@Getter
public enum StateTypeEnum {
    /**
     * 黑名单
     */
    BLACKLIST(-1, "黑名单"),
    /**
     * 白名单
     */
    WHITELIST(0, "白名单"),

    //

    /**
     * 下载完成
     */
    DONE(102, "下载完成"),
    /**
     * 下载中
     */
    DOWNLOADING(101, "下载中"),
    /**
     * 未开始/待重试
     */
    STARTED(100, "未开始/待重试"),

    //

    /**
     * 已解析--album专用
     */
    ANALYSIS(200, "已解析"),
    /**
     * 未达预期
     */
    EXCEPTION(500, "未达预期"),
    /**
     * 找不到资源
     */
    NOTFOUND(404, "找不到资源"),

    ;

    private int seq;
    private String desc;

    StateTypeEnum(int seq, String desc) {
        this.seq = seq;
        this.desc = desc;
    }
}
