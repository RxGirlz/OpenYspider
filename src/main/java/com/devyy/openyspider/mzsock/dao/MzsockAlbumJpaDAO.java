package com.devyy.openyspider.mzsock.dao;

import com.devyy.openyspider.mzsock.model.MzsockAlbumDO;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA 接口
 *
 * @since 2019-09-13
 */
public interface MzsockAlbumJpaDAO extends JpaRepository<MzsockAlbumDO, Long> {

    MzsockAlbumDO findByAlbumUrlEquals(String url);
}
