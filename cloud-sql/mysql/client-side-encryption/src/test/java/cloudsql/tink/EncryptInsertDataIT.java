/*
 * Copyright 2021 Google LLC
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

package cloudsql.tink;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

import com.google.crypto.tink.Aead;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class EncryptInsertDataIT {

  private static List<String> requiredEnvVars =
      Arrays.asList("MYSQL_USER", "MYSQL_PASS", "MYSQL_DB", "MYSQL_CONNECTION_NAME", "CLOUD_KMS_URI");

  private static DataSource pool;
  private static String tableName;
  private static Aead envAead;
  private static final String CLOUD_KMS_URI = System.getenv("CLOUD_KMS_URI");
  private static final String MYSQL_USER = System.getenv("MYSQL_USER");
  private static final String MYSQL_PASS = System.getenv("MYSQL_PASS");
  private static final String MYSQL_DB = System.getenv("MYSQL_DB");
  private static final String MYSQL_CONNECTION_NAME = System.getenv("MYSQL_CONNECTION_NAME");

  private ByteArrayOutputStream bout;
  private PrintStream originalOut = System.out;


  public static void checkEnvVars() {
    // Check that required env vars are set
    requiredEnvVars.forEach((varName) -> {
      assertWithMessage(
          String.format("Environment variable '%s' must be set to perform these tests.", varName))
          .that(System.getenv(varName)).isNotEmpty();
    });
  }

  @BeforeClass
  public static void setUp() throws GeneralSecurityException, SQLException {
    checkEnvVars();
    tableName = String.format("votes_%s", UUID.randomUUID().toString().replace("-", ""));
    pool = QueryAndDecryptData
        .createConnectionPool(MYSQL_USER, MYSQL_PASS, MYSQL_DB, MYSQL_CONNECTION_NAME);
    EncryptAndInsertData.createTable(pool, tableName);

    envAead = new CloudKmsEnvelopeAead(CLOUD_KMS_URI).envAead;
  }

  @AfterClass
  public static void tearDown() throws SQLException {
    if (pool != null)  {
      try (Connection conn = pool.getConnection()) {
        String stmt = String.format("DROP TABLE %s;", tableName);
        try (PreparedStatement createTableStatement = conn.prepareStatement(stmt);) {
          createTableStatement.execute();
        }
      }
    }
  }

  @Before
  public void captureOutput() {
    bout = new ByteArrayOutputStream();
    System.setOut(new PrintStream(bout));
  }

  @After
  public  void resetOutput() {
    System.setOut(originalOut);
    bout.reset();
  }

  @Test
  public void testEncryptAndInsertData() throws GeneralSecurityException, SQLException {
    EncryptAndInsertData
        .encryptAndInsertData(pool, envAead, tableName, "TABS", "hello@example.com");
    String output = bout.toString();
    assertThat(output).contains("Successfully inserted row into table");

    List<String> decryptedEmails = new ArrayList<>();
    try (Connection conn = pool.getConnection()) {
      String stmt = String.format(
          "SELECT team, time_cast, voter_email FROM %s ORDER BY time_cast DESC LIMIT 5", tableName);
      try (PreparedStatement voteStmt = conn.prepareStatement(stmt);) {
        ResultSet voteResults = voteStmt.executeQuery();
        while (voteResults.next()) {
          byte[] decryptedEmail = envAead
              .decrypt(voteResults.getBytes(3), voteResults.getString(1).getBytes());
          decryptedEmails.add(new String(decryptedEmail));
        }
      }
    }
    assertThat(decryptedEmails).contains("hello@example.com");
  }

}