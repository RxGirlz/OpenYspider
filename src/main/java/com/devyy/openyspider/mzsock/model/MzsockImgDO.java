package com.devyy.openyspider.mzsock.model;

import javax.persistence.*;

/**
 * mzsock-图片实体类
 *
 * @author zhangyiyang
 * @since 2019-09-13
 */
@Entity
@Table(name = "tbl_sub_mzsock_album2")
public class MzsockImgDO {

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
    private Integer albumId;

    public String getImgName() {
        return imgName;
    }

    public void setImgName(String imgName) {
        this.imgName = imgName;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public Integer getAlbumId() {
        return albumId;
    }

    public void setAlbumId(Integer albumId) {
        this.albumId = albumId;
    }
}
