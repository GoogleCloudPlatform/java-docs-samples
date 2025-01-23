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

/** Configuration for the JDBC DataSource to connect to the PostgreSQL server. */
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
      throw new IllegalArgumentException("Database URL (DB_URL) is not configured");
    }
    if (username == null || username.isEmpty()) {
      throw new IllegalArgumentException("Database username (DB_USERNAME) is not configured");
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
