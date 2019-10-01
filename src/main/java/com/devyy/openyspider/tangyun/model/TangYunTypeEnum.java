package com.devyy.openyspider.tangyun.model;

import java.util.Objects;

/**
 * 唐韵文化-相册类型枚举
 *
 * @since 2019-06-20
 */
public enum TangYunTypeEnum {

    /**
     * 原创套图
     */
    YUAN_CHUANG_TAO_TU(1, "原创套图", "http://www.tangyun365.com/d/fileUrl/photo/yuanchuangtaotu/", "http://www.tangyun365.com/photo/yuanchuangtaotu/"),

    /**
     * 电子杂志
     */
    MAGEZINE(2, "电子杂志", "http://www.tangyun365.com/d/fileUrl/photo/magazine/", "http://www.tangyun365.com/photo/magazine/"),

    /**
     * 会员独享相册
     */
    VIP(3, "会员独享相册", "http://www.tangyun365.com/d/fileUrl/vip/vipphoto/", "http://www.tangyun365.com/vip/vipphoto/"),

    ;

    /**
     * 序号
     */
    private int seq;
    /**
     * 相册名称
     */
    private String desc;
    /**
     * 图片路径前缀
     */
    private String fileUrl;
    /**
     * 相册页面前缀
     */
    private String webUrl;

    TangYunTypeEnum(int seq, String desc, String fileUrl, String webUrl) {
        this.seq = seq;
        this.desc = desc;
        this.fileUrl = fileUrl;
        this.webUrl = webUrl;
    }

    public int getSeq() {
        return seq;
    }

    public String getDesc() {
        return desc;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public static TangYunTypeEnum getEnumBySeq(int seq) {
        for (TangYunTypeEnum e : values()) {
            if (Objects.equals(e.getSeq(), seq)) {
                return e;
            }
        }
        return null;
    }
}
