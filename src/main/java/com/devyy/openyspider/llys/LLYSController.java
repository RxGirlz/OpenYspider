package com.devyy.openyspider.llys;

import com.devyy.openyspider.common.ReptileUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.net.URLDecoder;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * http://m7.22c.im
 *
 * @since 2019-10-01
 */
@RestController
@RequestMapping("/llys")
public class LLYSController {

    private static final Logger logger = LoggerFactory.getLogger(LLYSController.class);

    /**
     * Step1: 网站限制每个请求带时间戳，因此无法持久化 URL，另外限制非会员只能单线程限制，所以使用同步下载
     *
     * @see LLYSTypeEnum
     */
    @PostMapping("/step1")
    public String step1() {
        // 配置 chromedriver.exe 路径
        System.setProperty("webdriver.chrome.driver", "C:/Users/DEVYY/Documents/chromedriver_win32/chromedriver.exe");
        // 启动一个 chrome 实例
        WebDriver webDriver = new ChromeDriver();
        // 设置超时时间为 10s
        webDriver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
        // 预设网页
        webDriver.get("http://m7.22c.im/rs1");
        // wait 10s 启动 flash
        this.waitSeconds(10);

        LLYSTypeEnum[] enums = LLYSTypeEnum.values();
        for (LLYSTypeEnum e : enums) {
            String urlPrefix = e.getPrefix();
            String mp4FolderName = e.getDesc();
            int startIndex = e.getStart();
            int endIndex = e.getEnd();

            for (int i = startIndex; i <= endIndex; i++) {
                String mp4FileName = "D:/恋恋影视爬虫/" + mp4FolderName + "-" + i + ".mp4";
                // 幂等，若当前文件未下载，则进行下载
                File mp4File = new File(mp4FileName);
                if (!mp4File.exists()) {
                    String url = urlPrefix + i;
                    logger.info("==>url={}", url);
                    try {
                        webDriver.get(url);
                    }
                    // 此处捕获所有 Throwable 因为并不需要关心，还会中断程序
                    catch (Throwable t) {
                        logger.warn(t.getMessage().substring(0, 30));
                    }
                    // wait 2s 加载动态页面
                    this.waitSeconds(2);

                    Document document = Jsoup.parse(webDriver.getPageSource());
                    if (Objects.nonNull(document.getElementById("nb"))) {
                        try {
                            String mp4Url = document
                                    .getElementById("nb")
                                    .children()
                                    .get(0)
                                    .getElementsByTag("embed")
                                    .attr("flashvars")
                                    .replace("f=", "");
                            String onlinePath = URLDecoder.decode(mp4Url, "UTF-8");
                            logger.info("onlinePath={}", onlinePath);
                            ReptileUtil.syncDownload(onlinePath, mp4FileName);
                        } catch (Throwable t) {
                            logger.warn("decode failed {}", t.getMessage());
                        }
                    }
                }
            }
        }
        return "success";
    }

    /**
     * 线程睡眠
     *
     * @param seconds 秒
     */
    private void waitSeconds(int seconds) {
        try {
            logger.info("==>waitSeconds {}s", seconds);
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
        }
    }
}
