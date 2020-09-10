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

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.common.testing.TestLogHandler;
import com.google.common.truth.Truth;
import com.google.gson.Gson;
import functions.eventpojos.MockContext;
import java.util.Map;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(JUnit4.class)
public class FirebaseFirestoreReactiveTest {

  @Mock private Firestore firestoreMock;
  @Mock private DocumentReference referenceMock;

  // Loggers + handlers for various tested classes
  // (Must be declared at class-level, or LoggingHandler won't detect log records!)
  private static final Logger logger = Logger.getLogger(FirebaseFirestoreReactive.class.getName());

  private static final TestLogHandler LOG_HANDLER = new TestLogHandler();

  private static final Gson gson = new Gson();

  @BeforeClass
  public static void beforeClass() {
    logger.addHandler(LOG_HANDLER);
  }

  @Before
  public void beforeTest() {
    MockitoAnnotations.initMocks(this);

    when(referenceMock.set(any())).thenReturn(null);

    when(firestoreMock.document(any())).thenReturn(referenceMock);

    LOG_HANDLER.clear();
  }

  @After
  public void afterTest() {
    System.out.flush();
    LOG_HANDLER.clear();
  }

  @Test
  public void functionsFirebaseReactive_shouldCapitalizeOriginalValue()  {

    String jsonStr = gson.toJson(Map.of("value",
        Map.of("fields",
            Map.of("original",
                Map.of("stringValue", "foo")))));

    MockContext context = new MockContext();
    context.resource = "projects/_/databases/(default)/documents/messages/ABCDE12345";

    FirebaseFirestoreReactive functionInstance = new FirebaseFirestoreReactive(firestoreMock);

    functionInstance.accept(jsonStr, context);

    Truth.assertThat(LOG_HANDLER.getStoredLogRecords().get(0).getMessage()).isEqualTo(
        "Replacing value: foo --> FOO");
  }

  @Test
  public void functionsFirebaseReactive_shouldIgnoreCapitalizedValues()  {

    String jsonStr = gson.toJson(Map.of("value",
        Map.of("fields",
            Map.of("original",
                Map.of("stringValue", "FOO")))));

    MockContext context = new MockContext();
    context.resource = "projects/_/databases/(default)/documents/messages/ABCDE12345";

    FirebaseFirestoreReactive functionInstance = new FirebaseFirestoreReactive(firestoreMock);

    functionInstance.accept(jsonStr, context);

    Truth.assertThat(LOG_HANDLER.getStoredLogRecords().get(0).getMessage()).isEqualTo(
        "Value is already upper-case.");
  }

  @Test
  public void functionsFirebaseReactive_shouldReportBadJson()  {
    String jsonStr = gson.toJson(Map.of("value",
        Map.of("fields",
            Map.of("original",
                Map.of("missingValue", "foo")))));

    MockContext context = new MockContext();
    context.resource = "projects/_/databases/(default)/documents/messages/ABCDE12345";

    FirebaseFirestoreReactive functionInstance = new FirebaseFirestoreReactive(firestoreMock);

    IllegalArgumentException e = Assertions.assertThrows(
        IllegalArgumentException.class, () -> functionInstance.accept(jsonStr, context));
    Truth.assertThat(e).hasMessageThat().startsWith("Malformed JSON");
  }
}
