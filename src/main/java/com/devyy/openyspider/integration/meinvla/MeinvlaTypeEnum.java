package com.devyy.openyspider.integration.meinvla;

import lombok.Getter;

import java.util.List;

/**
 * @since 2019-12-01
 */
@Getter
public enum MeinvlaTypeEnum {

    T86(86, "rq-sata"),
    T87(87, "禁忌摄影"),
    T88(88, "国产美女"),
    T89(89, "韩国美女"),
    T90(90, "dgc套图"),
    T91(91, "bwh套图"),
    T92(92, "ys-web"),
    T93(93, "wpb套图"),
    T94(94, "台湾美腿女郎"),
    T95(95, "丝宝丝魅vip"),
    T96(96, "台湾写真tpimage"),
    T97(97, "topqueen"),
    T98(98, "sabra-net"),
    T99(99, "minisuka-tv"),
    T100(100, "bejean-on-line"),
    // 15
    T101(101, "misty套图"),
    T102(102, "bomb-tv套图"),
    T103(103, "妖妖vip套图"),
    T104(104, "拍美vip套图"),
    T105(105, "波斯猫儿vip"),
    T106(106, "丽柜ligui套图"),
    T107(107, "小美vip套图"),
    T108(108, "欧美丝袜泰斗"),
    T109(109, "动感小站vip"),
    T110(110, "中高艺套图"),
    T111(111, "ns-eyes套图"),
    T112(112, "two-套图"),
    T113(113, "柔丝晴晴"),
    T114(114, "cosplay美女"),
    T115(115, "rosi丝袜"),
    T116(116, "丝间舞"),
    T117(117, "丝图阁"),
    T118(118, "唯美丝"),
    T119(119, "disi写真"),
    T120(120, "4k-star日本ol"),
    // 35
    T121(121, "naked-art日本女优"),
    T122(122, "pans写真"),
    T123(123, "趣向俱乐部"),
    T124(124, "syukou-club丝袜"),
    T125(125, "d-ch丝袜诱惑"),
    T126(126, "princess collection"),
    T127(127, "3agirl写真"),
    T128(128, "秀人模特"),
    T129(129, "ru1mm丝袜"),
    T130(130, "丝尚美女"),
    T131(131, "allgravure套图"),
    T132(132, "image-tv套图"),
    T133(133, "imouto-tv套图"),
    T134(134, "上海炫彩时尚"),
    T135(135, "layered-nylons"),
    T136(136, "leghacker"),
    T137(137, "丽阁影像"),
    T138(138, "网络收集"),
    T139(139, "街拍美女"),
    T140(140, "唯美写真"),
    // 55
    T141(141, "legku丝足"),
    T142(142, "推女郎"),
    T143(143, "尤果性感美女"),
    T144(144, "芬妮高清丝足"),
    T145(145, "神艺缘"),
    T146(146, "蕾丝兔宝宝"),
    T147(147, "mygirl美媛馆"),
    T148(148, "aiss爱丝"),
    T149(149, "tgod推女神"),
    T150(150, "丝雅写真"),
    T151(151, "heisiai写真"),
    T152(152, "头条女神"),
    T153(153, "imiss爱蜜社"),
    T154(154, "bololi菠萝社"),
    T155(155, "蜜桃社"),
    T156(156, "尤物馆"),
    T157(157, "嗲囡囡"),
    T158(158, "顽味生活"),
    T159(159, "假面女皇"),
    T160(160, "模范学院"),
    // 75
    T161(161, "魅妍社"),
    T162(162, "爱秀网"),
    T163(163, "优星馆"),
    T164(164, "iess异思趣向"),
    T165(165, "kelagirls克拉女神"),
    T166(166, "dkgirl御女郎"),
    T167(167, "candy糖果画报"),
    T168(168, "youmi尤蜜荟"),
    T169(169, "HuaYan花の颜"),
    T170(170, "中国腿模"),
    T171(171, "qingdouke青豆客"),
    T172(172, "kimoe激萌文化"),
    T173(173, "leyuan星乐园"),
    T174(174, "bindart美束"),
    T175(175, "MICAT猫萌榜"),
    T176(176, "Girlt果团网"),
    T177(177, "青丘女神"),
    T178(178, "花漾写真"),
    T179(179, "星颜社"),
    T180(180, "网红福利美女"),
    // 95
    T181(181, "MISSLEG蜜丝"),
    T182(182, "网红写真"),
    T183(183, "SLADY猎女神"),
    T184(184, "高中生大西瓜"),
    T185(185, "XIAOYU画语界"),
    T186(186, "纳丝摄影"),
    T187(187, "森萝财团"),
    T188(188, "PartyCat轰趴猫"),
    T189(189, "丝维空间"),
    T190(190, "希威社"),
    T191(191, "战前女神"),
    T192(192, "YALAYI雅拉伊"),
    T193(193, "YouMei尤美"),
    // 108
    ;

    private int seq;
    private String desc;

    MeinvlaTypeEnum(int seq, String desc) {
        this.seq = seq;
        this.desc = desc;
    }

    public static List<MeinvlaTypeEnum> getEnums() {
        return List.of(values());
    }
}
