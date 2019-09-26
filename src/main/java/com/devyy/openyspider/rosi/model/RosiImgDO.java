package com.devyy.openyspider.rosi.model;

import javax.persistence.*;

/**
 * ROSI-IMG实体类
 *
 * @author zhangyiyang
 * @since 2019-09-13
 */
@Entity
@Table(name = "tbl_rosiimg_album")
public class RosiImgDO {

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
     * data_id 唯一值
     */
    @Column(name = "data_id")
    private Integer dataId;

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

    public Integer getDataId() {
        return dataId;
    }

    public void setDataId(Integer dataId) {
        this.dataId = dataId;
    }
}
