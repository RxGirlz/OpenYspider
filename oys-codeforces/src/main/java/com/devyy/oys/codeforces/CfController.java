package com.devyy.oys.codeforces;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.devyy.oys.codeforces.dao.CfMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import lombok.extern.slf4j.Slf4j;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.List;

/**
 * Controller + ServiceImpl
 *
 * @since 2021-01-24
 */
@Slf4j
@RestController
@RequestMapping("/codeforces")
@Api(tags = "Codeforces 爬虫")
public class CfController {
    @Value("${oys.codeforces.handleOrEmail:123}")
    private String handleOrEmail;

    @Value("${oys.codeforces.password:123}")
    private String password;

    @Autowired
    private CfMapper cfMapper;

    @ApiOperation(value = "获取片段")
    @PostMapping("/step1")
    public String doGetFragment() throws InterruptedException {
        System.setProperty("webdriver.chrome.driver", "D:\\GITHUB\\LTS\\codeforces-spider\\chromedriver.exe");
        WebDriver webDriver = new ChromeDriver();
        webDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(30));

        // login
        webDriver.get("https://codeforces.com/enter?back=%2F");
        webDriver.findElement(By.id("handleOrEmail")).sendKeys(handleOrEmail);
        webDriver.findElement(By.id("password")).sendKeys(password);
        webDriver.findElement(By.id("remember")).click();
        webDriver.findElement(By.className("submit")).click();
        Thread.sleep(5000);

        List<CfDO> cfDOList = cfMapper.selectList(new QueryWrapper<CfDO>().select().isNull("FRAGMENT"));
        log.info("==>cfDOList size={}", cfDOList.size());
        for (CfDO cfDO : cfDOList) {
            String submissionId = cfDO.getSubmissionId();

            String url = "https://codeforces.com/contest/1593/submission/" + submissionId;
            log.info("==>url={}", url);
            try {
                webDriver.get(url);
                webDriver.findElement(By.className("click-to-view-tests")).click();
                Thread.sleep(3000);

                List<WebElement> outputs = webDriver.findElements(By.className("output"));
                int size = outputs.size();
                if (outputs.size() < 7) {
                    Thread.sleep(3000);
                    outputs = webDriver.findElements(By.className("output"));
                    size = outputs.size();
                }

                WebElement outputs8 = outputs.get(size - 1);
                String fragment = outputs8.getText().substring(0, 500);
                cfDO.setFragment(fragment);
                log.info(fragment);

                cfMapper.updateById(cfDO);
                log.info("<==success submissionId={}", submissionId);
            } catch (Exception e) {
                log.warn("<==failed submissionId={}", submissionId);
            }
        }
        webDriver.close();

        return "success";
    }

    @ApiOperation(value = "合并片段")
    @PostMapping("/step2")
    public String doMergeFragment() {
        List<CfDO> cfDOList = cfMapper.selectList(new QueryWrapper<CfDO>().select().orderByAsc("NO"));
        log.info("==>cfDOList size={}", cfDOList.size());
        StringBuilder stringBuilder = new StringBuilder();
        for (CfDO cfDO : cfDOList) {
            String fragment = cfDO.getFragment();
            stringBuilder.append(fragment);
        }
        log.info("==>doMergeFragment={}", stringBuilder);

        return "success";
    }

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;


    @ApiOperation(value = "Kafka")
    @PostMapping("/step3")
    public String testKafka() {
        kafkaTemplate.send("T_ubuntu", "test");
        log.info("==>kafkaTemplate.send success");
        return "success";
    }

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @ApiOperation(value = "redis")
    @PostMapping("/step4")
    public String testRedis() {
        stringRedisTemplate.boundValueOps("ubuntu:redis:test").set("hello");
        log.info("==>redis set success");

        String value = stringRedisTemplate.boundValueOps("ubuntu:redis:test").get();
        log.info("==>redis get value={}", value);
        return "success";
    }
}
