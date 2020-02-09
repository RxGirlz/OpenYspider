package com.devyy.openyspider.integration.leetcode.enums;

import lombok.Getter;

/**
 * Leetcode 题目难度
 *
 * @since 2019-02-06
 */
@Getter
public enum LeetcodeDifficultyTypeEnum {
    /**
     * 简单
     */
    LEVEL_EASY(1, "简单"),
    /**
     * 中等
     */
    LEVEL_MEDIUM(2, "中等"),
    /**
     * 困难
     */
    LEVEL_HARD(3, "困难");

    private int seq;
    private String desc;

    LeetcodeDifficultyTypeEnum(int seq, String desc) {
        this.seq = seq;
        this.desc = desc;
    }
}
