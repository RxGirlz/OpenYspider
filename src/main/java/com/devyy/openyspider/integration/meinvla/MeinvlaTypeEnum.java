package com.devyy.openyspider.integration.meinvla;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * @since 2019-12-01
 */
@Getter
public enum MeinvlaTypeEnum {
    // videos start
    V04(4, "V-美女街拍"),
    V05(5, "V-丝袜美女"),
    V06(6, "V-日本美女"),
    V07(7, "V-丝宝丝魅"),
    V08(8, "V-妖妖vip"),
    V09(9, "V-小狐仙vip"),
    V10(10, "V-柔丝晴晴"),
    V11(11, "V-梦幻小妖"),
    V12(12, "V-动感之星"),
    V13(13, "V-车模写真"),
    V14(14, "V-欧美女郎"),
    V15(15, "V-性感美女"),
    V16(16, "V-pans写真"),
    V17(17, "V-beautyleg"),
    V18(18, "V-丝间舞"),
    V19(19, "V-细高跟"),
    V20(20, "V-美足美脚"),
    V21(21, "V-台湾女郎"),
    V22(22, "V-丽柜vip"),
    V23(23, "V-rosi美女"),
    V24(24, "V-丝雅写真"),
    V25(25, "V-如一美女"),
    V26(26, "V-HS写真"),
    V27(27, "V-aaa女郎"),
    V28(28, "V-嗲囡囡写真"),
    V29(29, "V-韩国饭拍"),
    V30(30, "V-头条女神"),
    V31(31, "V-1080酱TV"),
    V32(32, "V-aiss丝袜"),
    V33(33, "V-ugirls尤果网"),
    V34(34, "V-菠萝社"),
    V35(35, "V-mestar范模学院"),
    V36(36, "V-网红写真"),
    // videos end

    // albums start
    T86(86, "rq-sata"), // pass
    T87(87, "禁忌摄影"),
    T88(88, "国产美女"), // done
    T89(89, "韩国美女"),
    T90(90, "dgc套图"),
    T91(91, "bwh套图"),
    T92(92, "ys-web"), // pass
    T93(93, "wpb套图"), // pass
    T94(94, "台湾美腿女郎"), // pass
    T95(95, "丝宝丝魅vip"),
    T96(96, "台湾写真tpimage"),
    T97(97, "topqueen"),
    T98(98, "sabra-net"), // pass
    T99(99, "minisuka-tv"), // pass
    T100(100, "bejean-on-line"), // pass
    // 15
    T101(101, "misty套图"),
    T102(102, "bomb-tv套图"),
    T103(103, "妖妖vip套图"), // done
    T104(104, "拍美vip套图"), // done
    T105(105, "波斯猫儿vip"), // done
    T106(106, "丽柜ligui套图"), // pass
    T107(107, "小美vip套图"), // done
    T108(108, "欧美丝袜泰斗"), // pass
    T109(109, "动感小站vip"), // done
    T110(110, "中高艺套图"), // done
    T111(111, "ns-eyes套图"),
    T112(112, "two-套图"),
    T113(113, "柔丝晴晴"),
    T114(114, "cosplay美女"), // pass
    T115(115, "rosi丝袜"), // pass
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
    T128(128, "秀人模特"), // pass
    T129(129, "ru1mm丝袜"),
    T130(130, "丝尚美女"),
    T131(131, "allgravure套图"),
    T132(132, "image-tv套图"), // pass
    T133(133, "imouto-tv套图"), // pass
    T134(134, "上海炫彩时尚"),
    T135(135, "layered-nylons"), // pass
    T136(136, "leghacker"),
    T137(137, "丽阁影像"),
    T138(138, "网络收集"),
    T139(139, "街拍美女"),  // pass
    T140(140, "唯美写真"),
    // 55
    T141(141, "legku丝足"), // done
    T142(142, "推女郎"), // pass
    T143(143, "尤果性感美女"), // pass
    T144(144, "芬妮高清丝足"),
    T145(145, "神艺缘"),
    T146(146, "蕾丝兔宝宝"),
    T147(147, "mygirl美媛馆"), // pass
    T148(148, "aiss爱丝"), // pass
    T149(149, "tgod推女神"), // pass
    T150(150, "丝雅写真"), // done
    T151(151, "heisiai写真"), // done
    T152(152, "头条女神"), // pass
    T153(153, "imiss爱蜜社"), // pass
    T154(154, "bololi菠萝社"), // pass
    T155(155, "蜜桃社"), // pass
    T156(156, "尤物馆"), // pass
    T157(157, "嗲囡囡"), // pass
    T158(158, "顽味生活"), // pass
    T159(159, "假面女皇"), // done
    T160(160, "模范学院"), // pass
    // 75
    T161(161, "魅妍社"), // pass
    T162(162, "爱秀网"), // pass
    T163(163, "优星馆"), // pass
    T164(164, "iess异思趣向"), // pass
    T165(165, "kelagirls克拉女神"), // pass
    T166(166, "dkgirl御女郎"), // pass
    T167(167, "candy糖果画报"), // pass
    T168(168, "youmi尤蜜荟"), // pass
    T169(169, "HuaYan花の颜"), // pass
    T170(170, "中国腿模"), // done
    T171(171, "qingdouke青豆客"), // pass
    T172(172, "kimoe激萌文化"), // pass
    T173(173, "leyuan星乐园"), // pass
    T174(174, "bindart美束"), // pass
    T175(175, "MICAT猫萌榜"), // pass
    T176(176, "Girlt果团网"), // pass
    T177(177, "青丘女神"), // pass
    T178(178, "花漾写真"), // pass
    T179(179, "星颜社"), // pass
    T180(180, "网红福利美女"), // done
    // 95
    T181(181, "MISSLEG蜜丝"), // pass
    T182(182, "网红写真"), // done
    T183(183, "SLADY猎女神"), // pass
    T184(184, "高中生大西瓜"), // done
    T185(185, "XIAOYU画语界"), // pass
    T186(186, "纳丝摄影"), // done
    T187(187, "森萝财团"), // done
    T188(188, "PartyCat轰趴猫"), // done
    T189(189, "丝维空间"), // done
    T190(190, "希威社"), // done
    T191(191, "战前女神"), // done
    T192(192, "YALAYI雅拉伊"), // pass
    T193(193, "YouMei尤美"), // pass
    // 108
    // albums end

    UNDEFINED(404, "未定义"),
    ;

    private int seq;
    private String desc;
    private boolean album;

    MeinvlaTypeEnum(int seq, String desc) {
        this.seq = seq;
        this.desc = desc;
        this.album = seq > 36;
    }

    public static List<MeinvlaTypeEnum> getEnums() {
        return List.of(values());
    }

    public static MeinvlaTypeEnum getEnumBySeq(int seq) {
        for (MeinvlaTypeEnum typeEnum : values()) {
            if (typeEnum.getSeq() == seq) {
                return typeEnum;
            }
        }
        return UNDEFINED;
    }
}
