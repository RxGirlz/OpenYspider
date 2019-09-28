package com.devyy.openyspider.yalayi;

/**
 * 雅拉伊-机构编号枚举
 *
 * @author zhangyiyang
 * @since 2019-09-13
 */
public enum YalayiTypeEnum {

    /**
     * 作品
     */
    GALLERY(1, "作品"),
    /**
     * 精选
     */
    SELECTED(2, "精选"),
    /**
     * 视频
     */
    VIDEO(3, "视频"),
    /**
     * 免费体验
     */
    FREE(4, "免费体验"),

    ;

    /**
     * 序号
     */
    private int seq;
    /**
     * 文件夹名
     */
    private String desc;

    YalayiTypeEnum(int seq, String desc) {
        this.seq = seq;
        this.desc = desc;
    }

    public int getSeq() {
        return seq;
    }

    public String getDesc() {
        return desc;
    }
}
