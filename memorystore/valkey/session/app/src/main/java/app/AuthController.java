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

/**
 * The Auth controller for the application.
 *
 * <p>The controller contains the following endpoints: - POST /auth/register - Registers a new user
 * - POST /auth/login - Logs in a user - POST /auth/logout - Logs out a user - POST /auth/verify -
 * Verifies a user's token
 */
package app;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final DataController dataController;

    public AuthController(DataController dataController) {
        this.dataController = dataController;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterInfo info) {
        String email = info.email;
        String username = info.username;
        String password = info.password;

        // Validate email
        if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            return ResponseEntity.badRequest().body(Global.EMAIL_INVALID);
        }

        // Validate username
        if (!username.matches("^[a-zA-Z0-9._-]+$")) {
            return ResponseEntity.badRequest().body(Global.USERNAME_INVALID);
        } else if (username.length() < 3 || username.length() > 20) {
            return ResponseEntity.badRequest().body(Global.USERNAME_LENGTH);
        }

        // Validate password
        if (password.length() < 8 || password.length() > 255) {
            return ResponseEntity.badRequest().body(Global.PASSWORD_LENGTH);
        }

        // Check if email or username is already taken
        if (dataController.checkIfEmailExists(email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Global.EMAIL_ALREADY_REGISTERED);
        }
        if (dataController.checkIfUsernameExists(username)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Global.USERNAME_TAKEN);
        }

        // Register user
        dataController.register(email, username, password);
        return ResponseEntity.ok(Global.REGISTERED);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginInfo info, HttpServletResponse response) {
        String username = info.username;
        String password = info.password;

        // Attempt to log in
        String token = dataController.login(username, password);

        // Invalid credentials
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Global.INVALID_CREDENTIALS);
        }

        // Create and set a cookie
        response.addCookie(Utils.createCookie(token));
        return ResponseEntity.ok(Global.LOGGED_IN);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        String token = Utils.getTokenFromCookie(request.getCookies());
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Global.INVALID_TOKEN);
        }

        // Logout user
        dataController.logout(token);

        return ResponseEntity.ok(Global.LOGGED_OUT);
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verify(HttpServletRequest request, HttpServletResponse response) {
        String token = Utils.getTokenFromCookie(request.getCookies());
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Global.INVALID_TOKEN);
        }

        // Verify token and extend session
        String username = dataController.verify(token);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Global.INVALID_TOKEN);
        }

        // Refresh cookie expiration
        Cookie cookie = Utils.createCookie(token);
        response.addCookie(cookie);
        return ResponseEntity.ok(
                new VerifyResponse(username, cookie.getMaxAge()).toJson().toString());
    }
}
