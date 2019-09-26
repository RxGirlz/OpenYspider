package com.devyy.openyspider.rosi.model;

import java.util.Objects;

/**
 * ROSI-机构编号枚举
 *
 * @author zhangyiyang
 * @since 2019-09-15
 */
public enum RosiTypeEnum {

    /**
     * ROSI写真 2846
     */
    ROSIMM(1, "ROSI写真"),
    /**
     * 情趣系列 54
     */
    ROSISEX(2, "情趣系列"),
    /**
     * 口罩系列 804
     */
    ROSIKZ(3, "口罩系列"),

    ;

    /**
     * 序号
     */
    private int seq;
    /**
     * 文件夹名
     */
    private String desc;

    RosiTypeEnum(int seq, String desc) {
        this.seq = seq;
        this.desc = desc;
    }

    public int getSeq() {
        return seq;
    }

    public String getDesc() {
        return desc;
    }

    public static RosiTypeEnum getEnumBySeq(int seq) {
        for (RosiTypeEnum e : values()) {
            if (Objects.equals(e.getSeq(), seq)) {
                return e;
            }
        }
        return null;
    }
}
