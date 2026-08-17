package com.notificationapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@SpringBootApplication
public class EventpostingApplication {
    //TODO: Implement Rate limiter on the email APP

	public static void main(String[] args) {
		SpringApplication.run(EventpostingApplication.class, args);
	}

}