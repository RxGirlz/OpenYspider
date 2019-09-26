package com.devyy.openyspider.rosi.dao;

import com.devyy.openyspider.rosi.model.RosiImgDO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * JPA 接口
 *
 * @since 2019-09-13
 */
public interface RosiImgJpaDAO extends JpaRepository<RosiImgDO, Long> {

    RosiImgDO findByImgUrlEquals(String imgUrl);

    List<RosiImgDO> findAllByDataIdEquals(Integer dataId);
}
