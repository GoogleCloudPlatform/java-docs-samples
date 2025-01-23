/*
 * Copyright 2025 Google LLC
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

/** Use this configuration to allow CORS requests from the frontend. */
package app;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {

  @Value("${ALLOWED_ORIGINS:localhost:3000}") // Default to localhost:3000 if not set
  private String allowedOrigins;

  @Value(
      "${ALLOWED_METHODS:GET,POST,PUT,DELETE}") // Default to GET,POST,PUT,DELETE methods if not set
  private String allowedMethods;

  @Value("${ALLOWED_HEADERS:*}") // Default to all headers if not set
  private String allowedHeaders;

  @Bean
  public WebMvcConfigurer corsConfigurer() {
    return new WebMvcConfigurer() {
      @Override
      public void addCorsMappings(CorsRegistry registry) {
        registry
            .addMapping("/**") // Allow all endpoints
            .allowedOrigins(allowedOrigins.split(",")) // Allow requests from the frontend
            .allowedMethods(allowedMethods.split(",")) // Restrict HTTP methods
            .allowedHeaders(allowedHeaders.split(",")) // Specify allowed headers
            .allowCredentials(true); // Allow cookies and credentials
      }
    };
  }
}
