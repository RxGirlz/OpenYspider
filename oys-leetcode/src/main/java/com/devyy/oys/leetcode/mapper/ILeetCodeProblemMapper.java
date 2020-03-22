package com.devyy.oys.leetcode.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.devyy.oys.leetcode.domain.LeetCodeProblemDO;
import org.springframework.stereotype.Repository;

/**
 * @since 2019-02-06
 */
@Repository
public interface ILeetCodeProblemMapper extends BaseMapper<LeetCodeProblemDO> {
}
