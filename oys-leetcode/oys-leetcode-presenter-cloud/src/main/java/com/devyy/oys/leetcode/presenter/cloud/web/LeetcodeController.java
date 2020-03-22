package com.devyy.oys.leetcode.presenter.cloud.web;

import com.devyy.oys.leetcode.presenter.service.ILeetcodeScannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @since 2019-02-06
 */
@Component
public class LeetcodeController implements ILeetcodeController {
    @Autowired
    private ILeetcodeScannerService leetcodeScannerService;

    @Override
    public String step1() {
        return leetcodeScannerService.doScanProblems();
    }

    @Override
    public String step2() {
        return leetcodeScannerService.doScanTiTleCn();
    }

    @Override
    public String step3() {
        return leetcodeScannerService.doScanProblemsDetail();
    }

    @Override
    public String step4() {
        return leetcodeScannerService.doTestVuePressBugs();
    }

    @Override
    public String step5() {
        return leetcodeScannerService.doScanImages();
    }

    @Override
    public String step6() {
        return leetcodeScannerService.doDownload();
    }

    @Override
    public String step7() {
        return leetcodeScannerService.doScanTextContents();
    }
}
