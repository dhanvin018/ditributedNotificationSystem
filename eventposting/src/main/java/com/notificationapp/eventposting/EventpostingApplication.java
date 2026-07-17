package com.notificationapp.eventposting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EventpostingApplication {
    //TODO: Implement Rate limiter on the email APP

	public static void main(String[] args) {
		SpringApplication.run(EventpostingApplication.class, args);
	}

}