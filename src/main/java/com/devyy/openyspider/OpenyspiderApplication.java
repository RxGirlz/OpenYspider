package com.devyy.openyspider;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @since 2019-12-01
 */
@SpringBootApplication
@MapperScan("com.devyy.openyspider.integration.*")
public class OpenyspiderApplication {

    public static void main(String[] args) {
        SpringApplication.run(OpenyspiderApplication.class, args);
    }

}
