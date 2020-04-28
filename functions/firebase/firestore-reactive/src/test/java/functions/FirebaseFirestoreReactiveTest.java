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

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.common.testing.TestLogHandler;
import com.google.common.truth.Truth;
import functions.eventpojos.MockContext;
import java.io.IOException;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.reflect.Whitebox;

@RunWith(JUnit4.class)
public class FirebaseFirestoreReactiveTest {

  @Mock private Firestore firestoreMock;
  @Mock private DocumentReference referenceMock;

  // Loggers + handlers for various tested classes
  // (Must be declared at class-level, or LoggingHandler won't detect log records!)
  private static final Logger logger = Logger.getLogger(FirebaseFirestoreReactive.class.getName());

  private static final TestLogHandler LOG_HANDLER = new TestLogHandler();

  @BeforeClass
  public static void beforeClass() {
    logger.addHandler(LOG_HANDLER);
  }

  @Before
  public void beforeTest() throws IOException {
    Mockito.mockitoSession().initMocks(this);

    referenceMock = mock(DocumentReference.class, Mockito.RETURNS_DEEP_STUBS);
    when(referenceMock.set(ArgumentMatchers.any())).thenReturn(null);

    firestoreMock = PowerMockito.mock(Firestore.class);
    when(firestoreMock.document(ArgumentMatchers.any())).thenReturn(referenceMock);

    LOG_HANDLER.clear();
  }

  @After
  public void afterTest() {
    System.out.flush();
    LOG_HANDLER.clear();
  }

  @Test
  public void functionsFirebaseReactive_shouldCapitalizeOriginalValue()  {
    String jsonStr = "{\"value\":{\"fields\":{\"original\":{\"stringValue\":\"foo\"}}}}";

    MockContext context = new MockContext();
    context.resource = "projects/_/databases/(default)/documents/messages/ABCDE12345";

    FirebaseFirestoreReactive functionInstance = new FirebaseFirestoreReactive();
    Whitebox.setInternalState(FirebaseFirestoreReactive.class, "FIRESTORE", firestoreMock);

    functionInstance.accept(jsonStr, context);

    Truth.assertThat(LOG_HANDLER.getStoredLogRecords().get(0).getMessage()).isEqualTo(
        "Replacing value: foo --> FOO");
  }

  @Test
  public void functionsFirebaseReactive_shouldReportBadJson()  {
    String jsonStr = "{\"value\":{\"fields\":{\"original\":{\"missingValue\":\"foo\"}}}}";

    MockContext context = new MockContext();
    context.resource = "projects/_/databases/(default)/documents/messages/ABCDE12345";

    FirebaseFirestoreReactive functionInstance = new FirebaseFirestoreReactive();
    Whitebox.setInternalState(FirebaseFirestoreReactive.class, "FIRESTORE", firestoreMock);

    IllegalArgumentException e = Assertions.assertThrows(
        IllegalArgumentException.class, () -> functionInstance.accept(jsonStr, context));
    Truth.assertThat(e).hasMessageThat().startsWith("Malformed JSON");
  }
}
