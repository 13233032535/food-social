package com.imooc.dinners;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Created by Liu Ming on 2020/11/25.
 */
@MapperScan("com.imooc.dinners.mapper")
@SpringBootApplication
public class DinnersApplication {
    public static void main(String[] args) {
        SpringApplication.run(DinnersApplication.class, args);
    }
}
