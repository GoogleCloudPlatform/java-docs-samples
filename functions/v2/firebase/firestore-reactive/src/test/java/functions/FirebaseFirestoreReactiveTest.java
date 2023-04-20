/*
 * Copyright 2023 Google LLC
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

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.common.testing.TestLogHandler;
import com.google.events.cloud.firestore.v1.Document;
import com.google.events.cloud.firestore.v1.DocumentEventData;
import com.google.events.cloud.firestore.v1.Value;
import com.google.protobuf.InvalidProtocolBufferException;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import java.net.URI;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(JUnit4.class)
public class FirebaseFirestoreReactiveTest {
  // Loggers + handlers for various tested classes
  // (Must be declared at class-level, or LoggingHandler won't detect log
  // records!)
  private static final Logger logger = Logger.getLogger(FirebaseFirestoreReactive.class.getName());
  private static final TestLogHandler LOG_HANDLER = new TestLogHandler();

  @Mock
  private Firestore firestoreMock;
  @Mock
  private DocumentReference referenceMock;
  @Mock
  private ApiFuture<WriteResult> futureMock;

  @BeforeClass
  public static void beforeClass() {
    logger.addHandler(LOG_HANDLER);
  }

  @Before
  public void beforeTest() throws InterruptedException, ExecutionException {
    MockitoAnnotations.openMocks(this);
    when(futureMock.get()).thenReturn(null);
    when(referenceMock.set(any(), any())).thenReturn(futureMock);
    when(firestoreMock.document(any())).thenReturn(referenceMock);
  }

  @After
  public void afterTest() {
    LOG_HANDLER.clear();
  }

  @Test
  public void firebaseFirestoreReactive_shouldCapitalizeOriginalValue()
      throws InvalidProtocolBufferException, InterruptedException, ExecutionException {

    CloudEvent event = CloudEventBuilder.v1()
        .withId("0")
        .withSubject("test subject")
        .withSource(URI.create("https://example.com"))
        .withType("google.cloud.firestore.document.v1.written")
        .withData(buildPayload("foo").toByteArray())
        .withDataContentType("application/protobuf")
        .build();

    new FirebaseFirestoreReactive(firestoreMock).accept(event);

    String logMessage = LOG_HANDLER.getStoredLogRecords().get(0).getMessage();
    assertThat(logMessage).isEqualTo("Replacing values: foo --> FOO");
  }

  @Test
  public void firebaseFirestore_shouldIgnoreCapitalizedValues()
      throws InvalidProtocolBufferException, InterruptedException, ExecutionException {

    CloudEvent event = CloudEventBuilder.v1()
        .withId("0")
        .withSubject("test subject")
        .withSource(URI.create("https://example.com"))
        .withType("google.cloud.firestore.document.v1.written")
        .withData(buildPayload("FOO").toByteArray())
        .withDataContentType("application/protobuf")
        .build();

    new FirebaseFirestoreReactive(firestoreMock).accept(event);

    String logMessage = LOG_HANDLER.getStoredLogRecords().get(0).getMessage();
    assertThat(logMessage).isEqualTo("Value is already upper-case");
  }

  @Test
  public void firebaseFirestore_shouldDetectNullDataPayload()
      throws InvalidProtocolBufferException, InterruptedException, ExecutionException {
    CloudEvent event = CloudEventBuilder.v1()
        .withId("0")
        .withSubject("test subject")
        .withSource(URI.create("https://example.com"))
        .withType("google.cloud.firestore.document.v1.written")
        .withDataContentType("application/protobuf")
        .build();

    new FirebaseFirestoreReactive(firestoreMock).accept(event);

    String logMessage = LOG_HANDLER.getStoredLogRecords().get(0).getMessage();
    assertThat(logMessage).isEqualTo("No data found in event!");
  }

  @Test
  public void firebaseFirestore_shouldDetectIncorrectContentType()
      throws InvalidProtocolBufferException, InterruptedException, ExecutionException {
    CloudEvent event = CloudEventBuilder.v1()
        .withId("0")
        .withSubject("test subject")
        .withSource(URI.create("https://example.com"))
        .withType("google.cloud.firestore.document.v1.written")
        .withDataContentType("application/text")
        .withData("testing".getBytes())
        .build();

    new FirebaseFirestoreReactive(firestoreMock).accept(event);

    String logMessage = LOG_HANDLER.getStoredLogRecords().get(0).getMessage();
    assertThat(logMessage)
        .isEqualTo("Found unexpected content type application/text, expected application/protobuf");
  }

  private static DocumentEventData buildPayload(String originalValue) {
    Document newValue = Document.newBuilder()
        .setName("projects/_/databases/(default)/documents/messages/ABCDE12345")
        .putFields("original", Value.newBuilder()
            .setStringValue(originalValue)
            .build())
        .build();
    return DocumentEventData.newBuilder()
        .setValue(newValue)
        .build();
  }
}
