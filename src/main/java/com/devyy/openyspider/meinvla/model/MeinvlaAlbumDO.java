package com.devyy.openyspider.meinvla.model;

import lombok.Builder;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "tbl_meinvla_album")
public class MeinvlaAlbumDO {
    /**
     * ID 主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 相册名
     */
    @Column(name = "album_name")
    private String albumName;

    /**
     * 相册Url
     */
    @Column(name = "album_url")
    private String albumUrl;

    /**
     * 相册类型
     */
    @Column(name = "album_type")
    private String albumType;
}
