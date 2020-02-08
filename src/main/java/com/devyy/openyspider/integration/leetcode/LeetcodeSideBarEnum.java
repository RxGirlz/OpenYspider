package com.devyy.openyspider.integration.leetcode;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * Vuepress Sidebar 模块分页
 *
 * @since 2019-02-07
 */
@Getter
public enum LeetcodeSideBarEnum {

    SIDEBAR_001_100(1, 100),
    SIDEBAR_101_200(101, 200),
    SIDEBAR_201_300(201, 300),
    SIDEBAR_301_400(301, 400),
    SIDEBAR_401_500(401, 500),
    SIDEBAR_501_600(501, 600),
    SIDEBAR_601_700(601, 700),
    SIDEBAR_701_800(701, 800),
    SIDEBAR_801_900(801, 900),
    SIDEBAR_901_1000(901, 1000),
    SIDEBAR_1001_1100(1001, 1100),
    SIDEBAR_1101_1200(1101, 1200),
    SIDEBAR_1201_1300(1201, 1300),
    SIDEBAR_1301_1400(1301, 1400),

    ;

    /**
     * 开始 ID
     */
    private int startSeq;
    /**
     * 终止 ID
     */
    private int endSeq;
    /**
     * sidebar 切片
     */
    private String sidebarSlice;
    /**
     * js 模块名
     */
    private String jsModuleName;

    LeetcodeSideBarEnum(int startSeq, int endSeq) {
        this.startSeq = startSeq;
        this.endSeq = endSeq;
        if (startSeq == 1) {
            this.sidebarSlice = "/001-" + endSeq + "/";
            this.jsModuleName = "sidebarOf001To" + endSeq;
        } else {
            this.sidebarSlice = "/" + startSeq + "-" + endSeq + "/";
            this.jsModuleName = "sidebarOf" + startSeq + "To" + endSeq;
        }
    }

    public static List<LeetcodeSideBarEnum> getEnums() {
        return Arrays.asList(values());
    }
}
