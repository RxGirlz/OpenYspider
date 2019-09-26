package com.devyy.openyspider.rosi.model;

import javax.persistence.*;

/**
 * ROSI-相册实体类
 *
 * @author zhangyiyang
 * @since 2019-09-13
 */
@Entity
@Table(name = "tbl_rosi_album")
public class RosiAlbumDO {

    // <ul id="sliding">
    //   <li data-id="5142">
    //     <a
    //       href="/ROSIMM/2019/09/13/5142.htm"
    //       title="ROSI写真No.2839 三色抱臀短裙真空肉丝性感美臀玉足写真"
    //       target="_blank"
    //       ><img
    //         src="https://wvw.rosmm33.com/pic/upload/2019/09/13/rosmm-2839-113840648.jpg"
    //         alt="ROSI写真No.2839 三色抱臀短裙真空肉丝性感美臀玉足写真"
    //     /></a>
    //     <div class="picnum">86P</div>
    //     <p>
    //       <span class="like" title="0人喜欢">0</span><span class="watch" title="0人欣赏过">0</span
    //       ><span class="new" title="最新图片"><font color="red">NEW</font></span>
    //     </p>
    //     <p class="p-title">
    //       <a href="/ROSIMM/2019/09/13/5142.htm">ROSI写真No.2839 三色抱臀短裙真空肉丝性感美臀玉足写真</a>
    //     </p>
    //   </li>
    // </ul>

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

    /**
     * data_id 唯一值
     */
    @Column(name = "data_id")
    private Integer dataId;

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

    public Integer getDataId() {
        return dataId;
    }

    public void setDataId(Integer dataId) {
        this.dataId = dataId;
    }
}
