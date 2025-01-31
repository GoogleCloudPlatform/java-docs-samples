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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import redis.clients.jedis.Jedis;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class DataControllerTest {

    @Mock private AccountRepository accountRepository;

    @Mock private Jedis jedis;

    private DataController dataController;

    @BeforeEach
    void setUp() {
        dataController = new DataController(accountRepository, jedis);
    }

    @Nested
    @DisplayName("Testing register() method")
    class RegisterTests {

        @Test
        @DisplayName("Should register a new user")
        void testRegister() {
            String email = "test@example.com";
            String username = "testUser";
            String password = "securePassword";

            // Action
            dataController.register(email, username, password);

            // Verify
            verify(accountRepository).registerUser(email, username, password);
        }
    }

    @Nested
    @DisplayName("Testing login() method")
    class LoginTests {

        @Test
        @DisplayName("Should return token for valid credentials")
        void testLogin_ValidCredentials() {
            String username = "testUser";
            String password = "securePassword";
            String token = "generatedToken";

            // Given
            given(accountRepository.authenticateUser(username, password))
                    .willReturn(Optional.of(1)); // pretend userId = 1

            // Mock static Utils.generateToken(...)
            try (MockedStatic<Utils> mockedUtils = Mockito.mockStatic(Utils.class)) {
                mockedUtils
                        .when(() -> Utils.generateToken(Global.TOKEN_BYTE_LENGTH))
                        .thenReturn(token);

                // Action
                String result = dataController.login(username, password);

                // Assert & Verify
                assertEquals(token, result);
                verify(jedis).set(token, username);
                verify(jedis).expire(token, Global.TOKEN_EXPIRATION);
            }
        }

        @Test
        @DisplayName("Should return null for invalid credentials")
        void testLogin_InvalidCredentials() {
            String username = "testUser";
            String password = "wrongPassword";

            given(accountRepository.authenticateUser(username, password))
                    .willReturn(Optional.empty());

            String result = dataController.login(username, password);

            assertNull(result);
        }

        @Test
        @DisplayName("Should throw RuntimeException if Jedis operation fails")
        void testLogin_JedisFailure() {
            String username = "testUser";
            String password = "securePassword";
            String token = "generatedToken";

            given(accountRepository.authenticateUser(username, password))
                    .willReturn(Optional.of(1));

            try (MockedStatic<Utils> mockedUtils = Mockito.mockStatic(Utils.class)) {
                mockedUtils
                        .when(() -> Utils.generateToken(Global.TOKEN_BYTE_LENGTH))
                        .thenReturn(token);

                // Force an error in Jedis.set(...)
                doThrow(new RuntimeException("Jedis error")).when(jedis).set(token, username);

                // Should throw RuntimeException because Jedis fails
                assertThrows(
                        RuntimeException.class, () -> dataController.login(username, password));
            }
        }
    }

    @Nested
    @DisplayName("Testing logout() method")
    class LogoutTests {

        @Test
        @DisplayName("Should delete token from Jedis")
        void testLogout() {
            String token = "testToken";

            dataController.logout(token);

            // Verify it deletes from Jedis
            verify(jedis).del(token);
        }

        @Test
        @DisplayName("Should throw RuntimeException if Jedis operation fails")
        void testLogout_JedisFailure() {
            String token = "testToken";

            // Force an error in Jedis.del(...)
            doThrow(new RuntimeException("Jedis error")).when(jedis).del(token);

            assertThrows(RuntimeException.class, () -> dataController.logout(token));
        }
    }

    @Nested
    @DisplayName("Testing verify() method")
    class VerifyTests {

        @Test
        @DisplayName("Should return username and extend token expiration if valid")
        void testVerify_ValidToken() {
            String token = "testToken";
            String username = "testUser";

            given(jedis.get(token)).willReturn(username);

            String result = dataController.verify(token);

            assertEquals(username, result);
            verify(jedis).expire(token, Global.TOKEN_EXPIRATION);
        }

        @Test
        @DisplayName("Should return null if token is invalid")
        void testVerify_InvalidToken() {
            String token = "invalidToken";

            given(jedis.get(token)).willReturn(null);

            String result = dataController.verify(token);

            assertNull(result);
        }

        @Test
        @DisplayName("Should throw RuntimeException if Jedis operation fails")
        void testVerify_JedisFailure() {
            String token = "testToken";

            doThrow(new RuntimeException("Jedis error")).when(jedis).get(token);

            assertThrows(RuntimeException.class, () -> dataController.verify(token));
        }
    }

    @Nested
    @DisplayName("Testing checkIfEmailExists() method")
    class CheckIfEmailExistsTests {

        @Test
        @DisplayName("Should return true if email exists")
        void testCheckIfEmailExists_True() {
            String email = "test@example.com";

            given(accountRepository.isEmailRegistered(email)).willReturn(true);

            assertTrue(dataController.checkIfEmailExists(email));
        }

        @Test
        @DisplayName("Should return false if email does not exist")
        void testCheckIfEmailExists_False() {
            String email = "nonexistent@example.com";

            given(accountRepository.isEmailRegistered(email)).willReturn(false);

            assertFalse(dataController.checkIfEmailExists(email));
        }
    }

    @Nested
    @DisplayName("Testing checkIfUsernameExists() method")
    class CheckIfUsernameExistsTests {

        @Test
        @DisplayName("Should return true if username exists")
        void testCheckIfUsernameExists_True() {
            String username = "testUser";

            given(accountRepository.isUsernameRegistered(username)).willReturn(true);

            assertTrue(dataController.checkIfUsernameExists(username));
        }

        @Test
        @DisplayName("Should return false if username does not exist")
        void testCheckIfUsernameExists_False() {
            String username = "nonexistentUser";

            given(accountRepository.isUsernameRegistered(username)).willReturn(false);

            assertFalse(dataController.checkIfUsernameExists(username));
        }
    }
}
