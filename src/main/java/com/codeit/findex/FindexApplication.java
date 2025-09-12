package com.codeit.findex;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling   // ★ 스케줄러 활성화
public class FindexApplication {

    public static void main(String[] args) {
        SpringApplication.run(FindexApplication.class, args);
    }

}
