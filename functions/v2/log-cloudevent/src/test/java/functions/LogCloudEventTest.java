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

import com.google.common.testing.TestLogHandler;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import java.net.URI;
import java.util.logging.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class LogCloudEventTest {
  private static final Logger logger = Logger.getLogger(LogCloudEvent.class.getName());
  private static final TestLogHandler logHandler = new TestLogHandler();

  @BeforeClass
  public static void beforeClass() {
    logger.addHandler(logHandler);
  }

  @Test
  public void functionsLogCloudEvent_shouldLogCloudEvent() throws Exception {
    // Build a CloudEvent Log Entry
    JsonObject protoPayload = new JsonObject();
    JsonObject authInfo = new JsonObject();
    String email = "test@gmail.com";
    authInfo.addProperty("principalEmail", email);

    protoPayload.add("authenticationInfo", authInfo);
    protoPayload.addProperty("resourceName", "test resource");
    protoPayload.addProperty("methodName", "test method");

    JsonObject encodedData = new JsonObject();
    encodedData.add("protoPayload", protoPayload);
    encodedData.addProperty("name", "test name");


    CloudEvent event =
        CloudEventBuilder.v1()
        .withId("0")
        .withSubject("test subject")
        .withType("google.cloud.audit.log.v1.written")
        .withSource(URI.create("https://example.com"))
        .withData(new Gson().toJson(encodedData).getBytes())
        .build();

    new LogCloudEvent().accept(event);

    assertThat("Event Subject: " + event.getSubject()).isEqualTo(
        logHandler.getStoredLogRecords().get(1).getMessage());
    assertThat("Authenticated User: " + email).isEqualTo(
        logHandler.getStoredLogRecords().get(4).getMessage());
  }
}
