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

package functions;

import com.google.common.testing.TestLogHandler;
import com.google.common.truth.Truth;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class FirebaseAuthTest {

  // Loggers + handlers for various tested classes
  // (Must be declared at class-level, or LoggingHandler won't detect log records!)
  private static final Logger logger = Logger.getLogger(FirebaseAuth.class.getName());

  private static final TestLogHandler LOG_HANDLER = new TestLogHandler();

  private static final Gson gson = new Gson();

  @BeforeClass
  public static void beforeClass() {
    logger.addHandler(LOG_HANDLER);
  }

  @Before
  public void beforeTest() throws IOException {
    LOG_HANDLER.clear();
  }

  @Test
  public void functionsFirebaseAuth_shouldShowUserId() {
    String jsonStr = gson.toJson(Map.of("uid", "foo"));
    new FirebaseAuth().accept(jsonStr, null);

    Truth.assertThat(LOG_HANDLER.getStoredLogRecords().get(0).getMessage()).isEqualTo(
        "Function triggered by change to user: foo");
  }

  @Test
  public void functionsFirebaseAuth_shouldShowOrigin() {
    String jsonStr = gson.toJson(Map.of("metadata", Map.of("createdAt", "123")));
    new FirebaseAuth().accept(jsonStr, null);

    Truth.assertThat(LOG_HANDLER.getStoredLogRecords().get(0).getMessage()).isEqualTo(
        "Created at: 123");
  }

  @Test
  public void functionsFirebaseAuth_shouldShowVersion()  {
    String jsonStr = gson.toJson(Map.of("email", "foo@google.com"));
    new FirebaseAuth().accept(jsonStr, null);

    Truth.assertThat(LOG_HANDLER.getStoredLogRecords().get(0).getMessage()).isEqualTo(
        "Email: foo@google.com");
  }
}
