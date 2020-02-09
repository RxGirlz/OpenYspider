package com.devyy.openyspider.integration.leetcode.service;

import com.devyy.openyspider.base.IBaseService;

/**
 * @since 2019-02-06
 */
public interface ILeetcodeService extends IBaseService {
    /**
     * 扫描问题集
     *
     * @return success
     */
    String doScanProblems();

    /**
     * 匹配中文标题
     *
     * @return success
     */
    String doScanTiTleCn();

    /**
     * 扫描问题题目
     *
     * @return success
     */
    String doScanProblemsDetail();

    /**
     * 测试 Vuepress 渲染
     *
     * @return success
     */
    String doTestVuePressBugs();
}
