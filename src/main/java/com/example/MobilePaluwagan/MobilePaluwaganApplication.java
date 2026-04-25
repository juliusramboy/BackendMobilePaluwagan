package com.example.MobilePaluwagan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import java.util.TimeZone;

@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableCaching
public class MobilePaluwaganApplication {
	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Manila"));
		SpringApplication.run(MobilePaluwaganApplication.class, args);
	}

}
