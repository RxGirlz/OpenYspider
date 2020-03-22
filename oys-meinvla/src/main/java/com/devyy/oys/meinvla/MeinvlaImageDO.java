package com.devyy.oys.meinvla;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @since 2019-12-01
 */
@Data
@TableName("tbl_meinvla_image")
public class MeinvlaImageDO {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("img_name")
    private String imgName;

    @TableField("img_url")
    private String imgUrl;

    @TableField("album_id")
    private Integer albumId;

    private Integer state;
}
