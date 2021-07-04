package com.devyy.oys.jav.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 番号实体
 *
 * @since 2021-03-21
 */
@Data
@TableName("oys_jav_entity")
public class JavEntityDO {
    /**
     * 识别码（番号）
     */
    @TableId
    private String id;
    /**
     * 名称
     */
    private String title;
    /**
     * 详情页 URL
     */
    @TableField("page_url")
    private String pageUrl;
    private Integer state;

    /**
     * 出版商
     */
    private String studio;
    /**
     * 类型
     */
    private String genre;
    /**
     * 系列
     */
    private String label;
    /**
     * 发售日
     */
    @TableField("release_date")
    private String releaseDate;
    /**
     * 时长
     */
    private String length;
    /**
     * 演员
     */
    private String stars;
    /**
     * 剧情
     */
    private String story;
    /**
     * 封面 URL
     */
    @TableField("cover_url")
    private String coverUrl;
}
