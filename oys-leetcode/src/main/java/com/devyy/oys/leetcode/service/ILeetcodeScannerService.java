package com.devyy.oys.leetcode.service;

import com.devyy.oys.core.base.IBaseService;

/**
 * @since 2019-02-06
 */
public interface ILeetcodeScannerService extends IBaseService {
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
     * 扫描 Text 文本内容
     *
     * @return success
     */
    String doScanTextContents();

    /**
     * 测试 Vuepress 渲染
     *
     * @return success
     */
    String doTestVuePressBugs();
}
