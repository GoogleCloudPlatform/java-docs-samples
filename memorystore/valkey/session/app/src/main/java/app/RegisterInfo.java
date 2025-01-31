/**
 * Data class for holding registration information.
 */

package app;

public class RegisterInfo {

  public String email;
  public String username;
  public String password;

  public RegisterInfo(String email, String username, String password) {
    this.email = email;
    this.username = username;
    this.password = password;
  }
}
