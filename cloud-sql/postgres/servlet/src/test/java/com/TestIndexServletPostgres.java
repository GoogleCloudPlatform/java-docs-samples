/*
 * Copyright 2020 Google LLC
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

import static com.google.common.truth.Truth.assertWithMessage;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


public class TestIndexServletPostgres {

  private static List<String> requiredEnvVars =
      Arrays.asList("PG_USER", "PG_PASS", "PG_DB", "PG_CONNECTION_NAME");

  private static DataSource pool;
  private static String tableName;

  public static void checkEnvVars() {
    // Check that required env vars are set
    requiredEnvVars.forEach((varName) -> {
      assertWithMessage(
          String.format("Environment variable '%s' must be set to perform these tests.", varName))
          .that(System.getenv(varName)).isNotEmpty();
    });
  }

  private static void createTable(DataSource pool) throws SQLException {
    // Safely attempt to create the table schema.
    tableName = String.format("votes_%s", UUID.randomUUID().toString().replace("-", ""));
    try (Connection conn = pool.getConnection()) {
      String stmt =
          "CREATE TABLE IF NOT EXISTS "
              + tableName
              + " ( vote_id SERIAL NOT NULL, time_cast timestamp NOT NULL,"
              + " candidate CHAR(6) NOT NULL,"
              + " PRIMARY KEY (vote_id) );";
      try (PreparedStatement createTableStatement = conn.prepareStatement(stmt);) {
        createTableStatement.execute();
      }
    }
  }


  @BeforeClass
  public static void createPool() throws SQLException {
    checkEnvVars();
    HikariConfig config = new HikariConfig();

    config.setJdbcUrl(String.format("jdbc:postgresql:///%s", System.getenv("PG_DB")));
    config.setUsername(System.getenv("PG_USER")); // e.g. "root", "mysql"
    config.setPassword(System.getenv("PG_PASS")); // e.g. "my-password"
    config.addDataSourceProperty("socketFactory", "com.google.cloud.sql.postgres.SocketFactory");
    config.addDataSourceProperty("cloudSqlInstance", System.getenv("PG_CONNECTION_NAME"));

    pool = new HikariDataSource(config);
    createTable(pool);

  }

  @AfterClass
  public static void dropTable() throws SQLException {
    try (Connection conn = pool.getConnection()) {
      String stmt = String.format("DROP TABLE %s;", tableName);
      try (PreparedStatement createTableStatement = conn.prepareStatement(stmt);) {
        createTableStatement.execute();
      }
    }
  }

  @Test
  public void testGetTemplateData() throws Exception {
    TemplateData templateData = new IndexServlet().getTemplateData(pool);

    assertNotNull(templateData.tabCount);
    assertNotNull(templateData.spaceCount);
    assertNotNull(templateData.recentVotes);
  }

  @Test
  public void testServletPost() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    ServletContext context = mock(ServletContext.class);

    when(request.getServletContext()).thenReturn(context);
    when(context.getAttribute("my-pool")).thenReturn(pool);
    when(request.getParameter("team")).thenReturn("TABS");

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    new IndexServlet().doPost(request, response);

    writer.flush();
    assertTrue(stringWriter.toString().contains("Vote successfully cast for"));
  }
}