/**
 * This class is used to create a response object for the verify endpoint.
 * It contains the username and expiration timestamp of the token.
 */

package app;

import java.security.Timestamp;
import org.json.JSONObject;

public class VerifyResponse {

  private String username;
  private int expirationSecs;

  public VerifyResponse(String username, int expiration) {
    this.username = username;
    this.expirationSecs = expiration;
  }

  public JSONObject toJson() {
    JSONObject json = new JSONObject();
    json.put("username", username);
    json.put("expirationSecs", expirationSecs);
    return json;
  }
}
