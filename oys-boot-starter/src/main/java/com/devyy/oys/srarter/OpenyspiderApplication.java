package com.devyy.oys.srarter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @since 2019-12-01
 */
@SpringBootApplication
@ComponentScan("com.devyy.oys")
public class OpenyspiderApplication {

    public static void main(String[] args) {
        SpringApplication.run(OpenyspiderApplication.class, args);
    }

}

