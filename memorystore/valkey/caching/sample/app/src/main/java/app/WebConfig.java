/**
 * Use this configuration to allow CORS requests from the frontend.
 */

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

  @Value("${ALLOWED_METHODS:GET,POST,PUT,DELETE}") // Default to GET,POST,PUT,DELETE methods if not set
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
