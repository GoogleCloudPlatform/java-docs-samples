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

package com.example.functions.firebase;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import com.example.functions.firebase.eventpojos.MockContext;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.common.testing.TestLogHandler;
import com.google.common.truth.Truth;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.List;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.reflect.Whitebox;

@RunWith(JUnit4.class)
public class FirebaseTest {

  @Mock private Firestore firestoreMock;
  @Mock private DocumentReference referenceMock;

  // Loggers + handlers for various tested classes
  // (Must be declared at class-level, or LoggingHandler won't detect log records!)
  private static final Logger FIRESTORE_LOGGER = Logger.getLogger(
      FirebaseFirestore.class.getName());
  private static final Logger RTDB_LOGGER = Logger.getLogger(FirebaseRtdb.class.getName());
  private static final Logger REMOTE_CONFIG_LOGGER = Logger.getLogger(
      FirebaseRemoteConfig.class.getName());
  private static final Logger AUTH_LOGGER = Logger.getLogger(FirebaseAuth.class.getName());
  private static final Logger REACTIVE_LOGGER = Logger.getLogger(
      FirebaseFirestoreReactive.class.getName());

  private static final TestLogHandler logHandler = new TestLogHandler();

  // Use GSON (https://github.com/google/gson) to parse JSON content.
  private Gson gson = new Gson();

  @Rule
  public EnvironmentVariables environmentVariables = new EnvironmentVariables();

  @BeforeClass
  public static void beforeClass() {
    FIRESTORE_LOGGER.addHandler(logHandler);
    RTDB_LOGGER.addHandler(logHandler);
    REMOTE_CONFIG_LOGGER.addHandler(logHandler);
    AUTH_LOGGER.addHandler(logHandler);
    REACTIVE_LOGGER.addHandler(logHandler);
  }

  @Before
  public void beforeTest() throws IOException {
    Mockito.mockitoSession().initMocks(this);

    referenceMock = mock(DocumentReference.class, Mockito.RETURNS_DEEP_STUBS);
    when(referenceMock.set(ArgumentMatchers.any())).thenReturn(null);

    firestoreMock = PowerMockito.mock(Firestore.class);
    when(firestoreMock.document(ArgumentMatchers.any())).thenReturn(referenceMock);

    logHandler.clear();
  }

  @After
  public void afterTest() {
    System.out.flush();
    logHandler.clear();
  }

  @Test
  public void functionsFirebaseFirestore_shouldIgnoreMissingValues() {
    MockContext context = new MockContext();
    context.resource = "resource_1";
    context.eventType = "event_type_2";

    new FirebaseFirestore().accept("", context);

    List<LogRecord> logs = logHandler.getStoredLogRecords();
    Truth.assertThat(logs.size()).isEqualTo(2);
    Truth.assertThat(logs.get(0).getMessage()).isEqualTo(
        "Function triggered by event on: resource_1");
    Truth.assertThat(logs.get(1).getMessage()).isEqualTo("Event type: event_type_2");
  }

  @Test
  public void functionsFirebaseFirestore_shouldProcessPresentValues() {
    String jsonStr = "{\"oldValue\": 999, \"value\": 777 }";

    MockContext context = new MockContext();
    context.resource = "resource_1";
    context.eventType = "event_type_2";

    new FirebaseFirestore().accept(jsonStr, context);

    List<LogRecord> logs = logHandler.getStoredLogRecords();
    Truth.assertThat(logs.size()).isEqualTo(6);
    Truth.assertThat(logs.get(0).getMessage()).isEqualTo(
        "Function triggered by event on: resource_1");
    Truth.assertThat(logs.get(1).getMessage()).isEqualTo("Event type: event_type_2");
    Truth.assertThat(logs.get(2).getMessage()).isEqualTo("Old value:");
    Truth.assertThat(logs.get(4).getMessage()).isEqualTo("New value:");
  }

  @Test
  public void functionsFirebaseRtdb_shouldDefaultAdminToZero() {
    MockContext context = new MockContext();
    context.resource = "resource_1";

    new FirebaseRtdb().accept("", context);

    List<LogRecord> logs = logHandler.getStoredLogRecords();
    Truth.assertThat(logs.get(0).getMessage()).isEqualTo(
        "Function triggered by change to: resource_1");
    Truth.assertThat(logs.get(1).getMessage()).isEqualTo("Admin?: false");
  }

  @Test
  public void functionsFirebaseRtdb_shouldDisplayAdminStatus() {
    String jsonStr = "{\"auth\": { \"admin\": true }}";

    MockContext context = new MockContext();
    context.resource = "resource_1";
    context.eventType = "event_type_2";

    new FirebaseRtdb().accept(jsonStr, context);

    List<LogRecord> logs = logHandler.getStoredLogRecords();
    Truth.assertThat(logs.get(0).getMessage()).isEqualTo(
        "Function triggered by change to: resource_1");
    Truth.assertThat(logs.get(1).getMessage()).isEqualTo("Admin?: true");
  }

  @Test
  public void functionsFirebaseRtdb_shouldShowDelta() {
    String jsonStr = "{\"delta\": { \"value\": 2 }}";

    MockContext context = new MockContext();
    context.resource = "resource_1";
    context.eventType = "event_type_2";

    new FirebaseRtdb().accept(jsonStr, context);

    List<LogRecord> logs = logHandler.getStoredLogRecords();
    Truth.assertThat(logs.size()).isEqualTo(4);
    Truth.assertThat(logs.get(0).getMessage()).isEqualTo(
        "Function triggered by change to: resource_1");
    Truth.assertThat(logs.get(2).getMessage()).isEqualTo("Delta:");
    Truth.assertThat(logs.get(3).getMessage()).isEqualTo("{\"value\":2}");
  }

  @Test
  public void functionsFirebaseRemoteConfig_shouldShowUpdateType() {
    new FirebaseRemoteConfig().accept("{\"updateType\": \"foo\"}", null);

    Truth.assertThat(logHandler.getStoredLogRecords().get(0).getMessage()).isEqualTo(
        "Update type: foo");
  }

  @Test
  public void functionsFirebaseRemoteConfig_shouldShowOrigin() {
    new FirebaseRemoteConfig().accept("{\"updateOrigin\": \"foo\"}", null);

    Truth.assertThat(logHandler.getStoredLogRecords().get(0).getMessage()).isEqualTo(
        "Origin: foo");
  }

  @Test
  public void functionsFirebaseRemoteConfig_shouldShowVersion() {
    new FirebaseRemoteConfig().accept("{\"versionNumber\": 2}", null);

    Truth.assertThat(logHandler.getStoredLogRecords().get(0).getMessage()).isEqualTo("Version: 2");
  }

  @Test
  public void functionsFirebaseAuth_shouldShowUserId() {
    new FirebaseAuth().accept("{\"uid\": \"foo\"}", null);

    Truth.assertThat(logHandler.getStoredLogRecords().get(0).getMessage()).isEqualTo(
        "Function triggered by change to user: foo");
  }

  @Test
  public void functionsFirebaseAuth_shouldShowOrigin() {
    new FirebaseAuth().accept("{\"metadata\": {\"createdAt\": \"123\"}}", null);

    Truth.assertThat(logHandler.getStoredLogRecords().get(0).getMessage()).isEqualTo(
        "Created at: 123");
  }

  @Test
  public void functionsFirebaseAuth_shouldShowVersion()  {
    new FirebaseAuth().accept("{\"email\": \"foo@google.com\"}", null);

    Truth.assertThat(logHandler.getStoredLogRecords().get(0).getMessage()).isEqualTo(
        "Email: foo@google.com");
  }

  @Test
  public void functionsFirebaseReactive_shouldCapitalizeOriginalValue()  {
    String jsonStr = "{\"value\":{\"fields\":{\"original\":{\"stringValue\":\"foo\"}}}}";

    MockContext context = new MockContext();
    context.resource = "projects/_/databases/(default)/documents/messages/ABCDE12345";

    FirebaseFirestoreReactive functionInstance = new FirebaseFirestoreReactive();
    Whitebox.setInternalState(FirebaseFirestoreReactive.class, "firestore", firestoreMock);

    functionInstance.accept(jsonStr, context);

    Truth.assertThat(logHandler.getStoredLogRecords().get(0).getMessage()).isEqualTo(
        "Replacing value: foo --> FOO");
  }

  @Test
  public void functionsFirebaseReactive_shouldReportBadJson()  {
    String jsonStr = "{\"value\":{\"fields\":{\"original\":{\"missingValue\":\"foo\"}}}}";

    MockContext context = new MockContext();
    context.resource = "projects/_/databases/(default)/documents/messages/ABCDE12345";

    FirebaseFirestoreReactive functionInstance = new FirebaseFirestoreReactive();
    Whitebox.setInternalState(FirebaseFirestoreReactive.class, "firestore", firestoreMock);

    IllegalArgumentException e = Assertions.assertThrows(
        IllegalArgumentException.class, () -> functionInstance.accept(jsonStr, context));
    Truth.assertThat(e).hasMessageThat().isEqualTo(
        "Malformed JSON");
  }
}
