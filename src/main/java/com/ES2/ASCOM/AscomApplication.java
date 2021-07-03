package com.ES2.ASCOM;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

import com.ES2.ASCOM.helpers.ConfigValues;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class AscomApplication {

	public static void main(String[] args) {
		ConfigValues config = new ConfigValues();
		SpringApplication.run(AscomApplication.class, args);
	}

}
