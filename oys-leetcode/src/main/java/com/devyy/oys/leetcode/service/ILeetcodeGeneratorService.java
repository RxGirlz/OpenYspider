package com.devyy.oys.leetcode.service;

/**
 * @since 2019-02-06
 */
public interface ILeetcodeGeneratorService {
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
     * 批量生成 HTML 文件
     *
     * @return success
     */
    String doGeneratorHtmlSrcFiles();
}
