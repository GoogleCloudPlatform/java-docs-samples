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
import java.util.List;
import java.util.Map;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class FirebaseFirestoreTest {

  // Loggers + handlers for various tested classes
  // (Must be declared at class-level, or LoggingHandler won't detect log records!)
  private static final Logger logger = Logger.getLogger(FirebaseFirestore.class.getName());

  private static final TestLogHandler LOG_HANDLER = new TestLogHandler();

  private static final Gson gson = new Gson();

  @BeforeClass
  public static void beforeClass() {
    logger.addHandler(LOG_HANDLER);
  }

  @After
  public void afterTest() {
    LOG_HANDLER.clear();
  }

  @Test
  public void functionsFirebaseFirestore_shouldIgnoreMissingValuesTest() {
    MockContext context = new MockContext();
    context.resource = "resource_1";
    context.eventType = "event_type_2";

    new FirebaseFirestore().accept("", context);

    List<LogRecord> logs = LOG_HANDLER.getStoredLogRecords();
    Truth.assertThat(logs.size()).isEqualTo(2);
    Truth.assertThat(logs.get(0).getMessage()).isEqualTo(
        "Function triggered by event on: resource_1");
    Truth.assertThat(logs.get(1).getMessage()).isEqualTo("Event type: event_type_2");
  }

  @Test
  public void functionsFirebaseFirestore_shouldProcessPresentValues() {
    String jsonStr = gson.toJson(Map.of("oldValue", 999, "value", 777));

    MockContext context = new MockContext();
    context.resource = "resource_1";
    context.eventType = "event_type_2";

    new FirebaseFirestore().accept(jsonStr, context);

    List<LogRecord> logs = LOG_HANDLER.getStoredLogRecords();
    Truth.assertThat(logs.size()).isEqualTo(6);
    Truth.assertThat(logs.get(0).getMessage()).isEqualTo(
        "Function triggered by event on: resource_1");
    Truth.assertThat(logs.get(1).getMessage()).isEqualTo("Event type: event_type_2");
    Truth.assertThat(logs.get(2).getMessage()).isEqualTo("Old value:");
    Truth.assertThat(logs.get(4).getMessage()).isEqualTo("New value:");
  }
}
