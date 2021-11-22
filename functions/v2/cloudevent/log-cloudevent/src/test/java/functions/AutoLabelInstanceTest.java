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
import java.util.Base64;
import java.util.logging.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class AutoLabelInstanceTest {
  private static final Logger logger = Logger.getLogger(LogCloudEvent.class.getName());
  private static final TestLogHandler logHandler = new TestLogHandler();

  @BeforeClass
  public static void beforeClass() {
    logger.addHandler(logHandler);
  }

  @Test
  public void functionsAutoLabelInstance() throws Exception {
    // Build a CloudEvent Log Entry
    JsonObject protoPayload = new JsonObject();

    JsonObject auth = new JsonObject();
    metadata.addProperty("callerIp", "0.0.0.0");
    metadata.addProperty("callerSuppliedUserAgent", "test useragent");

    protoPayload.add("requestMetadata", metadata);
    protoPayload.addProperty("resourceName", "test resource");
    protoPayload.addProperty("methodName", "test method");

    JsonObject encodedData = new JsonObject();
    encodedData.add("protoPayload", protoPayload);


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
  }
}

// {
//   "protoPayload": {
//     "@type": "type.googleapis.com/google.cloud.audit.AuditLog",
//     "authenticationInfo": {
//       "principalEmail": "akitsch@google.com"
//     },
//     "requestMetadata": {
//       "callerIp": "2601:601:1100:2350:e8b5:f86f:8406:4692",
//       "callerSuppliedUserAgent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.69 Safari/537.36,gzip(gfe),gzip(gfe)"
//     },
//     "serviceName": "compute.googleapis.com",
//     "methodName": "beta.compute.instances.insert",
//     "resourceName": "projects/starter-akitsch/zones/us-central1-a/instances/instance-1",
//     "request": {
//       "@type": "type.googleapis.com/compute.instances.insert"
//     }
//   },
//   "insertId": "p9fn8ie1fn8w",
//   "resource": {
//     "type": "gce_instance",
//     "labels": {
//       "project_id": "starter-akitsch",
//       "zone": "us-central1-a",
//       "instance_id": "3212917591118034299"
//     }
//   },
//   "timestamp": "2021-11-22T21:29:01.980019Z",
//   "severity": "NOTICE",
//   "logName": "projects/starter-akitsch/logs/cloudaudit.googleapis.com%2Factivity",
//   "operation": {
//     "id": "operation-1637616532093-5d1674fac7a19-e355d053-5a13b52b",
//     "producer": "compute.googleapis.com",
//     "last": true
//   },
//   "receiveTimestamp": "2021-11-22T21:29:02.440154971Z"
// }