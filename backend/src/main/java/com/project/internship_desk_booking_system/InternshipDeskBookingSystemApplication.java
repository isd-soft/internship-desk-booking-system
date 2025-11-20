package com.project.internship_desk_booking_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@EnableAsync
public class InternshipDeskBookingSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(InternshipDeskBookingSystemApplication.class, args);
	}

}
