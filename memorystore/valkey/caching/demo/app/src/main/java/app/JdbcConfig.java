/**
 * Configuration for the JDBC DataSource to connect to the PostgreSQL server.
 */

package app;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
public class JdbcConfig {

  // Database configuration properties with environment variable fallback
  @Value("${DB_URL:jdbc:postgresql://localhost:5432/postgres}")
  private String url;

  @Value("${DB_USERNAME:postgres}")
  private String username;

  @Value("${DB_PASSWORD:}")
  private String password;

  @Bean
  public DataSource dataSource() {
    // Validate mandatory properties
    if (url == null || url.isEmpty()) {
      throw new IllegalArgumentException(
        "Database URL (DB_URL) is not configured"
      );
    }
    if (username == null || username.isEmpty()) {
      throw new IllegalArgumentException(
        "Database username (DB_USERNAME) is not configured"
      );
    }

    // Set up the DataSource
    DriverManagerDataSource dataSource = new DriverManagerDataSource();
    dataSource.setDriverClassName("org.postgresql.Driver");
    dataSource.setUrl(url);
    dataSource.setUsername(username);
    dataSource.setPassword(password);

    return dataSource;
  }
}
