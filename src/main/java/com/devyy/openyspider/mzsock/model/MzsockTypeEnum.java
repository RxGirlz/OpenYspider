package com.devyy.openyspider.mzsock.model;

import java.util.Objects;

/**
 * 相册类型枚举
 *
 * @since 2019-09-13
 */
public enum MzsockTypeEnum {

    MV(1, "棉袜", "http://mzsock.com/mv/"),
    CV(2, "船袜", "http://mzsock.com/cy/"),
    SW(3, "丝袜", "http://mzsock.com/sw/"),
    LZ(4, "裸足", "http://mzsock.com/lz/"),
    FBX(5, "帆布鞋", "http://mzsock.com/fbx/"),
    YDX(6, "运动鞋", "http://mzsock.com/ydx/"),
    RZT(7, "人字拖", "http://mzsock.com/rzt/"),
    CWZP(8, "自拍", "http://mzsock.com/cwzp/"),

    ;

    /**
     * 序号
     */
    private int seq;
    /**
     * 类型名
     */
    private String desc;
    /**
     * url
     */
    private String url;

    MzsockTypeEnum(int seq, String desc, String url) {
        this.seq = seq;
        this.desc = desc;
        this.url = url;
    }

    public int getSeq() {
        return seq;
    }

    public String getDesc() {
        return desc;
    }

    public String getUrl() {
        return url;
    }

    public static MzsockTypeEnum getEnumByUrl(String url) {
        for (MzsockTypeEnum e : values()) {
            if (Objects.equals(e.getDesc(), url)) {
                return e;
            }
        }
        return null;
    }

}
