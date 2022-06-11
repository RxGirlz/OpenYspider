package com.devyy.oys.tujidao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 实体类
 *
 * @since 2019-12-01
 */
@Data
@TableName("oys_tujidao_album_t")
public class TuJiDaoAlbumDO {
    /**
     * 自增 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 状态
     *
     * @see com.devyy.oys.srarter.core.enums.StateTypeEnum
     */
    private Integer state;

    /**
     * 总数
     */
    private Integer total;

    /**
     * 相册名
     */
    @TableField("album_name")
    private String albumName;

    /**
     * 相册 ID
     */
    @TableField("album_id")
    private Integer albumId;
}
