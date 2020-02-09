package com.devyy.openyspider.integration.leetcode.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @since 2020-02-09
 */
@Data
@TableName("tbl_leetcode_image")
public class LeetcodeImageDO {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("img_name")
    private String imgName;

    @TableField("img_url")
    private String imgUrl;
    /**
     * 问题 ID eg. 1
     */
    @TableField("question_id")
    private Long questionId;

    private Integer state;
}
