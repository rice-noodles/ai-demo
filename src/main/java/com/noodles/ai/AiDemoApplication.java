package com.noodles.ai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Noodles
 * @date 2026/3/25 9:35
 */
@SpringBootApplication
@MapperScan("com.noodles.ai.mapper")
public class AiDemoApplication {

  public static void main(String[] args) {
    SpringApplication.run(AiDemoApplication.class, args);
  }

}
