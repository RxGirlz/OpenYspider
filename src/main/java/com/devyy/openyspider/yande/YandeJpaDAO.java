package com.devyy.openyspider.yande;

import org.springframework.data.jpa.repository.JpaRepository;


/**
 * JPA 接口
 *
 * @since 2019-09-13
 */
public interface YandeJpaDAO extends JpaRepository<YandeDO, Long> {

    YandeDO findByImgNameEquals(String imgName);

}
