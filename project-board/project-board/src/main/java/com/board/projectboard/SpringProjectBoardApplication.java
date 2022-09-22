package com.board.projectboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class SpringProjectBoardApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringProjectBoardApplication.class, args);
	}

}
