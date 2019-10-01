package com.devyy.openyspider.tangyun.model;

import javax.persistence.*;

/**
 * 唐韵文化-相册实体类
 * <p>
 * eg.http://www.tangyun365.com/photo/yuanchuangtaotu/2016-06-16/331.html
 *
 * @since 2019-06-20
 */
@Table(name = "tbl_tangyun_album")
@Entity
public class TangYunAlbumDO {

    /**
     * ID 主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 相册名
     * eg.雪儿皮靴黑丝足(P0098)图片集
     */
    @Column(name = "name")
    private String name;

    /**
     * url
     * eg.2016-06-16/331.html
     */
    @Column(name = "url")
    private String url;

    /**
     *
     */
    @Column(name = "source_type")
    private Integer SourceType;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getSourceType() {
        return SourceType;
    }

    public void setSourceType(Integer sourceType) {
        SourceType = sourceType;
    }
}
