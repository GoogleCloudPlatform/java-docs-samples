package app;

import com.github.javafaker.Faker;
import java.sql.Timestamp;
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
      System.out.println("Populating accounts...");
      populateAccounts(jdbcTemplate);
      System.out.println("Populating sessions...");
      populateSessions(jdbcTemplate);
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

  private static void populateAccounts(JdbcTemplate jdbcTemplate) {
    String sql =
      "INSERT INTO account (email, username, password) VALUES (?, ?, ?)";

    // Prepare batch arguments
    List<Object[]> batchArgs = new ArrayList<>();
    for (int i = 0; i < MAX_GENERATED_ENTRIES; i++) {
      String email = FAKER.internet().emailAddress();
      String username = FAKER.name().username();
      username = username.length() > 20 ? username.substring(0, 20) : username;
      String password = FAKER.internet().password();

      batchArgs.add(new Object[] { email, username, password });
    }

    // Execute batch update
    jdbcTemplate.batchUpdate(sql, batchArgs);
  }

  private static void populateSessions(JdbcTemplate jdbcTemplate) {
    String sql =
      "INSERT INTO session (token, account_id, expires_at) VALUES (?, ?, ?)";

    // Prepare batch arguments
    List<Object[]> batchArgs = new ArrayList<>();
    for (int i = 0; i < MAX_GENERATED_ENTRIES; i++) {
      String token = FAKER.internet().uuid();
      int accountId = RANDOM.nextInt(MAX_GENERATED_ENTRIES) + 1;
      long expiresAt = System.currentTimeMillis() + 3600000;
      Timestamp expiresAtTimestamp = new Timestamp(expiresAt);

      batchArgs.add(new Object[] { token, accountId, expiresAtTimestamp });
    }

    // Execute batch update
    jdbcTemplate.batchUpdate(sql, batchArgs);
  }

  private static JdbcTemplate configureJdbcTemplate() {
    String jdbcUrl = System.getenv()
      .getOrDefault("DB_URL", "jdbc:postgresql://localhost:5432/postgres");
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
