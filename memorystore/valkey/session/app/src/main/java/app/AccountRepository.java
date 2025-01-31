/**
 * Handles CRUD operations for the account table.
 */

package app;

import java.util.Map;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Repository;

@Repository
public class AccountRepository {

  private final JdbcTemplate jdbcTemplate;

  public AccountRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public Optional<Integer> authenticateUser(String username, String password) {
    try {
      // Fetch hashedPassword and userId in a single query
      Map<String, Object> accountData = jdbcTemplate.queryForMap(
        "SELECT id, password FROM account WHERE username = ?",
        username
      );

      String hashedPassword = (String) accountData.get("password");
      Integer userId = (Integer) accountData.get("id");

      // Check password validity
      if (hashedPassword != null && BCrypt.checkpw(password, hashedPassword)) {
        return Optional.of(userId); // Authentication successful
      } else {
        return Optional.empty(); // Authentication failed
      }
    } catch (EmptyResultDataAccessException e) {
      return Optional.empty(); // No user found
    }
  }

  public void registerUser(String email, String username, String password) {
    // Validate input
    if (email == null || username == null || password == null) {
      throw new IllegalArgumentException(
        "Email, username, and password must not be null"
      );
    }

    // Hash the password to securely store it
    String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

    // Insert user into the database
    jdbcTemplate.update(
      "INSERT INTO account (email, username, password) VALUES (?, ?, ?)",
      email,
      username,
      hashedPassword
    );
  }

  public boolean isEmailRegistered(String email) {
    String sql = "SELECT EXISTS (SELECT 1 FROM account WHERE email = ?)";
    return Boolean.TRUE.equals(
      jdbcTemplate.queryForObject(sql, Boolean.class, email)
    );
  }

  public boolean isUsernameRegistered(String username) {
    String sql = "SELECT EXISTS (SELECT 1 FROM account WHERE username = ?)";
    return Boolean.TRUE.equals(
      jdbcTemplate.queryForObject(sql, Boolean.class, username)
    );
  }
}
