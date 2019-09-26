package com.devyy.openyspider.tujidao;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * JPA 接口
 *
 * @since 2019-09-12
 */
public interface TujidaoJpaDAO extends JpaRepository<TujidaoDO, Long> {

    /**
     * 根据相册编号获取单个相册记录
     *
     * @param number 相册编号
     */
    TujidaoDO findByNumberEquals(Integer number);

    /**
     * 根据相册编号区间获取相册记录
     *
     * @param start 开始 number
     * @param end   终止 number
     */
    List<TujidaoDO> findAllByNumberBetweenOrderByNumberDesc(Integer start, Integer end);
}
