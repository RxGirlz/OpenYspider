package com.devyy.openyspider.integration.leetcode;

/**
 * @since 2019-02-06
 */
public interface ILeetcodeService {
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
     * 批量生成 Markdown 文件
     *
     * @return success
     */
    String doGeneratorMarkdownFiles();

    /**
     * 批量生成 Sidebar 模块文件
     *
     * @return success
     */
    String doGeneratorSidebarFiles();

    /**
     * 批量生成 Git 提交脚本
     *
     * @return success
     */
    String doGeneratorGitCommitCmd();

    /**
     * 测试 Vuepress 渲染
     *
     * @return success
     */
    String doTestVuePressBugs();
}
