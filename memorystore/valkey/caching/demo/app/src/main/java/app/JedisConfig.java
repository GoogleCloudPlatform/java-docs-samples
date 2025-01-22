/**
 * Configuration for the Jedis client to connect to the Valkey server.
 */

package app;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.Jedis;

@Configuration
public class JedisConfig {

  // Redis server configuration properties
  @Value("${VALKEY_HOST:localhost}") // Default to localhost if not set
  private String redisHost;

  @Value("${VALKEY_PORT:6379}") // Default to 6379 if not set
  private int redisPort;

  @Value("${VALKEY_PASSWORD:}") // Empty by default if not set
  private String redisPassword;

  @Bean
  public Jedis jedis() {
    // Validate mandatory properties
    if (redisHost == null || redisHost.isEmpty()) {
      throw new IllegalArgumentException(
        "Redis host (VALKEY_HOST) is not configured"
      );
    }
    if (redisPort <= 0 || redisPort > 65535) {
      throw new IllegalArgumentException("Redis port (VALKEY_PORT) is invalid");
    }

    Jedis jedis = new Jedis(redisHost, redisPort);

    // Authenticate if a password is set
    if (!redisPassword.isEmpty()) {
      jedis.auth(redisPassword);
    }

    // Verify the connection to the Redis server
    try {
      jedis.ping();
    } catch (Exception e) {
      String msg =
        "Failed to connect to Redis server at " + redisHost + ":" + redisPort;
      throw new RuntimeException(msg, e);
    }

    return jedis;
  }
}
