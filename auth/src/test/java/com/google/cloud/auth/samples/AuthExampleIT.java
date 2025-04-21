/*
 * Copyright 2018 Google Inc.
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

package com.google.cloud.auth.samples;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.api.apikeys.v2.Key;
import com.google.api.gax.rpc.InvalidArgumentException;
import com.google.cloud.ServiceOptions;
import io.grpc.StatusRuntimeException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
//CHECKSTYLE OFF: AbbreviationAsWordInName
public class AuthExampleIT {
  //CHECKSTYLE ON: AbbreviationAsWordInName
  private ByteArrayOutputStream bout;
  private PrintStream out;
  private String credentials;

  @Before
  public void setUp() {
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);
    credentials = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
    assertNotNull(credentials);
  }

  @Test
  public void testAuthImplicit() throws IOException {
    AuthExample.main(new String[] {});
    String output = bout.toString();
    assertTrue(output.contains("Buckets:"));
  }

  @Test
  public void testAuthExplicitNoPath() throws IOException {
    AuthExample.main(new String[] {"explicit", credentials});
    String output = bout.toString();
    assertTrue(output.contains("Buckets:"));
  }

  @Test
  public void testAuthApiKey() throws IOException, IllegalStateException {
    String projectId = ServiceOptions.getDefaultProjectId();
    String keyDisplayName = "Test API Key";
    String service = "language.googleapis.com";
    String method = "google.cloud.language.v2.LanguageService.AnalyzeSentiment";
    Key apiKey = null;
    try {
      apiKey = AuthTestUtils.createTestApiKey(projectId, keyDisplayName, service, method);

      String output = authenticateUsingApiKeyWithRetry(apiKey.getKeyString());

      assertTrue(output.contains("magnitude:"));
    } finally {
      if (apiKey != null) {
        AuthTestUtils.deleteTestApiKey(apiKey.getName());
      }
    }
  }

  static String authenticateUsingApiKeyWithRetry(String apiKey) throws IOException {
    int retries = 5;
    int delay = 2000; // 2 seconds

    for (int i = 0; i < retries; i++) {
      try {
        return ApiKeyAuthExample.authenticateUsingApiKey(apiKey);
      } catch (StatusRuntimeException | InvalidArgumentException e) {
        if (e.getMessage().contains("API key expired")) {
          System.out.println("API key not yet active, retrying...");
          try {
            Thread.sleep(delay);
          } catch (InterruptedException ignored) {
            // ignore iterrupted exception and retry test
          }
        } else {
          throw e;
        }
      }
    }

    throw new IOException("API key never became active after retries.");
  }
}
