package app;

import com.github.javafaker.Faker;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;

public class Main {

  private static final int MAX_GENERATED_ENTRIES = 15000;

  private static final Faker FAKER = new Faker();
  private static final Random RANDOM = new Random();

  public static void main(String[] args) {
    // Connect to PostgreSQL
    System.out.println("Connecting to PostgreSQL...");
    JdbcTemplate jdbcTemplate = configureJdbcTemplate();

    // Populate leaderboard with test data
    try {
      System.out.println("Populating items table with sample data...");
      populateItems(jdbcTemplate);
    } catch (CannotGetJdbcConnectionException e) {
      System.out.println(
        "Failed to connect to the database. Retrying in 5 seconds..."
      );
      // Sleep for 5 seconds and retry
      try {
        Thread.sleep(5000);
      } catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
      }
      main(args);
    }
  }

  private static void populateItems(JdbcTemplate jdbcTemplate) {
    String sql =
      "INSERT INTO items (name, description, price) VALUES (?, ?, ?)";

    // Prepare batch arguments
    List<Object[]> batchArgs = new ArrayList<>();
    for (int i = 0; i < MAX_GENERATED_ENTRIES; i++) {
      String name = generateProductName();
      String description = generateDescription();
      double price = RANDOM.nextInt(10000) / 100.0;

      batchArgs.add(new Object[] { name, description, price });
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
    String jdbcUrl = System.getenv()
      .getOrDefault("DB_URL", "jdbc:postgresql://localhost:5432/items");
    String jdbcUsername = System.getenv().getOrDefault("DB_USERNAME", "root");
    String jdbcPassword = System.getenv()
      .getOrDefault("DB_PASSWORD", "password");

    JdbcTemplate jdbcTemplate = new JdbcTemplate();
    jdbcTemplate.setDataSource(
      DataSourceBuilder.create()
        .url(jdbcUrl)
        .username(jdbcUsername)
        .password(jdbcPassword)
        .build()
    );
    return jdbcTemplate;
  }
}
