package com.farmily;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
@EnableScheduling//開啟排程功能，定期檢查團購達標與否
@SpringBootApplication
@EnableAsync    // 啟用 @Async，讓寄信可以非同步執行
public class Cka101G3Application {

	public static void main(String[] args) {
		SpringApplication.run(Cka101G3Application.class, args);
	}

}
