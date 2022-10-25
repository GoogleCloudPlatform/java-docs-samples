/*
 * Copyright 2022 Google LLC
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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import functions.eventpojos.StorageObjectData;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

public class OcrProcessImageTest {
  private static String FUNCTIONS_BUCKET = "nodejs-docs-samples-tests";

  private static final Logger logger = Logger.getLogger(OcrProcessImage.class.getName());

  private static final TestLogHandler LOG_HANDLER = new TestLogHandler();
  
  // Create custom serializer to handle timestamps in event data
  class DateSerializer implements JsonSerializer<OffsetDateTime> {
    @Override
    public JsonElement serialize(
        OffsetDateTime time, Type typeOfSrc, JsonSerializationContext context)
        throws JsonParseException {
      return new JsonPrimitive(time.toString());
    }
  }

  private final Gson gson =
      new GsonBuilder().registerTypeAdapter(OffsetDateTime.class, new DateSerializer()).create();

  private static OcrProcessImage sampleUnderTest;

  @BeforeClass
  public static void setUpClass() throws IOException {
    Truth.assertThat(System.getenv("GCP_PROJECT"));
    Truth.assertThat(System.getenv("TO_LANG"));
    Truth.assertThat(System.getenv("TRANSLATE_TOPIC"));
    sampleUnderTest = new OcrProcessImage();
    logger.addHandler(LOG_HANDLER);
  }

  @After
  public void afterTest() {
    LOG_HANDLER.clear();
  }

  @Test(expected = IllegalArgumentException.class)
  public void functionsOcrProcess_shouldValidateParams() throws IOException, URISyntaxException {
    StorageObjectData data = new StorageObjectData();
    CloudEvent event =
        CloudEventBuilder.v1()
            .withId("000")
            .withType("google.cloud.storage.object.v1.finalized")
            .withSource(new URI("curl-command"))
            .withData("application/json", gson.toJson(data).getBytes())
            .build();

    sampleUnderTest.accept(event);
  }

  @Test
  public void functionsOcrProcess_shouldDetectText() throws IOException, URISyntaxException {
    StorageObjectData data = new StorageObjectData();
    data.setBucket(FUNCTIONS_BUCKET);
    data.setName("wakeupcat.jpg");
    CloudEvent event =
        CloudEventBuilder.v1()
            .withId("000")
            .withType("google.cloud.storage.object.v1.finalized")
            .withSource(new URI("curl-command"))
            .withData("application/json", gson.toJson(data).getBytes())
            .build();

    sampleUnderTest.accept(event);

    List<LogRecord> logs = LOG_HANDLER.getStoredLogRecords();
    Truth.assertThat(logs.get(1).getMessage())
        .contains("Extracted text from image: Wake up human!");
    Truth.assertThat(logs.get(2).getMessage())
        .contains("Detected language en for file wakeupcat.jpg");
  }
}
