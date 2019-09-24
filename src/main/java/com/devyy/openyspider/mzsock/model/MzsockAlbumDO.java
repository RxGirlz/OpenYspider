package com.devyy.openyspider.mzsock.model;

import javax.persistence.*;

/**
 * mzsock-相册实体类
 *
 * @author zhangyiyang
 * @since 2019-09-13
 */
@Entity
@Table(name = "tbl_mzsock_album2")
public class MzsockAlbumDO {

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
     * 相册图片数
     */
    @Column(name = "album_num")
    private Integer albumNum;

    /**
     * 相册类型
     */
    @Column(name = "album_type")
    private Integer albumType;

    public Long getId() {
        return id;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public String getAlbumUrl() {
        return albumUrl;
    }

    public void setAlbumUrl(String albumUrl) {
        this.albumUrl = albumUrl;
    }

    public Integer getAlbumNum() {
        return albumNum;
    }

    public void setAlbumNum(Integer albumNum) {
        this.albumNum = albumNum;
    }

    public Integer getAlbumType() {
        return albumType;
    }

    public void setAlbumType(Integer albumType) {
        this.albumType = albumType;
    }

}
