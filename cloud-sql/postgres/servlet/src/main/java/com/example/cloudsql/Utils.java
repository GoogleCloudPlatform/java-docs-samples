/*
 * Copyright 2022 Google LLC
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

package com.example.cloudsql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Locale;
import javax.annotation.Nullable;
import javax.sql.DataSource;

public class Utils {

  // Used to validate user input. All user provided data should be validated and sanitized before
  // being used something like a SQL query. Returns null if invalid.
  @Nullable
  public static String validateTeam(String input) {
    if (input != null) {
      input = input.toUpperCase(Locale.ENGLISH);
      // Must be either "TABS" or "SPACES"
      if (!"TABS".equals(input) && !"SPACES".equals(input)) {
        return null;
      }
    }
    return input;
  }

  public static void createTable(DataSource pool) throws SQLException {
    // Safely attempt to create the table schema.
    try (Connection conn = pool.getConnection()) {
      String stmt =
          "CREATE TABLE IF NOT EXISTS votes ( "
              + "vote_id SERIAL NOT NULL, time_cast timestamp NOT NULL, candidate CHAR(6) NOT NULL,"
              + " PRIMARY KEY (vote_id) );";
      try (PreparedStatement createTableStatement = conn.prepareStatement(stmt);) {
        createTableStatement.execute();
      }
    }
  }
}
