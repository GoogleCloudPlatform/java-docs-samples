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

import static com.google.common.truth.Truth.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.testing.TestLogHandler;
import functions.eventpojos.GcsEvent;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import java.net.URI;
import java.util.Date;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/** Unit tests for main.java.com.example.functions.helloworld.HelloGcs. */
public class HelloGcsTest {
  private static final TestLogHandler LOG_HANDLER = new TestLogHandler();
  private static final Logger logger = Logger.getLogger(HelloGcs.class.getName());

  @Before
  public void beforeTest() throws Exception {
    logger.addHandler(LOG_HANDLER);
  }

  @After
  public void afterTest() {
    LOG_HANDLER.clear();
  }

  @Test
  public void helloGcs_shouldPrintFileName() throws JsonProcessingException {
    // Create event data
    GcsEvent gcsEvent = new GcsEvent();
    gcsEvent.setName("foo.txt");
    gcsEvent.setBucket("gs://test-bucket");
    gcsEvent.setMetageneration("metageneration-data");
    gcsEvent.setTimeCreated(new Date());
    gcsEvent.setUpdated(new Date());
    // Convert event data to a JSON string
    ObjectMapper Obj = new ObjectMapper();
    String jsonData = Obj.writeValueAsString(gcsEvent);
    // Construct a CloudEvent
    CloudEvent event =
        CloudEventBuilder.v1()
            .withId("0")
            .withType("google.storage.object.finalize")
            .withSource(URI.create("https://example.com"))
            .withData("application/json", jsonData.getBytes())
            .build();

    new HelloGcs().accept(event);

    String message = LOG_HANDLER.getStoredLogRecords().get(3).getMessage();
    assertThat(message).contains("File: foo.txt");
  }
}
