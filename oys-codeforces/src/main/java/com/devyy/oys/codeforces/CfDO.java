package com.devyy.oys.codeforces;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * Entity 实体类
 *
 * @since 2021-01-24
 */
@Data
@TableName("oys_codeforces")
public class CfDO {
    @TableId("SUBMISSION_ID")
    private String submissionId;

    @TableField("NO")
    private Long no;

    @TableField("FRAGMENT")
    private String fragment;
}
