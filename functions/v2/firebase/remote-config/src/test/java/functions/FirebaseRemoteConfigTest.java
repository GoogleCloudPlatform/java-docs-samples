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

import com.google.common.testing.TestLogHandler;
import com.google.common.truth.Truth;
import com.google.events.firebase.remoteconfig.v1.RemoteConfigEventData;
import com.google.events.firebase.remoteconfig.v1.RemoteConfigUpdateOrigin;
import com.google.events.firebase.remoteconfig.v1.RemoteConfigUpdateType;
import com.google.protobuf.util.JsonFormat;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import java.net.URI;
import java.util.List;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class FirebaseRemoteConfigTest {
  // Loggers + handlers for various tested classes
  // (Must be declared at class-level, or LoggingHandler won't detect log
  // records!)
  private static final Logger logger = Logger.getLogger(FirebaseRemoteConfig.class.getName());

  private static final TestLogHandler LOG_HANDLER = new TestLogHandler();

  @BeforeClass
  public static void beforeClass() {
    logger.addHandler(LOG_HANDLER);
  }

  @After
  public void afterTest() {
    LOG_HANDLER.clear();
  }

  @Test
  public void functionsFirebaseRemoteConfig_shouldShowUpdateType() throws Exception {
    RemoteConfigEventData.Builder builder = RemoteConfigEventData.newBuilder()
        .setUpdateType(RemoteConfigUpdateType.INCREMENTAL_UPDATE)
        .setUpdateOrigin(RemoteConfigUpdateOrigin.CONSOLE)
        .setVersionNumber(1);

    RemoteConfigEventData data = builder.build();
    String jsonData = JsonFormat.printer().print(data);

    // Construct a CloudEvent
    CloudEvent event = CloudEventBuilder.v1()
        .withId("0")
        .withType("google.firebase.remoteconfig.remoteConfig.v1.updated")
        .withSource(URI.create("https://example.com"))
        .withData("application/json", jsonData.getBytes())
        .build();

    new FirebaseRemoteConfig().accept(event);

    List<LogRecord> logRecords = LOG_HANDLER.getStoredLogRecords();

    Truth.assertThat(logRecords.get(0).getMessage()).isEqualTo(
        "Update type: " + data.getUpdateType().name());
    Truth.assertThat(logRecords.get(1).getMessage()).isEqualTo(
        "Origin: " + data.getUpdateOrigin().name());
    Truth.assertThat(logRecords.get(2).getMessage()).isEqualTo(
        "Version: " + data.getVersionNumber());
  }
}
