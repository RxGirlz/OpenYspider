package com.devyy.openyspider.integration.leetcode;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @since 2019-02-06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("tbl_leetcode_problem")
public class LeetCodeProblemDO {

    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 标题名 eg. "Two Sum"
     */
    private String title;
    /**
     * 中文标题名 eg. "两数之和"
     */
    @TableField("title_cn")
    private String titleCn;
    /**
     * 路径名 eg. "two-sum"
     */
    @TableField("title_slug")
    private String titleSlug;
    /**
     * 是否付费 eg. false
     */
    @TableField("paid_only")
    private Boolean paidOnly;
    /**
     * 问题 ID eg. 1
     */
    @TableField("question_id")
    private Long questionId;
    /**
     * 问题 前端 ID eg. "1"
     */
    @TableField("fe_question_id")
    private String feQuestionId;
    /**
     * 难度
     */
    private Integer difficulty;
    /**
     * 是否存在渲染bug eg. 0 否
     */
    @TableField("has_bug")
    private Integer hasBug;
}
