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

package app;

import com.github.javafaker.Faker;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;

public final class Main {

  private Main() {
    throw new UnsupportedOperationException(
        "This is a utility class and cannot be instantiated");
  }

  /** Maximum number of entries to generate. */
  private static final int MAX_GENERATED_ENTRIES = 15000;
  /** Faker instance for generating random data. */
  private static final Faker FAKER = new Faker();
  /** Random number generator for generating random data. */
  private static final Random RANDOM = new Random();
  /** Sleep time in milliseconds before retrying to connect. */
  private static final int SLEEP_TIME = 5000;
  /** Bound on price. */
  private static final int PRICE_BOUND = 10000;
  /** Scaling factor for price. */
  private static final int PRICE_SCALE = 100;

  /**
   * Main method to start sample-data application.
   *
   * @param args Command-line arguments
   */
  public static void main(final String[] args) {
    // Connect to PostgreSQL
    System.out.println("Connecting to PostgreSQL...");
    JdbcTemplate jdbcTemplate = configureJdbcTemplate();

    // Populate leaderboard with test data
    try {
      System.out.println("Populating items table with sample data...");
      populateItems(jdbcTemplate);
    } catch (CannotGetJdbcConnectionException e) {
      System.out
          .println("Failed to connect to the"
              + " database. Retrying in 5 seconds...");
      // Sleep for 5 seconds and retry
      try {
        Thread.sleep(SLEEP_TIME);
      } catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
      }
      main(args);
    }
  }

  private static void populateItems(final JdbcTemplate jdbcTemplate) {
    String sql = "INSERT INTO items"
        + " (name, description, price) VALUES (?, ?, ?)";

    // Prepare batch arguments
    List<Object[]> batchArgs = new ArrayList<>();
    for (int i = 0; i < MAX_GENERATED_ENTRIES; i++) {
      String name = generateProductName();
      String description = generateDescription();
      double price = RANDOM.nextInt(PRICE_BOUND) / PRICE_SCALE;

      batchArgs.add(new Object[] {
          name, description, price });
    }

    // Execute batch update
    jdbcTemplate.batchUpdate(sql, batchArgs);
  }

  private static String generateProductName() {
    return FAKER.commerce().productName();
  }

  private static String generateDescription() {
    return FAKER.lorem().paragraph();
  }

  private static JdbcTemplate configureJdbcTemplate() {
    String jdbcUrl = System
        .getenv()
        .getOrDefault("DB_URL", "jdbc:postgresql://localhost:5432/items");
    String jdbcUsername = System
        .getenv()
        .getOrDefault("DB_USERNAME", "root");
    String jdbcPassword = System
        .getenv()
        .getOrDefault("DB_PASSWORD", "password");

    JdbcTemplate jdbcTemplate = new JdbcTemplate();
    jdbcTemplate.setDataSource(
        DataSourceBuilder.create()
            .url(jdbcUrl)
            .username(jdbcUsername)
            .password(jdbcPassword)
            .build());
    return jdbcTemplate;
  }

}
