package com.devyy.openyspider.meinvla.model;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "tbl_meinvla_image")
public class MeinvlaImgDO {
    /**
     * ID 主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 图片名
     */
    @Column(name = "img_name")
    private String imgName;

    /**
     * 图片Url
     */
    @Column(name = "img_url")
    private String imgUrl;

    /**
     * 父ID
     */
    @Column(name = "album_id")
    private Long albumId;
}
