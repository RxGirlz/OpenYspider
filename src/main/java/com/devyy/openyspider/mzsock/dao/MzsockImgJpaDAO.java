package com.devyy.openyspider.mzsock.dao;

import com.devyy.openyspider.mzsock.model.MzsockImgDO;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA 接口
 *
 * @since 2019-09-13
 */
public interface MzsockImgJpaDAO extends JpaRepository<MzsockImgDO, Long> {

    MzsockImgDO findByImgUrlEquals(String imgUrl);
}
