package com.nationalid.endpoint;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import nationalid.loggers.ConsoleLogger;
import nationalid.loggers.FileLogger;
import nationalid.loggers.LogManager;
import nationalid.models.Calculators.GlobalCodeCalculator;
import nationalid.models.Calculators.LithuanianCodeCalculator;

@EnableScheduling
@SpringBootApplication
public class NationalidendpointApplication {

	public static void main(String[] args) {
		SpringApplication.run(NationalidendpointApplication.class, args);
	}

}
