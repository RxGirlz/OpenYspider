package com.devyy.oys.tujidao;

/**
 * @since 2019-12-01
 */
public enum TujidaoTypeEnum {

    FENG_ZHI_LING_YU(86, "风之领域"),

    HUA_YU_JIE(85, "语画界"),

    GIRLZ_HIGH(79, "Girlz-High"),

    TAI_WAN_ZHENG_MEI(78, "台湾正妹"),

    SEN_LUO_CAI_TUAN(82, "森萝财团"),

    XIU_REN_WANG(59, "秀人网"),

    BEAUTY_LEG(57, "Beautyleg"),

    LI_GUI(49, "丽柜"),

    ;

    private int seq;
    private String desc;

    TujidaoTypeEnum(int seq, String desc) {
        this.seq = seq;
        this.desc = desc;
    }
}
