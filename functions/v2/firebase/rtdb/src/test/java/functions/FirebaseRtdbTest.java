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
import com.google.events.firebase.database.v1.ReferenceEventData;
import com.google.protobuf.Value;
import com.google.protobuf.util.JsonFormat;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import java.io.IOException;
import java.net.URI;
import java.util.List;
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
  // (Must be declared at class-level, or LoggingHandler won't detect log
  // records!)
  private static final Logger logger = Logger.getLogger(FirebaseRtdb.class.getName());

  private static final TestLogHandler LOG_HANDLER = new TestLogHandler();

  @BeforeClass
  public static void beforeClass() {
    logger.addHandler(LOG_HANDLER);
  }

  @Before
  public void beforeTest() throws IOException {
    LOG_HANDLER.clear();
  }

  @Test
  public void functionsFirebaseRtdb_shouldShowDelta() throws Exception {
    ReferenceEventData data = ReferenceEventData.newBuilder()
        .setDelta(Value.newBuilder().setStringValue("hello"))
        .setData(Value.newBuilder().setStringValue("world"))
        .build();

    String jsonData = JsonFormat.printer().print(data);

    // Construct a CloudEvent
    CloudEvent event = CloudEventBuilder.v1()
        .withId("0")
        .withType("google.firebase.database.ref.v1.written")
        .withSource(URI.create(
            "//firebasedatabase.googleapis.com/projects/_/locations/_/instances/default"))
        .withData("application/json", jsonData.getBytes())
        .build();

    new FirebaseRtdb().accept(event);

    List<LogRecord> logs = LOG_HANDLER.getStoredLogRecords();
    Truth.assertThat(logs.get(0).getMessage()).isEqualTo(
        "Function triggered by change to: "
            + "//firebasedatabase.googleapis.com/projects/_/locations/_/instances/default");
    Truth.assertThat(logs.get(1).getMessage()).isEqualTo("Delta: " + data.getDelta().toString());
    Truth.assertThat(logs.get(2).getMessage()).isEqualTo("Data: " + data.getData().toString());
  }

}
