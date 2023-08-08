/*
 * Copyright 2021 Google LLC
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

// [START functions_cloudevent_storage_unit_test]

import static com.google.common.truth.Truth.assertThat;

import com.google.common.testing.TestLogHandler;
import com.google.events.cloud.storage.v1.StorageObjectData;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Timestamp;
import com.google.protobuf.util.JsonFormat;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import java.net.URI;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for main.java.com.example.functions.helloworld.HelloGcs. */
@RunWith(JUnit4.class)
public class HelloGcsTest {
  private static final TestLogHandler LOG_HANDLER = new TestLogHandler();
  private static final Logger logger = Logger.getLogger(HelloGcs.class.getName());

  @BeforeClass
  public static void beforeClass() throws Exception {
    logger.addHandler(LOG_HANDLER);
  }

  @After
  public void afterTest() {
    LOG_HANDLER.clear();
  }

  @Test
  public void helloGcs_shouldPrintFileName() throws InvalidProtocolBufferException {
    // Create event data
    String file = "foo.txt";
    String bucket = "gs://test-bucket";

    // Get the current time in milliseconds
    long millis = System.currentTimeMillis();

    // Create a Timestamp object
    Timestamp timestamp = Timestamp.newBuilder()
        .setSeconds(millis / 1000)
        .setNanos((int) ((millis % 1000) * 1000000))
        .build();

    StorageObjectData.Builder dataBuilder = StorageObjectData.newBuilder()
        .setName(file)
        .setBucket(bucket)
        .setMetageneration(10)
        .setTimeCreated(timestamp)
        .setUpdated(timestamp);

    String jsonData = JsonFormat.printer().print(dataBuilder);

    // Construct a CloudEvent
    CloudEvent event = CloudEventBuilder.v1()
        .withId("0")
        .withType("google.storage.object.finalize")
        .withSource(URI.create("https://example.com"))
        .withData("application/json", jsonData.getBytes())
        .build();

    new HelloGcs().accept(event);

    String actualBucket = LOG_HANDLER.getStoredLogRecords().get(2).getMessage();
    String actualFile = LOG_HANDLER.getStoredLogRecords().get(3).getMessage();
    assertThat(actualFile).contains("File: " + file);
    assertThat(actualBucket).contains("Bucket: " + bucket);
  }

  @Test
  public void helloGcs_shouldPrintNotifyIfDataIsNull() throws InvalidProtocolBufferException {
    // Construct a CloudEvent
    CloudEvent event = CloudEventBuilder.v1()
        .withId("0")
        .withType("google.storage.object.finalize")
        .withSource(URI.create("https://example.com"))
        .build();

    new HelloGcs().accept(event);

    String logMessage = LOG_HANDLER.getStoredLogRecords().get(2).getMessage();
    assertThat(logMessage).isEqualTo("No data found in cloud event payload!");
  }
}

// [END functions_cloudevent_storage_unit_test]
