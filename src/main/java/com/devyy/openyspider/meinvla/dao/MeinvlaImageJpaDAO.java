package com.devyy.openyspider.meinvla.dao;

import com.devyy.openyspider.meinvla.model.MeinvlaImgDO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MeinvlaImageJpaDAO extends JpaRepository<MeinvlaImgDO, Long> {

    MeinvlaImgDO findByImgUrlEquals(String url);

}
