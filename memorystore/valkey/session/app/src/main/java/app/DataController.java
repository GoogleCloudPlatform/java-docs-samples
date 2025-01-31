/**
 * Responsible for handling the data operations between the API, Valkey, and the database.
 */

package app;

import java.util.Optional;
import org.springframework.stereotype.Controller;
import redis.clients.jedis.Jedis;

@Controller
public class DataController {

  private final AccountRepository accountRepository;
  private final Jedis jedis;

  public DataController(AccountRepository accountRepository, Jedis jedis) {
    this.accountRepository = accountRepository;
    this.jedis = jedis;
  }

  public void register(String email, String username, String password) {
    accountRepository.registerUser(email, username, password);
  }

  public String login(String username, String password) {
    // Authenticate user
    Optional<Integer> userId = accountRepository.authenticateUser(
      username,
      password
    );

    // No user found
    if (userId.isEmpty()) {
      return null;
    }

    // Generate token for the user
    String token = Utils.generateToken(Global.TOKEN_BYTE_LENGTH);

    // Store token in Valkey
    jedis.setex(token, Global.TOKEN_EXPIRATION, username);

    return token;
  }

  public void logout(String token) {
    jedis.del(token);
  }

  public String verify(String token) {
    // Retrieve username from Valkey
    String username = jedis.get(token);

    // No username found for the token
    if (username == null) {
      return null;
    }

    // Extend token expiration
    jedis.expire(token, Global.TOKEN_EXPIRATION);

    return username;
  }

  public boolean checkIfEmailExists(String email) {
    return accountRepository.isEmailRegistered(email);
  }

  public boolean checkIfUsernameExists(String username) {
    return accountRepository.isUsernameRegistered(username);
  }
}
