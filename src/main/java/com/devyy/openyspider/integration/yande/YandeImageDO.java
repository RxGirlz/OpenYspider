package com.devyy.openyspider.integration.yande;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @since 2019-12-01
 */
@Data
@TableName("tbl_yande_image")
class YandeImageDO {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("img_name")
    private String imgName;

    @TableField("img_url")
    private String imgUrl;

    private Integer state;
}
