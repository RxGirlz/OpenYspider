package com.devyy.openyspider.integration.meinvla;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @since 2019-12-01
 */
@Data
@TableName("tbl_meinvla_album")
public class MeinvlaAlbumDO {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer state;

    @TableField("album_name")
    private String albumName;

    @TableField("album_id")
    private Integer albumId;

    private Integer type;

    private Integer total;

    @TableField("cur_total")
    private Integer curTotal;
}
