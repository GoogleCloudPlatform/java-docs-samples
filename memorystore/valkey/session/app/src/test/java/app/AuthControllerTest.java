package app;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class AuthControllerTest {

  @Mock
  private DataController dataController;

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  private AuthController authController;

  @BeforeEach
  void setUp() {
    dataController = Mockito.mock(DataController.class);
    request = Mockito.mock(HttpServletRequest.class);
    response = Mockito.mock(HttpServletResponse.class);
    authController = new AuthController(dataController);
  }

  @Nested
  @DisplayName("Testing register() method")
  class RegisterTests {

    @Test
    @DisplayName("Should return 400 if email is invalid")
    void testRegister_InvalidEmail() {
      RegisterInfo info = new RegisterInfo(
        "invalidEmail",
        "username",
        "password123"
      );

      ResponseEntity<String> response = authController.register(info);

      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
      assertEquals(Global.EMAIL_INVALID, response.getBody());
    }

    @Test
    @DisplayName("Should return 409 if email is already registered")
    void testRegister_EmailAlreadyRegistered() {
      given(dataController.checkIfEmailExists("test@example.com")).willReturn(
        true
      );
      RegisterInfo info = new RegisterInfo(
        "test@example.com",
        "username",
        "password123"
      );

      ResponseEntity<String> response = authController.register(info);

      assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
      assertEquals(Global.EMAIL_ALREADY_REGISTERED, response.getBody());
    }

    @Test
    @DisplayName("Should return 200 if registration is successful")
    void testRegister_Success() {
      RegisterInfo info = new RegisterInfo(
        "test@example.com",
        "username",
        "password123"
      );

      ResponseEntity<String> response = authController.register(info);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertEquals(Global.REGISTERED, response.getBody());
      verify(dataController).register(info.email, info.username, info.password);
    }
  }

  @Nested
  @DisplayName("Testing login() method")
  class LoginTests {

    @Test
    @DisplayName("Should return 401 for invalid credentials")
    void testLogin_InvalidCredentials() {
      LoginInfo info = new LoginInfo("username", "wrongPassword");

      given(dataController.login(info.username, info.password)).willReturn(
        null
      );

      HttpServletResponse mockResponse = Mockito.mock(
        HttpServletResponse.class
      );
      ResponseEntity<String> response = authController.login(
        info,
        mockResponse
      );

      assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
      assertEquals(Global.INVALID_CREDENTIALS, response.getBody());
    }

    @Test
    @DisplayName("Should return 200 and set cookie for valid credentials")
    void testLogin_ValidCredentials() {
      LoginInfo info = new LoginInfo("username", "password123");
      String token = "validToken";

      given(dataController.login(info.username, info.password)).willReturn(
        token
      );

      ResponseEntity<String> responseEntity = authController.login(
        info,
        response
      );

      assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
      verify(response).addCookie(any(Cookie.class));
    }
  }

  @Nested
  @DisplayName("Testing logout() method")
  class LogoutTests {

    @Test
    @DisplayName("Should return 401 if token is missing")
    void testLogout_NoToken() {
      given(request.getCookies()).willReturn(null);

      ResponseEntity<String> responseEntity = authController.logout(request);

      assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
      assertEquals(Global.INVALID_TOKEN, responseEntity.getBody());
    }

    @Test
    @DisplayName("Should return 200 and logout user if token is valid")
    void testLogout_ValidToken() {
      Cookie tokenCookie = new Cookie("token", "validToken");
      given(request.getCookies()).willReturn(new Cookie[] { tokenCookie });

      ResponseEntity<String> responseEntity = authController.logout(request);

      assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
      assertEquals(Global.LOGGED_OUT, responseEntity.getBody());
      verify(dataController).logout("validToken");
    }
  }

  @Nested
  @DisplayName("Testing verify() method")
  class VerifyTests {

    @Test
    @DisplayName("Should return 401 if token is missing")
    void testVerify_NoToken() {
      given(request.getCookies()).willReturn(null);

      ResponseEntity<String> responseEntity = authController.verify(
        request,
        response
      );

      assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
      assertEquals(Global.INVALID_TOKEN, responseEntity.getBody());
    }

    @Test
    @DisplayName("Should return 200 and username if token is valid")
    void testVerify_ValidToken() {
      Cookie tokenCookie = new Cookie("token", "validToken");
      given(request.getCookies()).willReturn(new Cookie[] { tokenCookie });
      given(dataController.verify("validToken")).willReturn("username");

      ResponseEntity<String> responseEntity = authController.verify(
        request,
        response
      );

      assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
      verify(response).addCookie(any(Cookie.class));
    }

    @Test
    @DisplayName("Should return 401 if token is invalid")
    void testVerify_InvalidToken() {
      Cookie tokenCookie = new Cookie("token", "invalidToken");
      given(request.getCookies()).willReturn(new Cookie[] { tokenCookie });
      given(dataController.verify("invalidToken")).willReturn(null);

      ResponseEntity<String> responseEntity = authController.verify(
        request,
        response
      );

      assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
      assertEquals(Global.INVALID_TOKEN, responseEntity.getBody());
    }
  }
}
