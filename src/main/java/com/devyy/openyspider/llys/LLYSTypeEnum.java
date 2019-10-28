package com.devyy.openyspider.llys;

/**
 * 充当小型数据库使用
 * 注释掉的部分为当前已下载
 *
 * @since 2019-10-01
 */
public enum LLYSTypeEnum {

//    BEAUTYLEG(1, "http://m7.22c.im/b", "Beautyleg", 989),
//    _3AGIRL(2, "http://m7.22c.im/3a", "3AGirl", 91),
//    _4K_STAR(3, "http://m7.22c.im/4k", "4K-STAR", 118),
//    RQ_STAR(4, "http://m7.22c.im/rq", "RQ-STAR", 156),
//    COMMON(5, "http://m7.22c.im/m", "经典写真", 82),
//    ROSIMM(6, "http://m7.22c.im/rs", "ROSIMM", 312),
//    SIYAMM(7, "http://m7.22c.im/sy", "SIYAMM", 148),
//    RU1MM(8, "http://m7.22c.im/ru", "RU1MM", 233),
//    SHOWGIRL(9, "http://m7.22c.im/s", "SHOW-GIRL", 101),
//    NY(10, "http://m7.22c.im/ny", "Pantyhose", 56),
//    LIGUI(11,"http://m7.22c.im/lg","Ligui",22),
//    XI_GAO_GEN(12,"http://m7.22c.im/xi","细高跟",74),
//    WEI_PAI(13, "http://m7.22c.im/p", "微拍福利", 578, 671),
//    XUE_YUAN_PAI_X1Y(14, "http://m7.22c.im/x1y", "学院派-财贸学院药药", 21),
//    XUE_YUAN_PAI_X2Y(14, "http://m7.22c.im/x2y", "学院派-舞蹈学院杨雨彤", 33),
//    XUE_YUAN_PAI_X3Y(14, "http://m7.22c.im/x3y", "学院派-首医大爱莉丝", 18),
//    XUE_YUAN_PAI_X4Y(14, "http://m7.22c.im/x4y", "学院派-北理工米椰贝贝", 19),
//    XUE_YUAN_PAI_X5Y(14, "http://m7.22c.im/x5y", "学院派-北理工菲菲贝贝", 15),
//    XUE_YUAN_PAI_X6Y(14, "http://m7.22c.im/x6y", "学院派-民族学院咪咪佳", 26),
//    XUE_YUAN_PAI_X7Y(14, "http://m7.22c.im/x7y", "学院派-中音美少女琦琦", 18),
//    XUE_YUAN_PAI_X8Y(14, "http://m7.22c.im/x8y", "学院派-北航嫩模苏琪娜", 19),
//    XUE_YUAN_PAI_X9Y(14, "http://m7.22c.im/x9y", "学院派-北外黑丝欣妍儿", 25),
//    XUE_YUAN_PAI_X10Y(14, "http://m7.22c.im/x10y", "学院派-戏剧学院涵涵", 12),
//    XUE_YUAN_PAI_X11Y(14, "http://m7.22c.im/x11y", "学院派-科技大学王小妖", 18),
//    XUE_YUAN_PAI_X12Y(14, "http://m7.22c.im/x12y", "学院派-首师丽泽夏玄月", 14),
//    XUE_YUAN_PAI_X13Y(14, "http://m7.22c.im/x13y", "学院派-北师AimeeGirl", 12),
//    XUE_YUAN_PAI_X14Y(14, "http://m7.22c.im/x14y", "学院派-北音小鱼美眉", 13),
//    XUE_YUAN_PAI_X15Y(14, "http://m7.22c.im/x15y", "学院派-服装学院甘露露", 13),
//    XING_GAN_CHE_MO(15, "http://m7.22c.im/c", "性感车模", 217),

//    PANS(16, "http://m7.22a.im/ps", "PANS写真", 1004,1211),
//    DON_GAN(17, "http://m7.22a.im/q", "动感小站",95, 275),
    GUO_CHAN_1(19, "http://m7.22a.im/a", "国产私拍一", 54),
    GUO_CHAN_2(20, "http://m7.22a.im/2a", "国产私拍二", 162),

//    AI_SI(21, "http://m7.22c.im/as", "爱丝", 49),
//    TUI_NV_LANG(22, "http://m7.22a.im/tg", "推女郎", 48),

    ;

    // 序号
    int seq;
    // 前缀
    String prefix;
    // 描述
    String desc;
    // 起始Index
    int start;
    // 结束Index
    int end;

    LLYSTypeEnum(int seq, String prefix, String desc, int end) {
        this.seq = seq;
        this.prefix = prefix;
        this.desc = desc;
        this.start = 1;
        this.end = end;
    }

    LLYSTypeEnum(int seq, String prefix, String desc, int start, int end) {
        this.seq = seq;
        this.prefix = prefix;
        this.desc = desc;
        this.start = start;
        this.end = end;
    }

    public int getSeq() {
        return seq;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getDesc() {
        return desc;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }
}
