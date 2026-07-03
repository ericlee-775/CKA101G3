package com.farmily;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync    // 啟用 @Async，讓寄信可以非同步執行
public class Cka101G3Application {

	public static void main(String[] args) {
		SpringApplication.run(Cka101G3Application.class, args);

	}
}


