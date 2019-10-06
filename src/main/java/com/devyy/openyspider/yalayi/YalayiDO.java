package com.devyy.openyspider.yalayi;


import javax.persistence.*;

/**
 * 雅拉伊-相册实体类
 *
 * @author zhangyiyang
 * @since 2019-09-13
 */
@Entity
@Table(name = "tbl_yalayi_album")
public class YalayiDO {

    // <div class="img-box">
    //   <div class="nei">
    //     <div class="img">
    //       <a href="https://www.yalayi.com/gallery/328.html" target="_blank"
    //         ><img src="./原创美女写真摄影作品列表 - 雅拉伊_files/cover(1).jpg"
    //       /></a>
    //       <div class="num">42p</div>
    //     </div>
    //     <div class="info">
    //       <div class="left">Y296</div>
    //       <div class="right">2019-09-11</div>
    //     </div>
    //     <div class="album">
    //       <a href="https://www.yalayi.com/models/126.html" target="_blank"
    //         ><img src="./原创美女写真摄影作品列表 - 雅拉伊_files/126.jpg" class="avatar"
    //       /></a>
    //       <span class="name"><a href="https://www.yalayi.com/models/126.html" target="_blank">沈美美</a></span>
    //       <span class="sub"
    //         ><a href="https://www.yalayi.com/gallery/328.html" target="_blank">《婷婷袅袅》</a></span
    //       >
    //     </div>
    //   </div>
    // </div>

    /**
     * ID 主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 模特名
     * eg.沈美美
     */
    @Column(name = "model_name")
    private String modelName;

    /**
     * 相册名
     * eg.《婷婷袅袅》
     */
    @Column(name = "album_name")
    private String albumName;

    /**
     * 相册Url
     * eg.https://www.yalayi.com/gallery/328.html
     */
    @Column(name = "album_url")
    private String albumUrl;

    /**
     * 相册更新时间
     * eg.2019-09-11
     */
    @Column(name = "album_update")
    private String albumUpdate;

    /**
     * 相册编号
     * eg.Y296
     */
    @Column(name = "album_info")
    private String albumInfo;

    /**
     * 相册图片数
     * eg.42p
     */
    @Column(name = "album_num")
    private Integer albumNum;

    /**
     * 相册类型
     */
    @Column(name = "album_type")
    private Integer albumType;

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
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

    public String getAlbumUpdate() {
        return albumUpdate;
    }

    public void setAlbumUpdate(String albumUpdate) {
        this.albumUpdate = albumUpdate;
    }

    public String getAlbumInfo() {
        return albumInfo;
    }

    public void setAlbumInfo(String albumInfo) {
        this.albumInfo = albumInfo;
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
