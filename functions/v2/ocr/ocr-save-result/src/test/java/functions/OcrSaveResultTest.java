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

import static com.google.common.truth.Truth.assertThat;

import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.common.testing.TestLogHandler;
import com.google.common.truth.Truth;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import functions.eventpojos.MessagePublishedData;
import functions.eventpojos.Message;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class OcrSaveResultTest {
  private static String RESULT_BUCKET = System.getenv("RESULT_BUCKET");

  private static final Logger logger = Logger.getLogger(OcrSaveResult.class.getName());

  private static final TestLogHandler LOG_HANDLER = new TestLogHandler();

  private static final Gson gson = new Gson();

  private static final Storage STORAGE = StorageOptions.getDefaultInstance().getService();
  private static final String RANDOM_STRING = UUID.randomUUID().toString();

  @BeforeClass
  public static void setUpClass() {
    assertThat(RESULT_BUCKET).isNotNull();
    logger.addHandler(LOG_HANDLER);
  }

  @After
  public void afterTest() {
    LOG_HANDLER.clear();
  }

  @AfterClass
  public static void tearDownClass() {
    String deletedFilename = String.format("test-%s.jpg_to_es.txt", RANDOM_STRING);
    STORAGE.delete(RESULT_BUCKET, deletedFilename);
  }

  @Test(expected = IllegalArgumentException.class)
  public void functionsOcrSave_shouldValidateParams() throws IOException, URISyntaxException {
    MessagePublishedData data = new MessagePublishedData();
    Message message = new Message();
    message.setData(new String(Base64.getEncoder().encode("{}".getBytes())));
    data.setMessage(message);
    
    CloudEvent event = CloudEventBuilder.v1().withId("000").withType("google.cloud.pubsub.topic.v1.messagePublished").withSource(new URI("curl-command")).withData("application/json", gson.toJson(data).getBytes()).build();

    new OcrSaveResult().accept(event);
  }

  @Test
  public void functionsOcrSave_shouldPublishTranslatedText() throws IOException, URISyntaxException {
    String text = "Wake up human!";
    String filename = String.format("test-%s.jpg", RANDOM_STRING);
    String lang = "es";

    JsonObject dataJson = new JsonObject();
    dataJson.addProperty("text", text);
    dataJson.addProperty("filename", filename);
    dataJson.addProperty("lang", lang);

    MessagePublishedData data = new MessagePublishedData();
    Message message = new Message();
    message.setData(new String(Base64.getEncoder().encode(gson.toJson(dataJson).getBytes())));
    data.setMessage(message);
    CloudEvent event = CloudEventBuilder.v1().withId("000").withType("google.cloud.pubsub.topic.v1.messagePublished").withSource(new URI("curl-command")).withData("application/json", gson.toJson(data).getBytes()).build();

    new OcrSaveResult().accept(event);

    String resultFilename = filename + "_to_es.txt";

    // Check log messages
    List<LogRecord> logs = LOG_HANDLER.getStoredLogRecords();
    String expectedMessage = String.format(
        "Saving result to %s in bucket %s", resultFilename, RESULT_BUCKET);
    Truth.assertThat(LOG_HANDLER.getStoredLogRecords().get(1).getMessage()).isEqualTo(
        expectedMessage);

    // Check that file was written
    BlobInfo resultBlob = STORAGE.get(RESULT_BUCKET, resultFilename);
    assertThat(resultBlob).isNotNull();
  }
}
