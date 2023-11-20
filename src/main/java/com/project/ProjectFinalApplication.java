package com.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
@ComponentScan(basePackages = "com.project")
@EntityScan(basePackages = "com.project.domain.entity")
public class ProjectFinalApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProjectFinalApplication.class, args);
	}

}
