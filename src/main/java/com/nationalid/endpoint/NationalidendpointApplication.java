package com.nationalid.endpoint;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class NationalidendpointApplication {

	public static void main(String[] args) {
		SpringApplication.run(NationalidendpointApplication.class, args);
	}

}
