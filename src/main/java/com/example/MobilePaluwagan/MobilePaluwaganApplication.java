package com.example.MobilePaluwagan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class MobilePaluwaganApplication {

	public static void main(String[] args) {
		SpringApplication.run(MobilePaluwaganApplication.class, args);
	}

}
