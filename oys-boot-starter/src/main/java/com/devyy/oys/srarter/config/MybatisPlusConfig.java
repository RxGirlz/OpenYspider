package com.devyy.oys.srarter.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * Mybatis-Plus 配置类
 *
 * @since 2020-03-22
 */
@Configuration
@MapperScan("com.devyy.oys.**.dao")
public class MybatisPlusConfig {
}
