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
public class AutoLabelInstanceTest {
  private static final Logger logger = Logger.getLogger(AutoLabelInstance.class.getName());
  private static final TestLogHandler logHandler = new TestLogHandler();

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String ZONE = "us-central1-a";
  private static final String INSTANCE = "lite1";

  @BeforeClass
  public static void beforeClass() {
    logger.addHandler(logHandler);
  }

  @Test
  public void functionsAutoLabelInstance() throws Exception {
    // Build a CloudEvent Log Entry
    JsonObject protoPayload = new JsonObject();
    JsonObject authInfo = new JsonObject();
    String email = "test@gmail.com";
    authInfo.addProperty("principalEmail", email);

    protoPayload.add("authenticationInfo", authInfo);

    String resource =
        String.format(
            "compute.googleapis.com/projects/%s/zones/%s/instances/%s", PROJECT_ID, ZONE, INSTANCE);
    protoPayload.addProperty("resourceName", resource);
    protoPayload.addProperty("methodName", "beta.compute.instances.insert");

    JsonObject encodedData = new JsonObject();
    encodedData.add("protoPayload", protoPayload);
    encodedData.addProperty("name", "test name");

    CloudEvent event =
        CloudEventBuilder.v1()
            .withId("0")
            .withSubject(resource)
            .withType("google.cloud.audit.log.v1.written")
            .withSource(URI.create("https://example.com"))
            .withData(new Gson().toJson(encodedData).getBytes())
            .build();

    new AutoLabelInstance().accept(event);

    assertThat(
            String.format(
                "Adding label, \"{'creator': '%s'}\", to instance, \"%s\".",
                "test-gmail-com", INSTANCE))
        .isEqualTo(logHandler.getStoredLogRecords().get(0).getMessage());
  }
}
