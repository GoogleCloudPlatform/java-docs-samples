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

/** Responsible for handling the data operations between the API, Valkey, and the database. */
package app;

import org.springframework.stereotype.Controller;

import redis.clients.jedis.Jedis;

import java.util.Optional;

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
        Optional<Integer> userId = accountRepository.authenticateUser(username, password);

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
