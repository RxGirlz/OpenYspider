package com.devyy.openyspider.yalayi;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * JPA 接口
 *
 * @since 2019-09-12
 */
public interface YalayiJpaDAO extends JpaRepository<YalayiDO, Long> {

    /**
     * 根据相册编号获取单个相册记录
     *
     * @param albumInfo 相册编号
     */
    YalayiDO findByAlbumInfoEquals(String albumInfo);


    /**
     * 根据相册类型获取单个相册记录
     *
     * @param albumType 相册类型
     */
    List<YalayiDO> findByAlbumTypeEquals(Integer albumType);
}
