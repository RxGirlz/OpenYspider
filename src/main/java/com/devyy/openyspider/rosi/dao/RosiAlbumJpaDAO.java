package com.devyy.openyspider.rosi.dao;

import com.devyy.openyspider.rosi.model.RosiAlbumDO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * JPA 接口
 *
 * @since 2019-09-13
 */
public interface RosiAlbumJpaDAO extends JpaRepository<RosiAlbumDO, Long> {

    RosiAlbumDO findByDataIdEquals(Integer dataId);

    List<RosiAlbumDO> findAllByDataIdBetweenOrderByDataIdDesc(Integer start, Integer end);

    List<RosiAlbumDO> findAllByAlbumTypeEquals(Integer albumType);
}
