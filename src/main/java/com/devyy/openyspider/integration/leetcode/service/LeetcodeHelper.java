package com.devyy.openyspider.integration.leetcode.service;


import com.devyy.openyspider.integration.leetcode.enums.LeetcodeSideBarEnum;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class LeetcodeHelper {
    public static final String URL1 = "https://s3-lc-upload.s3.amazonaws.com";
    public static final String URL2 = "https://assets.leetcode-cn.com";
    public static final String URL3 = "https://assets.leetcode.com";
    public static final String URL4 = "https://aliyun-lc-upload.oss-cn-hangzhou.aliyuncs.com";
    public static final String URL5 = "https://upload.wikimedia.org";
    public static final String URL6 = "http://upload.wikimedia.org";

    public static final String FILE_FOLDER_NAME = "C:/Users/DEVYY/Documents/GitHub/翻译工程/Leetcode-Hub-beta/generator3";

    /**
     * 根据 feQuestionId 获取 SidebarSlice
     *
     * @param feQuestionId feQuestionId
     * @return SidebarSlice
     */
    public static String getSidebarSliceByFeQuestionId(String feQuestionId) {
        List<LeetcodeSideBarEnum> leetcodeSideBarEnums = LeetcodeSideBarEnum.getEnums();
        if (StringUtils.isNumeric(feQuestionId)) {
            int feQuestionIdNum = Integer.parseInt(feQuestionId);
            for (LeetcodeSideBarEnum leetcodeSideBarEnum : leetcodeSideBarEnums) {
                if (leetcodeSideBarEnum.getStartSeq() <= feQuestionIdNum
                        && feQuestionIdNum <= leetcodeSideBarEnum.getEndSeq()) {
                    return leetcodeSideBarEnum.getSidebarSlice();
                }
            }
        } else if (feQuestionId.contains("LCP")) {
            return LeetcodeSideBarEnum.SIDEBAR_1301_1400.getSidebarSlice();
        }
        return LeetcodeSideBarEnum.SIDEBAR_INTERVIEW.getSidebarSlice();
    }
}
