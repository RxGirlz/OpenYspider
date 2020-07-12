package com.devyy.oys.srarter.core;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;


@Configuration
public class OpenYspiderAutoConfig {
    private static String WEBDRIVER_CHROME_DRIVER_PATH;

    public static String getWEBDRIVER_CHROME_DRIVER_PATH() {
        return WEBDRIVER_CHROME_DRIVER_PATH;
    }

    @Value("${oys.config.webdriver.chrome.driver.path}")
    public void setWEBDRIVER_CHROME_DRIVER_PATH(String WEBDRIVER_CHROME_DRIVER_PATH) {
        OpenYspiderAutoConfig.WEBDRIVER_CHROME_DRIVER_PATH = WEBDRIVER_CHROME_DRIVER_PATH;
    }
}
