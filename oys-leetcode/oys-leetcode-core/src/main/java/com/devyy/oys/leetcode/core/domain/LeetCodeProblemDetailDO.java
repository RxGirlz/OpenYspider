package com.devyy.oys.leetcode.core.domain;

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
@TableName("oys_leetcode_problem_detail")
public class LeetCodeProblemDetailDO {

    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 问题 ID eg. 1
     */
    @TableField("question_id")
    private Long questionId;
    /**
     * HTML 问题内容
     */
    @TableField("html_content")
    private String htmlContent;
    /**
     * TXT 问题内容
     */
    @TableField("txt_content")
    private String txtContent;
}
