package com.devyy.oys.tujidao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * @since 2019-12-01
 */
@Data
@TableName("oys_tujidao_album")
public class TujidaoAlbumDO {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer state;

    private Integer total;

    @TableField("album_name")
    private String albumName;

    @TableField("album_id")
    private Integer albumId;

    private Integer type;
}
