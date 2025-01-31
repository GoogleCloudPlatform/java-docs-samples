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

/** Global constants for the application. */
package app;

public class Global {

    public static final String INVALID_CREDENTIALS = "Invalid username or password";
    public static final String INVALID_TOKEN = "Invalid token";
    public static final String REGISTERED = "User registered successfully";
    public static final String EMAIL_INVALID = "Invalid email format";
    public static final String EMAIL_ALREADY_REGISTERED = "Email is already registered";
    public static final String USERNAME_INVALID =
            "Username must only contain letters, numbers, periods, underscores, and hyphens";
    public static final String USERNAME_LENGTH = "Username must be between 3 and 20 characters";
    public static final String USERNAME_TAKEN = "Username is already taken";
    public static final String PASSWORD_LENGTH = "Password must be between 8 and 255 characters";
    public static final String LOGGED_IN = "Logged in";
    public static final String LOGGED_OUT = "Logged out";

    public static final Integer TOKEN_BYTE_LENGTH = 128;
    public static final Integer TOKEN_EXPIRATION =
            1800; // Token expiration time in seconds (30 minutes)
    public static final String TOKEN_COOKIE_NAME = "token";
}
