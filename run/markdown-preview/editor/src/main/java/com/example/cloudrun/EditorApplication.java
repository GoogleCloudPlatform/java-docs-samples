package com.example.cloudrun;

import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EditorApplication {

	private static final Logger logger = LoggerFactory.getLogger(EditorApplication.class);

	public static void main(String[] args) {
		String port = System.getenv("PORT");
    if (port == null) {
      port = "8080";
      logger.warn("Defaulting to port " + port);
    }
		SpringApplication app = new SpringApplication(EditorApplication.class);
    app.setDefaultProperties(Collections.singletonMap("server.port", port));

    // Start the Spring Boot application.
    app.run(args);
		logger.info("Listening on port " + port);
  }

}