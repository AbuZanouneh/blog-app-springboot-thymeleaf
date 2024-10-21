package com.ats.blogapp;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.filter.HiddenHttpMethodFilter;

@SpringBootApplication
@EnableMethodSecurity(prePostEnabled = true)
public class BlogappApplication {

	public static void main(String[] args) {
		SpringApplication.run(BlogappApplication.class, args);
	}

	// Add the HiddenHttpMethodFilter bean to your application to enable HTTP method overriding.
	@Bean
	public HiddenHttpMethodFilter hiddenHttpMethodFilter() {
		return new HiddenHttpMethodFilter();
	}
}
