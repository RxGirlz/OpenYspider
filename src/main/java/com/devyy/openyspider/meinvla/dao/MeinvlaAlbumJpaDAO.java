package com.devyy.openyspider.meinvla.dao;

import com.devyy.openyspider.meinvla.model.MeinvlaAlbumDO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeinvlaAlbumJpaDAO extends JpaRepository<MeinvlaAlbumDO, Long> {

    MeinvlaAlbumDO findByAlbumUrlEquals(String url);
}
