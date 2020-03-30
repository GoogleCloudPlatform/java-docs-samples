/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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