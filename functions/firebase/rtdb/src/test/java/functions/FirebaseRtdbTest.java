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
import functions.eventpojos.MockContext;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class FirebaseRtdbTest {
  // Loggers + handlers for various tested classes
  // (Must be declared at class-level, or LoggingHandler won't detect log records!)
  private static final Logger logger = Logger.getLogger(FirebaseRtdb.class.getName());

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
  public void functionsFirebaseRtdb_shouldDefaultAdminToZero() {
    MockContext context = new MockContext();
    context.resource = "resource_1";

    new FirebaseRtdb().accept("", context);

    List<LogRecord> logs = LOG_HANDLER.getStoredLogRecords();
    Truth.assertThat(logs.get(0).getMessage()).isEqualTo(
        "Function triggered by change to: resource_1");
    Truth.assertThat(logs.get(1).getMessage()).isEqualTo("Admin?: false");
  }

  @Test
  public void functionsFirebaseRtdb_shouldDisplayAdminStatus() {
    String jsonStr = gson.toJson(Map.of("auth", Map.of("admin", true)));

    MockContext context = new MockContext();
    context.resource = "resource_1";
    context.eventType = "event_type_2";

    new FirebaseRtdb().accept(jsonStr, context);

    List<LogRecord> logs = LOG_HANDLER.getStoredLogRecords();
    Truth.assertThat(logs.get(0).getMessage()).isEqualTo(
        "Function triggered by change to: resource_1");
    Truth.assertThat(logs.get(1).getMessage()).isEqualTo("Admin?: true");
  }

  @Test
  public void functionsFirebaseRtdb_shouldShowDelta() {
    String jsonStr = gson.toJson(Map.of("delta", Map.of("value", 2)));

    MockContext context = new MockContext();
    context.resource = "resource_1";
    context.eventType = "event_type_2";

    new FirebaseRtdb().accept(jsonStr, context);

    List<LogRecord> logs = LOG_HANDLER.getStoredLogRecords();
    Truth.assertThat(logs.size()).isEqualTo(4);
    Truth.assertThat(logs.get(0).getMessage()).isEqualTo(
        "Function triggered by change to: resource_1");
    Truth.assertThat(logs.get(2).getMessage()).isEqualTo("Delta:");

    String expectedJsonStr = gson.toJson(Map.of("value", 2));
    Truth.assertThat(logs.get(3).getMessage()).isEqualTo(expectedJsonStr);
  }

}
